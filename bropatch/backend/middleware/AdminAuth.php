<?php
declare(strict_types=1);

namespace Bropatch\Middleware;

use Bropatch\Config\Database;
use Bropatch\Helpers\Response;
use PDO;

class AdminAuth {
    private const SESSION_TIMEOUT = 3600; // 1 hour
    private const MAX_LOGIN_ATTEMPTS = 5;
    private const LOCKOUT_DURATION = 900; // 15 minutes

    /**
     * Initialize secure admin session
     */
    public static function startSession(): void {
        if (session_status() === PHP_SESSION_NONE) {
            ini_set('session.cookie_httponly', '1');
            ini_set('session.cookie_secure', '1'); // HTTPS only
            ini_set('session.cookie_samesite', 'Strict');
            ini_set('session.gc_maxlifetime', self::SESSION_TIMEOUT);
            
            session_start();
        }
    }

    /**
     * Authenticate admin login
     */
    public static function login(string $email, string $password): array {
        self::startSession();
        
        $email = trim(strtolower($email));
        
        if (empty($email) || empty($password)) {
            throw new \Exception('Email and password are required');
        }

        if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
            throw new \Exception('Invalid email format');
        }

        $db = Database::getConnection();

        // Check for login attempt lockout
        $lockoutKey = "admin_lockout_{$email}";
        $attemptsKey = "admin_attempts_{$email}";
        
        if (isset($_SESSION[$lockoutKey]) && time() < $_SESSION[$lockoutKey]) {
            throw new \Exception('Account temporarily locked. Try again in 15 minutes.');
        }

        // Get admin user
        $stmt = $db->prepare("
            SELECT a.id, a.name, a.email, a.password_hash, a.is_active, a.last_login_at,
                   r.id as role_id, r.name as role_name
            FROM admin_users a
            JOIN admin_roles r ON a.role_id = r.id
            WHERE a.email = :email
        ");
        $stmt->execute(['email' => $email]);
        $admin = $stmt->fetch(PDO::FETCH_ASSOC);

        if (!$admin) {
            self::recordFailedAttempt($email);
            throw new \Exception('Invalid email or password');
        }

        if (!$admin['is_active']) {
            throw new \Exception('Your admin account has been deactivated');
        }

        if (!password_verify($password, $admin['password_hash'])) {
            self::recordFailedAttempt($email);
            throw new \Exception('Invalid email or password');
        }

        // Clear failed attempts
        unset($_SESSION[$attemptsKey]);
        unset($_SESSION[$lockoutKey]);

        // Update last login and set session
        $db->prepare("
            UPDATE admin_users 
            SET last_login_at = NOW() 
            WHERE id = :id
        ")->execute(['id' => $admin['id']]);

        $_SESSION['admin_id'] = $admin['id'];
        $_SESSION['admin_name'] = $admin['name'];
        $_SESSION['admin_email'] = $admin['email'];
        $_SESSION['admin_role_id'] = $admin['role_id'];
        $_SESSION['admin_role'] = $admin['role_name'];
        $_SESSION['admin_login_time'] = time();
        $_SESSION['admin_ip'] = $_SERVER['REMOTE_ADDR'] ?? '';
        $_SESSION['admin_user_agent'] = $_SERVER['HTTP_USER_AGENT'] ?? '';

        return [
            'id' => $admin['id'],
            'name' => $admin['name'],
            'email' => $admin['email'],
            'role_id' => $admin['role_id'],
            'role' => $admin['role_name']
        ];
    }

    /**
     * Check if admin session is valid
     */
    public static function requireSession(): array {
        self::startSession();

        if (!isset($_SESSION['admin_id'])) {
            http_response_code(401);
            exit(json_encode([
                'success' => false,
                'message' => 'Unauthorized. Please login to admin panel.'
            ]));
        }

        // Check session timeout
        $loginTime = $_SESSION['admin_login_time'] ?? 0;
        if (time() - $loginTime > self::SESSION_TIMEOUT) {
            self::destroySession();
            http_response_code(401);
            exit(json_encode([
                'success' => false,
                'message' => 'Session expired. Please login again.'
            ]));
        }

        // Check IP/User Agent changes (basic security)
        if (($_SESSION['admin_ip'] ?? '') !== ($_SERVER['REMOTE_ADDR'] ?? '')) {
            self::destroySession();
            http_response_code(401);
            exit(json_encode([
                'success' => false,
                'message' => 'Session invalidated due to IP change.'
            ]));
        }

        // Refresh session timeout
        $_SESSION['admin_login_time'] = time();

        return [
            'id' => $_SESSION['admin_id'],
            'name' => $_SESSION['admin_name'],
            'email' => $_SESSION['admin_email'],
            'role_id' => $_SESSION['admin_role_id'],
            'role' => $_SESSION['admin_role']
        ];
    }

    /**
     * Check if admin has specific permission
     */
    public static function hasPermission(array $admin, string $module, string $action = 'read'): bool {
        // Super Admin has all permissions
        if ($admin['role'] === 'Super Admin') {
            return true;
        }

        $db = Database::getConnection();
        $stmt = $db->prepare("
            SELECT * FROM admin_permissions 
            WHERE role_id = :role_id AND module = :module
        ");
        $stmt->execute([
            'role_id' => $admin['role_id'],
            'module' => $module
        ]);
        $perm = $stmt->fetch(PDO::FETCH_ASSOC);

        if (!$perm) {
            return false;
        }

        switch ($action) {
            case 'read':
                return (bool)$perm['can_read'];
            case 'write':
                return (bool)$perm['can_write'];
            case 'delete':
                return (bool)$perm['can_delete'];
            default:
                return false;
        }
    }

    /**
     * Require specific permission
     */
    public static function requirePermission(array $admin, string $module, string $action = 'read'): void {
        if (!self::hasPermission($admin, $module, $action)) {
            http_response_code(403);
            exit(json_encode([
                'success' => false,
                'message' => "You don't have permission to {$action} {$module}."
            ]));
        }
    }

    /**
     * Logout admin
     */
    public static function destroySession(): void {
        self::startSession();
        session_destroy();
        $_SESSION = [];
    }

    /**
     * Record failed login attempt for rate limiting
     */
    private static function recordFailedAttempt(string $email): void {
        self::startSession();
        
        $attemptsKey = "admin_attempts_{$email}";
        $lockoutKey = "admin_lockout_{$email}";
        
        $_SESSION[$attemptsKey] = ($_SESSION[$attemptsKey] ?? 0) + 1;
        
        if ($_SESSION[$attemptsKey] >= self::MAX_LOGIN_ATTEMPTS) {
            $_SESSION[$lockoutKey] = time() + self::LOCKOUT_DURATION;
        }
    }

    /**
     * Log admin action to audit log
     */
    public static function logAction(
        array $admin,
        string $action,
        string $entity,
        int $entityId,
        ?array $previousData = null,
        ?array $newData = null
    ): void {
        try {
            $db = Database::getConnection();
            $stmt = $db->prepare("
                INSERT INTO audit_logs (
                    admin_id, action, entity, entity_id, 
                    previous_data, new_data, ip_address, user_agent, created_at
                ) VALUES (
                    :admin_id, :action, :entity, :entity_id,
                    :prev_data, :new_data, :ip, :agent, NOW()
                )
            ");

            $stmt->execute([
                'admin_id' => $admin['id'],
                'action' => $action,
                'entity' => $entity,
                'entity_id' => $entityId,
                'prev_data' => $previousData ? json_encode($previousData) : null,
                'new_data' => $newData ? json_encode($newData) : null,
                'ip' => $_SERVER['REMOTE_ADDR'] ?? 'unknown',
                'agent' => $_SERVER['HTTP_USER_AGENT'] ?? 'unknown'
            ]);
        } catch (\Exception $e) {
            // Log but don't fail the main operation
            error_log("Failed to log admin action: " . $e->getMessage());
        }
    }
}
