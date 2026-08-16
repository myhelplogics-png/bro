<?php
declare(strict_types=1);

namespace Bropatch\Controllers;

use Bropatch\Config\Database;
use Bropatch\Helpers\Response;
use PDO;

class BookingController {
    // Valid state transitions
    private static array $transitions = [
        'pending' => ['searching_provider', 'provider_assigned', 'cancelled'],
        'searching_provider' => ['provider_assigned', 'cancelled'],
        'provider_assigned' => ['provider_accepted', 'searching_provider', 'cancelled'],
        'provider_accepted' => ['provider_on_way', 'cancelled'],
        'provider_on_way' => ['provider_arrived', 'cancelled'],
        'provider_arrived' => ['work_started', 'cancelled'],
        'work_started' => ['work_completed', 'disputed'],
        'work_completed' => ['payment_pending', 'completed', 'disputed'],
        'payment_pending' => ['completed', 'disputed'],
        'completed' => ['disputed'],
        'cancelled' => [],
        'disputed' => ['resolved', 'completed', 'cancelled']
    ];

    public static function create(array $currentUser): void {
        $data = json_decode(file_get_contents('php://input'), true) ?? [];

        $serviceId = (int)($data['service_id'] ?? 0);
        $addressId = (int)($data['address_id'] ?? 0);
        $scheduledDate = $data['scheduled_date'] ?? '';
        $scheduledTimeSlot = $data['scheduled_time_slot'] ?? '';
        $problemDescription = trim($data['problem_description'] ?? '');
        $couponCode = trim($data['coupon_code'] ?? '');
        $paymentMethod = in_array($data['payment_method'] ?? '', ['razorpay', 'cod', 'wallet']) ? $data['payment_method'] : 'razorpay';
        $contactPhone = trim($data['customer_phone'] ?? $currentUser['phone'] ?? '+91 98765 43210');

        if ($serviceId <= 0 || $addressId <= 0 || empty($scheduledDate) || empty($scheduledTimeSlot)) {
            Response::error('Service, address, date, and time slot are required', 422);
        }

        $db = Database::getConnection();

        // 1. Fetch Service & Validate
        $stmt = $db->prepare("SELECT * FROM services WHERE id = :id AND is_active = 1 AND deleted_at IS NULL");
        $stmt->execute(['id' => $serviceId]);
        $service = $stmt->fetch(PDO::FETCH_ASSOC);

        if (!$service) {
            Response::error('Selected service is unavailable', 404);
        }

        // 2. Fetch Address & Validate
        $aStmt = $db->prepare("SELECT * FROM addresses WHERE id = :id AND user_id = :uid AND deleted_at IS NULL");
        $aStmt->execute(['id' => $addressId, 'uid' => $currentUser['id']]);
        $address = $aStmt->fetch(PDO::FETCH_ASSOC);

        if (!$address) {
            Response::error('Selected address is invalid or not found', 404);
        }

        // 3. Calculate Pricing Server-Side (Never trust frontend amount)
        $baseAmount = (float)($service['discount_price'] ?? $service['base_price']);
        $discountAmount = 0.00;
        $couponId = null;

        if (!empty($couponCode)) {
            $cStmt = $db->prepare("
                SELECT * FROM coupons 
                WHERE code = :code AND is_active = 1 AND valid_until >= NOW()
            ");
            $cStmt->execute(['code' => $couponCode]);
            $coupon = $cStmt->fetch(PDO::FETCH_ASSOC);

            if ($coupon && $baseAmount >= (float)$coupon['min_order_amount']) {
                $couponId = (int)$coupon['id'];
                if ($coupon['discount_type'] === 'percentage') {
                    $discountAmount = ($baseAmount * (float)$coupon['discount_value']) / 100;
                    if ($coupon['max_discount_amount'] && $discountAmount > (float)$coupon['max_discount_amount']) {
                        $discountAmount = (float)$coupon['max_discount_amount'];
                    }
                } else {
                    $discountAmount = (float)$coupon['discount_value'];
                }
                // increment coupon total used
                $db->prepare("UPDATE coupons SET total_used = total_used + 1 WHERE id = :id")->execute(['id' => $couponId]);
            }
        }

        $subtotal = max(0.00, $baseAmount - $discountAmount);
        $taxGstRate = 0.18; // 18% GST
        $taxAmount = round($subtotal * $taxGstRate, 2);
        $finalAmount = round($subtotal + $taxAmount, 2);

        $platformCommissionPercent = 15; // 15% platform fee
        $platformFee = round(($subtotal * $platformCommissionPercent) / 100, 2);
        $providerPayoutAmount = round($subtotal - $platformFee, 2);

        $bookingCode = 'BP-' . date('Y') . '-' . strtoupper(substr(md5(uniqid()), 0, 6));

        $db->beginTransaction();
        try {
            // Find an approved online provider for this service category or assign smart match
            $pStmt = $db->prepare("
                SELECT p.id 
                FROM providers p
                JOIN provider_services ps ON ps.provider_id = p.id
                WHERE ps.service_id = :sid AND p.verification_status = 'approved' AND p.is_available = 1
                LIMIT 1
            ");
            $pStmt->execute(['sid' => $serviceId]);
            $assignedProvider = $pStmt->fetch(PDO::FETCH_ASSOC);
            $providerId = $assignedProvider ? (int)$assignedProvider['id'] : null;
            $initialStatus = $providerId ? 'provider_assigned' : 'searching_provider';

            $bStmt = $db->prepare("
                INSERT INTO bookings (
                    booking_code, customer_id, provider_id, service_id, address_id, coupon_id,
                    status, scheduled_date, scheduled_time_slot, problem_description,
                    base_amount, discount_amount, tax_amount, final_amount, platform_fee, provider_payout_amount,
                    payment_method, payment_status, customer_contact_phone
                ) VALUES (
                    :code, :cid, :pid, :sid, :aid, :cpid,
                    :status, :sdate, :slot, :prob,
                    :base, :disc, :tax, :final, :pfee, :payout,
                    :pmethod, :pstatus, :phone
                )
            ");
            $bStmt->execute([
                'code' => $bookingCode,
                'cid' => $currentUser['id'],
                'pid' => $providerId,
                'sid' => $serviceId,
                'aid' => $addressId,
                'cpid' => $couponId,
                'status' => $initialStatus,
                'sdate' => $scheduledDate,
                'slot' => $scheduledTimeSlot,
                'prob' => $problemDescription,
                'base' => $baseAmount,
                'disc' => $discountAmount,
                'tax' => $taxAmount,
                'final' => $finalAmount,
                'pfee' => $platformFee,
                'payout' => $providerPayoutAmount,
                'pmethod' => $paymentMethod,
                'pstatus' => ($paymentMethod === 'razorpay') ? 'paid' : 'pending',
                'phone' => $contactPhone
            ]);
            $bookingId = (int)$db->lastInsertId();

            // Line item
            $db->prepare("
                INSERT INTO booking_items (booking_id, service_id, item_name, quantity, unit_price, total_price)
                VALUES (:bid, :sid, :name, 1, :uprice, :tprice)
            ")->execute([
                'bid' => $bookingId,
                'sid' => $serviceId,
                'name' => $service['name'],
                'uprice' => $baseAmount,
                'tprice' => $baseAmount
            ]);

            // Status history
            $db->prepare("
                INSERT INTO booking_status_history (booking_id, old_status, new_status, changed_by_user_id, changed_by_role, reason)
                VALUES (:bid, 'pending', :nstatus, :uid, 'customer', 'Booking placed by customer')
            ")->execute([
                'bid' => $bookingId,
                'nstatus' => $initialStatus,
                'uid' => $currentUser['id']
            ]);

            // Create chat conversation
            $db->prepare("INSERT INTO conversations (booking_id) VALUES (:bid)")->execute(['bid' => $bookingId]);
            $convId = (int)$db->lastInsertId();
            $db->prepare("INSERT INTO conversation_participants (conversation_id, user_id) VALUES (:cid, :uid)")
               ->execute(['cid' => $convId, 'uid' => $currentUser['id']]);

            if ($providerId) {
                // Get provider user_id
                $pUserStmt = $db->prepare("SELECT user_id FROM providers WHERE id = :pid");
                $pUserStmt->execute(['pid' => $providerId]);
                $pUser = $pUserStmt->fetch(PDO::FETCH_ASSOC);
                if ($pUser) {
                    $db->prepare("INSERT INTO conversation_participants (conversation_id, user_id) VALUES (:cid, :uid)")
                       ->execute(['cid' => $convId, 'uid' => $pUser['user_id']]);
                }
            }

            // Create initial payment record
            $db->prepare("
                INSERT INTO payments (booking_id, user_id, amount, payment_method, payment_status)
                VALUES (:bid, :uid, :amt, :pm, :ps)
            ")->execute([
                'bid' => $bookingId,
                'uid' => $currentUser['id'],
                'amt' => $finalAmount,
                'pm' => $paymentMethod,
                'ps' => ($paymentMethod === 'razorpay') ? 'captured' : 'pending'
            ]);

            $db->commit();

            Response::success([
                'booking_id' => $bookingId,
                'booking_code' => $bookingCode,
                'status' => $initialStatus,
                'final_amount' => $finalAmount,
                'scheduled_date' => $scheduledDate,
                'scheduled_time_slot' => $scheduledTimeSlot
            ], 'Booking placed successfully', 201);

        } catch (\Exception $e) {
            $db->rollBack();
            Response::error('Failed to create booking: ' . $e->getMessage(), 500);
        }
    }

    public static function index(array $currentUser): void {
        $db = Database::getConnection();
        $isProvider = ($currentUser['role_name'] === 'provider');
        
        $sql = "
            SELECT b.*, 
                   s.name as service_name, s.image_url as service_image,
                   u.name as customer_name, u.phone as customer_phone,
                   p.business_name as provider_business_name,
                   pu.name as provider_name, pu.phone as provider_phone,
                   a.street_address, a.city, a.latitude as address_lat, a.longitude as address_lng
            FROM bookings b
            JOIN services s ON b.service_id = s.id
            JOIN users u ON b.customer_id = u.id
            JOIN addresses a ON b.address_id = a.id
            LEFT JOIN providers p ON b.provider_id = p.id
            LEFT JOIN users pu ON p.user_id = pu.id
            WHERE b.deleted_at IS NULL
        ";
        $params = [];

        if ($isProvider) {
            $sql .= " AND (b.provider_id = :pid OR (b.status = 'searching_provider'))";
            $params['pid'] = $currentUser['provider_id'];
        } else {
            $sql .= " AND b.customer_id = :cid";
            $params['cid'] = $currentUser['id'];
        }

        $sql .= " ORDER BY b.created_at DESC";

        $stmt = $db->prepare($sql);
        $stmt->execute($params);
        $bookings = $stmt->fetchAll(PDO::FETCH_ASSOC);

        Response::success($bookings);
    }

    public static function updateStatus(int $id, array $currentUser): void {
        $data = json_decode(file_get_contents('php://input'), true) ?? [];
        $newStatus = $data['status'] ?? '';
        $reason = trim($data['reason'] ?? '');
        $lat = isset($data['latitude']) ? (float)$data['latitude'] : null;
        $lng = isset($data['longitude']) ? (float)$data['longitude'] : null;

        $db = Database::getConnection();
        $stmt = $db->prepare("SELECT * FROM bookings WHERE id = :id AND deleted_at IS NULL");
        $stmt->execute(['id' => $id]);
        $booking = $stmt->fetch(PDO::FETCH_ASSOC);

        if (!$booking) {
            Response::notFound('Booking not found');
        }

        $currentStatus = $booking['status'];

        // Validate state machine transition
        $allowed = self::$transitions[$currentStatus] ?? [];
        if (!in_array($newStatus, $allowed, true)) {
            Response::error("Invalid status transition from '{$currentStatus}' to '{$newStatus}'", 422);
        }

        $db->beginTransaction();
        try {
            $db->prepare("UPDATE bookings SET status = :status WHERE id = :id")
               ->execute(['status' => $newStatus, 'id' => $id]);

            // If completed, update provider earnings & total jobs, generate invoice
            if ($newStatus === 'completed') {
                if ($booking['provider_id']) {
                    $db->prepare("
                        UPDATE providers 
                        SET total_jobs_completed = total_jobs_completed + 1,
                            total_earnings = total_earnings + :payout,
                            pending_payout_balance = pending_payout_balance + :payout
                        WHERE id = :pid
                    ")->execute([
                        'payout' => $booking['provider_payout_amount'],
                        'pid' => $booking['provider_id']
                    ]);
                }
            }

            // Log status history
            $db->prepare("
                INSERT INTO booking_status_history (booking_id, old_status, new_status, changed_by_user_id, changed_by_role, reason, latitude, longitude)
                VALUES (:bid, :old, :new, :uid, :role, :reason, :lat, :lng)
            ")->execute([
                'bid' => $id,
                'old' => $currentStatus,
                'new' => $newStatus,
                'uid' => $currentUser['id'],
                'role' => $currentUser['role_name'],
                'reason' => $reason,
                'lat' => $lat,
                'lng' => $lng
            ]);

            $db->commit();

            Response::success([
                'booking_id' => $id,
                'old_status' => $currentStatus,
                'new_status' => $newStatus
            ], "Booking status updated to {$newStatus}");

        } catch (\Exception $e) {
            $db->rollBack();
            Response::error('Failed to update status: ' . $e->getMessage(), 500);
        }
    }
}
