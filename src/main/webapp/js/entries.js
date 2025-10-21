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
                const response = await fetch('/MentalJournalApp/api/entries', {
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
        const response = await fetch(`/MentalJournalApp/api/entries`);
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
            <div class="entry-content">${entry.content}</div>
        `;
        entriesContainer.appendChild(entryDiv);
    });
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


