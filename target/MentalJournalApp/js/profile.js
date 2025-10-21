document.addEventListener("DOMContentLoaded", async () => {
    if (!checkAuth()) return;
    try {
        // Load user profile (session-based)
        const response = await fetch(`/api/profile`);
        if (response.ok) {
            const user = await response.json();
            populateProfile(user);
        } else {
            console.error('Failed to load profile');
        }
    } catch (error) {
        console.error('Error loading profile:', error);
    }
    
    // Handle profile form submission
    const form = document.getElementById('profileForm');
    if (form) {
        form.addEventListener('submit', updateProfile);
    }
});

function populateProfile(user) {
    document.getElementById('profileName').value = user.name || '';
    document.getElementById('profileEmail').value = user.email || '';
    
    // Display user info
    const userInfoDiv = document.getElementById('userInfo');
    if (userInfoDiv) {
        userInfoDiv.innerHTML = `
            <h3>Welcome, ${user.name}!</h3>
            <p>Email: ${user.email}</p>
            <p>Member since: ${new Date(user.createdAt || Date.now()).toLocaleDateString()}</p>
        `;
    }
}

async function updateProfile(e) {
    e.preventDefault();
    
    const name = document.getElementById('profileName').value;
    const email = document.getElementById('profileEmail').value;
    const currentPassword = document.getElementById('currentPassword').value;
    const newPassword = document.getElementById('newPassword').value;
    const confirmNewPassword = document.getElementById('confirmNewPassword').value;
    
    // Validate password change if requested
    if (newPassword || confirmNewPassword) {
        if (!currentPassword) {
            alert('Current password is required to change password');
            return;
        }
        if (newPassword !== confirmNewPassword) {
            alert('New passwords do not match');
            return;
        }
        if (newPassword.length < 6) {
            alert('New password must be at least 6 characters');
            return;
        }
    }
    
    try {
        const updateData = { email: email, name: name };
        if (newPassword) {
            updateData.currentPassword = currentPassword;
            updateData.newPassword = newPassword;
        }
        
        const response = await fetch('/api/profile', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(updateData)
        });
        
        const data = await response.json();
        
        if (response.ok) {
            alert('Profile updated successfully!');
            // Update session storage
            sessionStorage.setItem('username', name);
            // Clear password fields
            document.getElementById('currentPassword').value = '';
            document.getElementById('newPassword').value = '';
            document.getElementById('confirmNewPassword').value = '';
        } else {
            alert('Error: ' + data.message);
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Failed to update profile. Please try again.');
    }
}
