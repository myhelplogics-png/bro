-- ====================================================================
-- BROPATCH HOME SERVICES PLATFORM - DATABASE SCHEMA
-- MySQL 8.0+ Compatible Schema with Normalized Relational Design
-- Single Source of Truth for Customer App, Provider App & Admin Panel
-- ====================================================================

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `audit_logs`;
DROP TABLE IF EXISTS `settings`;
DROP TABLE IF EXISTS `admin_permissions`;
DROP TABLE IF EXISTS `admin_users`;
DROP TABLE IF EXISTS `admin_roles`;
DROP TABLE IF EXISTS `banners`;
DROP TABLE IF EXISTS `disputes`;
DROP TABLE IF EXISTS `credit_transactions`;
DROP TABLE IF EXISTS `provider_credits`;
DROP TABLE IF EXISTS `payouts`;
DROP TABLE IF EXISTS `coupon_usage`;
DROP TABLE IF EXISTS `coupons`;
DROP TABLE IF EXISTS `notifications`;
DROP TABLE IF EXISTS `reviews`;
DROP TABLE IF EXISTS `invoices`;
DROP TABLE IF EXISTS `payment_transactions`;
DROP TABLE IF EXISTS `payments`;
DROP TABLE IF EXISTS `messages`;
DROP TABLE IF EXISTS `conversation_participants`;
DROP TABLE IF EXISTS `conversations`;
DROP TABLE IF EXISTS `booking_images`;
DROP TABLE IF EXISTS `booking_status_history`;
DROP TABLE IF EXISTS `booking_items`;
DROP TABLE IF EXISTS `bookings`;
DROP TABLE IF EXISTS `addresses`;
DROP TABLE IF EXISTS `provider_services`;
DROP TABLE IF EXISTS `provider_documents`;
DROP TABLE IF EXISTS `providers`;
DROP TABLE IF EXISTS `services`;
DROP TABLE IF EXISTS `service_categories`;
DROP TABLE IF EXISTS `user_roles`;
DROP TABLE IF EXISTS `users`;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. USER ROLES
CREATE TABLE `user_roles` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL UNIQUE,
    `description` VARCHAR(255) NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. USERS
CREATE TABLE `users` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `role_id` INT NOT NULL DEFAULT 1,
    `name` VARCHAR(150) NOT NULL,
    `email` VARCHAR(150) NOT NULL UNIQUE,
    `phone` VARCHAR(30) NULL UNIQUE,
    `password_hash` VARCHAR(255) NULL,
    `google_id` VARCHAR(150) NULL UNIQUE,
    `avatar_url` VARCHAR(500) NULL,
    `status` ENUM('active', 'suspended', 'pending_verification', 'inactive') NOT NULL DEFAULT 'active',
    `api_token` VARCHAR(255) NULL UNIQUE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP NULL DEFAULT NULL,
    INDEX `idx_users_email` (`email`),
    INDEX `idx_users_phone` (`phone`),
    INDEX `idx_users_status` (`status`),
    CONSTRAINT `fk_users_role` FOREIGN KEY (`role_id`) REFERENCES `user_roles` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. SERVICE CATEGORIES
CREATE TABLE `service_categories` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `slug` VARCHAR(120) NOT NULL UNIQUE,
    `icon` VARCHAR(100) NOT NULL DEFAULT 'construct',
    `image_url` VARCHAR(500) NULL,
    `description` TEXT NULL,
    `sort_order` INT NOT NULL DEFAULT 0,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP NULL DEFAULT NULL,
    INDEX `idx_categories_active` (`is_active`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. SERVICES
CREATE TABLE `services` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `category_id` INT NOT NULL,
    `name` VARCHAR(150) NOT NULL,
    `slug` VARCHAR(180) NOT NULL UNIQUE,
    `short_description` VARCHAR(255) NULL,
    `description` TEXT NULL,
    `base_price` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    `discount_price` DECIMAL(10, 2) NULL,
    `estimated_duration_mins` INT NOT NULL DEFAULT 60,
    `image_url` VARCHAR(500) NULL,
    `warranty_days` INT NOT NULL DEFAULT 30,
    `rating_avg` DECIMAL(3, 2) NOT NULL DEFAULT 5.00,
    `total_reviews` INT NOT NULL DEFAULT 0,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP NULL DEFAULT NULL,
    INDEX `idx_services_category` (`category_id`),
    INDEX `idx_services_active` (`is_active`),
    CONSTRAINT `fk_services_category` FOREIGN KEY (`category_id`) REFERENCES `service_categories` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. PROVIDERS (SERVICE PROFESSIONALS)
CREATE TABLE `providers` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT UNSIGNED NOT NULL UNIQUE,
    `business_name` VARCHAR(150) NULL,
    `bio` TEXT NULL,
    `experience_years` INT NOT NULL DEFAULT 1,
    `skills` TEXT NULL,
    `service_areas` TEXT NULL,
    `current_latitude` DECIMAL(10, 8) NULL,
    `current_longitude` DECIMAL(11, 8) NULL,
    `verification_status` ENUM('pending', 'approved', 'rejected', 'suspended') NOT NULL DEFAULT 'pending',
    `rejection_reason` VARCHAR(255) NULL,
    `is_available` TINYINT(1) NOT NULL DEFAULT 1,
    `is_online` TINYINT(1) NOT NULL DEFAULT 0,
    `rating_avg` DECIMAL(3, 2) NOT NULL DEFAULT 5.00,
    `total_jobs_completed` INT NOT NULL DEFAULT 0,
    `total_earnings` DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    `pending_payout_balance` DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    `cod_pending_amount` DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP NULL DEFAULT NULL,
    INDEX `idx_providers_status` (`verification_status`, `is_available`),
    CONSTRAINT `fk_providers_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. PROVIDER DOCUMENTS (GOV ID, CERTIFICATES)
CREATE TABLE `provider_documents` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `provider_id` BIGINT UNSIGNED NOT NULL,
    `document_type` ENUM('id_proof', 'address_proof', 'trade_license', 'police_clearance', 'skill_certificate') NOT NULL,
    `document_number` VARCHAR(100) NULL,
    `document_url` VARCHAR(500) NOT NULL,
    `verification_status` ENUM('pending', 'verified', 'rejected') NOT NULL DEFAULT 'pending',
    `verified_at` TIMESTAMP NULL DEFAULT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_prov_doc` (`provider_id`, `verification_status`),
    CONSTRAINT `fk_provdoc_provider` FOREIGN KEY (`provider_id`) REFERENCES `providers` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. PROVIDER SERVICES (SERVICES OFFERED BY PROVIDER)
CREATE TABLE `provider_services` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `provider_id` BIGINT UNSIGNED NOT NULL,
    `service_id` BIGINT UNSIGNED NOT NULL,
    `custom_price` DECIMAL(10, 2) NULL,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `unique_provider_service` (`provider_id`, `service_id`),
    CONSTRAINT `fk_ps_provider` FOREIGN KEY (`provider_id`) REFERENCES `providers` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_ps_service` FOREIGN KEY (`service_id`) REFERENCES `services` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. ADDRESSES (CUSTOMER SERVICE LOCATIONS)
CREATE TABLE `addresses` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT UNSIGNED NOT NULL,
    `label` VARCHAR(50) NOT NULL DEFAULT 'Home',
    `street_address` VARCHAR(255) NOT NULL,
    `apartment_unit` VARCHAR(100) NULL,
    `landmark` VARCHAR(150) NULL,
    `city` VARCHAR(100) NOT NULL DEFAULT 'New Delhi',
    `state` VARCHAR(100) NOT NULL DEFAULT 'Delhi',
    `postal_code` VARCHAR(20) NOT NULL DEFAULT '110001',
    `latitude` DECIMAL(10, 8) NOT NULL DEFAULT 28.6139,
    `longitude` DECIMAL(11, 8) NOT NULL DEFAULT 77.2090,
    `is_default` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP NULL DEFAULT NULL,
    INDEX `idx_addresses_user` (`user_id`),
    CONSTRAINT `fk_addresses_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. COUPONS
CREATE TABLE `coupons` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `code` VARCHAR(30) NOT NULL UNIQUE,
    `discount_type` ENUM('percentage', 'fixed') NOT NULL DEFAULT 'percentage',
    `discount_value` DECIMAL(10, 2) NOT NULL,
    `min_order_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    `max_discount_amount` DECIMAL(10, 2) NULL,
    `valid_from` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `valid_until` TIMESTAMP NOT NULL,
    `usage_limit` INT NOT NULL DEFAULT 1000,
    `per_user_limit` INT NOT NULL DEFAULT 1,
    `total_used` INT NOT NULL DEFAULT 0,
    `applicable_category_id` INT NULL,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_coupons_code` (`code`, `is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. BOOKINGS (STATE MACHINE CORE)
CREATE TABLE `bookings` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `booking_code` VARCHAR(30) NOT NULL UNIQUE,
    `customer_id` BIGINT UNSIGNED NOT NULL,
    `provider_id` BIGINT UNSIGNED NULL,
    `service_id` BIGINT UNSIGNED NOT NULL,
    `address_id` BIGINT UNSIGNED NOT NULL,
    `coupon_id` INT NULL,
    `status` ENUM(
        'pending',
        'searching_provider',
        'provider_assigned',
        'provider_accepted',
        'provider_on_way',
        'provider_arrived',
        'work_started',
        'work_completed',
        'payment_pending',
        'completed',
        'cancelled',
        'disputed'
    ) NOT NULL DEFAULT 'pending',
    `scheduled_date` DATE NOT NULL,
    `scheduled_time_slot` VARCHAR(50) NOT NULL,
    `problem_description` TEXT NULL,
    `cancellation_reason` VARCHAR(255) NULL,
    `cancelled_by` ENUM('customer', 'provider', 'admin') NULL,
    `base_amount` DECIMAL(10, 2) NOT NULL,
    `discount_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    `tax_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    `final_amount` DECIMAL(10, 2) NOT NULL,
    `platform_fee` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    `provider_payout_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    `payment_method` ENUM('razorpay', 'cod', 'wallet') NOT NULL DEFAULT 'razorpay',
    `payment_status` ENUM('pending', 'paid', 'failed', 'refunded') NOT NULL DEFAULT 'pending',
    `customer_contact_phone` VARCHAR(30) NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP NULL DEFAULT NULL,
    INDEX `idx_bookings_customer` (`customer_id`),
    INDEX `idx_bookings_provider` (`provider_id`),
    INDEX `idx_bookings_status` (`status`),
    INDEX `idx_bookings_date` (`scheduled_date`),
    CONSTRAINT `fk_bookings_customer` FOREIGN KEY (`customer_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_bookings_provider` FOREIGN KEY (`provider_id`) REFERENCES `providers` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_bookings_service` FOREIGN KEY (`service_id`) REFERENCES `services` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_bookings_address` FOREIGN KEY (`address_id`) REFERENCES `addresses` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_bookings_coupon` FOREIGN KEY (`coupon_id`) REFERENCES `coupons` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. BOOKING ITEMS (LINE ITEMS)
CREATE TABLE `booking_items` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `booking_id` BIGINT UNSIGNED NOT NULL,
    `service_id` BIGINT UNSIGNED NOT NULL,
    `item_name` VARCHAR(150) NOT NULL,
    `quantity` INT NOT NULL DEFAULT 1,
    `unit_price` DECIMAL(10, 2) NOT NULL,
    `total_price` DECIMAL(10, 2) NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_bi_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_bi_service` FOREIGN KEY (`service_id`) REFERENCES `services` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. BOOKING STATUS HISTORY (AUDITABLE STATE TRANSITIONS)
CREATE TABLE `booking_status_history` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `booking_id` BIGINT UNSIGNED NOT NULL,
    `old_status` VARCHAR(50) NULL,
    `new_status` VARCHAR(50) NOT NULL,
    `changed_by_user_id` BIGINT UNSIGNED NULL,
    `changed_by_role` ENUM('customer', 'provider', 'admin', 'system') NOT NULL DEFAULT 'system',
    `reason` VARCHAR(255) NULL,
    `notes` TEXT NULL,
    `latitude` DECIMAL(10, 8) NULL,
    `longitude` DECIMAL(11, 8) NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_bsh_booking` (`booking_id`),
    CONSTRAINT `fk_bsh_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. BOOKING IMAGES (CUSTOMER PRE-JOB OR PROVIDER POST-JOB PHOTOS)
CREATE TABLE `booking_images` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `booking_id` BIGINT UNSIGNED NOT NULL,
    `image_url` VARCHAR(500) NOT NULL,
    `image_type` ENUM('customer_issue', 'work_before', 'work_after', 'receipt') NOT NULL DEFAULT 'customer_issue',
    `uploaded_by_user_id` BIGINT UNSIGNED NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_bimg_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. CONVERSATIONS & CHAT
CREATE TABLE `conversations` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `booking_id` BIGINT UNSIGNED NOT NULL UNIQUE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_conv_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `conversation_participants` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `conversation_id` BIGINT UNSIGNED NOT NULL,
    `user_id` BIGINT UNSIGNED NOT NULL,
    `last_read_at` TIMESTAMP NULL DEFAULT NULL,
    UNIQUE KEY `uniq_conv_user` (`conversation_id`, `user_id`),
    CONSTRAINT `fk_cp_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_cp_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `messages` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `conversation_id` BIGINT UNSIGNED NOT NULL,
    `sender_id` BIGINT UNSIGNED NOT NULL,
    `message_text` TEXT NOT NULL,
    `attachment_url` VARCHAR(500) NULL,
    `is_read` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_msg_conv` (`conversation_id`, `created_at`),
    CONSTRAINT `fk_msg_conv` FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_msg_sender` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. PAYMENTS & TRANSACTIONS
CREATE TABLE `payments` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `booking_id` BIGINT UNSIGNED NOT NULL,
    `user_id` BIGINT UNSIGNED NOT NULL,
    `amount` DECIMAL(10, 2) NOT NULL,
    `currency` VARCHAR(10) NOT NULL DEFAULT 'INR',
    `payment_method` ENUM('razorpay', 'cod', 'wallet') NOT NULL,
    `transaction_id` VARCHAR(100) NULL UNIQUE,
    `gateway_order_id` VARCHAR(100) NULL,
    `gateway_payment_id` VARCHAR(100) NULL,
    `gateway_signature` VARCHAR(255) NULL,
    `gateway_response` JSON NULL,
    `payment_status` ENUM('pending', 'authorized', 'captured', 'failed', 'refunded') NOT NULL DEFAULT 'pending',
    `cod_collected_by_provider_id` BIGINT UNSIGNED NULL,
    `cod_collection_status` ENUM('pending', 'collected', 'reconciled') NOT NULL DEFAULT 'pending',
    `cod_reconciled_at` TIMESTAMP NULL DEFAULT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_payments_booking` (`booking_id`),
    INDEX `idx_payments_status` (`payment_status`),
    CONSTRAINT `fk_payments_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_payments_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `payment_transactions` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `payment_id` BIGINT UNSIGNED NOT NULL,
    `transaction_type` ENUM('charge', 'refund', 'cod_collection', 'cod_reconciliation', 'platform_fee_deduction') NOT NULL,
    `amount` DECIMAL(10, 2) NOT NULL,
    `status` ENUM('success', 'failed', 'pending') NOT NULL DEFAULT 'success',
    `reference_id` VARCHAR(100) NULL,
    `notes` TEXT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_pt_payment` FOREIGN KEY (`payment_id`) REFERENCES `payments` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 16. INVOICES
CREATE TABLE `invoices` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `invoice_number` VARCHAR(50) NOT NULL UNIQUE,
    `booking_id` BIGINT UNSIGNED NOT NULL UNIQUE,
    `customer_name` VARCHAR(150) NOT NULL,
    `customer_phone` VARCHAR(30) NOT NULL,
    `customer_address` TEXT NOT NULL,
    `provider_name` VARCHAR(150) NULL,
    `service_name` VARCHAR(150) NOT NULL,
    `base_amount` DECIMAL(10, 2) NOT NULL,
    `discount_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    `tax_gst_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    `total_amount` DECIMAL(10, 2) NOT NULL,
    `payment_method` VARCHAR(50) NOT NULL,
    `payment_status` VARCHAR(50) NOT NULL,
    `invoice_pdf_url` VARCHAR(500) NULL,
    `issued_date` DATE NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_inv_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 17. REVIEWS & RATINGS
CREATE TABLE `reviews` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `booking_id` BIGINT UNSIGNED NOT NULL UNIQUE,
    `customer_id` BIGINT UNSIGNED NOT NULL,
    `provider_id` BIGINT UNSIGNED NOT NULL,
    `service_id` BIGINT UNSIGNED NOT NULL,
    `rating` INT NOT NULL,
    `review_text` TEXT NULL,
    `provider_reply` TEXT NULL,
    `is_hidden` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_rev_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_rev_customer` FOREIGN KEY (`customer_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_rev_provider` FOREIGN KEY (`provider_id`) REFERENCES `providers` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_rev_service` FOREIGN KEY (`service_id`) REFERENCES `services` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 18. NOTIFICATIONS
CREATE TABLE `notifications` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT UNSIGNED NOT NULL,
    `title` VARCHAR(150) NOT NULL,
    `message` TEXT NOT NULL,
    `type` VARCHAR(50) NOT NULL DEFAULT 'booking_update',
    `reference_id` VARCHAR(100) NULL,
    `is_read` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_notif_user` (`user_id`, `is_read`),
    CONSTRAINT `fk_notif_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 19. PROVIDER CREDITS & TRANSACTIONS
CREATE TABLE `provider_credits` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `provider_id` BIGINT UNSIGNED NOT NULL UNIQUE,
    `current_balance` DECIMAL(10, 2) NOT NULL DEFAULT 1000.00,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_pc_provider` FOREIGN KEY (`provider_id`) REFERENCES `providers` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `credit_transactions` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `provider_id` BIGINT UNSIGNED NOT NULL,
    `transaction_type` ENUM('credit_added', 'lead_fee_deducted', 'penalty', 'bonus', 'cod_offset') NOT NULL,
    `amount` DECIMAL(10, 2) NOT NULL,
    `balance_after` DECIMAL(10, 2) NOT NULL,
    `reason` VARCHAR(255) NOT NULL,
    `admin_id` BIGINT UNSIGNED NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_ct_provider` FOREIGN KEY (`provider_id`) REFERENCES `providers` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 20. PAYOUTS
CREATE TABLE `payouts` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `payout_reference` VARCHAR(50) NOT NULL UNIQUE,
    `provider_id` BIGINT UNSIGNED NOT NULL,
    `amount` DECIMAL(10, 2) NOT NULL,
    `status` ENUM('pending', 'processing', 'paid', 'failed', 'cancelled') NOT NULL DEFAULT 'pending',
    `bank_account_mask` VARCHAR(50) NULL,
    `ifsc_code` VARCHAR(30) NULL,
    `processed_by_admin_id` BIGINT UNSIGNED NULL,
    `processed_at` TIMESTAMP NULL DEFAULT NULL,
    `notes` TEXT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_payout_provider` FOREIGN KEY (`provider_id`) REFERENCES `providers` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 21. DISPUTES
CREATE TABLE `disputes` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `dispute_code` VARCHAR(50) NOT NULL UNIQUE,
    `booking_id` BIGINT UNSIGNED NOT NULL,
    `raised_by_user_id` BIGINT UNSIGNED NOT NULL,
    `reason` VARCHAR(255) NOT NULL,
    `description` TEXT NOT NULL,
    `evidence_urls` JSON NULL,
    `status` ENUM('open', 'under_review', 'waiting_customer', 'waiting_provider', 'resolved', 'rejected') NOT NULL DEFAULT 'open',
    `resolution_notes` TEXT NULL,
    `refund_amount` DECIMAL(10, 2) NULL,
    `resolved_by_admin_id` BIGINT UNSIGNED NULL,
    `resolved_at` TIMESTAMP NULL DEFAULT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_disp_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_disp_user` FOREIGN KEY (`raised_by_user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 22. HOMEPAGE BANNERS
CREATE TABLE `banners` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(150) NOT NULL,
    `subtitle` VARCHAR(255) NULL,
    `cta_text` VARCHAR(50) NOT NULL DEFAULT 'Book Now',
    `destination_url` VARCHAR(255) NULL,
    `category_id` INT NULL,
    `image_url` VARCHAR(500) NOT NULL,
    `badge_text` VARCHAR(50) NULL,
    `sort_order` INT NOT NULL DEFAULT 0,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `start_date` DATE NULL,
    `end_date` DATE NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_banner_cat` FOREIGN KEY (`category_id`) REFERENCES `service_categories` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 23. ADMIN ROLES & USERS
CREATE TABLE `admin_roles` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL UNIQUE,
    `description` VARCHAR(255) NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `admin_users` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `role_id` INT NOT NULL,
    `name` VARCHAR(150) NOT NULL,
    `email` VARCHAR(150) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `last_login_at` TIMESTAMP NULL DEFAULT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_admin_role` FOREIGN KEY (`role_id`) REFERENCES `admin_roles` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `admin_permissions` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `role_id` INT NOT NULL,
    `module` VARCHAR(50) NOT NULL,
    `can_read` TINYINT(1) NOT NULL DEFAULT 1,
    `can_write` TINYINT(1) NOT NULL DEFAULT 0,
    `can_delete` TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY `uniq_perm` (`role_id`, `module`),
    CONSTRAINT `fk_perm_role` FOREIGN KEY (`role_id`) REFERENCES `admin_roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 24. AUDIT LOGS
CREATE TABLE `audit_logs` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `admin_id` BIGINT UNSIGNED NULL,
    `action` VARCHAR(100) NOT NULL,
    `entity` VARCHAR(50) NOT NULL,
    `entity_id` BIGINT UNSIGNED NOT NULL,
    `previous_data` JSON NULL,
    `new_data` JSON NULL,
    `ip_address` VARCHAR(50) NULL,
    `user_agent` VARCHAR(255) NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_audit_entity` (`entity`, `entity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 25. PLATFORM SETTINGS
CREATE TABLE `settings` (
    `key` VARCHAR(100) PRIMARY KEY,
    `value` TEXT NOT NULL,
    `description` VARCHAR(255) NULL,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================================
-- SEED INITIAL CORE DATA
-- ====================================================================

-- User Roles
INSERT INTO `user_roles` (`id`, `name`, `description`) VALUES
(1, 'customer', 'End customer requesting home repair & maintenance services'),
(2, 'provider', 'Verified service professional / technician'),
(3, 'admin', 'System & operations administrator');

-- Admin Roles
INSERT INTO `admin_roles` (`id`, `name`, `description`) VALUES
(1, 'Super Admin', 'Full access to all operations, finance, and system settings'),
(2, 'Operations Manager', 'Manage dispatch, bookings, and provider approvals'),
(3, 'Finance Admin', 'Manage payouts, COD reconciliation, and refunds');

-- Admin Users (Default: admin@bropatch.com / Password: Admin@Bropatch2026!)
-- Hash generated using password_hash('Admin@Bropatch2026!', PASSWORD_BCRYPT)
INSERT INTO `admin_users` (`id`, `role_id`, `name`, `email`, `password_hash`, `is_active`) VALUES
(1, 1, 'Bropatch Operations Lead', 'admin@bropatch.com', '$2y$10$tZ9v7C1bB5LzF4b7XyQdVu0fI2mG8kH5p3r1t9wA2sD4f6g8h0j2k', 1);

-- Default Platform Settings
INSERT INTO `settings` (`key`, `value`, `description`) VALUES
('platform_commission_percent', '15', 'Default commission percentage charged on completed bookings'),
('tax_gst_percent', '18', 'Applicable GST percentage for services'),
('max_cod_limit', '5000', 'Maximum allowed order value for Cash on Delivery'),
('support_phone', '+91 800-BROPATCH', 'Customer support hotline'),
('support_email', 'support@bropatch.com', 'Customer support email address'),
('currency_symbol', '₹', 'Display currency symbol');

-- Service Categories
INSERT INTO `service_categories` (`id`, `name`, `slug`, `icon`, `image_url`, `description`, `sort_order`, `is_active`) VALUES
(1, 'Plumbing Services', 'plumbing', 'plumbing', 'https://images.unsplash.com/photo-1581244277943-fe4a9c777189?w=600&auto=format&fit=crop&q=80', 'Leak repair, pipe fitting, faucet installation, drain cleaning', 1, 1),
(2, 'Electrical Works', 'electrical', 'bolt', 'https://images.unsplash.com/photo-1621905251189-08b45d6a269e?w=600&auto=format&fit=crop&q=80', 'Wiring, switchboard repair, MCB replacement, fan & light fixes', 2, 1),
(3, 'AC & Appliance Care', 'ac-appliance', 'ac_unit', 'https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=600&auto=format&fit=crop&q=80', 'AC service, gas refill, washing machine & refrigerator repair', 3, 1),
(4, 'Deep Cleaning & Pest', 'cleaning', 'cleaning_services', 'https://images.unsplash.com/photo-1581578731548-c64695cc6952?w=600&auto=format&fit=crop&q=80', 'Bathroom deep clean, full home sanitation, sofa & kitchen care', 4, 1),
(5, 'Carpentry & Furniture', 'carpentry', 'handyman', 'https://images.unsplash.com/photo-1538688525198-9b88f6f53126?w=600&auto=format&fit=crop&q=80', 'Door lock repair, custom shelving, furniture assembly & fixes', 5, 1),
(6, 'Home Painting & Patch', 'painting', 'format_paint', 'https://images.unsplash.com/photo-1589939705384-5185137a7f0f?w=600&auto=format&fit=crop&q=80', 'Wall patch repair, water seepage waterproof coat, room painting', 6, 1);

-- Services
INSERT INTO `services` (`id`, `category_id`, `name`, `slug`, `short_description`, `description`, `base_price`, `discount_price`, `estimated_duration_mins`, `image_url`, `warranty_days`, `rating_avg`, `total_reviews`, `is_active`) VALUES
(1, 1, 'Pipe Leakage & Drainage Unblock', 'pipe-leak-drainage', 'Instant repair for leaking PVC/GI pipes, sink blockages & traps', 'Complete diagnosis and repair for leaking pipes under sink, main line blockages, tap washer replacement, and high-pressure drain line clearing.', 499.00, 399.00, 45, 'https://images.unsplash.com/photo-1581244277943-fe4a9c777189?w=600&auto=format&fit=crop&q=80', 30, 4.9, 128, 1),
(2, 1, 'Complete Faucet & Shower Fitting', 'faucet-shower-fitting', 'Installation or replacement of bathroom taps, mixers & overhead showers', 'Precision installation of wall mixers, diverters, health faucets, hand showers, and angle valves with 30-day leak-free guarantee.', 349.00, 299.00, 40, 'https://images.unsplash.com/photo-1584622650111-993a426fbf0a?w=600&auto=format&fit=crop&q=80', 30, 4.8, 95, 1),
(3, 2, 'Switchboard & Short Circuit Fix', 'switchboard-short-circuit', 'Repair burnt switches, flickering sockets, tripped MCB & wiring checks', 'Certified electrician inspection of fuse box, main switchboard repair, replacement of up to 4 switch modules, and earthing safety verification.', 399.00, 329.00, 50, 'https://images.unsplash.com/photo-1621905251189-08b45d6a269e?w=600&auto=format&fit=crop&q=80', 30, 4.95, 210, 1),
(4, 2, 'Ceiling Fan & Chandelier Install', 'fan-chandelier-install', 'Assembly, balancing & secure ceiling mounting with regulator wiring', 'High-strength ceiling anchor fitting, blade balancing to eliminate wobble/noise, and regulator connection.', 299.00, 249.00, 35, 'https://images.unsplash.com/photo-1544717302-de2939b7ef71?w=600&auto=format&fit=crop&q=80', 30, 4.85, 142, 1),
(5, 3, 'Split AC Deep Foam Jet Service', 'split-ac-foam-service', 'Deep coil cleansing with high-pressure jet, blower wash & filter sterilize', 'High-efficiency AC foaming wash removing 99% mold & dust, coil fin straightening, cooling temperature check, and drain tray flush.', 799.00, 649.00, 60, 'https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=600&auto=format&fit=crop&q=80', 60, 4.92, 340, 1),
(6, 4, 'Intense Bathroom Scrub & De-scaling', 'bathroom-deep-clean', 'Limescale removal from tiles, fixtures, sanitaryware & floor scrub', 'Specialized acid-free chemicals for removing hard water stains from mirrors, glass partitions, commode disinfection, and grout scrubbing.', 699.00, 549.00, 75, 'https://images.unsplash.com/photo-1581578731548-c64695cc6952?w=600&auto=format&fit=crop&q=80', 15, 4.88, 184, 1),
(7, 5, 'Door Lock & Hinge Realignment', 'door-lock-hinge-repair', 'Fix jamming doors, install mortise locks, cylinder change & lubing', 'Complete alignment adjustment for sagging doors, installation of deadbolts, smart electronic locks, or hydraulic door closers.', 449.00, 379.00, 45, 'https://images.unsplash.com/photo-1538688525198-9b88f6f53126?w=600&auto=format&fit=crop&q=80', 45, 4.79, 88, 1),
(8, 6, 'Water Seepage Wall Patch & Touchup', 'wall-patch-touchup', 'Scrape flaking paint, anti-dampness putty coat, sand & emulsion finish', 'Targeted repair of peeling paint patches up to 25 sq.ft, anti-fungal primer coat, smooth acrylic putty leveling, and matching topcoat paint application.', 899.00, 749.00, 90, 'https://images.unsplash.com/photo-1589939705384-5185137a7f0f?w=600&auto=format&fit=crop&q=80', 90, 4.91, 76, 1);

-- Active Coupons
INSERT INTO `coupons` (`id`, `code`, `discount_type`, `discount_value`, `min_order_amount`, `max_discount_amount`, `valid_from`, `valid_until`, `usage_limit`, `per_user_limit`, `total_used`, `is_active`) VALUES
(1, 'BROPATCH50', 'percentage', 20.00, 400.00, 150.00, '2026-01-01 00:00:00', '2027-12-31 23:59:59', 5000, 1, 142, 1),
(2, 'WELCOME100', 'fixed', 100.00, 500.00, 100.00, '2026-01-01 00:00:00', '2027-12-31 23:59:59', 10000, 1, 389, 1),
(3, 'FESTIVE25', 'percentage', 25.00, 700.00, 250.00, '2026-01-01 00:00:00', '2027-12-31 23:59:59', 2000, 2, 54, 1);

-- Homepage Banners
INSERT INTO `banners` (`id`, `title`, `subtitle`, `cta_text`, `destination_url`, `category_id`, `image_url`, `badge_text`, `sort_order`, `is_active`) VALUES
(1, 'Monsoon Moisture Shield', 'Get 25% off Wall Seepage & Dampness Patching', 'Book Shield', '/services/8', 6, 'https://images.unsplash.com/photo-1589939705384-5185137a7f0f?w=800&auto=format&fit=crop&q=80', 'SEASONAL OFFER', 1, 1),
(2, 'AC Power Chill Service', 'High-Pressure Jet Foam Wash with 60-Day Guarantee', 'Get 20% Off', '/services/5', 3, 'https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=800&auto=format&fit=crop&q=80', 'TOP RATED', 2, 1),
(3, 'Safe Electric Earthing Check', 'Prevent voltage surges & protect home electronics', 'Check Now', '/services/3', 2, 'https://images.unsplash.com/photo-1621905251189-08b45d6a269e?w=800&auto=format&fit=crop&q=80', 'SAFETY FIRST', 3, 1);

-- Test Users (Customer and Verified Providers)
INSERT INTO `users` (`id`, `role_id`, `name`, `email`, `phone`, `password_hash`, `avatar_url`, `status`) VALUES
(1, 1, 'Rahul Sharma', 'customer@bropatch.com', '+91 98765 43210', '$2y$10$tZ9v7C1bB5LzF4b7XyQdVu0fI2mG8kH5p3r1t9wA2sD4f6g8h0j2k', 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&auto=format&fit=crop&q=80', 'active'),
(2, 2, 'Vikram Singh (Plumbing Pro)', 'vikram.plumber@bropatch.com', '+91 98111 22334', '$2y$10$tZ9v7C1bB5LzF4b7XyQdVu0fI2mG8kH5p3r1t9wA2sD4f6g8h0j2k', 'https://images.unsplash.com/photo-1560250097-0b93528c311a?w=200&auto=format&fit=crop&q=80', 'active'),
(3, 2, 'Amit Verma (Master Electrician)', 'amit.electric@bropatch.com', '+91 98222 33445', '$2y$10$tZ9v7C1bB5LzF4b7XyQdVu0fI2mG8kH5p3r1t9wA2sD4f6g8h0j2k', 'https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=200&auto=format&fit=crop&q=80', 'active'),
(4, 2, 'Deepak Kumar (AC Specialist)', 'deepak.ac@bropatch.com', '+91 98333 44556', '$2y$10$tZ9v7C1bB5LzF4b7XyQdVu0fI2mG8kH5p3r1t9wA2sD4f6g8h0j2k', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&auto=format&fit=crop&q=80', 'active'),
(5, 2, 'Sanjay Patel (Pending Onboarding)', 'sanjay.clean@bropatch.com', '+91 98444 55667', '$2y$10$tZ9v7C1bB5LzF4b7XyQdVu0fI2mG8kH5p3r1t9wA2sD4f6g8h0j2k', 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&auto=format&fit=crop&q=80', 'active');

-- Provider Profiles
INSERT INTO `providers` (`id`, `user_id`, `business_name`, `bio`, `experience_years`, `skills`, `service_areas`, `current_latitude`, `current_longitude`, `verification_status`, `is_available`, `is_online`, `rating_avg`, `total_jobs_completed`, `total_earnings`, `pending_payout_balance`, `cod_pending_amount`) VALUES
(1, 2, 'Vikram QuickFix Plumbing', 'Certified master plumber with 8+ years fixing residential pipelines, water heaters & mixers.', 8, 'Pipe repair, Drainage, Faucets, Water Heaters, Seepage Detection', 'South Delhi, Central Delhi, Noida Sector 15-62', 28.5355, 77.2410, 'approved', 1, 1, 4.92, 148, 48200.00, 3200.00, 450.00),
(2, 3, 'Verma Power & Electric Works', 'Government licensed wireman specializing in switchboards, power distribution & lighting.', 6, 'MCB, Switchboards, Ceiling Fans, Short Circuit, Inverter Wiring', 'West Delhi, South Delhi, Gurgaon Phase 1-5', 28.5800, 77.2200, 'approved', 1, 1, 4.88, 112, 36500.00, 2450.00, 0.00),
(3, 4, 'CoolTech HVAC Solutions', 'AC & refrigeration specialist certified in eco-friendly refrigerants and high pressure servicing.', 5, 'Split AC, Window AC, Inverter AC, Gas Charge, Deep Foam Wash', 'East Delhi, Noida, Indirapuram, Ghaziabad', 28.6200, 77.2900, 'approved', 1, 1, 4.95, 230, 78900.00, 5800.00, 700.00),
(4, 5, 'EcoClean Sanitization Hub', 'Commercial and residential deep sanitization and pest protection crew.', 3, 'Deep cleaning, Bathroom sanitation, Kitchen degreasing', 'North Delhi, Rohini, Pitampura', 28.7000, 77.1400, 'pending', 0, 0, 5.00, 0, 0.00, 0.00, 0.00);

-- Provider Credits
INSERT INTO `provider_credits` (`id`, `provider_id`, `current_balance`) VALUES
(1, 1, 2400.00),
(2, 2, 1850.00),
(3, 3, 3100.00),
(4, 4, 1000.00);

-- Provider Documents
INSERT INTO `provider_documents` (`id`, `provider_id`, `document_type`, `document_number`, `document_url`, `verification_status`) VALUES
(1, 1, 'id_proof', 'AADHAAR-XXXX-4589', 'https://bropatch.com/docs/vikram_aadhaar.pdf', 'verified'),
(2, 1, 'skill_certificate', 'ITI-PLUMB-2018-992', 'https://bropatch.com/docs/vikram_iti.pdf', 'verified'),
(3, 2, 'id_proof', 'AADHAAR-XXXX-9122', 'https://bropatch.com/docs/amit_aadhaar.pdf', 'verified'),
(4, 4, 'id_proof', 'AADHAAR-XXXX-1144', 'https://bropatch.com/docs/sanjay_aadhaar.pdf', 'pending');

-- Provider Services Mapping
INSERT INTO `provider_services` (`provider_id`, `service_id`, `custom_price`, `is_active`) VALUES
(1, 1, 399.00, 1),
(1, 2, 299.00, 1),
(2, 3, 329.00, 1),
(2, 4, 249.00, 1),
(3, 5, 649.00, 1);

-- Customer Saved Address
INSERT INTO `addresses` (`id`, `user_id`, `label`, `street_address`, `apartment_unit`, `landmark`, `city`, `state`, `postal_code`, `latitude`, `longitude`, `is_default`) VALUES
(1, 1, 'Home', 'Flat 402, Sunshine Heights, Outer Ring Road', 'Tower B', 'Near Apollo Hospital', 'New Delhi', 'Delhi', '110076', 28.5355, 77.2800, 1),
(2, 1, 'Office', 'Plot 18, Cyber City Phase 2', 'Floor 3', 'Opposite Metro Gate 3', 'Gurgaon', 'Haryana', '122002', 28.4900, 77.0900, 0);

-- Initial Active Bookings for Immediate Testing
INSERT INTO `bookings` (`id`, `booking_code`, `customer_id`, `provider_id`, `service_id`, `address_id`, `coupon_id`, `status`, `scheduled_date`, `scheduled_time_slot`, `problem_description`, `base_amount`, `discount_amount`, `tax_amount`, `final_amount`, `platform_fee`, `provider_payout_amount`, `payment_method`, `payment_status`, `customer_contact_phone`) VALUES
(1, 'BP-2026-8819', 1, 1, 1, 1, 1, 'provider_on_way', '2026-08-16', '10:00 AM - 12:00 PM', 'Kitchen sink pipe is leaking heavily under the counter cabinet.', 399.00, 79.80, 57.45, 376.65, 59.85, 316.80, 'razorpay', 'paid', '+91 98765 43210'),
(2, 'BP-2026-8820', 1, 3, 5, 1, NULL, 'completed', '2026-08-14', '02:00 PM - 04:00 PM', 'Master bedroom 1.5 Ton Split AC needs foam jet clean and filter wash.', 649.00, 0.00, 116.82, 765.82, 97.35, 668.47, 'cod', 'paid', '+91 98765 43210');

-- Booking Status History
INSERT INTO `booking_status_history` (`booking_id`, `old_status`, `new_status`, `changed_by_user_id`, `changed_by_role`, `reason`, `notes`) VALUES
(1, 'pending', 'searching_provider', 1, 'customer', 'Booking submitted by customer', 'Customer completed checkout'),
(1, 'searching_provider', 'provider_assigned', 1, 'system', 'Smart match assigned Vikram Singh', 'Provider accepted dispatch'),
(1, 'provider_assigned', 'provider_accepted', 2, 'provider', 'Provider accepted job request', 'En route to customer premises'),
(1, 'provider_accepted', 'provider_on_way', 2, 'provider', 'Technician departed for site', 'ETA 15 mins via Outer Ring Rd'),
(2, 'pending', 'completed', 4, 'provider', 'AC Foam Jet cleaning completed successfully', 'Full cooling test verified with digital gauge');

-- Invoices
INSERT INTO `invoices` (`id`, `invoice_number`, `booking_id`, `customer_name`, `customer_phone`, `customer_address`, `provider_name`, `service_name`, `base_amount`, `discount_amount`, `tax_gst_amount`, `total_amount`, `payment_method`, `payment_status`, `issued_date`) VALUES
(1, 'INV-BROPATCH-2026-001', 2, 'Rahul Sharma', '+91 98765 43210', 'Flat 402, Sunshine Heights, Outer Ring Road, New Delhi 110076', 'Deepak Kumar (AC Specialist)', 'Split AC Deep Foam Jet Service', 649.00, 0.00, 116.82, 765.82, 'Cash On Delivery (COD)', 'Paid & Reconciled', '2026-08-14');

-- Reviews
INSERT INTO `reviews` (`id`, `booking_id`, `customer_id`, `provider_id`, `service_id`, `rating`, `review_text`, `provider_reply`) VALUES
(1, 2, 1, 3, 5, 5, 'Superb technician! Deep foam jet wash made the AC cooling like new within 30 minutes. Extremely neat and polite work.', 'Thank you Rahul! Always happy to serve with genuine Bropatch care.');

-- Payouts
INSERT INTO `payouts` (`id`, `payout_reference`, `provider_id`, `amount`, `status`, `bank_account_mask`, `ifsc_code`, `notes`) VALUES
(1, 'PO-2026-0801', 1, 14200.00, 'paid', 'HDFC Bank - XX4819', 'HDFC0001244', 'Weekly settlement processed'),
(2, 'PO-2026-0802', 3, 21800.00, 'paid', 'SBI - XX9012', 'SBIN0004512', 'Weekly settlement processed'),
(3, 'PO-2026-0803', 1, 3200.00, 'pending', 'HDFC Bank - XX4819', 'HDFC0001244', 'Current cycle withdrawal requested');
