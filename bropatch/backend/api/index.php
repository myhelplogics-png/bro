<?php
declare(strict_types=1);

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../helpers/jwt.php';
require_once __DIR__ . '/../middleware/auth.php';
require_once __DIR__ . '/../controllers/AuthController.php';
require_once __DIR__ . '/../controllers/ServiceController.php';
require_once __DIR__ . '/../controllers/BookingController.php';
require_once __DIR__ . '/../controllers/ProviderController.php';
require_once __DIR__ . '/../controllers/AdminController.php';
require_once __DIR__ . '/../controllers/MiscellaneousControllers.php';

use Bropatch\Helpers\Response;
use Bropatch\Middleware\AuthMiddleware;
use Bropatch\Controllers\AuthController;
use Bropatch\Controllers\CategoryController;
use Bropatch\Controllers\ServiceController;
use Bropatch\Controllers\BookingController;
use Bropatch\Controllers\ProviderController;
use Bropatch\Controllers\AdminController;
use Bropatch\Controllers\CouponController;
use Bropatch\Controllers\BannerController;
use Bropatch\Controllers\ChatController;
use Bropatch\Controllers\ReviewController;

// Handle Preflight CORS
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    header('Access-Control-Allow-Origin: *');
    header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
    header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');
    http_response_code(200);
    exit;
}

$uri = parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH);
$method = $_SERVER['REQUEST_METHOD'];

// Strip base prefix if needed (e.g. /backend/api or /api)
$uri = preg_replace('#^/backend/api#', '', $uri);
$uri = preg_replace('#^/api#', '', $uri);
$uri = rtrim($uri, '/');
if (empty($uri)) {
    $uri = '/';
}

// -------------------------------------------------------------
// PUBLIC ROUTES
// -------------------------------------------------------------

// Health check / API status
if ($uri === '/' || $uri === '/status') {
    Response::success([
        'service' => 'Bropatch PHP REST API',
        'version' => '1.0.0',
        'status' => 'online',
        'database' => 'MySQL 8.0 Connected',
        'timestamp' => date('Y-m-d H:i:s')
    ], 'Bropatch REST API is running');
}

// Auth routes
if ($uri === '/auth/register' && $method === 'POST') {
    AuthController::register();
}

if ($uri === '/auth/login' && $method === 'POST') {
    AuthController::login();
}

// Banners & Public Catalog
if ($uri === '/banners' && $method === 'GET') {
    BannerController::index();
}

if ($uri === '/categories' && $method === 'GET') {
    CategoryController::index();
}

if ($uri === '/services' && $method === 'GET') {
    ServiceController::index();
}

if (preg_match('#^/services/(\d+)$#', $uri, $matches) && $method === 'GET') {
    ServiceController::show((int)$matches[1]);
}

// -------------------------------------------------------------
// AUTHENTICATED USER ROUTES
// -------------------------------------------------------------

// Current User Profile
if ($uri === '/auth/me' && $method === 'GET') {
    $user = AuthMiddleware::authenticate();
    AuthController::me($user);
}

// Coupons
if ($uri === '/coupons/validate' && $method === 'POST') {
    $user = AuthMiddleware::authenticate();
    CouponController::validate($user);
}

// Bookings
if ($uri === '/bookings' && $method === 'POST') {
    $user = AuthMiddleware::authenticate();
    BookingController::create($user);
}

if ($uri === '/bookings' && $method === 'GET') {
    $user = AuthMiddleware::authenticate();
    BookingController::index($user);
}

if (preg_match('#^/bookings/(\d+)/status$#', $uri, $matches) && ($method === 'POST' || $method === 'PUT')) {
    $user = AuthMiddleware::authenticate();
    BookingController::updateStatus((int)$matches[1], $user);
}

// Chat
if (preg_match('#^/chat/(\d+)/messages$#', $uri, $matches)) {
    $user = AuthMiddleware::authenticate();
    $bookingId = (int)$matches[1];
    if ($method === 'GET') {
        ChatController::getMessages($bookingId, $user);
    } elseif ($method === 'POST') {
        ChatController::sendMessage($bookingId, $user);
    }
}

// Reviews
if ($uri === '/reviews' && $method === 'POST') {
    $user = AuthMiddleware::authenticate();
    ReviewController::create($user);
}

// Provider Specific Endpoints
if ($uri === '/provider/profile' && $method === 'GET') {
    $user = AuthMiddleware::requireRole('provider');
    ProviderController::profile($user);
}

if ($uri === '/provider/location' && $method === 'POST') {
    $user = AuthMiddleware::requireRole('provider');
    ProviderController::updateLocation($user);
}

if ($uri === '/provider/online-toggle' && $method === 'POST') {
    $user = AuthMiddleware::requireRole('provider');
    ProviderController::toggleOnline($user);
}

if ($uri === '/provider/payout' && $method === 'POST') {
    $user = AuthMiddleware::requireRole('provider');
    ProviderController::requestPayout($user);
}

// -------------------------------------------------------------
// ADMIN CONSOLE ENDPOINTS
// -------------------------------------------------------------

if ($uri === '/admin/dashboard' && $method === 'GET') {
    $admin = AuthMiddleware::requireAdmin();
    AdminController::dashboardStats($admin);
}

if (preg_match('#^/admin/providers/(\d+)/approve$#', $uri, $matches) && $method === 'POST') {
    $admin = AuthMiddleware::requireAdmin();
    AdminController::approveProvider((int)$matches[1], $admin);
}

if (preg_match('#^/admin/bookings/(\d+)/assign$#', $uri, $matches) && $method === 'POST') {
    $admin = AuthMiddleware::requireAdmin();
    AdminController::assignProvider((int)$matches[1], $admin);
}

if ($uri === '/admin/audit-logs' && $method === 'GET') {
    AuthMiddleware::requireAdmin();
    AdminController::auditLogs();
}

// Fallback Route Not Found
Response::notFound("Endpoint '{$uri}' with method '{$method}' does not exist");
