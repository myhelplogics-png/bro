<?php
declare(strict_types=1);

namespace Bropatch\Controllers;

use Bropatch\Config\Database;
use Bropatch\Helpers\Response;
use Bropatch\Middleware\AdminAuth;
use PDO;

class AdminAuthController {
    
    /**
     * Admin Login - POST /admin/api/login
     */
    public static function login(): void {
        try {
            $data = json_decode(file_get_contents('php://input'), true) ?? [];
            
            $email = trim($data['email'] ?? '');
            $password = $data['password'] ?? '';

            if (empty($email) || empty($password)) {
                Response::error('Email and password are required', 422);
                return;
            }

            // Use AdminAuth middleware to handle login
            $admin = AdminAuth::login($email, $password);

            Response::success([
                'admin' => $admin,
                'session' => [
                    'id' => session_id(),
                    'expires_in' => 3600
                ]
            ], 'Admin login successful', 200);

        } catch (\Exception $e) {
            Response::error($e->getMessage(), 401);
        }
    }

    /**
     * Admin Logout - POST /admin/api/logout
     */
    public static function logout(): void {
        try {
            $admin = AdminAuth::requireSession();
            
            AdminAuth::logAction(
                $admin,
                'admin_logout',
                'admin_users',
                $admin['id']
            );

            AdminAuth::destroySession();

            Response::success([], 'Admin logged out successfully');
        } catch (\Exception $e) {
            Response::error($e->getMessage(), 500);
        }
    }

    /**
     * Get Current Admin Session - GET /admin/api/me
     */
    public static function me(): void {
        try {
            $admin = AdminAuth::requireSession();
            
            $db = Database::getConnection();
            $stmt = $db->prepare("
                SELECT a.id, a.name, a.email, a.is_active, a.last_login_at,
                       r.id as role_id, r.name as role_name, r.description as role_description
                FROM admin_users a
                JOIN admin_roles r ON a.role_id = r.id
                WHERE a.id = :id
            ");
            $stmt->execute(['id' => $admin['id']]);
            $adminData = $stmt->fetch(PDO::FETCH_ASSOC);

            if (!$adminData) {
                Response::error('Admin user not found', 404);
                return;
            }

            Response::success([
                'admin' => [
                    'id' => (int)$adminData['id'],
                    'name' => $adminData['name'],
                    'email' => $adminData['email'],
                    'role_id' => (int)$adminData['role_id'],
                    'role' => $adminData['role_name'],
                    'role_description' => $adminData['role_description'],
                    'is_active' => (bool)$adminData['is_active'],
                    'last_login_at' => $adminData['last_login_at']
                ]
            ], 'Admin profile retrieved');

        } catch (\Exception $e) {
            Response::error($e->getMessage(), 500);
        }
    }

    /**
     * Get Admin Permissions - GET /admin/api/permissions
     */
    public static function getPermissions(): void {
        try {
            $admin = AdminAuth::requireSession();
            
            $db = Database::getConnection();
            
            // Super Admin gets all permissions
            if ($admin['role'] === 'Super Admin') {
                $modules = [
                    'users' => ['read' => true, 'write' => true, 'delete' => true],
                    'providers' => ['read' => true, 'write' => true, 'delete' => true],
                    'services' => ['read' => true, 'write' => true, 'delete' => true],
                    'categories' => ['read' => true, 'write' => true, 'delete' => true],
                    'bookings' => ['read' => true, 'write' => true, 'delete' => true],
                    'payments' => ['read' => true, 'write' => true, 'delete' => true],
                    'payouts' => ['read' => true, 'write' => true, 'delete' => true],
                    'coupons' => ['read' => true, 'write' => true, 'delete' => true],
                    'banners' => ['read' => true, 'write' => true, 'delete' => true],
                    'reviews' => ['read' => true, 'write' => true, 'delete' => true],
                    'disputes' => ['read' => true, 'write' => true, 'delete' => true],
                    'notifications' => ['read' => true, 'write' => true, 'delete' => true],
                    'audit_logs' => ['read' => true, 'write' => false, 'delete' => false],
                    'settings' => ['read' => true, 'write' => true, 'delete' => false],
                    'admins' => ['read' => true, 'write' => true, 'delete' => true]
                ];

                Response::success(['permissions' => $modules], 'Admin permissions retrieved');
                return;
            }

            // Get role-specific permissions
            $stmt = $db->prepare("
                SELECT module, can_read, can_write, can_delete
                FROM admin_permissions
                WHERE role_id = :role_id
                ORDER BY module ASC
            ");
            $stmt->execute(['role_id' => $admin['role_id']]);
            $perms = $stmt->fetchAll(PDO::FETCH_ASSOC);

            $modules = [];
            foreach ($perms as $perm) {
                $modules[$perm['module']] = [
                    'read' => (bool)$perm['can_read'],
                    'write' => (bool)$perm['can_write'],
                    'delete' => (bool)$perm['can_delete']
                ];
            }

            Response::success(['permissions' => $modules], 'Admin permissions retrieved');

        } catch (\Exception $e) {
            Response::error($e->getMessage(), 500);
        }
    }
}
