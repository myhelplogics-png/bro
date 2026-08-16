-- =====================================================
-- Bropatch Database Schema
-- =====================================================
-- This schema includes all tables for the complete
-- service marketplace platform with admin management
-- =====================================================

-- Create Database (if not exists)
CREATE DATABASE IF NOT EXISTS bropatch_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bropatch_db;

-- =====================================================
-- USER ROLES AND PERMISSIONS
-- =====================================================

CREATE TABLE IF NOT EXISTS user_roles (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO user_roles (id, name, description) VALUES
(1, 'customer', 'Regular customer who books services'),
(2, 'provider', 'Service provider who fulfills bookings'),
(3, 'admin', 'Admin user'),
(4, 'super_admin', 'Super admin with all permissions')
ON DUPLICATE KEY UPDATE id=id;

CREATE TABLE IF NOT EXISTS admin_roles (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    permissions JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO admin_roles (id, name, description, permissions) VALUES
(1, 'super_admin', 'Super admin with all permissions', JSON_OBJECT('all', true)),
(2, 'admin', 'Admin with most permissions', JSON_OBJECT('users', true, 'providers', true, 'bookings', true, 'services', true)),
(3, 'moderator', 'Moderator for reviews and disputes', JSON_OBJECT('reviews', true, 'disputes', true))
ON DUPLICATE KEY UPDATE id=id;

-- =====================================================
-- USERS TABLE
-- =====================================================

CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    role_id INT NOT NULL DEFAULT 1,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(255),
    bio TEXT,
    status ENUM('active', 'inactive', 'suspended', 'banned') DEFAULT 'active',
    email_verified_at TIMESTAMP NULL,
    phone_verified_at TIMESTAMP NULL,
    last_login_at TIMESTAMP NULL,
    two_factor_enabled BOOLEAN DEFAULT false,
    two_factor_secret VARCHAR(255),
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    device_tokens JSON,
    preferences JSON,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (role_id) REFERENCES user_roles(id),
    INDEX idx_email (email),
    INDEX idx_phone (phone),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- ADMIN USERS TABLE
-- =====================================================

CREATE TABLE IF NOT EXISTS admin_users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    role_id INT NOT NULL DEFAULT 2,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(255),
    is_active BOOLEAN DEFAULT true,
    last_login_at TIMESTAMP NULL,
    two_factor_enabled BOOLEAN DEFAULT false,
    two_factor_secret VARCHAR(255),
    permissions JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (role_id) REFERENCES admin_roles(id),
    INDEX idx_email (email),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- PROVIDERS TABLE
-- =====================================================

CREATE TABLE IF NOT EXISTS providers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL UNIQUE,
    business_name VARCHAR(255),
    business_license VARCHAR(255),
    business_license_url VARCHAR(255),
    years_of_experience INT,
    description TEXT,
    hourly_rate DECIMAL(10, 2),
    rating DECIMAL(3, 2) DEFAULT 0,
    total_reviews INT DEFAULT 0,
    total_bookings INT DEFAULT 0,
    response_time_minutes INT,
    cancellation_rate DECIMAL(5, 2) DEFAULT 0,
    verification_status ENUM('pending', 'approved', 'rejected', 'suspended') DEFAULT 'pending',
    rejection_reason TEXT,
    is_available BOOLEAN DEFAULT true,
    is_online BOOLEAN DEFAULT false,
    verified_at TIMESTAMP NULL,
    verified_by_admin_id INT,
    service_area_radius INT DEFAULT 50,
    bank_account_number VARCHAR(30),
    bank_ifsc_code VARCHAR(20),
    bank_account_holder VARCHAR(100),
    aadhar_number VARCHAR(20),
    pan_number VARCHAR(20),
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (verified_by_admin_id) REFERENCES admin_users(id),
    INDEX idx_verification_status (verification_status),
    INDEX idx_is_available (is_available),
    INDEX idx_rating (rating),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- SERVICE CATEGORIES TABLE
-- =====================================================

CREATE TABLE IF NOT EXISTS service_categories (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    icon VARCHAR(100),
    description TEXT,
    image_url VARCHAR(255),
    sort_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_is_active (is_active),
    INDEX idx_sort_order (sort_order),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- SERVICES TABLE
-- =====================================================

CREATE TABLE IF NOT EXISTS services (
    id INT PRIMARY KEY AUTO_INCREMENT,
    category_id INT NOT NULL,
    name VARCHAR(150) NOT NULL,
    slug VARCHAR(150) NOT NULL,
    short_description VARCHAR(255),
    description TEXT,
    base_price DECIMAL(10, 2) NOT NULL,
    estimated_duration_mins INT DEFAULT 60,
    image_url VARCHAR(255),
    is_active BOOLEAN DEFAULT true,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (category_id) REFERENCES service_categories(id),
    INDEX idx_category_id (category_id),
    INDEX idx_is_active (is_active),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- PROVIDER SERVICES TABLE
-- =====================================================

CREATE TABLE IF NOT EXISTS provider_services (
    id INT PRIMARY KEY AUTO_INCREMENT,
    provider_id INT NOT NULL,
    service_id INT NOT NULL,
    customized_price DECIMAL(10, 2),
    customized_duration_mins INT,
    is_available BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (provider_id) REFERENCES providers(id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES services(id),
    UNIQUE KEY unique_provider_service (provider_id, service_id),
    INDEX idx_provider_id (provider_id),
    INDEX idx_service_id (service_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- PROVIDER DOCUMENTS TABLE
-- =====================================================

CREATE TABLE IF NOT EXISTS provider_documents (
    id INT PRIMARY KEY AUTO_INCREMENT,
    provider_id INT NOT NULL,
    document_type ENUM('aadhar', 'pan', 'license', 'certificate', 'other') NOT NULL,
    document_url VARCHAR(255) NOT NULL,
    verification_status ENUM('pending', 'verified', 'rejected') DEFAULT 'pending',
    verified_at TIMESTAMP NULL,
    verified_by_admin_id INT,
    rejection_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (provider_id) REFERENCES providers(id) ON DELETE CASCADE,
    FOREIGN KEY (verified_by_admin_id) REFERENCES admin_users(id),
    INDEX idx_provider_id (provider_id),
    INDEX idx_verification_status (verification_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- BOOKINGS TABLE
-- =====================================================

CREATE TABLE IF NOT EXISTS bookings (
    id INT PRIMARY KEY AUTO_INCREMENT,
    booking_code VARCHAR(50) NOT NULL UNIQUE,
    customer_id INT NOT NULL,
    provider_id INT,
    service_id INT NOT NULL,
    scheduled_date DATE NOT NULL,
    scheduled_time TIME NOT NULL,
    duration_minutes INT,
    service_location_latitude DECIMAL(10, 8),
    service_location_longitude DECIMAL(11, 8),
    service_address TEXT,
    customer_notes TEXT,
    status ENUM('pending', 'searching_provider', 'provider_assigned', 'in_progress', 'completed', 'cancelled') DEFAULT 'pending',
    base_amount DECIMAL(10, 2) NOT NULL,
    discount_amount DECIMAL(10, 2) DEFAULT 0,
    tax_amount DECIMAL(10, 2) DEFAULT 0,
    final_amount DECIMAL(10, 2) NOT NULL,
    platform_fee DECIMAL(10, 2),
    provider_payout_amount DECIMAL(10, 2),
    coupon_code VARCHAR(50),
    cancellation_reason TEXT,
    cancelled_by ENUM('customer', 'provider', 'admin'),
    cancelled_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    rating INT,
    review_text TEXT,
    reviewed_at TIMESTAMP NULL,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (customer_id) REFERENCES users(id),
    FOREIGN KEY (provider_id) REFERENCES providers(id),
    FOREIGN KEY (service_id) REFERENCES services(id),
    UNIQUE KEY unique_booking_code (booking_code),
    INDEX idx_customer_id (customer_id),
    INDEX idx_provider_id (provider_id),
    INDEX idx_status (status),
    INDEX idx_scheduled_date (scheduled_date),
    INDEX idx_created_at (created_at),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- BOOKING STATUS HISTORY
-- =====================================================

CREATE TABLE IF NOT EXISTS booking_status_history (
    id INT PRIMARY KEY AUTO_INCREMENT,
    booking_id INT NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_by_user_id INT,
    changed_by_role VARCHAR(50),
    reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    INDEX idx_booking_id (booking_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- BOOKING IMAGES TABLE
-- =====================================================

CREATE TABLE IF NOT EXISTS booking_images (
    id INT PRIMARY KEY AUTO_INCREMENT,
    booking_id INT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    image_type ENUM('before', 'after', 'progress') DEFAULT 'progress',
    uploaded_by_user_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by_user_id) REFERENCES users(id),
    INDEX idx_booking_id (booking_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- PAYMENTS TABLE
-- =====================================================

CREATE TABLE IF NOT EXISTS payments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    payment_code VARCHAR(50) NOT NULL UNIQUE,
    booking_id INT NOT NULL,
    user_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method ENUM('credit_card', 'debit_card', 'upi', 'wallet', 'bank_transfer') NOT NULL,
    payment_gateway ENUM('razorpay', 'stripe', 'paypal') DEFAULT 'razorpay',
    transaction_id VARCHAR(100) UNIQUE,
    reference_number VARCHAR(100),
    status ENUM('pending', 'completed', 'failed', 'refunded') DEFAULT 'pending',
    failure_reason TEXT,
    paid_at TIMESTAMP NULL,
    refund_amount DECIMAL(10, 2),
    refunded_at TIMESTAMP NULL,
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (booking_id) REFERENCES bookings(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_booking_id (booking_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_transaction_id (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- PAYOUTS TABLE
-- =====================================================

CREATE TABLE IF NOT EXISTS payouts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    payout_code VARCHAR(50) NOT NULL UNIQUE,
    provider_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    period_start DATE,
    period_end DATE,
    status ENUM('pending', 'processing', 'paid', 'failed', 'cancelled') DEFAULT 'pending',
    payment_method ENUM('bank_transfer', 'wallet', 'cheque') DEFAULT 'bank_transfer',
    transaction_id VARCHAR(100),
    processed_by_admin_id INT,
    processed_at TIMESTAMP NULL,
    failure_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (provider_id) REFERENCES providers(id),
    FOREIGN KEY (processed_by_admin_id) REFERENCES admin_users(id),
    INDEX idx_provider_id (provider_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- REVIEWS TABLE
-- =====================================================

CREATE TABLE IF NOT EXISTS reviews (
    id INT PRIMARY KEY AUTO_INCREMENT,
    booking_id INT NOT NULL,
    customer_id INT NOT NULL,
    provider_id INT NOT NULL,
    service_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    title VARCHAR(150),
    comment TEXT,
    is_hidden BOOLEAN DEFAULT false,
    hidden_reason TEXT,
    hidden_by_admin_id INT,
    helpful_count INT DEFAULT 0,
    unhelpful_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (booking_id) REFERENCES bookings(id),
    FOREIGN KEY (customer_id) REFERENCES users(id),
    FOREIGN KEY (provider_id) REFERENCES providers(id),
    FOREIGN KEY (service_id) REFERENCES services(id),
    FOREIGN KEY (hidden_by_admin_id) REFERENCES admin_users(id),
    UNIQUE KEY unique_booking_review (booking_id),
    INDEX idx_provider_id (provider_id),
    INDEX idx_rating (rating),
    INDEX idx_is_hidden (is_hidden)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- DISPUTES TABLE
-- =====================================================

CREATE TABLE IF NOT EXISTS disputes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    dispute_code VARCHAR(50) NOT NULL UNIQUE,
    booking_id INT NOT NULL,
    raised_by_user_id INT NOT NULL,
    raised_against_user_id INT,
    category ENUM('payment', 'service_quality', 'cancellation', 'no_show', 'other') DEFAULT 'other',
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    evidence_url VARCHAR(255),
    status ENUM('open', 'under_review', 'resolved', 'closed') DEFAULT 'open',
    resolution_type ENUM('refund', 'rebook', 'discount', 'compensation', 'no_action'),
    resolution_notes TEXT,
    resolved_by_admin_id INT,
    resolved_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (booking_id) REFERENCES bookings(id),
    FOREIGN KEY (raised_by_user_id) REFERENCES users(id),
    FOREIGN KEY (raised_against_user_id) REFERENCES users(id),
    FOREIGN KEY (resolved_by_admin_id) REFERENCES admin_users(id),
    INDEX idx_booking_id (booking_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- COUPONS TABLE
-- =====================================================

CREATE TABLE IF NOT EXISTS coupons (
    id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    discount_type ENUM('percentage', 'fixed_amount') DEFAULT 'percentage',
    discount_value DECIMAL(10, 2) NOT NULL,
    min_order_amount DECIMAL(10, 2) DEFAULT 0,
    max_discount_amount DECIMAL(10, 2),
    max_usage_count INT,
    current_usage_count INT DEFAULT 0,
    valid_from DATE,
    valid_until DATE NOT NULL,
    usage_per_user_limit INT DEFAULT 1,
    is_active BOOLEAN DEFAULT true,
    applicable_categories JSON,
    applicable_services JSON,
    created_by_admin_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (created_by_admin_id) REFERENCES admin_users(id),
    UNIQUE KEY unique_code (code),
    INDEX idx_is_active (is_active),
    INDEX idx_valid_until (valid_until)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- COUPON USAGE TABLE
-- =====================================================

CREATE TABLE IF NOT EXISTS coupon_usage (
    id INT PRIMARY KEY AUTO_INCREMENT,
    coupon_id INT NOT NULL,
    user_id INT NOT NULL,
    booking_id INT,
    used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (coupon_id) REFERENCES coupons(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (booking_id) REFERENCES bookings(id),
    INDEX idx_coupon_id (coupon_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- BANNERS TABLE
-- =====================================================

CREATE TABLE IF NOT EXISTS banners (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(150) NOT NULL,
    subtitle VARCHAR(255),
    cta_text VARCHAR(50),
    cta_action VARCHAR(255),
    image_url VARCHAR(255) NOT NULL,
    category_id INT,
    start_date DATE,
    end_date DATE,
    sort_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    click_count INT DEFAULT 0,
    created_by_admin_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (category_id) REFERENCES service_categories(id),
    FOREIGN KEY (created_by_admin_id) REFERENCES admin_users(id),
    INDEX idx_is_active (is_active),
    INDEX idx_sort_order (sort_order),
    INDEX idx_end_date (end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- NOTIFICATIONS TABLE
-- =====================================================

CREATE TABLE IF NOT EXISTS notifications (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    type ENUM('booking', 'payment', 'review', 'promotion', 'admin', 'system') DEFAULT 'system',
    related_entity_type VARCHAR(50),
    related_entity_id INT,
    is_read BOOLEAN DEFAULT false,
    read_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- AUDIT LOGS TABLE
-- =====================================================

CREATE TABLE IF NOT EXISTS audit_logs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    admin_id INT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id INT,
    old_values JSON,
    new_values JSON,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (admin_id) REFERENCES admin_users(id),
    INDEX idx_admin_id (admin_id),
    INDEX idx_action (action),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- SETTINGS TABLE
-- =====================================================

CREATE TABLE IF NOT EXISTS settings (
    key VARCHAR(100) PRIMARY KEY,
    value LONGTEXT,
    description TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default settings
INSERT INTO settings (key, value, description) VALUES
('platform_commission_percentage', '15', 'Platform commission on each booking'),
('min_provider_rating', '3.5', 'Minimum rating required for providers'),
('max_booking_search_radius', '50', 'Maximum search radius in km'),
('admin_email', 'admin@bropatch.com', 'Admin email for notifications'),
('support_phone', '+91-1234567890', 'Support phone number'),
('currency', 'INR', 'Platform currency'),
('payment_gateway', 'razorpay', 'Default payment gateway')
ON DUPLICATE KEY UPDATE key=key;

-- =====================================================
-- SESSION TABLE
-- =====================================================

CREATE TABLE IF NOT EXISTS sessions (
    id VARCHAR(100) PRIMARY KEY,
    user_id INT,
    session_data LONGTEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- ADMIN SESSIONS TABLE
-- =====================================================

CREATE TABLE IF NOT EXISTS admin_sessions (
    id VARCHAR(100) PRIMARY KEY,
    admin_id INT NOT NULL,
    session_data LONGTEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    
    FOREIGN KEY (admin_id) REFERENCES admin_users(id) ON DELETE CASCADE,
    INDEX idx_admin_id (admin_id),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- CREATE INDEXES FOR PERFORMANCE
-- =====================================================

-- Add composite indexes for common queries
ALTER TABLE bookings ADD INDEX idx_customer_status (customer_id, status);
ALTER TABLE bookings ADD INDEX idx_provider_status (provider_id, status);
ALTER TABLE bookings ADD INDEX idx_scheduled_date_status (scheduled_date, status);
ALTER TABLE payments ADD INDEX idx_user_status (user_id, status);
ALTER TABLE provider_services ADD INDEX idx_is_available (is_available);
ALTER TABLE users ADD INDEX idx_role_status (role_id, status);

-- =====================================================
-- SAMPLE DATA (OPTIONAL)
-- =====================================================

-- Insert sample categories
INSERT INTO service_categories (id, name, slug, icon, description, sort_order, is_active) VALUES
(1, 'Plumbing', 'plumbing', 'plumbing', 'Plumbing and water services', 1, true),
(2, 'Electrical', 'electrical', 'electrical-services', 'Electrical installation and repair', 2, true),
(3, 'Carpentry', 'carpentry', 'carpentry', 'Wood work and carpentry services', 3, true),
(4, 'Painting', 'painting', 'paint-brush', 'House painting and wall painting', 4, true),
(5, 'Cleaning', 'cleaning', 'broom', 'Professional cleaning services', 5, true)
ON DUPLICATE KEY UPDATE name=name;

-- Insert sample services
INSERT INTO services (category_id, name, slug, short_description, base_price, estimated_duration_mins, is_active) VALUES
(1, 'Pipe Repair', 'pipe-repair', 'Professional pipe repair service', 500.00, 60, true),
(1, 'Tap Installation', 'tap-installation', 'Expert tap and fixture installation', 800.00, 90, true),
(2, 'Switch Installation', 'switch-installation', 'Electrical switch and socket installation', 300.00, 45, true),
(2, 'Wiring Service', 'wiring-service', 'Complete electrical wiring solutions', 5000.00, 240, true),
(3, 'Shelf Installation', 'shelf-installation', 'Wall shelf and cabinet installation', 1000.00, 120, true),
(4, 'Wall Painting', 'wall-painting', 'Interior and exterior wall painting', 50.00, 60, true),
(5, 'House Cleaning', 'house-cleaning', 'Deep cleaning and house maintenance', 1500.00, 180, true)
ON DUPLICATE KEY UPDATE name=name;

-- =====================================================
-- END OF SCHEMA
-- =====================================================
