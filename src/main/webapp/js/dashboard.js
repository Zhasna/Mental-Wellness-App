document.addEventListener("DOMContentLoaded", async () => {
    // Get user ID from session storage
    const userId = sessionStorage.getItem('userId') || '1'; // Default to 1 for testing
    
    try {
        // Load dashboard statistics
        const response = await fetch(`/MentalJournalApp/api/stats?userId=${userId}`);
        if (response.ok) {
            const stats = await response.json();
            
            document.getElementById("totalEntries").textContent = stats.totalEntries || 0;
            document.getElementById("currentMood").textContent = stats.currentMood || "😐";
            document.getElementById("goalsProgress").textContent = stats.goalsProgress + "%" || "0%";
            
            // Update additional stats if elements exist
            const totalGoalsElement = document.getElementById("totalGoals");
            if (totalGoalsElement) {
                totalGoalsElement.textContent = stats.totalGoals || 0;
            }
            
            const completedGoalsElement = document.getElementById("completedGoals");
            if (completedGoalsElement) {
                completedGoalsElement.textContent = stats.completedGoals || 0;
            }
            
        } else {
            console.error('Failed to load dashboard stats');
        }
        
        // Load recent entries
        await loadRecentEntries(userId);
        
        // Load recent goals
        await loadRecentGoals(userId);
        
    } catch (error) {
        console.error('Error loading dashboard data:', error);
    }
});

async function loadRecentEntries(userId) {
    try {
        const response = await fetch(`/MentalJournalApp/api/entries?userId=${userId}`);
        if (response.ok) {
            const entries = await response.json();
            const recentEntries = entries.slice(0, 5); // Get last 5 entries
            displayRecentEntries(recentEntries);
        }
    } catch (error) {
        console.error('Error loading recent entries:', error);
    }
}

async function loadRecentGoals(userId) {
    try {
        const response = await fetch(`/MentalJournalApp/api/goals?userId=${userId}`);
        if (response.ok) {
            const goals = await response.json();
            const recentGoals = goals.slice(0, 3); // Get last 3 goals
            displayRecentGoals(recentGoals);
        }
    } catch (error) {
        console.error('Error loading recent goals:', error);
    }
}

function displayRecentEntries(entries) {
    const container = document.getElementById('recentEntries');
    if (!container) return;

    container.innerHTML = '<h3>Recent Entries</h3>';
    
    if (entries.length === 0) {
        container.innerHTML += '<p>No entries yet. <a href="entries.html">Create your first entry!</a></p>';
        return;
    }
    
    entries.forEach(entry => {
        const entryDiv = document.createElement('div');
        entryDiv.className = 'recent-entry';
        entryDiv.innerHTML = `
            <div class="entry-summary">
                <span class="entry-date">${entry.date}</span>
                <span class="entry-mood">${getMoodEmoji(entry.mood)}</span>
            </div>
            <div class="entry-preview">${entry.content.substring(0, 100)}${entry.content.length > 100 ? '...' : ''}</div>
        `;
        container.appendChild(entryDiv);
    });
}

function displayRecentGoals(goals) {
    const container = document.getElementById('recentGoals');
    if (!container) return;

    container.innerHTML = '<h3>Recent Goals</h3>';
    
    if (goals.length === 0) {
        container.innerHTML += '<p>No goals set. <a href="goals.html">Set your first goal!</a></p>';
        return;
    }
    
    goals.forEach(goal => {
        const goalDiv = document.createElement('div');
        goalDiv.className = 'recent-goal';
        goalDiv.innerHTML = `
            <div class="goal-title ${goal.completed ? 'completed' : ''}">${goal.title}</div>
            <div class="goal-status">${goal.completed ? '✅ Completed' : '🎯 In Progress'}</div>
        `;
        container.appendChild(goalDiv);
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
        'neutral': '😐'
    };
    return moodEmojis[mood.toLowerCase()] || '😐';
}

