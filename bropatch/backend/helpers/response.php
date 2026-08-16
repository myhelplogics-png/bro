<?php
declare(strict_types=1);

namespace Bropatch\Helpers;

class Response {
    public static function json(bool $success, mixed $data = null, string $message = '', int $statusCode = 200, array $extra = []): void {
        http_response_code($statusCode);
        header('Content-Type: application/json; charset=utf-8');
        header('Access-Control-Allow-Origin: *');
        header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
        header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

        $payload = [
            'success' => $success,
            'message' => $message,
            'data' => $data
        ];

        if (!empty($extra)) {
            $payload = array_merge($payload, $extra);
        }

        echo json_encode($payload, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        exit;
    }

    public static function success(mixed $data = null, string $message = 'Success', int $statusCode = 200, array $extra = []): void {
        self::json(true, $data, $message, $statusCode, $extra);
    }

    public static function error(string $message = 'An error occurred', int $statusCode = 400, mixed $data = null): void {
        self::json(false, $data, $message, $statusCode);
    }

    public static function unauthorized(string $message = 'Unauthorized access'): void {
        self::json(false, null, $message, 401);
    }

    public static function forbidden(string $message = 'Access forbidden'): void {
        self::json(false, null, $message, 403);
    }

    public static function notFound(string $message = 'Resource not found'): void {
        self::json(false, null, $message, 404);
    }
}
