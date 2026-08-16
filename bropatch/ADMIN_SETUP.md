# Bropatch Admin Panel Setup Guide

## Overview
The Bropatch Admin Panel is a comprehensive operations management system built with PHP, MySQL, and vanilla JavaScript. It provides admins with complete control over users, providers, bookings, services, payments, and platform operations.

## File Structure

```
bropatch/
├── backend/
│   ├── admin/
│   │   ├── api.php                 # Admin API Router (all endpoints)
│   │   ├── login.php               # Admin login page (HTML + JS)
│   │   └── dashboard.php           # Admin dashboard (HTML + JS)
│   ├── controllers/
│   │   ├── AdminAuthController.php # Authentication logic
│   │   ├── AdminController.php     # All CRUD operations
│   │   └── ...
│   ├── middleware/
│   │   └── AdminAuth.php           # Session & permission checks
│   ├── config/
│   │   └── database.php            # MySQL connection
│   └── helpers/
│       └── response.php            # JSON response utilities
├── .htaccess                       # URL routing rules
├── .env.example                    # Configuration template
└── README.md                       # This file
```

## Installation & Setup

### 1. Database Setup
```bash
# Create the database
mysql -u root -p < database_schema.sql

# Or manually create:
CREATE DATABASE bropatch_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Environment Configuration
```bash
# Copy the example config
cp .env.example .env

# Edit .env with your database credentials
nano .env
```

### 3. Create Admin User
```bash
# Via PHP CLI
php -r "
    require 'backend/config/database.php';
    \$db = Database::getConnection();
    \$email = 'admin@bropatch.com';
    \$password = 'Admin@Bropatch2026!';
    \$hash = password_hash(\$password, PASSWORD_BCRYPT);
    \$db->prepare('INSERT INTO admin_users (role_id, name, email, password_hash, is_active) VALUES (1, \"Admin\", ?, ?, 1)')
       ->execute([\$email, \$hash]);
    echo 'Admin user created!';
"
```

### 4. Enable Mod Rewrite
Ensure Apache's `mod_rewrite` is enabled:
```bash
a2enmod rewrite
systemctl restart apache2
```

## API Endpoints

### Authentication Routes
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/backend/admin/api/login` | Admin login |
| POST | `/backend/admin/api/logout` | Admin logout |
| GET | `/backend/admin/api/me` | Get current admin |
| GET | `/backend/admin/api/permissions` | Get admin permissions |

### Provider Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/backend/admin/api/providers` | List all providers |
| GET | `/backend/admin/api/providers/{id}` | Get provider details |
| POST | `/backend/admin/api/providers/{id}/approve` | Approve provider |
| POST | `/backend/admin/api/providers/{id}/reject` | Reject provider |
| POST | `/backend/admin/api/providers/{id}/suspend` | Suspend provider |
| POST | `/backend/admin/api/providers/{id}/activate` | Activate provider |

### Booking Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/backend/admin/api/bookings` | List all bookings |
| GET | `/backend/admin/api/bookings/{id}` | Get booking details |
| POST | `/backend/admin/api/bookings/{id}/assign` | Assign provider |
| POST | `/backend/admin/api/bookings/{id}/cancel` | Cancel booking |

### Service Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/backend/admin/api/services` | List all services |
| GET | `/backend/admin/api/services/{id}` | Get service |
| POST | `/backend/admin/api/services` | Create service |
| PUT | `/backend/admin/api/services/{id}` | Update service |

### Category Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/backend/admin/api/categories` | List categories |
| POST | `/backend/admin/api/categories` | Create category |
| PUT | `/backend/admin/api/categories/{id}` | Update category |

### Banner Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/backend/admin/api/banners` | List banners |
| POST | `/backend/admin/api/banners` | Create banner |
| PUT | `/backend/admin/api/banners/{id}` | Update banner |
| DELETE | `/backend/admin/api/banners/{id}` | Delete banner |

### Coupon Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/backend/admin/api/coupons` | List coupons |
| POST | `/backend/admin/api/coupons` | Create coupon |
| PUT | `/backend/admin/api/coupons/{id}` | Update coupon |

### Payment & Payout Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/backend/admin/api/payments` | List payments |
| GET | `/backend/admin/api/payments/{id}` | Get payment |
| GET | `/backend/admin/api/payouts` | List payouts |
| GET | `/backend/admin/api/payouts/{id}` | Get payout |
| POST | `/backend/admin/api/payouts/{id}/process` | Process payout |

### User Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/backend/admin/api/users` | List users |
| GET | `/backend/admin/api/users/{id}` | Get user |
| POST | `/backend/admin/api/users/{id}/suspend` | Suspend user |
| POST | `/backend/admin/api/users/{id}/activate` | Activate user |

### Review & Dispute Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/backend/admin/api/reviews` | List reviews |
| POST | `/backend/admin/api/reviews/{id}/hide` | Hide review |
| POST | `/backend/admin/api/reviews/{id}/restore` | Restore review |
| GET | `/backend/admin/api/disputes` | List disputes |
| GET | `/backend/admin/api/disputes/{id}` | Get dispute |
| POST | `/backend/admin/api/disputes/{id}/resolve` | Resolve dispute |

### Reports & Analytics
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/backend/admin/api/dashboard` | Dashboard metrics |
| GET | `/backend/admin/api/reports/bookings` | Booking report |
| GET | `/backend/admin/api/reports/revenue` | Revenue report |
| GET | `/backend/admin/api/audit-logs` | Audit logs |

### Platform Settings
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/backend/admin/api/settings` | Get settings |
| POST | `/backend/admin/api/settings` | Update settings |
| POST | `/backend/admin/api/notifications/send` | Send notification |

### Admin Management (Super Admin Only)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/backend/admin/api/admins` | List admins |
| POST | `/backend/admin/api/admins` | Create admin |
| PUT | `/backend/admin/api/admins/{id}` | Update admin |

## Access URLs

After setup, access these URLs:

- **Admin Login**: `http://localhost/backend/admin/login.php`
- **Admin Dashboard**: `http://localhost/backend/admin/dashboard.php`
- **API Base**: `http://localhost/backend/admin/api.php`

## Demo Credentials

For testing purposes, use:
- **Email**: `admin@bropatch.com`
- **Password**: `Admin@Bropatch2026!`

## Features

### Dashboard
- Real-time statistics from MySQL
- Total users, providers, bookings
- Revenue metrics and platform earnings
- Provider payout tracking
- Open disputes counter
- Recent bookings table

### User Management
- View all registered users
- Search and filter users
- Suspend/activate user accounts
- View user booking history

### Provider Management
- Approve/reject provider registrations
- View provider profiles and documents
- Suspend/activate providers
- Track provider availability status
- View provider ratings and reviews

### Booking Management
- List all bookings with status
- Assign providers to unassigned bookings
- Cancel bookings with reason
- View booking details and history
- Track booking completion

### Service Management
- Create and edit services
- Manage service categories
- Set pricing and duration
- Control service availability

### Marketing
- Manage promotional banners
- Create discount coupons
- Set coupon expiry and limits
- Track coupon usage

### Financial Management
- View all payment transactions
- Track provider payouts
- Process payout requests
- Generate revenue reports

### Quality Control
- Moderate customer reviews
- Hide/restore inappropriate reviews
- Manage disputes and conflicts
- Track resolution history

### Operations
- View comprehensive audit logs
- Track all admin actions
- Monitor system activity
- Generate analytics reports

## Security Features

✅ **Session Management**
- Secure cookie-based sessions
- Automatic timeout after 1 hour
- Session hijacking protection

✅ **Authentication**
- Bcrypt password hashing
- Email-based login
- "Remember me" functionality

✅ **Authorization**
- Role-based access control (RBAC)
- Permission checking on every action
- Resource-level authorization

✅ **Data Protection**
- SQL injection prevention (prepared statements)
- CSRF tokens on sensitive operations
- XSS protection via JSON responses

✅ **Audit Logging**
- All admin actions logged
- Timestamp tracking
- Action reason documentation

## API Response Format

All API responses follow this JSON structure:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "key": "value"
  },
  "code": 200
}
```

Error response:
```json
{
  "success": false,
  "message": "Error description",
  "code": 400
}
```

## Error Codes

| Code | Meaning |
|------|---------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 422 | Validation Error |
| 500 | Server Error |

## Troubleshooting

### Admin can't log in
1. Verify MySQL is running: `mysql -u root -p -e "SELECT 1"`
2. Check admin user exists: `SELECT * FROM admin_users;`
3. Clear browser cookies
4. Check `.htaccess` is properly configured

### API returns 404
1. Verify `.htaccess` is in place
2. Check `mod_rewrite` is enabled
3. Verify file paths in routes

### Dashboard shows no data
1. Verify database connection in `.env`
2. Check database tables exist
3. Verify user has database privileges
4. Check browser console for JS errors

### Session expires too quickly
1. Adjust `ADMIN_SESSION_TIMEOUT` in `.env`
2. Check server time is correct
3. Verify cookie domain settings

## Performance Optimization

- Database queries are paginated (default 20 per page)
- API responses include pagination metadata
- Audit logs are indexed by timestamp
- Consider adding database caching layer

## Future Enhancements

- [ ] Two-factor authentication (2FA)
- [ ] Advanced analytics dashboard
- [ ] Bulk actions (approve multiple providers)
- [ ] Email notifications for critical events
- [ ] Export reports to PDF/CSV
- [ ] Multi-language support
- [ ] Mobile admin app
- [ ] Real-time notifications via WebSockets

## Support & Documentation

For detailed API documentation, see:
- `AdminAuthController.php` - Authentication methods
- `AdminController.php` - All CRUD operations
- `AdminAuth.php` - Permission system

## License

Bropatch Admin Panel - All Rights Reserved
