const wrapper = document.querySelector('.wrapper')
const registerLink = document.querySelector('.register-link')
const loginLink = document.querySelector('.login-link')

const API_BASE = 'http://localhost:8080';

registerLink.onclick = () => {
    wrapper.classList.add('active')
}

loginLink.onclick = () => {
    wrapper.classList.remove('active')
}

// Helper: get CSRF token from cookie
function getCsrfToken() {
    const cookies = document.cookie.split(';');
    for (let c of cookies) {
        const [name, value] = c.trim().split('=');
        if (name === 'XSRF-TOKEN') {
            return decodeURIComponent(value);
        }
    }
    return null;
}

// Helper: fetch CSRF token first
async function fetchCsrfToken() {
    try {
        await fetch(`${API_BASE}/api/auth/login`, {
            method: 'OPTIONS',
            credentials: 'include'
        });
    } catch (e) {
        // Server may not be running
    }
}

// Login form handler
document.getElementById('loginForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const username = document.getElementById('loginUsername').value.trim();
    const password = document.getElementById('loginPassword').value;
    const errorDiv = document.getElementById('loginError');

    errorDiv.style.display = 'none';

    if (username.length < 3) {
        errorDiv.textContent = 'Please enter a valid username or email.';
        errorDiv.style.display = 'block';
        return;
    }
    if (password.length < 8) {
        errorDiv.textContent = 'Password must be at least 8 characters.';
        errorDiv.style.display = 'block';
        return;
    }

    try {
        await fetchCsrfToken();
        const csrfToken = getCsrfToken();

        const response = await fetch(`${API_BASE}/api/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...(csrfToken ? { 'X-XSRF-TOKEN': csrfToken } : {})
            },
            credentials: 'include',
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (!response.ok) {
            errorDiv.textContent = data.error || 'Login failed. Please try again.';
            errorDiv.style.display = 'block';
            return;
        }

        // Store session info
        sessionStorage.setItem('userId', data.userId);
        sessionStorage.setItem('username', data.username);

        window.location.href = 'dashboard.html';
    } catch (error) {
        errorDiv.textContent = 'Cannot connect to server. Please make sure the backend is running.';
        errorDiv.style.display = 'block';
    }
});

// Register form handler
document.getElementById('registerForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const username = document.getElementById('regUsername').value.trim();
    const email = document.getElementById('regEmail').value.trim();
    const password = document.getElementById('regPassword').value;
    const errorDiv = document.getElementById('registerError');
    const successDiv = document.getElementById('registerSuccess');

    errorDiv.style.display = 'none';
    successDiv.style.display = 'none';

    if (username.length < 3) {
        errorDiv.textContent = 'Username must be at least 3 characters.';
        errorDiv.style.display = 'block';
        return;
    }
    if (username.length > 50) {
        errorDiv.textContent = 'Username must not exceed 50 characters.';
        errorDiv.style.display = 'block';
        return;
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        errorDiv.textContent = 'Please enter a valid email address.';
        errorDiv.style.display = 'block';
        return;
    }
    if (password.length < 8) {
        errorDiv.textContent = 'Password must be at least 8 characters.';
        errorDiv.style.display = 'block';
        return;
    }

    try {
        await fetchCsrfToken();
        const csrfToken = getCsrfToken();

        const response = await fetch(`${API_BASE}/api/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...(csrfToken ? { 'X-XSRF-TOKEN': csrfToken } : {})
            },
            credentials: 'include',
            body: JSON.stringify({ username, email, password })
        });

        const data = await response.json();

        if (!response.ok) {
            errorDiv.textContent = data.error || 'Registration failed. Please try again.';
            errorDiv.style.display = 'block';
            return;
        }

        successDiv.textContent = 'Account created successfully! Please login.';
        successDiv.style.display = 'block';

        // Clear password field
        document.getElementById('regPassword').value = '';

        // Switch to login form after a delay
        setTimeout(() => {
            wrapper.classList.remove('active');
        }, 1500);
    } catch (error) {
        errorDiv.textContent = 'Cannot connect to server. Please make sure the backend is running.';
        errorDiv.style.display = 'block';
    }
});
