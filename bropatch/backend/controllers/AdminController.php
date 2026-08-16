<?php
declare(strict_types=1);

namespace Bropatch\Controllers;

use Bropatch\Config\Database;
use Bropatch\Helpers\Response;
use Bropatch\Middleware\AdminAuth;
use PDO;

class AdminController {
    
    /**
     * Dashboard Statistics
     */
    public static function dashboardStats(array $admin): void {
        $db = Database::getConnection();

        $totalUsers = (int)$db->query("SELECT COUNT(*) FROM users WHERE role_id = 1 AND deleted_at IS NULL")->fetchColumn();
        $totalProviders = (int)$db->query("SELECT COUNT(*) FROM providers WHERE deleted_at IS NULL")->fetchColumn();
        $activeProviders = (int)$db->query("SELECT COUNT(*) FROM providers WHERE verification_status = 'approved' AND deleted_at IS NULL")->fetchColumn();
        $pendingApprovals = (int)$db->query("SELECT COUNT(*) FROM providers WHERE verification_status = 'pending' AND deleted_at IS NULL")->fetchColumn();

        $todayBookings = (int)$db->query("SELECT COUNT(*) FROM bookings WHERE DATE(created_at) = CURDATE() AND deleted_at IS NULL")->fetchColumn();
        $pendingBookings = (int)$db->query("SELECT COUNT(*) FROM bookings WHERE status IN ('pending', 'searching_provider', 'provider_assigned') AND deleted_at IS NULL")->fetchColumn();
        $completedBookings = (int)$db->query("SELECT COUNT(*) FROM bookings WHERE status = 'completed' AND deleted_at IS NULL")->fetchColumn();
        $cancelledBookings = (int)$db->query("SELECT COUNT(*) FROM bookings WHERE status = 'cancelled' AND deleted_at IS NULL")->fetchColumn();

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
            ]
        ], 'Dashboard metrics retrieved');
    }

    // ===== PROVIDER MANAGEMENT =====

    public static function listProviders(array $admin): void {
        $db = Database::getConnection();
        $page = (int)($_GET['page'] ?? 1);
        $limit = (int)($_GET['limit'] ?? 20);
        $offset = ($page - 1) * $limit;
        $status = $_GET['status'] ?? null;

        $query = "SELECT p.*, u.name, u.email, u.phone FROM providers p JOIN users u ON p.user_id = u.id WHERE p.deleted_at IS NULL";
        if ($status) {
            $query .= " AND p.verification_status = :status";
        }
        $query .= " ORDER BY p.created_at DESC LIMIT :limit OFFSET :offset";

        $stmt = $db->prepare($query);
        if ($status) $stmt->bindValue(':status', $status);
        $stmt->bindValue(':limit', $limit, PDO::PARAM_INT);
        $stmt->bindValue(':offset', $offset, PDO::PARAM_INT);
        $stmt->execute();

        $providers = $stmt->fetchAll(PDO::FETCH_ASSOC);
        
        $total = (int)$db->query("SELECT COUNT(*) FROM providers WHERE deleted_at IS NULL" . ($status ? " AND verification_status = '$status'" : ""))->fetchColumn();

        Response::success([
            'providers' => $providers,
            'pagination' => ['page' => $page, 'limit' => $limit, 'total' => $total]
        ]);
    }

    public static function getProvider(int $providerId, array $admin): void {
        $db = Database::getConnection();
        $stmt = $db->prepare("
            SELECT p.*, u.name, u.email, u.phone, u.avatar_url
            FROM providers p
            JOIN users u ON p.user_id = u.id
            WHERE p.id = :id AND p.deleted_at IS NULL
        ");
        $stmt->execute(['id' => $providerId]);
        $provider = $stmt->fetch(PDO::FETCH_ASSOC);

        if (!$provider) {
            Response::error('Provider not found', 404);
            return;
        }

        $docs = $db->prepare("SELECT * FROM provider_documents WHERE provider_id = :id")->execute(['id' => $providerId]);
        $services = $db->prepare("SELECT ps.*, s.name FROM provider_services ps JOIN services s ON ps.service_id = s.id WHERE ps.provider_id = :id")->execute(['id' => $providerId]);

        Response::success([
            'provider' => $provider,
            'documents' => $db->prepare("SELECT * FROM provider_documents WHERE provider_id = :id")->execute(['id' => $providerId]) ? $db->query("SELECT * FROM provider_documents WHERE provider_id = $providerId")->fetchAll(PDO::FETCH_ASSOC) : [],
            'services' => $db->query("SELECT ps.*, s.name FROM provider_services ps JOIN services s ON ps.service_id = s.id WHERE ps.provider_id = $providerId")->fetchAll(PDO::FETCH_ASSOC)
        ]);
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

            $db->prepare("UPDATE provider_documents SET verification_status = 'verified' WHERE provider_id = :id")
               ->execute(['id' => $providerId]);

            AdminAuth::logAction($admin, 'approve_provider', 'providers', $providerId, null, ['status' => 'approved']);

            $db->commit();
            Response::success(['provider_id' => $providerId], 'Provider approved successfully');
        } catch (\Exception $e) {
            $db->rollBack();
            Response::error('Failed to approve provider: ' . $e->getMessage(), 500);
        }
    }

    public static function rejectProvider(int $providerId, array $admin): void {
        $data = json_decode(file_get_contents('php://input'), true) ?? [];
        $reason = trim($data['reason'] ?? '');

        if (empty($reason)) {
            Response::error('Rejection reason is required', 422);
            return;
        }

        $db = Database::getConnection();
        $db->beginTransaction();
        try {
            $db->prepare("
                UPDATE providers 
                SET verification_status = 'rejected', rejection_reason = :reason, is_available = 0, updated_at = NOW()
                WHERE id = :id
            ")->execute(['id' => $providerId, 'reason' => $reason]);

            AdminAuth::logAction($admin, 'reject_provider', 'providers', $providerId, null, ['status' => 'rejected', 'reason' => $reason]);

            $db->commit();
            Response::success(['provider_id' => $providerId], 'Provider rejected');
        } catch (\Exception $e) {
            $db->rollBack();
            Response::error('Failed to reject provider: ' . $e->getMessage(), 500);
        }
    }

    public static function suspendProvider(int $providerId, array $admin): void {
        $db = Database::getConnection();
        $db->beginTransaction();
        try {
            $db->prepare("UPDATE providers SET verification_status = 'suspended', is_available = 0, updated_at = NOW() WHERE id = :id")
               ->execute(['id' => $providerId]);
            AdminAuth::logAction($admin, 'suspend_provider', 'providers', $providerId);
            $db->commit();
            Response::success(['provider_id' => $providerId], 'Provider suspended');
        } catch (\Exception $e) {
            $db->rollBack();
            Response::error('Failed to suspend provider', 500);
        }
    }

    public static function activateProvider(int $providerId, array $admin): void {
        $db = Database::getConnection();
        $db->beginTransaction();
        try {
            $db->prepare("UPDATE providers SET is_available = 1, is_online = 1, updated_at = NOW() WHERE id = :id")
               ->execute(['id' => $providerId]);
            AdminAuth::logAction($admin, 'activate_provider', 'providers', $providerId);
            $db->commit();
            Response::success(['provider_id' => $providerId], 'Provider activated');
        } catch (\Exception $e) {
            $db->rollBack();
            Response::error('Failed to activate provider', 500);
        }
    }

    // ===== BOOKING MANAGEMENT =====

    public static function listBookings(array $admin): void {
        $db = Database::getConnection();
        $page = (int)($_GET['page'] ?? 1);
        $limit = (int)($_GET['limit'] ?? 20);
        $offset = ($page - 1) * $limit;
        $status = $_GET['status'] ?? null;

        $query = "SELECT b.*, u.name as customer_name, u.email as customer_email, s.name as service_name FROM bookings b JOIN users u ON b.customer_id = u.id JOIN services s ON b.service_id = s.id WHERE b.deleted_at IS NULL";
        if ($status) {
            $query .= " AND b.status = :status";
        }
        $query .= " ORDER BY b.created_at DESC LIMIT :limit OFFSET :offset";

        $stmt = $db->prepare($query);
        if ($status) $stmt->bindValue(':status', $status);
        $stmt->bindValue(':limit', $limit, PDO::PARAM_INT);
        $stmt->bindValue(':offset', $offset, PDO::PARAM_INT);
        $stmt->execute();

        $bookings = $stmt->fetchAll(PDO::FETCH_ASSOC);
        $total = (int)$db->query("SELECT COUNT(*) FROM bookings WHERE deleted_at IS NULL" . ($status ? " AND status = '$status'" : ""))->fetchColumn();

        Response::success(['bookings' => $bookings, 'pagination' => ['page' => $page, 'limit' => $limit, 'total' => $total]]);
    }

    public static function getBooking(int $bookingId, array $admin): void {
        $db = Database::getConnection();
        $stmt = $db->prepare("
            SELECT b.*, u.name as customer_name, u.phone as customer_phone, s.name as service_name, 
                   p.id as provider_id, pu.name as provider_name
            FROM bookings b
            JOIN users u ON b.customer_id = u.id
            JOIN services s ON b.service_id = s.id
            LEFT JOIN providers p ON b.provider_id = p.id
            LEFT JOIN users pu ON p.user_id = pu.id
            WHERE b.id = :id AND b.deleted_at IS NULL
        ");
        $stmt->execute(['id' => $bookingId]);
        $booking = $stmt->fetch(PDO::FETCH_ASSOC);

        if (!$booking) {
            Response::error('Booking not found', 404);
            return;
        }

        $statusHistory = $db->query("SELECT * FROM booking_status_history WHERE booking_id = $bookingId ORDER BY created_at DESC")->fetchAll(PDO::FETCH_ASSOC);
        $images = $db->query("SELECT * FROM booking_images WHERE booking_id = $bookingId")->fetchAll(PDO::FETCH_ASSOC);

        Response::success(['booking' => $booking, 'status_history' => $statusHistory, 'images' => $images]);
    }

    public static function assignProvider(int $bookingId, array $admin): void {
        $data = json_decode(file_get_contents('php://input'), true) ?? [];
        $providerId = (int)($data['provider_id'] ?? 0);

        if ($providerId <= 0) {
            Response::error('Valid provider ID is required', 422);
            return;
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

            AdminAuth::logAction($admin, 'assign_provider', 'bookings', $bookingId, null, ['assigned_provider_id' => $providerId]);

            $db->commit();
            Response::success(['booking_id' => $bookingId, 'provider_id' => $providerId], 'Provider assigned successfully');
        } catch (\Exception $e) {
            $db->rollBack();
            Response::error('Failed to assign provider: ' . $e->getMessage(), 500);
        }
    }

    public static function cancelBooking(int $bookingId, array $admin): void {
        $data = json_decode(file_get_contents('php://input'), true) ?? [];
        $reason = trim($data['reason'] ?? 'Admin cancelled');

        $db = Database::getConnection();
        $db->beginTransaction();
        try {
            $db->prepare("
                UPDATE bookings 
                SET status = 'cancelled', cancellation_reason = :reason, cancelled_by = 'admin', updated_at = NOW()
                WHERE id = :id
            ")->execute(['id' => $bookingId, 'reason' => $reason]);

            AdminAuth::logAction($admin, 'cancel_booking', 'bookings', $bookingId);
            $db->commit();
            Response::success(['booking_id' => $bookingId], 'Booking cancelled');
        } catch (\Exception $e) {
            $db->rollBack();
            Response::error('Failed to cancel booking', 500);
        }
    }

    // ===== SERVICE MANAGEMENT =====

    public static function listServices(array $admin): void {
        $db = Database::getConnection();
        $page = (int)($_GET['page'] ?? 1);
        $limit = (int)($_GET['limit'] ?? 20);
        $offset = ($page - 1) * $limit;

        $services = $db->query("
            SELECT s.*, c.name as category_name
            FROM services s
            JOIN service_categories c ON s.category_id = c.id
            WHERE s.deleted_at IS NULL
            ORDER BY s.created_at DESC
            LIMIT $limit OFFSET $offset
        ")->fetchAll(PDO::FETCH_ASSOC);

        $total = (int)$db->query("SELECT COUNT(*) FROM services WHERE deleted_at IS NULL")->fetchColumn();

        Response::success(['services' => $services, 'pagination' => ['page' => $page, 'limit' => $limit, 'total' => $total]]);
    }

    public static function getService(int $serviceId, array $admin): void {
        $db = Database::getConnection();
        $stmt = $db->prepare("SELECT s.*, c.name as category_name FROM services s JOIN service_categories c ON s.category_id = c.id WHERE s.id = :id AND s.deleted_at IS NULL");
        $stmt->execute(['id' => $serviceId]);
        $service = $stmt->fetch(PDO::FETCH_ASSOC);

        if (!$service) {
            Response::error('Service not found', 404);
            return;
        }

        Response::success(['service' => $service]);
    }

    public static function createService(array $admin): void {
        $data = json_decode(file_get_contents('php://input'), true) ?? [];

        if (empty($data['name']) || empty($data['category_id']) || !isset($data['base_price'])) {
            Response::error('Name, category_id, and base_price are required', 422);
            return;
        }

        $db = Database::getConnection();
        try {
            $slug = strtolower(str_replace(' ', '-', $data['name']));
            $db->prepare("
                INSERT INTO services (category_id, name, slug, short_description, description, base_price, estimated_duration_mins, is_active)
                VALUES (:cat, :name, :slug, :short_desc, :desc, :price, :duration, 1)
            ")->execute([
                'cat' => $data['category_id'],
                'name' => $data['name'],
                'slug' => $slug,
                'short_desc' => $data['short_description'] ?? '',
                'desc' => $data['description'] ?? '',
                'price' => $data['base_price'],
                'duration' => $data['estimated_duration_mins'] ?? 60
            ]);

            $serviceId = $db->lastInsertId();
            AdminAuth::logAction($admin, 'create_service', 'services', (int)$serviceId);

            Response::success(['service_id' => $serviceId], 'Service created successfully', 201);
        } catch (\Exception $e) {
            Response::error('Failed to create service: ' . $e->getMessage(), 500);
        }
    }

    public static function updateService(int $serviceId, array $admin): void {
        $data = json_decode(file_get_contents('php://input'), true) ?? [];
        $db = Database::getConnection();

        try {
            $fields = [];
            $params = ['id' => $serviceId];

            if (isset($data['name'])) {
                $fields[] = "name = :name";
                $params['name'] = $data['name'];
            }
            if (isset($data['base_price'])) {
                $fields[] = "base_price = :price";
                $params['price'] = $data['base_price'];
            }
            if (isset($data['is_active'])) {
                $fields[] = "is_active = :active";
                $params['active'] = $data['is_active'];
            }

            if (empty($fields)) {
                Response::error('No fields to update', 422);
                return;
            }

            $fields[] = "updated_at = NOW()";
            $query = "UPDATE services SET " . implode(', ', $fields) . " WHERE id = :id";
            $db->prepare($query)->execute($params);

            AdminAuth::logAction($admin, 'update_service', 'services', $serviceId, null, $data);

            Response::success(['service_id' => $serviceId], 'Service updated successfully');
        } catch (\Exception $e) {
            Response::error('Failed to update service: ' . $e->getMessage(), 500);
        }
    }

    // ===== CATEGORY MANAGEMENT =====

    public static function listCategories(array $admin): void {
        $db = Database::getConnection();
        $categories = $db->query("SELECT * FROM service_categories WHERE deleted_at IS NULL ORDER BY sort_order ASC")->fetchAll(PDO::FETCH_ASSOC);
        Response::success(['categories' => $categories]);
    }

    public static function createCategory(array $admin): void {
        $data = json_decode(file_get_contents('php://input'), true) ?? [];

        if (empty($data['name'])) {
            Response::error('Category name is required', 422);
            return;
        }

        $db = Database::getConnection();
        try {
            $slug = strtolower(str_replace(' ', '-', $data['name']));
            $db->prepare("
                INSERT INTO service_categories (name, slug, icon, description, is_active)
                VALUES (:name, :slug, :icon, :desc, 1)
            ")->execute([
                'name' => $data['name'],
                'slug' => $slug,
                'icon' => $data['icon'] ?? 'construct',
                'desc' => $data['description'] ?? ''
            ]);

            $categoryId = $db->lastInsertId();
            AdminAuth::logAction($admin, 'create_category', 'service_categories', (int)$categoryId);

            Response::success(['category_id' => $categoryId], 'Category created successfully', 201);
        } catch (\Exception $e) {
            Response::error('Failed to create category: ' . $e->getMessage(), 500);
        }
    }

    public static function updateCategory(int $categoryId, array $admin): void {
        $data = json_decode(file_get_contents('php://input'), true) ?? [];
        $db = Database::getConnection();

        try {
            $fields = [];
            $params = ['id' => $categoryId];

            if (isset($data['name'])) {
                $fields[] = "name = :name";
                $params['name'] = $data['name'];
            }
            if (isset($data['is_active'])) {
                $fields[] = "is_active = :active";
                $params['active'] = $data['is_active'];
            }

            if (empty($fields)) {
                Response::error('No fields to update', 422);
                return;
            }

            $fields[] = "updated_at = NOW()";
            $query = "UPDATE service_categories SET " . implode(', ', $fields) . " WHERE id = :id";
            $db->prepare($query)->execute($params);

            AdminAuth::logAction($admin, 'update_category', 'service_categories', $categoryId);

            Response::success(['category_id' => $categoryId], 'Category updated successfully');
        } catch (\Exception $e) {
            Response::error('Failed to update category: ' . $e->getMessage(), 500);
        }
    }

    // ===== BANNER MANAGEMENT =====

    public static function listBanners(array $admin): void {
        $db = Database::getConnection();
        $banners = $db->query("SELECT * FROM banners WHERE is_active = 1 ORDER BY sort_order ASC")->fetchAll(PDO::FETCH_ASSOC);
        Response::success(['banners' => $banners]);
    }

    public static function createBanner(array $admin): void {
        $data = json_decode(file_get_contents('php://input'), true) ?? [];

        if (empty($data['title']) || empty($data['image_url'])) {
            Response::error('Title and image_url are required', 422);
            return;
        }

        $db = Database::getConnection();
        try {
            $db->prepare("
                INSERT INTO banners (title, subtitle, cta_text, image_url, category_id, is_active, start_date, end_date)
                VALUES (:title, :subtitle, :cta, :image, :cat, 1, :start, :end)
            ")->execute([
                'title' => $data['title'],
                'subtitle' => $data['subtitle'] ?? '',
                'cta' => $data['cta_text'] ?? 'Book Now',
                'image' => $data['image_url'],
                'cat' => $data['category_id'] ?? null,
                'start' => $data['start_date'] ?? null,
                'end' => $data['end_date'] ?? null
            ]);

            $bannerId = $db->lastInsertId();
            AdminAuth::logAction($admin, 'create_banner', 'banners', (int)$bannerId);

            Response::success(['banner_id' => $bannerId], 'Banner created successfully', 201);
        } catch (\Exception $e) {
            Response::error('Failed to create banner: ' . $e->getMessage(), 500);
        }
    }

    public static function updateBanner(int $bannerId, array $admin): void {
        $data = json_decode(file_get_contents('php://input'), true) ?? [];
        $db = Database::getConnection();

        try {
            $fields = [];
            $params = ['id' => $bannerId];

            if (isset($data['title'])) {
                $fields[] = "title = :title";
                $params['title'] = $data['title'];
            }
            if (isset($data['is_active'])) {
                $fields[] = "is_active = :active";
                $params['active'] = $data['is_active'];
            }

            if (empty($fields)) {
                Response::error('No fields to update', 422);
                return;
            }

            $fields[] = "updated_at = NOW()";
            $query = "UPDATE banners SET " . implode(', ', $fields) . " WHERE id = :id";
            $db->prepare($query)->execute($params);

            AdminAuth::logAction($admin, 'update_banner', 'banners', $bannerId);

            Response::success(['banner_id' => $bannerId], 'Banner updated successfully');
        } catch (\Exception $e) {
            Response::error('Failed to update banner: ' . $e->getMessage(), 500);
        }
    }

    public static function deleteBanner(int $bannerId, array $admin): void {
        $db = Database::getConnection();
        try {
            $db->prepare("UPDATE banners SET is_active = 0, updated_at = NOW() WHERE id = :id")->execute(['id' => $bannerId]);
            AdminAuth::logAction($admin, 'delete_banner', 'banners', $bannerId);
            Response::success(['banner_id' => $bannerId], 'Banner deleted');
        } catch (\Exception $e) {
            Response::error('Failed to delete banner', 500);
        }
    }

    // ===== COUPON MANAGEMENT =====

    public static function listCoupons(array $admin): void {
        $db = Database::getConnection();
        $page = (int)($_GET['page'] ?? 1);
        $limit = (int)($_GET['limit'] ?? 20);
        $offset = ($page - 1) * $limit;

        $coupons = $db->query("SELECT * FROM coupons ORDER BY created_at DESC LIMIT $limit OFFSET $offset")->fetchAll(PDO::FETCH_ASSOC);
        $total = (int)$db->query("SELECT COUNT(*) FROM coupons")->fetchColumn();

        Response::success(['coupons' => $coupons, 'pagination' => ['page' => $page, 'limit' => $limit, 'total' => $total]]);
    }

    public static function createCoupon(array $admin): void {
        $data = json_decode(file_get_contents('php://input'), true) ?? [];

        if (empty($data['code']) || !isset($data['discount_value']) || empty($data['valid_until'])) {
            Response::error('Code, discount_value, and valid_until are required', 422);
            return;
        }

        $db = Database::getConnection();
        try {
            $db->prepare("
                INSERT INTO coupons (code, discount_type, discount_value, min_order_amount, max_discount_amount, valid_until, usage_limit, per_user_limit, is_active)
                VALUES (:code, :type, :value, :min, :max, :until, :usage_limit, :per_user, 1)
            ")->execute([
                'code' => strtoupper($data['code']),
                'type' => $data['discount_type'] ?? 'percentage',
                'value' => $data['discount_value'],
                'min' => $data['min_order_amount'] ?? 0,
                'max' => $data['max_discount_amount'] ?? null,
                'until' => $data['valid_until'],
                'usage_limit' => $data['usage_limit'] ?? 1000,
                'per_user' => $data['per_user_limit'] ?? 1
            ]);

            $couponId = $db->lastInsertId();
            AdminAuth::logAction($admin, 'create_coupon', 'coupons', (int)$couponId);

            Response::success(['coupon_id' => $couponId], 'Coupon created successfully', 201);
        } catch (\Exception $e) {
            Response::error('Failed to create coupon: ' . $e->getMessage(), 500);
        }
    }

    public static function updateCoupon(int $couponId, array $admin): void {
        $data = json_decode(file_get_contents('php://input'), true) ?? [];
        $db = Database::getConnection();

        try {
            $fields = [];
            $params = ['id' => $couponId];

            if (isset($data['discount_value'])) {
                $fields[] = "discount_value = :value";
                $params['value'] = $data['discount_value'];
            }
            if (isset($data['is_active'])) {
                $fields[] = "is_active = :active";
                $params['active'] = $data['is_active'];
            }

            if (empty($fields)) {
                Response::error('No fields to update', 422);
                return;
            }

            $fields[] = "updated_at = NOW()";
            $query = "UPDATE coupons SET " . implode(', ', $fields) . " WHERE id = :id";
            $db->prepare($query)->execute($params);

            AdminAuth::logAction($admin, 'update_coupon', 'coupons', $couponId);

            Response::success(['coupon_id' => $couponId], 'Coupon updated successfully');
        } catch (\Exception $e) {
            Response::error('Failed to update coupon: ' . $e->getMessage(), 500);
        }
    }

    // ===== PAYMENT MANAGEMENT =====

    public static function listPayments(array $admin): void {
        $db = Database::getConnection();
        $page = (int)($_GET['page'] ?? 1);
        $limit = (int)($_GET['limit'] ?? 20);
        $offset = ($page - 1) * $limit;

        $payments = $db->query("
            SELECT p.*, u.name as user_name, b.booking_code
            FROM payments p
            JOIN users u ON p.user_id = u.id
            JOIN bookings b ON p.booking_id = b.id
            ORDER BY p.created_at DESC
            LIMIT $limit OFFSET $offset
        ")->fetchAll(PDO::FETCH_ASSOC);

        $total = (int)$db->query("SELECT COUNT(*) FROM payments")->fetchColumn();

        Response::success(['payments' => $payments, 'pagination' => ['page' => $page, 'limit' => $limit, 'total' => $total]]);
    }

    public static function getPayment(int $paymentId, array $admin): void {
        $db = Database::getConnection();
        $stmt = $db->prepare("
            SELECT p.*, u.name as user_name, b.booking_code
            FROM payments p
            JOIN users u ON p.user_id = u.id
            JOIN bookings b ON p.booking_id = b.id
            WHERE p.id = :id
        ");
        $stmt->execute(['id' => $paymentId]);
        $payment = $stmt->fetch(PDO::FETCH_ASSOC);

        if (!$payment) {
            Response::error('Payment not found', 404);
            return;
        }

        Response::success(['payment' => $payment]);
    }

    // ===== PAYOUT MANAGEMENT =====

    public static function listPayouts(array $admin): void {
        $db = Database::getConnection();
        $page = (int)($_GET['page'] ?? 1);
        $limit = (int)($_GET['limit'] ?? 20);
        $offset = ($page - 1) * $limit;

        $payouts = $db->query("
            SELECT p.*, u.name as provider_name
            FROM payouts p
            JOIN providers pr ON p.provider_id = pr.id
            JOIN users u ON pr.user_id = u.id
            ORDER BY p.created_at DESC
            LIMIT $limit OFFSET $offset
        ")->fetchAll(PDO::FETCH_ASSOC);

        $total = (int)$db->query("SELECT COUNT(*) FROM payouts")->fetchColumn();

        Response::success(['payouts' => $payouts, 'pagination' => ['page' => $page, 'limit' => $limit, 'total' => $total]]);
    }

    public static function getPayout(int $payoutId, array $admin): void {
        $db = Database::getConnection();
        $stmt = $db->prepare("
            SELECT p.*, u.name as provider_name
            FROM payouts p
            JOIN providers pr ON p.provider_id = pr.id
            JOIN users u ON pr.user_id = u.id
            WHERE p.id = :id
        ");
        $stmt->execute(['id' => $payoutId]);
        $payout = $stmt->fetch(PDO::FETCH_ASSOC);

        if (!$payout) {
            Response::error('Payout not found', 404);
            return;
        }

        Response::success(['payout' => $payout]);
    }

    public static function processPayout(int $payoutId, array $admin): void {
        $db = Database::getConnection();
        $db->beginTransaction();
        try {
            $db->prepare("
                UPDATE payouts 
                SET status = 'paid', processed_by_admin_id = :admin, processed_at = NOW()
                WHERE id = :id
            ")->execute(['id' => $payoutId, 'admin' => $admin['id']]);

            AdminAuth::logAction($admin, 'process_payout', 'payouts', $payoutId);
            $db->commit();
            Response::success(['payout_id' => $payoutId], 'Payout processed successfully');
        } catch (\Exception $e) {
            $db->rollBack();
            Response::error('Failed to process payout: ' . $e->getMessage(), 500);
        }
    }

    // ===== USER MANAGEMENT =====

    public static function listUsers(array $admin): void {
        $db = Database::getConnection();
        $page = (int)($_GET['page'] ?? 1);
        $limit = (int)($_GET['limit'] ?? 20);
        $offset = ($page - 1) * $limit;

        $users = $db->query("
            SELECT u.*, r.name as role_name
            FROM users u
            JOIN user_roles r ON u.role_id = r.id
            WHERE u.deleted_at IS NULL
            ORDER BY u.created_at DESC
            LIMIT $limit OFFSET $offset
        ")->fetchAll(PDO::FETCH_ASSOC);

        $total = (int)$db->query("SELECT COUNT(*) FROM users WHERE deleted_at IS NULL")->fetchColumn();

        Response::success(['users' => $users, 'pagination' => ['page' => $page, 'limit' => $limit, 'total' => $total]]);
    }

    public static function getUser(int $userId, array $admin): void {
        $db = Database::getConnection();
        $stmt = $db->prepare("SELECT u.*, r.name as role_name FROM users u JOIN user_roles r ON u.role_id = r.id WHERE u.id = :id AND u.deleted_at IS NULL");
        $stmt->execute(['id' => $userId]);
        $user = $stmt->fetch(PDO::FETCH_ASSOC);

        if (!$user) {
            Response::error('User not found', 404);
            return;
        }

        Response::success(['user' => $user]);
    }

    public static function suspendUser(int $userId, array $admin): void {
        $db = Database::getConnection();
        try {
            $db->prepare("UPDATE users SET status = 'suspended', updated_at = NOW() WHERE id = :id")->execute(['id' => $userId]);
            AdminAuth::logAction($admin, 'suspend_user', 'users', $userId);
            Response::success(['user_id' => $userId], 'User suspended');
        } catch (\Exception $e) {
            Response::error('Failed to suspend user', 500);
        }
    }

    public static function activateUser(int $userId, array $admin): void {
        $db = Database::getConnection();
        try {
            $db->prepare("UPDATE users SET status = 'active', updated_at = NOW() WHERE id = :id")->execute(['id' => $userId]);
            AdminAuth::logAction($admin, 'activate_user', 'users', $userId);
            Response::success(['user_id' => $userId], 'User activated');
        } catch (\Exception $e) {
            Response::error('Failed to activate user', 500);
        }
    }

    // ===== REVIEW MANAGEMENT =====

    public static function listReviews(array $admin): void {
        $db = Database::getConnection();
        $page = (int)($_GET['page'] ?? 1);
        $limit = (int)($_GET['limit'] ?? 20);
        $offset = ($page - 1) * $limit;

        $reviews = $db->query("
            SELECT r.*, cu.name as customer_name, pu.name as provider_name, s.name as service_name
            FROM reviews r
            JOIN users cu ON r.customer_id = cu.id
            JOIN users pu ON r.provider_id = pu.id
            JOIN services s ON r.service_id = s.id
            ORDER BY r.created_at DESC
            LIMIT $limit OFFSET $offset
        ")->fetchAll(PDO::FETCH_ASSOC);

        $total = (int)$db->query("SELECT COUNT(*) FROM reviews")->fetchColumn();

        Response::success(['reviews' => $reviews, 'pagination' => ['page' => $page, 'limit' => $limit, 'total' => $total]]);
    }

    public static function hideReview(int $reviewId, array $admin): void {
        $db = Database::getConnection();
        try {
            $db->prepare("UPDATE reviews SET is_hidden = 1 WHERE id = :id")->execute(['id' => $reviewId]);
            AdminAuth::logAction($admin, 'hide_review', 'reviews', $reviewId);
            Response::success(['review_id' => $reviewId], 'Review hidden');
        } catch (\Exception $e) {
            Response::error('Failed to hide review', 500);
        }
    }

    public static function restoreReview(int $reviewId, array $admin): void {
        $db = Database::getConnection();
        try {
            $db->prepare("UPDATE reviews SET is_hidden = 0 WHERE id = :id")->execute(['id' => $reviewId]);
            AdminAuth::logAction($admin, 'restore_review', 'reviews', $reviewId);
            Response::success(['review_id' => $reviewId], 'Review restored');
        } catch (\Exception $e) {
            Response::error('Failed to restore review', 500);
        }
    }

    // ===== DISPUTE MANAGEMENT =====

    public static function listDisputes(array $admin): void {
        $db = Database::getConnection();
        $page = (int)($_GET['page'] ?? 1);
        $limit = (int)($_GET['limit'] ?? 20);
        $offset = ($page - 1) * $limit;

        $disputes = $db->query("
            SELECT d.*, u.name as customer_name, b.booking_code
            FROM disputes d
            JOIN users u ON d.raised_by_user_id = u.id
            JOIN bookings b ON d.booking_id = b.id
            ORDER BY d.created_at DESC
            LIMIT $limit OFFSET $offset
        ")->fetchAll(PDO::FETCH_ASSOC);

        $total = (int)$db->query("SELECT COUNT(*) FROM disputes")->fetchColumn();

        Response::success(['disputes' => $disputes, 'pagination' => ['page' => $page, 'limit' => $limit, 'total' => $total]]);
    }

    public static function getDispute(int $disputeId, array $admin): void {
        $db = Database::getConnection();
        $stmt = $db->prepare("
            SELECT d.*, u.name as customer_name, b.booking_code
            FROM disputes d
            JOIN users u ON d.raised_by_user_id = u.id
            JOIN bookings b ON d.booking_id = b.id
            WHERE d.id = :id
        ");
        $stmt->execute(['id' => $disputeId]);
        $dispute = $stmt->fetch(PDO::FETCH_ASSOC);

        if (!$dispute) {
            Response::error('Dispute not found', 404);
            return;
        }

        Response::success(['dispute' => $dispute]);
    }

    public static function resolveDispute(int $disputeId, array $admin): void {
        $data = json_decode(file_get_contents('php://input'), true) ?? [];
        $resolution = trim($data['resolution_notes'] ?? '');

        if (empty($resolution)) {
            Response::error('Resolution notes are required', 422);
            return;
        }

        $db = Database::getConnection();
        try {
            $db->prepare("
                UPDATE disputes 
                SET status = 'resolved', resolution_notes = :notes, resolved_by_admin_id = :admin, resolved_at = NOW()
                WHERE id = :id
            ")->execute(['id' => $disputeId, 'notes' => $resolution, 'admin' => $admin['id']]);

            AdminAuth::logAction($admin, 'resolve_dispute', 'disputes', $disputeId);
            Response::success(['dispute_id' => $disputeId], 'Dispute resolved');
        } catch (\Exception $e) {
            Response::error('Failed to resolve dispute: ' . $e->getMessage(), 500);
        }
    }

    // ===== AUDIT LOGS =====

    public static function auditLogs(array $admin): void {
        $db = Database::getConnection();
        $page = (int)($_GET['page'] ?? 1);
        $limit = (int)($_GET['limit'] ?? 50);
        $offset = ($page - 1) * $limit;

        $logs = $db->query("
            SELECT a.*, au.name as admin_name
            FROM audit_logs a
            LEFT JOIN admin_users au ON a.admin_id = au.id
            ORDER BY a.created_at DESC
            LIMIT $limit OFFSET $offset
        ")->fetchAll(PDO::FETCH_ASSOC);

        $total = (int)$db->query("SELECT COUNT(*) FROM audit_logs")->fetchColumn();

        Response::success(['logs' => $logs, 'pagination' => ['page' => $page, 'limit' => $limit, 'total' => $total]]);
    }

    // ===== REPORTS =====

    public static function reportDashboard(array $admin): void {
        self::dashboardStats($admin);
    }

    public static function reportBookings(array $admin): void {
        $db = Database::getConnection();
        $startDate = $_GET['start_date'] ?? date('Y-m-01');
        $endDate = $_GET['end_date'] ?? date('Y-m-d');

        $bookings = $db->query("
            SELECT DATE(created_at) as date, COUNT(*) as count, status
            FROM bookings
            WHERE created_at BETWEEN '$startDate' AND '$endDate'
            GROUP BY DATE(created_at), status
            ORDER BY DATE(created_at) DESC
        ")->fetchAll(PDO::FETCH_ASSOC);

        Response::success(['bookings' => $bookings, 'date_range' => ['start' => $startDate, 'end' => $endDate]]);
    }

    public static function reportRevenue(array $admin): void {
        $db = Database::getConnection();
        $startDate = $_GET['start_date'] ?? date('Y-m-01');
        $endDate = $_GET['end_date'] ?? date('Y-m-d');

        $revenue = $db->query("
            SELECT 
                DATE(created_at) as date,
                SUM(final_amount) as total_revenue,
                SUM(platform_fee) as platform_earnings,
                SUM(provider_payout_amount) as provider_earnings,
                COUNT(*) as bookings
            FROM bookings
            WHERE status = 'completed' AND created_at BETWEEN '$startDate' AND '$endDate'
            GROUP BY DATE(created_at)
            ORDER BY DATE(created_at) DESC
        ")->fetchAll(PDO::FETCH_ASSOC);

        Response::success(['revenue' => $revenue, 'date_range' => ['start' => $startDate, 'end' => $endDate]]);
    }

    // ===== SETTINGS =====

    public static function getSettings(array $admin): void {
        $db = Database::getConnection();
        $settings = $db->query("SELECT * FROM settings")->fetchAll(PDO::FETCH_KEY_PAIR);
        Response::success(['settings' => $settings]);
    }

    public static function updateSettings(array $admin): void {
        $data = json_decode(file_get_contents('php://input'), true) ?? [];
        $db = Database::getConnection();

        try {
            foreach ($data as $key => $value) {
                $db->prepare("INSERT INTO settings (key, value, updated_at) VALUES (:key, :value, NOW()) ON DUPLICATE KEY UPDATE value = :value, updated_at = NOW()")
                   ->execute(['key' => $key, 'value' => $value]);
            }

            AdminAuth::logAction($admin, 'update_settings', 'settings', 0, null, $data);
            Response::success([], 'Settings updated successfully');
        } catch (\Exception $e) {
            Response::error('Failed to update settings: ' . $e->getMessage(), 500);
        }
    }

    // ===== NOTIFICATIONS =====

    public static function sendNotification(array $admin): void {
        $data = json_decode(file_get_contents('php://input'), true) ?? [];

        if (empty($data['title']) || empty($data['message'])) {
            Response::error('Title and message are required', 422);
            return;
        }

        $db = Database::getConnection();
        try {
            $recipientType = $data['recipient_type'] ?? 'all'; // 'all', 'user', 'provider'
            $recipientId = $data['recipient_id'] ?? null;

            if ($recipientType === 'user' && $recipientId) {
                $db->prepare("
                    INSERT INTO notifications (user_id, title, message, type)
                    VALUES (:uid, :title, :msg, :type)
                ")->execute(['uid' => $recipientId, 'title' => $data['title'], 'msg' => $data['message'], 'type' => 'admin']);
            } elseif ($recipientType === 'all') {
                $users = $db->query("SELECT id FROM users WHERE deleted_at IS NULL")->fetchAll(PDO::FETCH_COLUMN);
                foreach ($users as $uid) {
                    $db->prepare("INSERT INTO notifications (user_id, title, message, type) VALUES (:uid, :title, :msg, :type)")
                       ->execute(['uid' => $uid, 'title' => $data['title'], 'msg' => $data['message'], 'type' => 'admin']);
                }
            }

            AdminAuth::logAction($admin, 'send_notification', 'notifications', 0, null, $data);
            Response::success([], 'Notification sent successfully');
        } catch (\Exception $e) {
            Response::error('Failed to send notification: ' . $e->getMessage(), 500);
        }
    }

    // ===== ADMIN MANAGEMENT (Super Admin Only) =====

    public static function listAdmins(array $admin): void {
        $db = Database::getConnection();
        $admins = $db->query("
            SELECT a.*, r.name as role_name
            FROM admin_users a
            JOIN admin_roles r ON a.role_id = r.id
            ORDER BY a.created_at DESC
        ")->fetchAll(PDO::FETCH_ASSOC);

        Response::success(['admins' => $admins]);
    }

    public static function createAdmin(array $admin): void {
        $data = json_decode(file_get_contents('php://input'), true) ?? [];

        if (empty($data['name']) || empty($data['email']) || empty($data['password']) || empty($data['role_id'])) {
            Response::error('Name, email, password, and role_id are required', 422);
            return;
        }

        $db = Database::getConnection();
        try {
            $passwordHash = password_hash($data['password'], PASSWORD_BCRYPT);
            $db->prepare("
                INSERT INTO admin_users (role_id, name, email, password_hash, is_active)
                VALUES (:role, :name, :email, :pass, 1)
            ")->execute([
                'role' => $data['role_id'],
                'name' => $data['name'],
                'email' => $data['email'],
                'pass' => $passwordHash
            ]);

            $adminId = $db->lastInsertId();
            AdminAuth::logAction($admin, 'create_admin', 'admin_users', (int)$adminId);

            Response::success(['admin_id' => $adminId], 'Admin created successfully', 201);
        } catch (\Exception $e) {
            Response::error('Failed to create admin: ' . $e->getMessage(), 500);
        }
    }

    public static function updateAdmin(int $adminId, array $admin): void {
        $data = json_decode(file_get_contents('php://input'), true) ?? [];
        $db = Database::getConnection();

        try {
            $fields = [];
            $params = ['id' => $adminId];

            if (isset($data['is_active'])) {
                $fields[] = "is_active = :active";
                $params['active'] = $data['is_active'];
            }

            if (empty($fields)) {
                Response::error('No fields to update', 422);
                return;
            }

            $fields[] = "updated_at = NOW()";
            $query = "UPDATE admin_users SET " . implode(', ', $fields) . " WHERE id = :id";
            $db->prepare($query)->execute($params);

            AdminAuth::logAction($admin, 'update_admin', 'admin_users', $adminId);

            Response::success(['admin_id' => $adminId], 'Admin updated successfully');
        } catch (\Exception $e) {
            Response::error('Failed to update admin: ' . $e->getMessage(), 500);
        }
    }
}
