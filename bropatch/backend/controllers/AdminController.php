<?php
declare(strict_types=1);

namespace Bropatch\Controllers;

use Bropatch\Config\Database;
use Bropatch\Helpers\Response;
use PDO;

class AdminController {
    public static function dashboardStats(array $admin): void {
        $db = Database::getConnection();

        // 1. User & Provider Counts
        $totalUsers = (int)$db->query("SELECT COUNT(*) FROM users WHERE role_id = 1 AND deleted_at IS NULL")->fetchColumn();
        $totalProviders = (int)$db->query("SELECT COUNT(*) FROM providers WHERE deleted_at IS NULL")->fetchColumn();
        $activeProviders = (int)$db->query("SELECT COUNT(*) FROM providers WHERE verification_status = 'approved' AND deleted_at IS NULL")->fetchColumn();
        $pendingApprovals = (int)$db->query("SELECT COUNT(*) FROM providers WHERE verification_status = 'pending' AND deleted_at IS NULL")->fetchColumn();

        // 2. Booking Counts
        $todayBookings = (int)$db->query("SELECT COUNT(*) FROM bookings WHERE DATE(created_at) = CURDATE() AND deleted_at IS NULL")->fetchColumn();
        $pendingBookings = (int)$db->query("SELECT COUNT(*) FROM bookings WHERE status IN ('pending', 'searching_provider', 'provider_assigned') AND deleted_at IS NULL")->fetchColumn();
        $completedBookings = (int)$db->query("SELECT COUNT(*) FROM bookings WHERE status = 'completed' AND deleted_at IS NULL")->fetchColumn();
        $cancelledBookings = (int)$db->query("SELECT COUNT(*) FROM bookings WHERE status = 'cancelled' AND deleted_at IS NULL")->fetchColumn();

        // 3. Financial Metrics calculated directly from MySQL tables
        $revenueRow = $db->query("
            SELECT 
                COALESCE(SUM(final_amount), 0) as gross_revenue,
                COALESCE(SUM(platform_fee), 0) as platform_earnings,
                COALESCE(SUM(provider_payout_amount), 0) as provider_earnings
            FROM bookings 
            WHERE status = 'completed' AND deleted_at IS NULL
        ")->fetch(PDO::FETCH_ASSOC);

        $pendingPayoutsTotal = (float)$db->query("SELECT COALESCE(SUM(amount), 0) FROM payouts WHERE status = 'pending'")->fetchColumn();
        $openDisputes = (int)$db->query("SELECT COUNT(*) FROM disputes WHERE status IN ('open', 'under_review')")->fetchColumn();

        // 4. Status distribution for charts
        $statusDist = $db->query("
            SELECT status, COUNT(*) as count 
            FROM bookings 
            WHERE deleted_at IS NULL 
            GROUP BY status
        ")->fetchAll(PDO::FETCH_KEY_PAIR);

        Response::success([
            'metrics' => [
                'total_users' => $totalUsers,
                'total_providers' => $totalProviders,
                'active_providers' => $activeProviders,
                'pending_approvals' => $pendingApprovals,
                'today_bookings' => $todayBookings,
                'pending_bookings' => $pendingBookings,
                'completed_bookings' => $completedBookings,
                'cancelled_bookings' => $cancelledBookings,
                'gross_revenue' => (float)$revenueRow['gross_revenue'],
                'platform_earnings' => (float)$revenueRow['platform_earnings'],
                'provider_earnings' => (float)$revenueRow['provider_earnings'],
                'pending_payouts' => $pendingPayoutsTotal,
                'open_disputes' => $openDisputes
            ],
            'charts' => [
                'status_distribution' => $statusDist
            ]
        ], 'Admin dashboard metrics retrieved');
    }

    public static function approveProvider(int $providerId, array $admin): void {
        $db = Database::getConnection();
        $db->beginTransaction();
        try {
            $db->prepare("
                UPDATE providers 
                SET verification_status = 'approved', is_available = 1, updated_at = NOW() 
                WHERE id = :id
            ")->execute(['id' => $providerId]);

            // Update documents to verified
            $db->prepare("UPDATE provider_documents SET verification_status = 'verified' WHERE provider_id = :id")
               ->execute(['id' => $providerId]);

            // Create audit log
            $db->prepare("
                INSERT INTO audit_logs (admin_id, action, entity, entity_id, notes, created_at)
                VALUES (:aid, 'approve_provider', 'providers', :eid, 'Provider verification approved', NOW())
            ")->execute(['aid' => $admin['id'], 'eid' => $providerId]);

            $db->commit();
            Response::success(['provider_id' => $providerId], 'Provider approved successfully');
        } catch (\Exception $e) {
            $db->rollBack();
            Response::error('Failed to approve provider: ' . $e->getMessage(), 500);
        }
    }

    public static function assignProvider(int $bookingId, array $admin): void {
        $data = json_decode(file_get_contents('php://input'), true) ?? [];
        $providerId = (int)($data['provider_id'] ?? 0);

        if ($providerId <= 0) {
            Response::error('Valid provider ID is required', 422);
        }

        $db = Database::getConnection();
        $db->beginTransaction();
        try {
            $db->prepare("
                UPDATE bookings 
                SET provider_id = :pid, status = 'provider_assigned', updated_at = NOW() 
                WHERE id = :bid
            ")->execute(['pid' => $providerId, 'bid' => $bookingId]);

            $db->prepare("
                INSERT INTO booking_status_history (booking_id, old_status, new_status, changed_by_user_id, changed_by_role, reason)
                VALUES (:bid, 'searching_provider', 'provider_assigned', :uid, 'admin', 'Manual dispatch assignment by admin')
            ")->execute(['bid' => $bookingId, 'uid' => $admin['id']]);

            // Audit log
            $db->prepare("
                INSERT INTO audit_logs (admin_id, action, entity, entity_id, new_data)
                VALUES (:aid, 'assign_provider', 'bookings', :eid, :data)
            ")->execute([
                'aid' => $admin['id'],
                'eid' => $bookingId,
                'data' => json_encode(['assigned_provider_id' => $providerId])
            ]);

            $db->commit();
            Response::success(['booking_id' => $bookingId, 'provider_id' => $providerId], 'Provider assigned successfully');
        } catch (\Exception $e) {
            $db->rollBack();
            Response::error('Failed to assign provider: ' . $e->getMessage(), 500);
        }
    }

    public static function auditLogs(): void {
        $db = Database::getConnection();
        $stmt = $db->query("
            SELECT a.*, u.name as admin_name 
            FROM audit_logs a
            LEFT JOIN admin_users u ON a.admin_id = u.id
            ORDER BY a.created_at DESC 
            LIMIT 50
        ");
        Response::success($stmt->fetchAll(PDO::FETCH_ASSOC));
    }
}
