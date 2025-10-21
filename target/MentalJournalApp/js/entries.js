let isEditMode = false; // Track if we're in edit mode

document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("journalForm");
    
    // Check authentication
    if (!checkAuth()) {
        return;
    }
    
    // Get user ID from session storage
    // server session holds user

    if (form) {
        form.addEventListener("submit", async (e) => {
            e.preventDefault();

            // If in edit mode, don't run this handler - let the edit handler run instead
            if (isEditMode) {
                return;
            }

            // Debug: Log form elements
            console.log('Form submitted');
            console.log('Form elements:', e.target.elements);

            const date = document.getElementById("date")?.value || '';
            const mood = document.getElementById("mood")?.value || '';
            const entry = document.getElementById("entry")?.value || '';

            // Debug: Log individual values
            console.log('Date:', date);
            console.log('Mood:', mood);
            console.log('Entry:', entry);

            // Client-side validation
            if (!date || !mood || !entry.trim()) {
                alert('Please fill in all fields');
                console.log('Validation failed - missing fields');
                console.log('Date empty:', !date);
                console.log('Mood empty:', !mood);
                console.log('Entry empty:', !entry.trim());
                return;
            }

            if (entry.trim().length === 0) {
                alert('Entry content cannot be empty');
                return;
            }

            try {
                const response = await fetch('/api/entries', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        date: date,
                        mood: mood,
                        content: entry.trim()
                    })
                });

                // Check if response is JSON
                const contentType = response.headers.get('content-type');
                if (!contentType || !contentType.includes('application/json')) {
                    const text = await response.text();
                    console.error('Non-JSON response:', text);
                    console.error('Status:', response.status, response.statusText);
                    alert('Server error (Status ' + response.status + '). Check console for details.');
                    return;
                }

                const data = await response.json();
                
                if (response.ok) {
                    alert("Journal entry saved successfully!");
                    form.reset();
                    loadUserEntries(); // Reload entries
                } else {
                    alert("Error: " + data.message);
                }
            } catch (error) {
                console.error('Error:', error);
                alert('Failed to save entry. Please try again.');
            }
        });
    }

    // Load existing entries for the user
    loadUserEntries();
});

async function loadUserEntries() {
    try {
        const response = await fetch(`/api/entries`);
        if (response.ok) {
            let entries = await response.json();
            // Exclude gratitude-tagged notes from regular entries
            entries = entries.filter(e => !String(e.content || '').trim().toLowerCase().startsWith('[gratitude]'));
            displayEntries(entries);
        } else {
            console.error('Failed to load entries');
        }
    } catch (error) {
        console.error('Error loading entries:', error);
    }
}

function displayEntries(entries) {
    const entriesContainer = document.getElementById('entriesContainer');
    if (!entriesContainer) return;

    entriesContainer.innerHTML = '';
    
    if (entries.length === 0) {
        entriesContainer.innerHTML = '<p class="no-entries">No entries yet. Create your first entry above!</p>';
        return;
    }
    
    entries.forEach(entry => {
        const entryDiv = document.createElement('div');
        entryDiv.className = 'entry-card';
        entryDiv.innerHTML = `
            <div class="entry-header">
                <span class="entry-date">${entry.entryDate || entry.date}</span>
                <span class="entry-mood">${getMoodEmoji(entry.mood)}</span>
            </div>
            <div class="entry-content">${escapeHtml(entry.content)}</div>
            <div class="entry-actions">
                <button class="edit-btn" onclick="editEntry(${entry.id}, '${(entry.entryDate || entry.date)}', '${entry.mood}', \`${escapeForAttribute(entry.content)}\`)">✏️ Edit</button>
                <button class="delete-btn" onclick="deleteEntry(${entry.id})">🗑️ Delete</button>
            </div>
        `;
        entriesContainer.appendChild(entryDiv);
    });
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function escapeForAttribute(text) {
    return text.replace(/\\/g, '\\\\').replace(/`/g, '\\`').replace(/\$/g, '\\$').replace(/\n/g, '\\n');
}

async function editEntry(id, date, mood, content) {
    // Set edit mode flag
    isEditMode = true;
    
    // Populate the form with the entry data
    document.getElementById('date').value = date;
    document.getElementById('mood').value = mood;
    document.getElementById('entry').value = content;
    
    // Change the form submission behavior
    const form = document.getElementById('journalForm');
    const submitBtn = form.querySelector('button[type="submit"]');
    const originalText = submitBtn.textContent;
    submitBtn.textContent = 'Update Entry';
    submitBtn.style.background = 'linear-gradient(135deg, #FFA07A 0%, #FF8C69 100%)';
    
    // Create cancel button if it doesn't exist
    let cancelBtn = document.getElementById('cancelEditBtn');
    if (!cancelBtn) {
        cancelBtn = document.createElement('button');
        cancelBtn.id = 'cancelEditBtn';
        cancelBtn.type = 'button';
        cancelBtn.textContent = 'Cancel';
        cancelBtn.className = 'cancel-btn';
        submitBtn.parentNode.appendChild(cancelBtn);
    }
    
    cancelBtn.onclick = () => {
        isEditMode = false; // Reset edit mode
        form.reset();
        submitBtn.textContent = originalText;
        submitBtn.style.background = '';
        cancelBtn.remove();
        form.onsubmit = null;
    };
    
    // Override form submission
    form.onsubmit = async (e) => {
        e.preventDefault();
        
        const updatedDate = document.getElementById('date').value;
        const updatedMood = document.getElementById('mood').value;
        const updatedContent = document.getElementById('entry').value;
        
        if (!updatedDate || !updatedMood || !updatedContent.trim()) {
            alert('Please fill in all fields');
            return;
        }
        
        try {
            const response = await fetch('/api/entries', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    entryId: id.toString(),
                    date: updatedDate,
                    mood: updatedMood,
                    content: updatedContent.trim()
                })
            });
            
            const data = await response.json();
            
            if (response.ok) {
                alert('Entry updated successfully!');
                isEditMode = false; // Reset edit mode
                form.reset();
                submitBtn.textContent = originalText;
                submitBtn.style.background = '';
                cancelBtn.remove();
                form.onsubmit = null;
                loadUserEntries();
            } else {
                alert('Error: ' + data.message);
            }
        } catch (error) {
            console.error('Error:', error);
            alert('Failed to update entry. Please try again.');
        }
    };
    
    // Scroll to form
    form.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

async function deleteEntry(id) {
    if (!confirm('Are you sure you want to delete this entry? This action cannot be undone.')) {
        return;
    }
    
    try {
        const response = await fetch(`/api/entries?entryId=${id}`, {
            method: 'DELETE'
        });
        
        const data = await response.json();
        
        if (response.ok) {
            alert('Entry deleted successfully!');
            loadUserEntries();
        } else {
            alert('Error: ' + data.message);
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Failed to delete entry. Please try again.');
    }
}

function getMoodEmoji(mood) {
    const moodEmojis = {
        'happy': '😊',
        'sad': '😢',
        'angry': '😠',
        'anxious': '😰',
        'calm': '😌',
        'tired': '😴',
        'neutral': '😐',
        'excited': '🤩',
        'frustrated': '😤',
        'peaceful': '🕊️'
    };
    return moodEmojis[mood.toLowerCase()] || '😐';
}


