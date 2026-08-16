<?php
declare(strict_types=1);

namespace Bropatch\Middleware;

use Bropatch\Helpers\JWT;
use Bropatch\Helpers\Response;
use Bropatch\Config\Database;
use PDO;

class AuthMiddleware {
    public static function authenticate(): array {
        $headers = getallheaders();
        $authHeader = $headers['Authorization'] ?? $headers['authorization'] ?? '';

        if (!preg_match('/Bearer\s(\S+)/', $authHeader, $matches)) {
            Response::unauthorized('Missing or invalid Authorization header token');
        }

        $token = $matches[1];
        $payload = JWT::verifyToken($token);

        if (!$payload || !isset($payload['user_id'])) {
            Response::unauthorized('Invalid or expired authentication token');
        }

        // Fetch user from DB to ensure still active
        $db = Database::getConnection();
        $stmt = $db->prepare("
            SELECT u.id, u.role_id, u.name, u.email, u.phone, u.status, r.name as role_name, p.id as provider_id, p.verification_status
            FROM users u
            JOIN user_roles r ON u.role_id = r.id
            LEFT JOIN providers p ON p.user_id = u.id AND p.deleted_at IS NULL
            WHERE u.id = :id AND u.deleted_at IS NULL
        ");
        $stmt->execute(['id' => $payload['user_id']]);
        $user = $stmt->fetch(PDO::FETCH_ASSOC);

        if (!$user) {
            Response::unauthorized('User not found or deleted');
        }

        if ($user['status'] === 'suspended') {
            Response::forbidden('Your account has been suspended by administration');
        }

        return $user;
    }

    public static function requireRole(string ...$allowedRoles): array {
        $user = self::authenticate();
        if (!in_array($user['role_name'], $allowedRoles, true)) {
            Response::forbidden("Action requires one of the following roles: " . implode(', ', $allowedRoles));
        }
        return $user;
    }

    public static function requireAdmin(): array {
        $headers = getallheaders();
        $authHeader = $headers['Authorization'] ?? $headers['authorization'] ?? '';

        if (!preg_match('/Bearer\s(\S+)/', $authHeader, $matches)) {
            Response::unauthorized('Missing admin authorization token');
        }

        $token = $matches[1];
        $payload = JWT::verifyToken($token);

        if (!$payload || !isset($payload['admin_id'])) {
            Response::unauthorized('Invalid admin token');
        }

        $db = Database::getConnection();
        $stmt = $db->prepare("
            SELECT a.id, a.role_id, a.name, a.email, a.is_active, r.name as role_name
            FROM admin_users a
            JOIN admin_roles r ON a.role_id = r.id
            WHERE a.id = :id AND a.is_active = 1
        ");
        $stmt->execute(['id' => $payload['admin_id']]);
        $admin = $stmt->fetch(PDO::FETCH_ASSOC);

        if (!$admin) {
            Response::forbidden('Admin account deactivated or unauthorized');
        }

        return $admin;
    }
}
