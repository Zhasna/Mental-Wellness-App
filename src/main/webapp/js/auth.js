// Login form
document.getElementById('loginForm')?.addEventListener('submit', async e => {
    e.preventDefault();
    const email = e.target.email.value.trim();
    const password = e.target.password.value;

    // Basic client-side validation
    if (!email || !password) {
        alert('Please fill in all fields');
        return;
    }

    if (!email.includes('@') || !email.includes('.')) {
        alert('Please enter a valid email address');
        return;
    }

    try {
        const res = await fetch('/api/login', {
            method: 'POST',
            headers: {'Content-Type':'application/json'},
            body: JSON.stringify({email, password})
        });
        
        // Check if response is JSON
        const contentType = res.headers.get('content-type');
        if (!contentType || !contentType.includes('application/json')) {
            const text = await res.text();
            console.error('Non-JSON response:', text);
            console.error('Status:', res.status, res.statusText);
            alert('Server error (Status ' + res.status + '). Check console for details.');
            return;
        }
        
        const data = await res.json();
        
        if(res.ok){
            // Store user info in session storage
            sessionStorage.setItem('userId', data.userId);
            sessionStorage.setItem('username', data.username);
            sessionStorage.setItem('email', data.email);
            
            // Redirect to dashboard
            window.location.href = 'dashboard.html';
        } else {
            alert(data.message || 'Login failed');
        }
    } catch(err){
        console.error('Login error:', err);
        alert('Login failed: ' + err.message);
    }
});

// Register form
document.getElementById('registerForm')?.addEventListener('submit', async e => {
    e.preventDefault();
    
    // Debug: Log form data
    console.log('Form submitted');
    console.log('Form elements:', e.target.elements);
    
    // Get form data using multiple methods for debugging
    const name = e.target.name?.value?.trim() || document.getElementById('name')?.value?.trim() || '';
    const email = e.target.email?.value?.trim() || document.getElementById('email')?.value?.trim() || '';
    const password = e.target.password?.value || document.getElementById('password')?.value || '';
    const confirmPassword = e.target.confirmPassword?.value || document.getElementById('confirmPassword')?.value || '';

    // Debug: Log individual values
    console.log('Name:', name);
    console.log('Email:', email);
    console.log('Password:', password);
    console.log('Confirm Password:', confirmPassword);
    console.log('All form elements:', Array.from(e.target.elements).map(el => ({name: el.name, id: el.id, value: el.value})));

    // Client-side validation
    if (!name || !email || !password || !confirmPassword) {
        alert('Please fill in all fields');
        console.log('Validation failed - missing fields');
        console.log('Name empty:', !name);
        console.log('Email empty:', !email);
        console.log('Password empty:', !password);
        console.log('ConfirmPassword empty:', !confirmPassword);
        return;
    }

    if (name.length < 2 || name.length > 100) {
        alert('Name must be between 2 and 100 characters');
        return;
    }

    if (!email.includes('@') || !email.includes('.')) {
        alert('Please enter a valid email address');
        return;
    }

    if (password.length < 6) {
        alert('Password must be at least 6 characters');
        return;
    }

    if (password !== confirmPassword) {
        alert('Passwords do not match');
        return;
    }

    try {
        console.log('Sending registration request...');
        const res = await fetch('/api/register', {
            method: 'POST',
            headers: {'Content-Type':'application/json'},
            body: JSON.stringify({username: name, email, password})
        });
        
        // Check if response is JSON
        const contentType = res.headers.get('content-type');
        if (!contentType || !contentType.includes('application/json')) {
            const text = await res.text();
            console.error('Non-JSON response:', text);
            console.error('Status:', res.status, res.statusText);
            alert('Server error (Status ' + res.status + '). Check console for details.');
            return;
        }
        
        const data = await res.json();
        console.log('Registration response:', data);
        alert(data.message);

        if(res.ok){
            window.location.href = 'login.html'; // redirect after successful registration
        }
    } catch(err){
        console.error('Registration error:', err);
        alert('Registration failed: ' + err.message);
    }
});

// Logout function
function logout() {
    sessionStorage.clear();
    window.location.href = 'index.html';
}

// Check if user is logged in (for protected pages)
function checkAuth() {
    const userId = sessionStorage.getItem('userId');
    if (!userId) {
        window.location.href = 'login.html';
        return false;
    }
    return true;
}

// Display user info in navigation
function displayUserInfo() {
    const username = sessionStorage.getItem('username');
    const userNameElements = document.querySelectorAll('#userName');
    userNameElements.forEach(element => {
        if (element) {
            element.textContent = username || 'User';
        }
    });
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    displayUserInfo();
});

