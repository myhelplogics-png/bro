<?php
declare(strict_types=1);

namespace Bropatch\Controllers;

use Bropatch\Config\Database;
use Bropatch\Helpers\Response;
use Bropatch\Helpers\JWT;
use PDO;

class AuthController {
    public static function register(): void {
        $data = json_decode(file_get_contents('php://input'), true) ?? [];
        
        $name = trim($data['name'] ?? '');
        $email = trim(strtolower($data['email'] ?? ''));
        $phone = trim($data['phone'] ?? '');
        $password = $data['password'] ?? '';
        $role = $data['role'] ?? 'customer'; // 'customer' or 'provider'

        if (empty($name) || empty($email) || empty($password)) {
            Response::error('Name, email, and password are required', 422);
        }

        if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
            Response::error('Invalid email format', 422);
        }

        if (strlen($password) < 6) {
            Response::error('Password must be at least 6 characters', 422);
        }

        $db = Database::getConnection();

        // Check if user exists
        $stmt = $db->prepare("SELECT id FROM users WHERE email = :email");
        $stmt->execute(['email' => $email]);
        if ($stmt->fetch()) {
            Response::error('An account with this email already exists', 409);
        }

        $roleId = ($role === 'provider') ? 2 : 1;
        $passwordHash = password_hash($password, PASSWORD_BCRYPT);

        $db->beginTransaction();
        try {
            $stmt = $db->prepare("
                INSERT INTO users (role_id, name, email, phone, password_hash, status)
                VALUES (:role_id, :name, :email, :phone, :hash, 'active')
            ");
            $stmt->execute([
                'role_id' => $roleId,
                'name' => $name,
                'email' => $email,
                'phone' => $phone,
                'hash' => $passwordHash
            ]);
            $userId = (int)$db->lastInsertId();

            $providerId = null;
            if ($role === 'provider') {
                $pStmt = $db->prepare("
                    INSERT INTO providers (user_id, business_name, verification_status, is_available)
                    VALUES (:user_id, :bname, 'pending', 0)
                ");
                $pStmt->execute([
                    'user_id' => $userId,
                    'bname' => $name . ' Services'
                ]);
                $providerId = (int)$db->lastInsertId();

                // Initialize credits
                $db->prepare("INSERT INTO provider_credits (provider_id, current_balance) VALUES (:pid, 1000.00)")
                   ->execute(['pid' => $providerId]);
            }

            $db->commit();

            $token = JWT::generateToken([
                'user_id' => $userId,
                'role_id' => $roleId,
                'email' => $email
            ]);

            Response::success([
                'token' => $token,
                'user' => [
                    'id' => $userId,
                    'name' => $name,
                    'email' => $email,
                    'phone' => $phone,
                    'role' => $role,
                    'provider_id' => $providerId,
                    'verification_status' => ($role === 'provider') ? 'pending' : null
                ]
            ], 'Account created successfully', 201);

        } catch (\Exception $e) {
            $db->rollBack();
            Response::error('Failed to create account: ' . $e->getMessage(), 500);
        }
    }

    public static function login(): void {
        $data = json_decode(file_get_contents('php://input'), true) ?? [];
        $email = trim(strtolower($data['email'] ?? ''));
        $password = $data['password'] ?? '';

        if (empty($email) || empty($password)) {
            Response::error('Email and password are required', 422);
        }

        $db = Database::getConnection();
        $stmt = $db->prepare("
            SELECT u.id, u.role_id, u.name, u.email, u.phone, u.password_hash, u.status, u.avatar_url,
                   r.name as role_name, p.id as provider_id, p.verification_status, p.is_online
            FROM users u
            JOIN user_roles r ON u.role_id = r.id
            LEFT JOIN providers p ON p.user_id = u.id AND p.deleted_at IS NULL
            WHERE u.email = :email AND u.deleted_at IS NULL
        ");
        $stmt->execute(['email' => $email]);
        $user = $stmt->fetch(PDO::FETCH_ASSOC);

        if (!$user || !password_verify($password, $user['password_hash'])) {
            Response::error('Invalid email or password', 401);
        }

        if ($user['status'] === 'suspended') {
            Response::forbidden('Your account is suspended. Contact support.');
        }

        $token = JWT::generateToken([
            'user_id' => (int)$user['id'],
            'role_id' => (int)$user['role_id'],
            'email' => $user['email']
        ]);

        Response::success([
            'token' => $token,
            'user' => [
                'id' => (int)$user['id'],
                'name' => $user['name'],
                'email' => $user['email'],
                'phone' => $user['phone'],
                'avatar_url' => $user['avatar_url'],
                'role' => $user['role_name'],
                'provider_id' => $user['provider_id'] ? (int)$user['provider_id'] : null,
                'verification_status' => $user['verification_status']
            ]
        ], 'Login successful');
    }

    public static function me(array $currentUser): void {
        Response::success(['user' => $currentUser], 'User profile retrieved');
    }
}
