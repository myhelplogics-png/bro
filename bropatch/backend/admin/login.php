<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bropatch Admin Login</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        :root {
            --primary: #1E3A8A;
            --primary-hover: #172554;
            --accent: #F59E0B;
            --bg: #F8FAFC;
            --surface: #FFFFFF;
            --text-main: #0F172A;
            --text-muted: #64748B;
            --border: #E2E8F0;
            --error: #EF4444;
            --success: #10B981;
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

        html, body {
            height: 100%;
            width: 100%;
        }

        body {
            background: linear-gradient(135deg, var(--primary) 0%, #0F1F3C 100%);
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
            padding: 20px;
        }

        .login-container {
            background: var(--surface);
            border-radius: var(--radius);
            box-shadow: var(--shadow-lg);
            width: 100%;
            max-width: 420px;
            padding: 40px;
        }

        .brand {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 32px;
            justify-content: center;
        }

        .brand-badge {
            background: linear-gradient(135deg, var(--primary), #3B82F6);
            color: white;
            width: 48px;
            height: 48px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 800;
            font-size: 1.5rem;
        }

        .brand-text h1 {
            font-size: 1.5rem;
            font-weight: 800;
            color: var(--primary);
            letter-spacing: -0.5px;
        }

        .brand-text p {
            font-size: 0.75rem;
            color: var(--text-muted);
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 1px;
            margin-top: 2px;
        }

        .header {
            margin-bottom: 28px;
            text-align: center;
        }

        .header h2 {
            font-size: 1.5rem;
            font-weight: 800;
            color: var(--text-main);
            margin-bottom: 6px;
        }

        .header p {
            color: var(--text-muted);
            font-size: 0.9rem;
        }

        .form-group {
            margin-bottom: 20px;
        }

        label {
            display: block;
            font-weight: 600;
            color: var(--text-main);
            margin-bottom: 8px;
            font-size: 0.9rem;
        }

        input[type="email"],
        input[type="password"] {
            width: 100%;
            padding: 12px 14px;
            border: 1px solid var(--border);
            border-radius: 8px;
            font-size: 0.95rem;
            transition: all 0.2s ease;
            background: var(--surface);
            color: var(--text-main);
        }

        input[type="email"]:focus,
        input[type="password"]:focus {
            outline: none;
            border-color: var(--primary);
            box-shadow: 0 0 0 3px rgba(30, 58, 138, 0.1);
        }

        .remember-forget {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin: 20px 0;
            font-size: 0.85rem;
        }

        .remember-forget a {
            color: var(--primary);
            text-decoration: none;
            font-weight: 600;
            transition: color 0.2s;
        }

        .remember-forget a:hover {
            color: var(--primary-hover);
        }

        .checkbox-group {
            display: flex;
            align-items: center;
            gap: 6px;
        }

        input[type="checkbox"] {
            width: 16px;
            height: 16px;
            cursor: pointer;
            accent-color: var(--primary);
        }

        .btn-login {
            width: 100%;
            padding: 12px;
            background: linear-gradient(135deg, var(--primary), #2D5AA8);
            color: white;
            border: none;
            border-radius: 8px;
            font-weight: 700;
            font-size: 0.95rem;
            cursor: pointer;
            transition: all 0.3s ease;
            margin-bottom: 16px;
        }

        .btn-login:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 12px rgba(30, 58, 138, 0.3);
        }

        .btn-login:active {
            transform: translateY(0);
        }

        .btn-login:disabled {
            opacity: 0.6;
            cursor: not-allowed;
            transform: none;
        }

        .alert {
            padding: 12px 14px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-size: 0.9rem;
            display: none;
        }

        .alert.error {
            background: #FEE2E2;
            color: #991B1B;
            border: 1px solid #FECACA;
            display: block;
        }

        .alert.success {
            background: #DCFCE7;
            color: #166534;
            border: 1px solid #BBF7D0;
            display: block;
        }

        .loading-spinner {
            display: none;
            width: 16px;
            height: 16px;
            border: 2px solid rgba(255, 255, 255, 0.3);
            border-top-color: white;
            border-radius: 50%;
            animation: spin 0.6s linear infinite;
            margin-right: 8px;
        }

        @keyframes spin {
            to { transform: rotate(360deg); }
        }

        .btn-login.loading {
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .divider {
            text-align: center;
            margin: 24px 0;
            font-size: 0.85rem;
            color: var(--text-muted);
        }

        .divider::before,
        .divider::after {
            content: '';
            display: inline-block;
            width: 40%;
            height: 1px;
            background: var(--border);
            vertical-align: middle;
        }

        .divider::before {
            margin-right: 10px;
        }

        .divider::after {
            margin-left: 10px;
        }

        .test-credentials {
            background: #EFF6FF;
            border: 1px solid #BFDBFE;
            border-radius: 8px;
            padding: 12px 14px;
            margin-bottom: 20px;
            font-size: 0.85rem;
            color: #1E40AF;
        }

        .test-credentials strong {
            display: block;
            margin-bottom: 6px;
            font-weight: 700;
        }

        .test-credentials code {
            background: white;
            padding: 2px 6px;
            border-radius: 4px;
            font-family: 'Courier New', monospace;
            font-weight: 600;
        }

        .footer {
            text-align: center;
            margin-top: 24px;
            padding-top: 20px;
            border-top: 1px solid var(--border);
            font-size: 0.8rem;
            color: var(--text-muted);
        }

        .footer a {
            color: var(--primary);
            text-decoration: none;
            font-weight: 600;
        }

        @media (max-width: 480px) {
            .login-container {
                padding: 32px 20px;
            }

            .header h2 {
                font-size: 1.25rem;
            }
        }
    </style>
</head>
<body>
    <div class="login-container">
        <div class="brand">
            <div class="brand-badge">BP</div>
            <div class="brand-text">
                <h1>BROPATCH</h1>
                <p>Admin Panel</p>
            </div>
        </div>

        <div class="header">
            <h2>Admin Login</h2>
            <p>Access the operations command center</p>
        </div>

        <div id="alert-container"></div>

        <div class="test-credentials">
            <strong>Demo Credentials:</strong>
            Email: <code>admin@bropatch.com</code><br>
            Password: <code>Admin@Bropatch2026!</code>
        </div>

        <form id="loginForm">
            <div class="form-group">
                <label for="email">Email Address</label>
                <input 
                    type="email" 
                    id="email" 
                    name="email" 
                    placeholder="admin@bropatch.com"
                    required 
                    autocomplete="email"
                >
            </div>

            <div class="form-group">
                <label for="password">Password</label>
                <input 
                    type="password" 
                    id="password" 
                    name="password" 
                    placeholder="••••••••"
                    required 
                    autocomplete="current-password"
                >
            </div>

            <div class="remember-forget">
                <div class="checkbox-group">
                    <input type="checkbox" id="remember" name="remember">
                    <label for="remember" style="margin-bottom: 0;">Remember me</label>
                </div>
                <a href="#" onclick="alert('Contact support to reset your password'); return false;">Forgot password?</a>
            </div>

            <button type="submit" class="btn-login" id="loginBtn">
                <span class="loading-spinner" id="spinner"></span>
                Sign In
            </button>
        </form>

        <div class="footer">
            Bropatch Operations Platform • <a href="https://bropatch.com" target="_blank">Learn More</a>
        </div>
    </div>

    <script>
        const loginForm = document.getElementById('loginForm');
        const emailInput = document.getElementById('email');
        const passwordInput = document.getElementById('password');
        const loginBtn = document.getElementById('loginBtn');
        const spinner = document.getElementById('spinner');
        const alertContainer = document.getElementById('alert-container');

        // Load email if "Remember me" was checked
        window.addEventListener('load', () => {
            const savedEmail = localStorage.getItem('adminEmail');
            const rememberCheckbox = document.getElementById('remember');
            if (savedEmail) {
                emailInput.value = savedEmail;
                rememberCheckbox.checked = true;
            }
        });

        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const email = emailInput.value.trim();
            const password = passwordInput.value;
            const remember = document.getElementById('remember').checked;

            if (!email || !password) {
                showAlert('Please enter both email and password', 'error');
                return;
            }

            // Show loading state
            loginBtn.disabled = true;
            spinner.style.display = 'inline-block';

            try {
                const response = await fetch('/backend/admin/api.php?action=login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    credentials: 'include', // Include cookies
                    body: JSON.stringify({ email, password })
                });

                const data = await response.json();

                if (!response.ok || !data.success) {
                    showAlert(data.message || 'Login failed. Please try again.', 'error');
                    loginBtn.disabled = false;
                    spinner.style.display = 'none';
                    return;
                }

                // Save email if remember me is checked
                if (remember) {
                    localStorage.setItem('adminEmail', email);
                } else {
                    localStorage.removeItem('adminEmail');
                }

                // Login successful
                showAlert('Login successful! Redirecting...', 'success');
                
                // Redirect to dashboard after 1.5 seconds
                setTimeout(() => {
                    window.location.href = '/backend/admin/dashboard.php';
                }, 1500);

            } catch (error) {
                console.error('Login error:', error);
                showAlert('Network error. Please check your connection and try again.', 'error');
                loginBtn.disabled = false;
                spinner.style.display = 'none';
            }
        });

        function showAlert(message, type) {
            const alertDiv = document.createElement('div');
            alertDiv.className = `alert ${type}`;
            alertDiv.textContent = message;
            alertContainer.innerHTML = '';
            alertContainer.appendChild(alertDiv);

            if (type === 'error') {
                setTimeout(() => {
                    alertDiv.remove();
                }, 5000);
            }
        }

        // Real-time validation
        emailInput.addEventListener('blur', () => {
            if (emailInput.value && !emailInput.validity.valid) {
                showAlert('Please enter a valid email address', 'error');
            }
        });
    </script>
</body>
</html>
