# Bropatch Platform - PHP REST API & MySQL Backend Documentation

## 1. Architectural Overview
Bropatch uses a unified **PHP 8.2+ REST API** and **MySQL 8.0+** relational database as the **Single Source of Truth** for both the **Bropatch Customer / Provider Android Application** and the **PHP Operations Admin Panel**.

- **Base API URL**: `/api` (or `/backend/api`)
- **Admin Panel URL**: `/admin` (or `/backend/admin`)
- **Authentication**: JWT Bearer Token in `Authorization: Bearer <TOKEN>` header.

---

## 2. API Endpoints Reference

### 2.1 Authentication & Profile (`/api/auth`)
| Method | Endpoint | Auth | Description | Required Payload |
|---|---|---|---|---|
| `POST` | `/auth/register` | Public | Register customer or service provider | `{"name": "...", "email": "...", "password": "...", "role": "customer|provider", "phone": "..."}` |
| `POST` | `/auth/login` | Public | Login with email & password | `{"email": "...", "password": "..."}` |
| `GET` | `/auth/me` | Bearer | Get current user profile and session info | None |

### 2.2 Public Catalog (`/api/services`, `/api/categories`, `/api/banners`)
| Method | Endpoint | Auth | Description | Parameters |
|---|---|---|---|---|
| `GET` | `/banners` | Public | List active promotional banners for home carousel | None |
| `GET` | `/categories` | Public | List service categories with service count | None |
| `GET` | `/services` | Public | List services with filters | `?category_id=1&search=leak` |
| `GET` | `/services/{id}` | Public | Detailed service information with ratings and reviews | None |

### 2.3 Coupons (`/api/coupons`)
| Method | Endpoint | Auth | Description | Required Payload |
|---|---|---|---|---|
| `POST` | `/coupons/validate` | Bearer | Validate coupon code & calculate discount server-side | `{"code": "BROPATCH50", "order_amount": 499.00}` |

### 2.4 Bookings & State Machine (`/api/bookings`)
| Method | Endpoint | Auth | Description | Required Payload |
|---|---|---|---|---|
| `POST` | `/bookings` | Bearer | Create booking with server-side price calculation & status history | `{"service_id": 1, "address_id": 1, "scheduled_date": "2026-08-16", "scheduled_time_slot": "10:00 AM - 12:00 PM", "problem_description": "...", "payment_method": "razorpay|cod"}` |
| `GET` | `/bookings` | Bearer | Get active and past bookings for customer/provider | None |
| `POST` | `/bookings/{id}/status` | Bearer | Update booking status in state machine with history audit | `{"status": "provider_on_way", "reason": "Technician departed", "latitude": 28.535, "longitude": 77.241}` |

#### State Machine Sequence:
1. `pending` → `searching_provider`
2. `searching_provider` → `provider_assigned`
3. `provider_assigned` → `provider_accepted`
4. `provider_accepted` → `provider_on_way`
5. `provider_on_way` → `provider_arrived`
6. `provider_arrived` → `work_started`
7. `work_started` → `work_completed`
8. `work_completed` → `payment_pending` / `completed`
9. `completed` (Invoice generated & reviews unlocked)
10. `cancelled` / `disputed`

### 2.5 In-App Chat (`/api/chat`)
| Method | Endpoint | Auth | Description | Required Payload |
|---|---|---|---|---|
| `GET` | `/chat/{booking_id}/messages` | Bearer | Fetch message stream for an active booking | None |
| `POST` | `/chat/{booking_id}/messages` | Bearer | Send message from customer or provider | `{"message_text": "Hello, I am near your gate."}` |

### 2.6 Reviews & Ratings (`/api/reviews`)
| Method | Endpoint | Auth | Description | Required Payload |
|---|---|---|---|---|
| `POST` | `/reviews` | Bearer | Submit review and rating for completed booking | `{"booking_id": 1, "rating": 5, "review_text": "Excellent service!"}` |

### 2.7 Provider Operations (`/api/provider`)
| Method | Endpoint | Auth | Description | Required Payload |
|---|---|---|---|---|
| `GET` | `/provider/profile` | Bearer (Provider) | Fetch partner profile, skills, documents, and credits | None |
| `POST` | `/provider/location` | Bearer (Provider) | Send live GPS coordinates for tracking | `{"latitude": 28.5355, "longitude": 77.2410}` |
| `POST` | `/provider/online-toggle` | Bearer (Provider) | Toggle online/offline status | `{"is_online": true}` |
| `POST` | `/provider/payout` | Bearer (Provider) | Request payout of accumulated earnings | `{"amount": 3200.00, "bank_account_mask": "HDFC - XX4819", "ifsc_code": "HDFC0001244"}` |

### 2.8 Admin Console (`/api/admin`)
| Method | Endpoint | Auth | Description | Required Payload |
|---|---|---|---|---|
| `GET` | `/admin/dashboard` | Admin | Real-time database metrics, gross revenue, fees | None |
| `POST` | `/admin/providers/{id}/approve` | Admin | Approve provider onboarding and documents | None |
| `POST` | `/admin/bookings/{id}/assign` | Admin | Manually assign provider to booking | `{"provider_id": 2}` |
| `GET` | `/admin/audit-logs` | Admin | View audit log history of operations | None |

---

## 3. Database Schema Tables
- `users`, `user_roles`
- `providers`, `provider_documents`, `provider_services`
- `service_categories`, `services`
- `addresses`
- `bookings`, `booking_items`, `booking_status_history`, `booking_images`
- `conversations`, `conversation_participants`, `messages`
- `payments`, `payment_transactions`, `invoices`
- `reviews`, `notifications`
- `coupons`, `coupon_usage`
- `provider_credits`, `credit_transactions`, `payouts`, `disputes`
- `banners`, `admin_users`, `admin_roles`, `admin_permissions`, `audit_logs`, `settings`
