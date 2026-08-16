<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bropatch Admin Dashboard</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        :root {
            --primary: #1E3A8A;
            --primary-hover: #172554;
            --accent: #F59E0B;
            --accent-hover: #D97706;
            --bg: #F8FAFC;
            --surface: #FFFFFF;
            --text-main: #0F172A;
            --text-muted: #64748B;
            --border: #E2E8F0;
            --success: #10B981;
            --warning: #F59E0B;
            --danger: #EF4444;
            --info: #3B82F6;
            --radius: 12px;
            --shadow: 0 4px 6px -1px rgb(0 0 0 / 0.05), 0 2px 4px -2px rgb(0 0 0 / 0.05);
            --shadow-lg: 0 10px 15px -3px rgb(0 0 0 / 0.08), 0 4px 6px -4px rgb(0 0 0 / 0.05);
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, sans-serif;
        }

        body {
            background-color: var(--bg);
            color: var(--text-main);
            display: flex;
            min-height: 100vh;
        }

        /* SIDEBAR */
        aside {
            width: 260px;
            background-color: var(--surface);
            border-right: 1px solid var(--border);
            display: flex;
            flex-direction: column;
            position: fixed;
            top: 0;
            bottom: 0;
            left: 0;
            z-index: 50;
            overflow-y: auto;
        }

        .brand {
            padding: 24px;
            display: flex;
            align-items: center;
            gap: 12px;
            border-bottom: 1px solid var(--border);
        }

        .brand-badge {
            background: linear-gradient(135deg, var(--primary), #3B82F6);
            color: white;
            width: 40px;
            height: 40px;
            border-radius: 10px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 800;
            font-size: 1.25rem;
        }

        .brand-text h1 {
            font-size: 1.15rem;
            font-weight: 800;
            color: var(--primary);
            letter-spacing: -0.5px;
        }

        .brand-text span {
            font-size: 0.75rem;
            color: var(--accent);
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .nav-menu {
            padding: 16px 12px;
            flex: 1;
            display: flex;
            flex-direction: column;
            gap: 4px;
        }

        .nav-item {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 12px 16px;
            border-radius: 8px;
            color: var(--text-muted);
            text-decoration: none;
            font-weight: 600;
            font-size: 0.9rem;
            transition: all 0.2s ease;
            cursor: pointer;
            border: none;
            background: none;
            width: 100%;
            text-align: left;
        }

        .nav-item:hover {
            background-color: #EFF6FF;
            color: var(--primary);
        }

        .nav-item.active {
            background-color: var(--primary);
            color: white;
        }

        .nav-divider {
            height: 1px;
            background: var(--border);
            margin: 8px 0;
        }

        .nav-section-title {
            padding: 8px 16px;
            font-size: 0.75rem;
            font-weight: 700;
            text-transform: uppercase;
            color: var(--text-muted);
            letter-spacing: 0.5px;
            margin-top: 8px;
        }

        .logout-btn {
            margin-top: auto;
            padding: 12px 16px;
            border-top: 1px solid var(--border);
            background: none;
            border: none;
            color: var(--danger);
            font-weight: 600;
            cursor: pointer;
            width: 100%;
            text-align: left;
            transition: all 0.2s;
        }

        .logout-btn:hover {
            background: #FEE2E2;
        }

        /* MAIN CONTENT */
        main {
            margin-left: 260px;
            flex: 1;
            padding: 32px;
            max-width: 1400px;
        }

        header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 32px;
        }

        .header-title h2 {
            font-size: 1.75rem;
            font-weight: 800;
            color: var(--text-main);
        }

        .header-title p {
            color: var(--text-muted);
            font-size: 0.95rem;
            margin-top: 4px;
        }

        .admin-pill {
            display: flex;
            align-items: center;
            gap: 12px;
            background: var(--surface);
            padding: 8px 16px;
            border-radius: 30px;
            border: 1px solid var(--border);
            box-shadow: var(--shadow);
        }

        .admin-avatar {
            width: 32px;
            height: 32px;
            border-radius: 50%;
            background: #DBEAFE;
            color: var(--primary);
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 700;
            font-size: 0.85rem;
        }

        /* STATS GRID */
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-bottom: 32px;
        }

        .stat-card {
            background: var(--surface);
            padding: 24px;
            border-radius: var(--radius);
            border: 1px solid var(--border);
            box-shadow: var(--shadow);
            transition: transform 0.2s;
        }

        .stat-card:hover {
            transform: translateY(-2px);
        }

        .stat-card.primary {
            border-left: 4px solid var(--primary);
        }

        .stat-card.success {
            border-left: 4px solid var(--success);
        }

        .stat-card.warning {
            border-left: 4px solid var(--warning);
        }

        .stat-card.danger {
            border-left: 4px solid var(--danger);
        }

        .stat-card .label {
            font-size: 0.82rem;
            text-transform: uppercase;
            font-weight: 700;
            color: var(--text-muted);
            letter-spacing: 0.5px;
        }

        .stat-card .value {
            font-size: 1.85rem;
            font-weight: 800;
            margin: 8px 0;
            color: var(--text-main);
        }

        .stat-card .trend {
            font-size: 0.8rem;
            font-weight: 600;
        }

        .trend.up { color: var(--success); }
        .trend.neutral { color: var(--text-muted); }
        .trend.down { color: var(--danger); }

        /* PANELS */
        .panel {
            background: var(--surface);
            border-radius: var(--radius);
            border: 1px solid var(--border);
            box-shadow: var(--shadow);
            padding: 24px;
            margin-bottom: 32px;
        }

        .panel-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
        }

        .panel-header h3 {
            font-size: 1.2rem;
            font-weight: 700;
        }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        th {
            background: #F8FAFC;
            padding: 12px 16px;
            font-size: 0.8rem;
            font-weight: 700;
            text-transform: uppercase;
            color: var(--text-muted);
            border-bottom: 1px solid var(--border);
            text-align: left;
        }

        td {
            padding: 16px;
            border-bottom: 1px solid var(--border);
            font-size: 0.9rem;
        }

        tr:last-child td {
            border-bottom: none;
        }

        .badge {
            display: inline-block;
            padding: 4px 10px;
            border-radius: 20px;
            font-size: 0.75rem;
            font-weight: 700;
            text-transform: uppercase;
        }

        .badge-success { background: #DCFCE7; color: #166534; }
        .badge-warning { background: #FEF3C7; color: #92400E; }
        .badge-info { background: #E0E7FF; color: #3730A3; }
        .badge-danger { background: #FEE2E2; color: #991B1B; }
        .badge-pending { background: #FEF08A; color: #713F12; }

        .btn {
            padding: 8px 16px;
            border-radius: 8px;
            font-weight: 600;
            font-size: 0.85rem;
            border: none;
            cursor: pointer;
            transition: all 0.2s;
            display: inline-flex;
            align-items: center;
            gap: 6px;
            text-decoration: none;
        }

        .btn-primary {
            background: var(--primary);
            color: white;
        }

        .btn-primary:hover {
            background: var(--primary-hover);
        }

        .btn-outline {
            background: transparent;
            border: 1px solid var(--border);
            color: var(--text-main);
        }

        .btn-outline:hover {
            background: #F1F5F9;
        }

        .btn-sm {
            padding: 4px 10px;
            font-size: 0.78rem;
        }

        .charts-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
            gap: 24px;
            margin-top: 32px;
        }

        .chart-container {
            background: var(--surface);
            border-radius: var(--radius);
            border: 1px solid var(--border);
            padding: 24px;
            box-shadow: var(--shadow);
            position: relative;
            height: 300px;
        }

        .loading {
            text-align: center;
            padding: 40px;
            color: var(--text-muted);
        }

        .error-message {
            background: #FEE2E2;
            color: #991B1B;
            padding: 12px 16px;
            border-radius: 8px;
            margin-bottom: 20px;
            border: 1px solid #FECACA;
        }

        @media (max-width: 1024px) {
            aside {
                width: 200px;
            }
            main {
                margin-left: 200px;
            }
        }

        @media (max-width: 768px) {
            aside {
                transform: translateX(-100%);
                transition: transform 0.3s;
                width: 100%;
                z-index: 100;
            }
            aside.open {
                transform: translateX(0);
            }
            main {
                margin-left: 0;
                padding: 16px;
            }
        }
    </style>
</head>
<body>
    <aside>
        <div class="brand">
            <div class="brand-badge">BP</div>
            <div class="brand-text">
                <h1>BROPATCH</h1>
                <span>ADMIN</span>
            </div>
        </div>

        <nav class="nav-menu">
            <button class="nav-item active" data-page="dashboard">📊 Dashboard</button>
            
            <div class="nav-section-title">Management</div>
            <button class="nav-item" data-page="users">👤 Users</button>
            <button class="nav-item" data-page="providers">👷 Providers</button>
            <button class="nav-item" data-page="bookings">📑 Bookings</button>
            
            <div class="nav-section-title">Services</div>
            <button class="nav-item" data-page="services">🛠 Services</button>
            <button class="nav-item" data-page="categories">📂 Categories</button>
            <button class="nav-item" data-page="banners">🖼 Banners</button>
            
            <div class="nav-section-title">Marketplace</div>
            <button class="nav-item" data-page="coupons">🎟 Coupons</button>
            <button class="nav-item" data-page="reviews">⭐ Reviews</button>
            <button class="nav-item" data-page="disputes">⚖️ Disputes</button>
            
            <div class="nav-section-title">Finance</div>
            <button class="nav-item" data-page="payments">💳 Payments</button>
            <button class="nav-item" data-page="payouts">💰 Payouts</button>
            
            <div class="nav-section-title">Operations</div>
            <button class="nav-item" data-page="audit">📜 Audit Logs</button>
            <button class="nav-item" data-page="settings">⚙️ Settings</button>

            <button class="logout-btn" id="logoutBtn">🚪 Logout</button>
        </nav>
    </aside>

    <main>
        <header>
            <div class="header-title">
                <h2 id="pageTitle">Dashboard</h2>
                <p id="pageSubtitle">Real-time metrics from MySQL database</p>
            </div>
            <div class="admin-pill">
                <div class="admin-avatar" id="adminAvatar">AD</div>
                <div>
                    <div style="font-weight: 700; font-size: 0.85rem;" id="adminName">Admin</div>
                    <div style="font-size: 0.72rem; color: var(--success); font-weight: 600;">● Connected</div>
                </div>
            </div>
        </header>

        <div id="content-area">
            <!-- DASHBOARD PAGE -->
            <div id="page-dashboard" class="page-content">
                <div class="stats-grid" id="statsGrid">
                    <div class="stat-card primary">
                        <div class="label">Total Users</div>
                        <div class="value" id="totalUsers">-</div>
                        <div class="trend neutral">Active customers</div>
                    </div>
                    <div class="stat-card success">
                        <div class="label">Approved Providers</div>
                        <div class="value" id="approvedProviders">-</div>
                        <div class="trend neutral">Ready to work</div>
                    </div>
                    <div class="stat-card warning">
                        <div class="label">Pending Approvals</div>
                        <div class="value" id="pendingApprovals">-</div>
                        <div class="trend neutral">Under review</div>
                    </div>
                    <div class="stat-card info">
                        <div class="label">Today's Bookings</div>
                        <div class="value" id="todayBookings">-</div>
                        <div class="trend neutral">Service requests</div>
                    </div>
                    <div class="stat-card primary">
                        <div class="label">Gross Revenue</div>
                        <div class="value" id="grossRevenue">-</div>
                        <div class="trend up">↑ Completed orders</div>
                    </div>
                    <div class="stat-card success">
                        <div class="label">Platform Earnings</div>
                        <div class="value" id="platformEarnings">-</div>
                        <div class="trend neutral">Commission (15%)</div>
                    </div>
                    <div class="stat-card warning">
                        <div class="label">Pending Payouts</div>
                        <div class="value" id="pendingPayouts">-</div>
                        <div class="trend neutral">To be processed</div>
                    </div>
                    <div class="stat-card danger">
                        <div class="label">Open Disputes</div>
                        <div class="value" id="openDisputes">-</div>
                        <div class="trend neutral">Need resolution</div>
                    </div>
                </div>

                <div class="panel">
                    <div class="panel-header">
                        <h3>Recent Bookings</h3>
                        <button class="btn btn-outline btn-sm" onclick="loadDashboard()">🔄 Refresh</button>
                    </div>
                    <table id="recentBookingsTable">
                        <thead>
                            <tr>
                                <th>Booking ID</th>
                                <th>Customer</th>
                                <th>Service</th>
                                <th>Amount</th>
                                <th>Status</th>
                                <th>Date</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr><td colspan="6" class="loading">Loading bookings...</td></tr>
                        </tbody>
                    </table>
                </div>

                <div class="charts-grid">
                    <div class="chart-container">
                        <canvas id="statusChart"></canvas>
                    </div>
                    <div class="chart-container">
                        <canvas id="revenueChart"></canvas>
                    </div>
                </div>
            </div>

            <!-- USERS PAGE -->
            <div id="page-users" class="page-content" style="display:none;">
                <div class="panel">
                    <div class="panel-header">
                        <h3>User Management</h3>
                        <input type="text" id="usersSearch" placeholder="Search users..." style="padding:8px 12px; border:1px solid var(--border); border-radius:8px; width:200px;">
                    </div>
                    <table id="usersTable">
                        <thead>
                            <tr>
                                <th>Name</th>
                                <th>Email</th>
                                <th>Phone</th>
                                <th>Status</th>
                                <th>Joined</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr><td colspan="6" class="loading">Loading users...</td></tr>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- PROVIDERS PAGE -->
            <div id="page-providers" class="page-content" style="display:none;">
                <div class="panel">
                    <div class="panel-header">
                        <h3>Provider Management</h3>
                        <select id="providerStatusFilter" onchange="loadProviders()" style="padding:8px 12px; border:1px solid var(--border); border-radius:8px;">
                            <option value="">All Statuses</option>
                            <option value="pending">Pending</option>
                            <option value="approved">Approved</option>
                            <option value="rejected">Rejected</option>
                            <option value="suspended">Suspended</option>
                        </select>
                    </div>
                    <table id="providersTable">
                        <thead>
                            <tr>
                                <th>Name</th>
                                <th>Business</th>
                                <th>Experience</th>
                                <th>Status</th>
                                <th>Verification</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr><td colspan="6" class="loading">Loading providers...</td></tr>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- BOOKINGS PAGE -->
            <div id="page-bookings" class="page-content" style="display:none;">
                <div class="panel">
                    <div class="panel-header">
                        <h3>Booking Management</h3>
                        <select id="bookingStatusFilter" onchange="loadBookings()" style="padding:8px 12px; border:1px solid var(--border); border-radius:8px;">
                            <option value="">All Statuses</option>
                            <option value="pending">Pending</option>
                            <option value="searching_provider">Searching Provider</option>
                            <option value="provider_assigned">Provider Assigned</option>
                            <option value="completed">Completed</option>
                            <option value="cancelled">Cancelled</option>
                        </select>
                    </div>
                    <table id="bookingsTable">
                        <thead>
                            <tr>
                                <th>Booking ID</th>
                                <th>Customer</th>
                                <th>Service</th>
                                <th>Provider</th>
                                <th>Amount</th>
                                <th>Status</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr><td colspan="7" class="loading">Loading bookings...</td></tr>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- SERVICES PAGE -->
            <div id="page-services" class="page-content" style="display:none;">
                <div class="panel">
                    <div class="panel-header">
                        <h3>Service Management</h3>
                        <button class="btn btn-primary btn-sm" onclick="alert('Add service functionality coming soon')">+ Add Service</button>
                    </div>
                    <table id="servicesTable">
                        <thead>
                            <tr>
                                <th>Service Name</th>
                                <th>Category</th>
                                <th>Base Price</th>
                                <th>Duration</th>
                                <th>Status</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr><td colspan="6" class="loading">Loading services...</td></tr>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- DEFAULT PLACEHOLDER -->
            <div id="page-default" class="page-content">
                <div class="panel">
                    <div class="panel-header">
                        <h3>Coming Soon</h3>
                    </div>
                    <p style="text-align:center; padding:40px; color:var(--text-muted);">This section is being developed...</p>
                </div>
            </div>
        </div>
    </main>

    <script>
        const API_BASE = '/backend/admin/api.php';

        // Load admin session
        async function loadAdminSession() {
            try {
                const response = await fetch(`${API_BASE}?action=me`, {
                    credentials: 'include'
                });
                const data = await response.json();
                
                if (data.success) {
                    document.getElementById('adminName').textContent = data.admin.name;
                    document.getElementById('adminAvatar').textContent = data.admin.name.substring(0, 2).toUpperCase();
                } else {
                    window.location.href = '/backend/admin/login.php';
                }
            } catch (e) {
                console.error('Failed to load admin session:', e);
                window.location.href = '/backend/admin/login.php';
            }
        }

        // Load dashboard data
        async function loadDashboard() {
            try {
                const response = await fetch(`${API_BASE}?action=dashboard`, {
                    credentials: 'include'
                });
                const data = await response.json();

                if (!data.success) {
                    console.error('Failed to load dashboard');
                    return;
                }

                const m = data.metrics;
                document.getElementById('totalUsers').textContent = m.total_users;
                document.getElementById('approvedProviders').textContent = m.active_providers;
                document.getElementById('pendingApprovals').textContent = m.pending_approvals;
                document.getElementById('todayBookings').textContent = m.today_bookings;
                document.getElementById('grossRevenue').textContent = '₹' + m.gross_revenue.toLocaleString('en-IN');
                document.getElementById('platformEarnings').textContent = '₹' + m.platform_earnings.toLocaleString('en-IN');
                document.getElementById('pendingPayouts').textContent = '₹' + m.pending_payouts.toLocaleString('en-IN');
                document.getElementById('openDisputes').textContent = m.open_disputes;

                // Load recent bookings
                loadBookings();
            } catch (e) {
                console.error('Error loading dashboard:', e);
            }
        }

        // Load bookings
        async function loadBookings() {
            try {
                const status = document.getElementById('bookingStatusFilter')?.value || '';
                const response = await fetch(`${API_BASE}?action=bookings&limit=10${status ? '&status=' + status : ''}`, {
                    credentials: 'include'
                });
                const data = await response.json();

                if (!data.success) return;

                const tbody = document.getElementById('bookingsTable')?.querySelector('tbody') || 
                              document.getElementById('recentBookingsTable')?.querySelector('tbody');
                if (!tbody) return;

                tbody.innerHTML = data.bookings.map(b => `
                    <tr>
                        <td><strong>${b.booking_code}</strong></td>
                        <td>${b.customer_name}</td>
                        <td>${b.service_name}</td>
                        <td>${b.provider_id ? 'Assigned' : 'Unassigned'}</td>
                        <td>₹${parseFloat(b.final_amount).toLocaleString('en-IN')}</td>
                        <td><span class="badge badge-info">${b.status}</span></td>
                        <td><button class="btn btn-outline btn-sm" onclick="alert('Booking detail view coming soon')">View</button></td>
                    </tr>
                `).join('');
            } catch (e) {
                console.error('Error loading bookings:', e);
            }
        }

        // Load users
        async function loadUsers() {
            try {
                const response = await fetch(`${API_BASE}?action=users&limit=20`, {
                    credentials: 'include'
                });
                const data = await response.json();

                if (!data.success) return;

                const tbody = document.getElementById('usersTable').querySelector('tbody');
                tbody.innerHTML = data.users.map(u => `
                    <tr>
                        <td><strong>${u.name}</strong></td>
                        <td>${u.email}</td>
                        <td>${u.phone || '-'}</td>
                        <td><span class="badge ${u.status === 'active' ? 'badge-success' : 'badge-danger'}">${u.status}</span></td>
                        <td>${new Date(u.created_at).toLocaleDateString()}</td>
                        <td><button class="btn btn-outline btn-sm" onclick="alert('User actions coming soon')">Manage</button></td>
                    </tr>
                `).join('');
            } catch (e) {
                console.error('Error loading users:', e);
            }
        }

        // Load providers
        async function loadProviders() {
            try {
                const status = document.getElementById('providerStatusFilter')?.value || '';
                const response = await fetch(`${API_BASE}?action=providers${status ? '&status=' + status : ''}`, {
                    credentials: 'include'
                });
                const data = await response.json();

                if (!data.success) return;

                const tbody = document.getElementById('providersTable').querySelector('tbody');
                tbody.innerHTML = data.providers.map(p => `
                    <tr>
                        <td><strong>${p.name}</strong></td>
                        <td>${p.business_name || '-'}</td>
                        <td>${p.experience_years} years</td>
                        <td><span class="badge ${p.verification_status === 'approved' ? 'badge-success' : 'badge-warning'}">${p.verification_status}</span></td>
                        <td>${p.is_available ? '✓ Available' : '✗ Unavailable'}</td>
                        <td><button class="btn btn-outline btn-sm" onclick="alert('Provider actions coming soon')">Manage</button></td>
                    </tr>
                `).join('');
            } catch (e) {
                console.error('Error loading providers:', e);
            }
        }

        // Load services
        async function loadServices() {
            try {
                const response = await fetch(`${API_BASE}?action=services&limit=20`, {
                    credentials: 'include'
                });
                const data = await response.json();

                if (!data.success) return;

                const tbody = document.getElementById('servicesTable').querySelector('tbody');
                tbody.innerHTML = data.services.map(s => `
                    <tr>
                        <td><strong>${s.name}</strong></td>
                        <td>${s.category_name}</td>
                        <td>₹${parseFloat(s.base_price).toLocaleString('en-IN')}</td>
                        <td>${s.estimated_duration_mins} mins</td>
                        <td><span class="badge ${s.is_active ? 'badge-success' : 'badge-danger'}">${s.is_active ? 'Active' : 'Inactive'}</span></td>
                        <td><button class="btn btn-outline btn-sm" onclick="alert('Service actions coming soon')">Edit</button></td>
                    </tr>
                `).join('');
            } catch (e) {
                console.error('Error loading services:', e);
            }
        }

        // Page navigation
        document.querySelectorAll('.nav-item').forEach(item => {
            item.addEventListener('click', (e) => {
                const page = e.target.dataset.page;
                if (!page) return;

                document.querySelectorAll('.page-content').forEach(p => p.style.display = 'none');
                document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));

                e.target.classList.add('active');

                const pageEl = document.getElementById(`page-${page}`);
                if (pageEl) {
                    pageEl.style.display = 'block';
                    
                    // Update header
                    const titles = {
                        dashboard: 'Dashboard',
                        users: 'User Management',
                        providers: 'Provider Management',
                        bookings: 'Booking Management',
                        services: 'Service Management',
                        categories: 'Category Management',
                        banners: 'Banner Management',
                        coupons: 'Coupon Management',
                        reviews: 'Review Moderation',
                        disputes: 'Dispute Resolution',
                        payments: 'Payment Transactions',
                        payouts: 'Provider Payouts',
                        audit: 'Audit Logs',
                        settings: 'Platform Settings'
                    };
                    document.getElementById('pageTitle').textContent = titles[page] || 'Admin Panel';
                    document.getElementById('pageSubtitle').textContent = 'Real-time data from MySQL database';

                    // Load data
                    if (page === 'users') loadUsers();
                    if (page === 'providers') loadProviders();
                    if (page === 'bookings') loadBookings();
                    if (page === 'services') loadServices();
                }
            });
        });

        // Logout
        document.getElementById('logoutBtn').addEventListener('click', async () => {
            try {
                await fetch(`${API_BASE}?action=logout`, {
                    method: 'POST',
                    credentials: 'include'
                });
                window.location.href = '/backend/admin/login.php';
            } catch (e) {
                window.location.href = '/backend/admin/login.php';
            }
        });

        // Initialize
        loadAdminSession();
        loadDashboard();
    </script>
</body>
</html>
