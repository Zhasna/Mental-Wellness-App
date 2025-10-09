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
        const res = await fetch('/MentalJournalApp/api/login', {
            method: 'POST',
            headers: {'Content-Type':'application/json'},
            body: JSON.stringify({email, password})
        });
        const data = await res.json();
        
        if(res.ok){
            // Store user session info directly from response
            sessionStorage.setItem('userId', data.userId);
            sessionStorage.setItem('username', data.username);
            sessionStorage.setItem('email', data.email);
            
            console.log('Login successful, stored in sessionStorage:', {
                userId: data.userId,
                username: data.username,
                email: data.email
            });
            
            alert(data.message);
            window.location.href = 'dashboard.html';
        } else {
            alert(data.message);
        }
    } catch(err){
        console.error(err);
        alert('Login failed. Please try again.');
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
        const res = await fetch('/MentalJournalApp/api/register', {
            method: 'POST',
            headers: {'Content-Type':'application/json'},
            body: JSON.stringify({username: name, email, password})
        });
        const data = await res.json();
        console.log('Registration response:', data);
        alert(data.message);

        if(res.ok){
            window.location.href = 'login.html'; // redirect after successful registration
        }
    } catch(err){
        console.error('Registration error:', err);
        alert('Registration failed. Please try again.');
    }
});

// Logout function
function logout() {
    sessionStorage.clear();
    window.location.href = 'index.html';
}

// Check if user is logged in (for protected pages)
async function checkAuth() {
    const userId = sessionStorage.getItem('userId');
    console.log('checkAuth() called - userId:', userId);
    
    if (!userId) {
        console.log('No userId found, redirecting to login');
        window.location.href = 'login.html';
        return false;
    }
    
    // Test server-side session by making a request to a protected endpoint
    try {
        const response = await fetch('/MentalJournalApp/api/goals', {
            method: 'GET',
            credentials: 'include' // Include cookies for session
        });
        
        if (response.status === 401) {
            console.log('Server session invalid, redirecting to login');
            sessionStorage.clear();
            window.location.href = 'login.html';
            return false;
        }
        
        console.log('Server session valid, authentication successful');
        return true;
    } catch (error) {
        console.error('Session validation error:', error);
        // On error, assume session is invalid
        sessionStorage.clear();
        window.location.href = 'login.html';
        return false;
    }
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

