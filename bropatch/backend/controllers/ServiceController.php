<?php
declare(strict_types=1);

namespace Bropatch\Controllers;

use Bropatch\Config\Database;
use Bropatch\Helpers\Response;
use PDO;

class CategoryController {
    public static function index(): void {
        $db = Database::getConnection();
        $stmt = $db->query("
            SELECT c.*, COUNT(s.id) as services_count
            FROM service_categories c
            LEFT JOIN services s ON s.category_id = c.id AND s.is_active = 1 AND s.deleted_at IS NULL
            WHERE c.is_active = 1 AND c.deleted_at IS NULL
            GROUP BY c.id
            ORDER BY c.sort_order ASC, c.name ASC
        ");
        $categories = $stmt->fetchAll(PDO::FETCH_ASSOC);
        Response::success($categories);
    }
}

class ServiceController {
    public static function index(): void {
        $db = Database::getConnection();
        $categoryId = isset($_GET['category_id']) ? (int)$_GET['category_id'] : null;
        $search = trim($_GET['search'] ?? '');

        $sql = "
            SELECT s.*, c.name as category_name, c.slug as category_slug
            FROM services s
            JOIN service_categories c ON s.category_id = c.id
            WHERE s.is_active = 1 AND s.deleted_at IS NULL
        ";
        $params = [];

        if ($categoryId) {
            $sql .= " AND s.category_id = :cat_id";
            $params['cat_id'] = $categoryId;
        }

        if (!empty($search)) {
            $sql .= " AND (s.name LIKE :search OR s.description LIKE :search OR s.short_description LIKE :search)";
            $params['search'] = "%{$search}%";
        }

        $sql .= " ORDER BY s.rating_avg DESC, s.name ASC";

        $stmt = $db->prepare($sql);
        $stmt->execute($params);
        $services = $stmt->fetchAll(PDO::FETCH_ASSOC);

        Response::success($services);
    }

    public static function show(int $id): void {
        $db = Database::getConnection();
        $stmt = $db->prepare("
            SELECT s.*, c.name as category_name, c.slug as category_slug
            FROM services s
            JOIN service_categories c ON s.category_id = c.id
            WHERE s.id = :id AND s.deleted_at IS NULL
        ");
        $stmt->execute(['id' => $id]);
        $service = $stmt->fetch(PDO::FETCH_ASSOC);

        if (!$service) {
            Response::notFound('Service not found');
        }

        // Fetch recent reviews
        $rStmt = $db->prepare("
            SELECT r.*, u.name as customer_name, u.avatar_url as customer_avatar
            FROM reviews r
            JOIN users u ON r.customer_id = u.id
            WHERE r.service_id = :sid AND r.is_hidden = 0
            ORDER BY r.created_at DESC
            LIMIT 10
        ");
        $rStmt->execute(['sid' => $id]);
        $service['recent_reviews'] = $rStmt->fetchAll(PDO::FETCH_ASSOC);

        Response::success($service);
    }
}
