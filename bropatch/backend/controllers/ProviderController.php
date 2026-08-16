<?php
declare(strict_types=1);

namespace Bropatch\Controllers;

use Bropatch\Config\Database;
use Bropatch\Helpers\Response;
use PDO;

class ProviderController {
    public static function profile(array $currentUser): void {
        $db = Database::getConnection();
        $stmt = $db->prepare("
            SELECT p.*, u.name, u.email, u.phone, u.avatar_url, pc.current_balance as credit_balance
            FROM providers p
            JOIN users u ON p.user_id = u.id
            LEFT JOIN provider_credits pc ON pc.provider_id = p.id
            WHERE p.user_id = :uid AND p.deleted_at IS NULL
        ");
        $stmt->execute(['uid' => $currentUser['id']]);
        $provider = $stmt->fetch(PDO::FETCH_ASSOC);

        if (!$provider) {
            Response::notFound('Provider profile not found');
        }

        // Fetch documents
        $dStmt = $db->prepare("SELECT * FROM provider_documents WHERE provider_id = :pid");
        $dStmt->execute(['pid' => $provider['id']]);
        $provider['documents'] = $dStmt->fetchAll(PDO::FETCH_ASSOC);

        // Fetch services
        $sStmt = $db->prepare("
            SELECT s.id, s.name, s.base_price, ps.custom_price, ps.is_active
            FROM provider_services ps
            JOIN services s ON ps.service_id = s.id
            WHERE ps.provider_id = :pid
        ");
        $sStmt->execute(['pid' => $provider['id']]);
        $provider['services'] = $sStmt->fetchAll(PDO::FETCH_ASSOC);

        Response::success($provider);
    }

    public static function updateLocation(array $currentUser): void {
        $data = json_decode(file_get_contents('php://input'), true) ?? [];
        $lat = (float)($data['latitude'] ?? 0);
        $lng = (float)($data['longitude'] ?? 0);

        if ($lat == 0 || $lng == 0) {
            Response::error('Valid latitude and longitude are required', 422);
        }

        $db = Database::getConnection();
        $stmt = $db->prepare("
            UPDATE providers 
            SET current_latitude = :lat, current_longitude = :lng, updated_at = NOW() 
            WHERE user_id = :uid
        ");
        $stmt->execute(['lat' => $lat, 'lng' => $lng, 'uid' => $currentUser['id']]);

        Response::success(['latitude' => $lat, 'longitude' => $lng], 'Provider location updated');
    }

    public static function toggleOnline(array $currentUser): void {
        $data = json_decode(file_get_contents('php://input'), true) ?? [];
        $isOnline = !empty($data['is_online']) ? 1 : 0;

        $db = Database::getConnection();
        $stmt = $db->prepare("UPDATE providers SET is_online = :online WHERE user_id = :uid");
        $stmt->execute(['online' => $isOnline, 'uid' => $currentUser['id']]);

        Response::success(['is_online' => $isOnline], $isOnline ? 'Provider is now Online' : 'Provider is now Offline');
    }

    public static function requestPayout(array $currentUser): void {
        $data = json_decode(file_get_contents('php://input'), true) ?? [];
        $amount = (float)($data['amount'] ?? 0);
        $bankMask = trim($data['bank_account_mask'] ?? 'HDFC Bank - XX4819');
        $ifsc = trim($data['ifsc_code'] ?? 'HDFC0001244');

        if ($amount < 500) {
            Response::error('Minimum withdrawal amount is ₹500.00', 422);
        }

        $db = Database::getConnection();
        $pStmt = $db->prepare("SELECT id, pending_payout_balance FROM providers WHERE user_id = :uid");
        $pStmt->execute(['uid' => $currentUser['id']]);
        $provider = $pStmt->fetch(PDO::FETCH_ASSOC);

        if (!$provider || (float)$provider['pending_payout_balance'] < $amount) {
            Response::error('Insufficient withdrawable balance', 422);
        }

        $db->beginTransaction();
        try {
            $ref = 'PO-' . date('Ymd') . '-' . rand(1000, 9999);
            $db->prepare("
                INSERT INTO payouts (payout_reference, provider_id, amount, status, bank_account_mask, ifsc_code)
                VALUES (:ref, :pid, :amt, 'pending', :bank, :ifsc)
            ")->execute([
                'ref' => $ref,
                'pid' => $provider['id'],
                'amt' => $amount,
                'bank' => $bankMask,
                'ifsc' => $ifsc
            ]);

            $db->prepare("
                UPDATE providers 
                SET pending_payout_balance = pending_payout_balance - :amt 
                WHERE id = :pid
            ")->execute(['amt' => $amount, 'pid' => $provider['id']]);

            $db->commit();

            Response::success(['payout_reference' => $ref, 'amount' => $amount], 'Payout request submitted successfully');
        } catch (\Exception $e) {
            $db->rollBack();
            Response::error('Failed to process payout: ' . $e->getMessage(), 500);
        }
    }
}
