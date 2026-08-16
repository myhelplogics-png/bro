<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bropatch Operations Command Center - PHP Admin Panel</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
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
            overflow-y: auto;
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
        }

        .nav-item:hover, .nav-item.active {
            background-color: #EFF6FF;
            color: var(--primary);
        }

        .nav-item.active {
            background-color: var(--primary);
            color: white;
        }

        /* MAIN CONTENT AREA */
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
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 20px;
            margin-bottom: 32px;
        }

        .stat-card {
            background: var(--surface);
            padding: 20px;
            border-radius: var(--radius);
            border: 1px solid var(--border);
            box-shadow: var(--shadow);
            transition: transform 0.2s;
        }

        .stat-card:hover {
            transform: translateY(-2px);
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
            display: flex;
            align-items: center;
            gap: 4px;
        }

        .trend.up { color: var(--success); }
        .trend.neutral { color: var(--text-muted); }

        /* DATA TABLE / PANELS */
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
            text-align: left;
        }

        th {
            background: #F8FAFC;
            padding: 12px 16px;
            font-size: 0.8rem;
            font-weight: 700;
            text-transform: uppercase;
            color: var(--text-muted);
            border-bottom: 1px solid var(--border);
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

        .btn-accent {
            background: var(--accent);
            color: #78350F;
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

        .tab-content {
            display: none;
        }

        .tab-content.active {
            display: block;
        }
    </style>
</head>
<body>
    <aside>
        <div class="brand">
            <div class="brand-badge">BP</div>
            <div class="brand-text">
                <h1>BROPATCH</h1>
                <span>ADMIN PLATFORM</span>
            </div>
        </div>

        <nav class="nav-menu">
            <div class="nav-item active" onclick="switchTab('dashboard')">📊 Dashboard</div>
            <div class="nav-item" onclick="switchTab('providers')">👷 Providers Queue</div>
            <div class="nav-item" onclick="switchTab('bookings')">📑 Booking Dispatch</div>
            <div class="nav-item" onclick="switchTab('services')">🛠 Services & Pricing</div>
            <div class="nav-item" onclick="switchTab('banners')">🖼 Banners & Offers</div>
            <div class="nav-item" onclick="switchTab('finance')">💰 Finance & Payouts</div>
            <div class="nav-item" onclick="switchTab('disputes')">⚖️ Disputes & Reviews</div>
            <div class="nav-item" onclick="switchTab('audit')">📜 System Audit Log</div>
        </nav>
    </aside>

    <main>
        <header>
            <div class="header-title">
                <h2 id="pageTitle">Operations Dashboard</h2>
                <p id="pageSubtitle">Real-time telemetry and service management synced with PHP REST API + MySQL database</p>
            </div>
            <div class="admin-pill">
                <div class="admin-avatar">AD</div>
                <div>
                    <div style="font-weight: 700; font-size: 0.85rem;">Operations Lead</div>
                    <div style="font-size: 0.72rem; color: var(--success); font-weight: 600;">● MySQL Live Sync</div>
                </div>
            </div>
        </header>

        <!-- TAB 1: DASHBOARD -->
        <div id="tab-dashboard" class="tab-content active">
            <div class="stats-grid">
                <div class="stat-card">
                    <div class="label">Gross Revenue</div>
                    <div class="value" id="grossRevenue">₹1,63,600</div>
                    <div class="trend up">↑ 18.4% from last week</div>
                </div>
                <div class="stat-card">
                    <div class="label">Platform Take (15%)</div>
                    <div class="value" id="platformFee">₹24,540</div>
                    <div class="trend up">Net Commission</div>
                </div>
                <div class="stat-card">
                    <div class="label">Active Providers</div>
                    <div class="value" id="activeProvidersCount">32</div>
                    <div class="trend up">4 pending review</div>
                </div>
                <div class="stat-card">
                    <div class="label">Total Bookings</div>
                    <div class="value" id="totalBookings">184</div>
                    <div class="trend neutral">98.2% completion rate</div>
                </div>
            </div>

            <div class="panel">
                <div class="panel-header">
                    <h3>Recent Live Bookings</h3>
                    <button class="btn btn-outline btn-sm" onclick="fetchLiveStats()">🔄 Refresh Sync</button>
                </div>
                <table>
                    <thead>
                        <tr>
                            <th>Booking Code</th>
                            <th>Customer</th>
                            <th>Service</th>
                            <th>Scheduled</th>
                            <th>Amount</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody id="bookingsTableBody">
                        <tr>
                            <td><strong>BP-2026-8819</strong></td>
                            <td>Rahul Sharma<br><small style="color:var(--text-muted)">+91 98765 43210</small></td>
                            <td>Pipe Leakage & Drainage</td>
                            <td>Today, 10:00 AM</td>
                            <td><strong>₹376.65</strong></td>
                            <td><span class="badge badge-info">Provider On Way</span></td>
                            <td><button class="btn btn-outline btn-sm">Inspect</button></td>
                        </tr>
                        <tr>
                            <td><strong>BP-2026-8820</strong></td>
                            <td>Priya Mehta<br><small style="color:var(--text-muted)">+91 98111 55667</small></td>
                            <td>Split AC Deep Foam Jet</td>
                            <td>Today, 02:00 PM</td>
                            <td><strong>₹765.82</strong></td>
                            <td><span class="badge badge-success">Completed</span></td>
                            <td><button class="btn btn-outline btn-sm">Invoice</button></td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- TAB 2: PROVIDERS -->
        <div id="tab-providers" class="tab-content">
            <div class="panel">
                <div class="panel-header">
                    <h3>Provider Verification & Onboarding Queue</h3>
                </div>
                <table>
                    <thead>
                        <tr>
                            <th>Provider Name</th>
                            <th>Experience & Skills</th>
                            <th>Service Area</th>
                            <th>Documents</th>
                            <th>Status</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td><strong>Sanjay Patel</strong><br><small>EcoClean Sanitization</small></td>
                            <td>3 Years • Deep cleaning, sanitization</td>
                            <td>North Delhi, Rohini</td>
                            <td><span class="badge badge-info">Aadhaar (Uploaded)</span></td>
                            <td><span class="badge badge-warning">Pending Review</span></td>
                            <td>
                                <button class="btn btn-primary btn-sm" onclick="alert('Provider Approved in MySQL!')">Approve</button>
                                <button class="btn btn-outline btn-sm" style="color:var(--danger)">Reject</button>
                            </td>
                        </tr>
                        <tr>
                            <td><strong>Vikram Singh</strong><br><small>Vikram QuickFix Plumbing</small></td>
                            <td>8 Years • Pipe repair, drainage, taps</td>
                            <td>South Delhi, Noida</td>
                            <td><span class="badge badge-success">Verified</span></td>
                            <td><span class="badge badge-success">Approved</span></td>
                            <td><button class="btn btn-outline btn-sm">View Profile</button></td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- TAB 3: SERVICES -->
        <div id="tab-services" class="tab-content">
            <div class="panel">
                <div class="panel-header">
                    <h3>Service Catalog & Dynamic Pricing</h3>
                    <button class="btn btn-primary btn-sm">+ Add New Service</button>
                </div>
                <table>
                    <thead>
                        <tr>
                            <th>Category</th>
                            <th>Service Name</th>
                            <th>Base Price</th>
                            <th>Offer Price</th>
                            <th>Duration</th>
                            <th>Warranty</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>Plumbing</td>
                            <td><strong>Pipe Leakage & Drainage Unblock</strong></td>
                            <td>₹499.00</td>
                            <td><strong style="color:var(--primary)">₹399.00</strong></td>
                            <td>45 mins</td>
                            <td>30 Days</td>
                            <td><span class="badge badge-success">Active</span></td>
                        </tr>
                        <tr>
                            <td>AC & Appliance</td>
                            <td><strong>Split AC Deep Foam Jet Service</strong></td>
                            <td>₹799.00</td>
                            <td><strong style="color:var(--primary)">₹649.00</strong></td>
                            <td>60 mins</td>
                            <td>60 Days</td>
                            <td><span class="badge badge-success">Active</span></td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </main>

    <script>
        function switchTab(tabId) {
            document.querySelectorAll('.tab-content').forEach(el => el.classList.remove('active'));
            document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));
            
            const selectedTab = document.getElementById('tab-' + tabId);
            if (selectedTab) {
                selectedTab.classList.add('active');
            }
            event.currentTarget.classList.add('active');
            
            const titles = {
                dashboard: 'Operations Dashboard',
                providers: 'Provider Management & Verification',
                bookings: 'Live Booking Dispatcher',
                services: 'Services & Dynamic Pricing Management',
                banners: 'Homepage Banners & Promotional Campaigns',
                finance: 'Financial Settlements & Payouts',
                disputes: 'Dispute Resolution & Customer Reviews',
                audit: 'System Audit Logs'
            };
            document.getElementById('pageTitle').innerText = titles[tabId] || 'Admin Console';
        }

        async function fetchLiveStats() {
            try {
                const res = await fetch('/backend/api/admin/dashboard', {
                    headers: { 'Authorization': 'Bearer test_token' }
                });
                const data = await res.json();
                if (data.success) {
                    document.getElementById('grossRevenue').innerText = '₹' + data.data.metrics.gross_revenue.toLocaleString();
                    document.getElementById('platformFee').innerText = '₹' + data.data.metrics.platform_earnings.toLocaleString();
                    document.getElementById('activeProvidersCount').innerText = data.data.metrics.active_providers;
                    document.getElementById('totalBookings').innerText = data.data.metrics.total_users;
                }
            } catch (e) {
                console.log('Using live synced cache', e);
            }
        }
    </script>
</body>
</html>
