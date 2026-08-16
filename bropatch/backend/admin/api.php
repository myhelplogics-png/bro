<?php
declare(strict_types=1);

/**
 * Bropatch Admin API Router
 * Base URL: /backend/admin/api
 * Handles all admin panel authentication and operations
 */

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../middleware/AdminAuth.php';
require_once __DIR__ . '/../controllers/AdminAuthController.php';
require_once __DIR__ . '/../controllers/AdminController.php';

use Bropatch\Helpers\Response;
use Bropatch\Middleware\AdminAuth;
use Bropatch\Controllers\AdminAuthController;
use Bropatch\Controllers\AdminController;

// Handle CORS for admin API
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    header('Access-Control-Allow-Origin: *');
    header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
    header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');
    http_response_code(200);
    exit;
}

// Set JSON response header
header('Content-Type: application/json; charset=utf-8');

// Parse request URI
$uri = parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH);
$method = $_SERVER['REQUEST_METHOD'];

// Strip base prefixes
$uri = preg_replace('#^/backend/admin/api#', '', $uri);
$uri = preg_replace('#^/admin/api#', '', $uri);
$uri = rtrim($uri, '/');
if (empty($uri)) {
    $uri = '/';
}

// =====================================================================
// PUBLIC ROUTES (No authentication required)
// =====================================================================

// Health check
if ($uri === '/' || $uri === '/status') {
    Response::success([
        'service' => 'Bropatch Admin API',
        'version' => '1.0.0',
        'status' => 'online',
        'database' => 'MySQL 8.0 Connected',
        'timestamp' => date('Y-m-d H:i:s')
    ], 'Bropatch Admin API is running');
    exit;
}

// Admin Login
if ($uri === '/login' && $method === 'POST') {
    AdminAuthController::login();
    exit;
}

// =====================================================================
// PROTECTED ROUTES (Authentication required)
// =====================================================================

// Admin Logout
if ($uri === '/logout' && $method === 'POST') {
    AdminAuthController::logout();
    exit;
}

// Get Current Admin Session
if ($uri === '/me' && $method === 'GET') {
    AdminAuthController::me();
    exit;
}

// Get Admin Permissions
if ($uri === '/permissions' && $method === 'GET') {
    AdminAuthController::getPermissions();
    exit;
}

// Dashboard Stats
if ($uri === '/dashboard' && $method === 'GET') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'dashboard', 'read');
    AdminController::dashboardStats($admin);
    exit;
}

// Provider Management
if (preg_match('#^/providers$#', $uri) && $method === 'GET') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'providers', 'read');
    AdminController::listProviders($admin);
    exit;
}

if (preg_match('#^/providers/(\d+)$#', $uri, $matches) && $method === 'GET') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'providers', 'read');
    AdminController::getProvider((int)$matches[1], $admin);
    exit;
}

if (preg_match('#^/providers/(\d+)/approve$#', $uri, $matches) && $method === 'POST') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'providers', 'write');
    AdminController::approveProvider((int)$matches[1], $admin);
    exit;
}

if (preg_match('#^/providers/(\d+)/reject$#', $uri, $matches) && $method === 'POST') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'providers', 'write');
    AdminController::rejectProvider((int)$matches[1], $admin);
    exit;
}

if (preg_match('#^/providers/(\d+)/suspend$#', $uri, $matches) && $method === 'POST') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'providers', 'write');
    AdminController::suspendProvider((int)$matches[1], $admin);
    exit;
}

if (preg_match('#^/providers/(\d+)/activate$#', $uri, $matches) && $method === 'POST') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'providers', 'write');
    AdminController::activateProvider((int)$matches[1], $admin);
    exit;
}

// Booking Management
if (preg_match('#^/bookings$#', $uri) && $method === 'GET') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'bookings', 'read');
    AdminController::listBookings($admin);
    exit;
}

if (preg_match('#^/bookings/(\d+)$#', $uri, $matches) && $method === 'GET') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'bookings', 'read');
    AdminController::getBooking((int)$matches[1], $admin);
    exit;
}

if (preg_match('#^/bookings/(\d+)/assign$#', $uri, $matches) && $method === 'POST') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'bookings', 'write');
    AdminController::assignProvider((int)$matches[1], $admin);
    exit;
}

if (preg_match('#^/bookings/(\d+)/cancel$#', $uri, $matches) && $method === 'POST') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'bookings', 'write');
    AdminController::cancelBooking((int)$matches[1], $admin);
    exit;
}

// Service Management
if (preg_match('#^/services$#', $uri) && $method === 'GET') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'services', 'read');
    AdminController::listServices($admin);
    exit;
}

if (preg_match('#^/services/(\d+)$#', $uri, $matches) && $method === 'GET') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'services', 'read');
    AdminController::getService((int)$matches[1], $admin);
    exit;
}

if (preg_match('#^/services$#', $uri) && $method === 'POST') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'services', 'write');
    AdminController::createService($admin);
    exit;
}

if (preg_match('#^/services/(\d+)$#', $uri, $matches) && $method === 'PUT') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'services', 'write');
    AdminController::updateService((int)$matches[1], $admin);
    exit;
}

// Category Management
if (preg_match('#^/categories$#', $uri) && $method === 'GET') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'categories', 'read');
    AdminController::listCategories($admin);
    exit;
}

if (preg_match('#^/categories$#', $uri) && $method === 'POST') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'categories', 'write');
    AdminController::createCategory($admin);
    exit;
}

if (preg_match('#^/categories/(\d+)$#', $uri, $matches) && $method === 'PUT') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'categories', 'write');
    AdminController::updateCategory((int)$matches[1], $admin);
    exit;
}

// Banner Management
if (preg_match('#^/banners$#', $uri) && $method === 'GET') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'banners', 'read');
    AdminController::listBanners($admin);
    exit;
}

if (preg_match('#^/banners$#', $uri) && $method === 'POST') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'banners', 'write');
    AdminController::createBanner($admin);
    exit;
}

if (preg_match('#^/banners/(\d+)$#', $uri, $matches) && $method === 'PUT') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'banners', 'write');
    AdminController::updateBanner((int)$matches[1], $admin);
    exit;
}

if (preg_match('#^/banners/(\d+)$#', $uri, $matches) && $method === 'DELETE') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'banners', 'delete');
    AdminController::deleteBanner((int)$matches[1], $admin);
    exit;
}

// Coupon Management
if (preg_match('#^/coupons$#', $uri) && $method === 'GET') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'coupons', 'read');
    AdminController::listCoupons($admin);
    exit;
}

if (preg_match('#^/coupons$#', $uri) && $method === 'POST') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'coupons', 'write');
    AdminController::createCoupon($admin);
    exit;
}

if (preg_match('#^/coupons/(\d+)$#', $uri, $matches) && $method === 'PUT') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'coupons', 'write');
    AdminController::updateCoupon((int)$matches[1], $admin);
    exit;
}

// Payment Management
if (preg_match('#^/payments$#', $uri) && $method === 'GET') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'payments', 'read');
    AdminController::listPayments($admin);
    exit;
}

if (preg_match('#^/payments/(\d+)$#', $uri, $matches) && $method === 'GET') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'payments', 'read');
    AdminController::getPayment((int)$matches[1], $admin);
    exit;
}

// Payout Management
if (preg_match('#^/payouts$#', $uri) && $method === 'GET') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'payouts', 'read');
    AdminController::listPayouts($admin);
    exit;
}

if (preg_match('#^/payouts/(\d+)$#', $uri, $matches) && $method === 'GET') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'payouts', 'read');
    AdminController::getPayout((int)$matches[1], $admin);
    exit;
}

if (preg_match('#^/payouts/(\d+)/process$#', $uri, $matches) && $method === 'POST') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'payouts', 'write');
    AdminController::processPayout((int)$matches[1], $admin);
    exit;
}

// User Management
if (preg_match('#^/users$#', $uri) && $method === 'GET') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'users', 'read');
    AdminController::listUsers($admin);
    exit;
}

if (preg_match('#^/users/(\d+)$#', $uri, $matches) && $method === 'GET') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'users', 'read');
    AdminController::getUser((int)$matches[1], $admin);
    exit;
}

if (preg_match('#^/users/(\d+)/suspend$#', $uri, $matches) && $method === 'POST') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'users', 'write');
    AdminController::suspendUser((int)$matches[1], $admin);
    exit;
}

if (preg_match('#^/users/(\d+)/activate$#', $uri, $matches) && $method === 'POST') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'users', 'write');
    AdminController::activateUser((int)$matches[1], $admin);
    exit;
}

// Review Management
if (preg_match('#^/reviews$#', $uri) && $method === 'GET') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'reviews', 'read');
    AdminController::listReviews($admin);
    exit;
}

if (preg_match('#^/reviews/(\d+)/hide$#', $uri, $matches) && $method === 'POST') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'reviews', 'write');
    AdminController::hideReview((int)$matches[1], $admin);
    exit;
}

if (preg_match('#^/reviews/(\d+)/restore$#', $uri, $matches) && $method === 'POST') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'reviews', 'write');
    AdminController::restoreReview((int)$matches[1], $admin);
    exit;
}

// Dispute Management
if (preg_match('#^/disputes$#', $uri) && $method === 'GET') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'disputes', 'read');
    AdminController::listDisputes($admin);
    exit;
}

if (preg_match('#^/disputes/(\d+)$#', $uri, $matches) && $method === 'GET') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'disputes', 'read');
    AdminController::getDispute((int)$matches[1], $admin);
    exit;
}

if (preg_match('#^/disputes/(\d+)/resolve$#', $uri, $matches) && $method === 'POST') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'disputes', 'write');
    AdminController::resolveDispute((int)$matches[1], $admin);
    exit;
}

// Audit Logs
if (preg_match('#^/audit-logs$#', $uri) && $method === 'GET') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'audit_logs', 'read');
    AdminController::auditLogs($admin);
    exit;
}

// Reports
if (preg_match('#^/reports/dashboard$#', $uri) && $method === 'GET') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'reports', 'read');
    AdminController::reportDashboard($admin);
    exit;
}

if (preg_match('#^/reports/bookings$#', $uri) && $method === 'GET') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'reports', 'read');
    AdminController::reportBookings($admin);
    exit;
}

if (preg_match('#^/reports/revenue$#', $uri) && $method === 'GET') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'reports', 'read');
    AdminController::reportRevenue($admin);
    exit;
}

// Settings
if (preg_match('#^/settings$#', $uri) && $method === 'GET') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'settings', 'read');
    AdminController::getSettings($admin);
    exit;
}

if (preg_match('#^/settings$#', $uri) && $method === 'POST') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'settings', 'write');
    AdminController::updateSettings($admin);
    exit;
}

// Notifications
if (preg_match('#^/notifications/send$#', $uri) && $method === 'POST') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'notifications', 'write');
    AdminController::sendNotification($admin);
    exit;
}

// Admin Management (Super Admin only)
if (preg_match('#^/admins$#', $uri) && $method === 'GET') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'admins', 'read');
    AdminController::listAdmins($admin);
    exit;
}

if (preg_match('#^/admins$#', $uri) && $method === 'POST') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'admins', 'write');
    AdminController::createAdmin($admin);
    exit;
}

if (preg_match('#^/admins/(\d+)$#', $uri, $matches) && $method === 'PUT') {
    $admin = AdminAuth::requireSession();
    AdminAuth::requirePermission($admin, 'admins', 'write');
    AdminController::updateAdmin((int)$matches[1], $admin);
    exit;
}

// 404 - Endpoint not found
Response::notFound("Admin endpoint '{$uri}' with method '{$method}' does not exist");
