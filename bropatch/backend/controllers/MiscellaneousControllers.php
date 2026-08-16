<?php
declare(strict_types=1);

namespace Bropatch\Controllers;

use Bropatch\Config\Database;
use Bropatch\Helpers\Response;
use PDO;

class ChatController {
    public static function getMessages(int $bookingId, array $currentUser): void {
        $db = Database::getConnection();
        $stmt = $db->prepare("
            SELECT m.*, u.name as sender_name, u.role_id
            FROM messages m
            JOIN conversations c ON m.conversation_id = c.id
            JOIN users u ON m.sender_id = u.id
            WHERE c.booking_id = :bid
            ORDER BY m.created_at ASC
        ");
        $stmt->execute(['bid' => $bookingId]);
        Response::success($stmt->fetchAll(PDO::FETCH_ASSOC));
    }

    public static function sendMessage(int $bookingId, array $currentUser): void {
        $data = json_decode(file_get_contents('php://input'), true) ?? [];
        $text = trim($data['message_text'] ?? '');

        if (empty($text)) {
            Response::error('Message text cannot be empty', 422);
        }

        $db = Database::getConnection();
        // Get or create conversation
        $cStmt = $db->prepare("SELECT id FROM conversations WHERE booking_id = :bid");
        $cStmt->execute(['bid' => $bookingId]);
        $conv = $cStmt->fetch(PDO::FETCH_ASSOC);

        if (!$conv) {
            $db->prepare("INSERT INTO conversations (booking_id) VALUES (:bid)")->execute(['bid' => $bookingId]);
            $convId = (int)$db->lastInsertId();
        } else {
            $convId = (int)$conv['id'];
        }

        $stmt = $db->prepare("
            INSERT INTO messages (conversation_id, sender_id, message_text, is_read)
            VALUES (:cid, :sid, :txt, 0)
        ");
        $stmt->execute([
            'cid' => $convId,
            'sid' => $currentUser['id'],
            'txt' => $text
        ]);
        $msgId = (int)$db->lastInsertId();

        Response::success([
            'id' => $msgId,
            'conversation_id' => $convId,
            'sender_id' => $currentUser['id'],
            'sender_name' => $currentUser['name'],
            'message_text' => $text,
            'created_at' => date('Y-m-d H:i:s')
        ], 'Message sent', 201);
    }
}

class CouponController {
    public static function validate(array $currentUser): void {
        $data = json_decode(file_get_contents('php://input'), true) ?? [];
        $code = trim(strtoupper($data['code'] ?? ''));
        $orderAmount = (float)($data['order_amount'] ?? 0.00);

        if (empty($code)) {
            Response::error('Coupon code is required', 422);
        }

        $db = Database::getConnection();
        $stmt = $db->prepare("
            SELECT * FROM coupons 
            WHERE code = :code AND is_active = 1 AND valid_until >= NOW()
        ");
        $stmt->execute(['code' => $code]);
        $coupon = $stmt->fetch(PDO::FETCH_ASSOC);

        if (!$coupon) {
            Response::error('Invalid or expired coupon code', 404);
        }

        if ($orderAmount < (float)$coupon['min_order_amount']) {
            Response::error("Minimum order amount of ₹{$coupon['min_order_amount']} required for this coupon", 422);
        }

        $discount = 0.00;
        if ($coupon['discount_type'] === 'percentage') {
            $discount = ($orderAmount * (float)$coupon['discount_value']) / 100;
            if ($coupon['max_discount_amount'] && $discount > (float)$coupon['max_discount_amount']) {
                $discount = (float)$coupon['max_discount_amount'];
            }
        } else {
            $discount = min($orderAmount, (float)$coupon['discount_value']);
        }

        Response::success([
            'coupon_id' => $coupon['id'],
            'code' => $coupon['code'],
            'discount_amount' => round($discount, 2),
            'discount_type' => $coupon['discount_type'],
            'discount_value' => (float)$coupon['discount_value']
        ], 'Coupon applied successfully');
    }
}

class BannerController {
    public static function index(): void {
        $db = Database::getConnection();
        $stmt = $db->query("
            SELECT * FROM banners 
            WHERE is_active = 1 
            ORDER BY sort_order ASC, id DESC
        ");
        Response::success($stmt->fetchAll(PDO::FETCH_ASSOC));
    }
}

class ReviewController {
    public static function create(array $currentUser): void {
        $data = json_decode(file_get_contents('php://input'), true) ?? [];
        $bookingId = (int)($data['booking_id'] ?? 0);
        $rating = (int)($data['rating'] ?? 5);
        $reviewText = trim($data['review_text'] ?? '');

        if ($bookingId <= 0 || $rating < 1 || $rating > 5) {
            Response::error('Valid booking ID and rating (1-5) are required', 422);
        }

        $db = Database::getConnection();
        $bStmt = $db->prepare("SELECT * FROM bookings WHERE id = :bid AND customer_id = :cid");
        $bStmt->execute(['bid' => $bookingId, 'cid' => $currentUser['id']]);
        $booking = $bStmt->fetch(PDO::FETCH_ASSOC);

        if (!$booking || $booking['status'] !== 'completed') {
            Response::error('You can only review completed bookings', 422);
        }

        $stmt = $db->prepare("
            INSERT INTO reviews (booking_id, customer_id, provider_id, service_id, rating, review_text)
            VALUES (:bid, :cid, :pid, :sid, :rating, :txt)
        ");
        $stmt->execute([
            'bid' => $bookingId,
            'cid' => $currentUser['id'],
            'pid' => $booking['provider_id'],
            'sid' => $booking['service_id'],
            'rating' => $rating,
            'txt' => $reviewText
        ]);

        Response::success(['review_id' => $db->lastInsertId()], 'Review submitted successfully', 201);
    }
}
