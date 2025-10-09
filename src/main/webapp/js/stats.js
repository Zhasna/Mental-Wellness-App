document.addEventListener("DOMContentLoaded", async () => {
    const userId = sessionStorage.getItem('userId') || '1';
    
    try {
        // Load detailed statistics
        const response = await fetch(`/MentalJournalApp/api/stats?userId=${userId}`);
        if (response.ok) {
            const stats = await response.json();
            displayStats(stats);
            
            // Load mood history for chart
            await loadMoodHistory(userId);
        } else {
            console.error('Failed to load stats');
        }
    } catch (error) {
        console.error('Error loading stats:', error);
    }
});

function displayStats(stats) {
    // Update basic stats
    document.getElementById("totalEntries").textContent = stats.totalEntries || 0;
    document.getElementById("totalGoals").textContent = stats.totalGoals || 0;
    document.getElementById("completedGoals").textContent = stats.completedGoals || 0;
    document.getElementById("goalsProgress").textContent = stats.goalsProgress + "%" || "0%";
    document.getElementById("currentMood").textContent = stats.currentMood || "😐";
    
    // Display mood distribution
    displayMoodDistribution(stats.moodDistribution || {});
}

function displayMoodDistribution(moodDistribution) {
    const container = document.getElementById('moodDistribution');
    if (!container) return;

    container.innerHTML = '<h3>Recent Mood Distribution</h3>';
    
    if (Object.keys(moodDistribution).length === 0) {
        container.innerHTML += '<p>No mood data available</p>';
        return;
    }
    
    Object.entries(moodDistribution).forEach(([mood, count]) => {
        const moodDiv = document.createElement('div');
        moodDiv.className = 'mood-stat';
        moodDiv.innerHTML = `
            <span class="mood-name">${getMoodEmoji(mood)} ${mood}</span>
            <span class="mood-count">${count}</span>
        `;
        container.appendChild(moodDiv);
    });
}

async function loadMoodHistory(userId) {
    try {
        const response = await fetch(`/MentalJournalApp/api/moods?userId=${userId}&limit=30`);
        if (response.ok) {
            const moods = await response.json();
            displayMoodChart(moods);
        }
    } catch (error) {
        console.error('Error loading mood history:', error);
    }
}

function displayMoodChart(moods) {
    const container = document.getElementById('moodChart');
    if (!container) return;

    container.innerHTML = '<h3>Mood History (Last 30 days)</h3>';
    
    if (moods.length === 0) {
        container.innerHTML += '<p>No mood history available</p>';
        return;
    }
    
    // Simple text-based chart (you could integrate Chart.js for better visualization)
    const chartDiv = document.createElement('div');
    chartDiv.className = 'mood-timeline';
    
    moods.reverse().forEach(mood => {
        const date = new Date(mood.loggedAt).toLocaleDateString();
        const moodDiv = document.createElement('div');
        moodDiv.className = 'mood-entry';
        moodDiv.innerHTML = `
            <span class="mood-date">${date}</span>
            <span class="mood-emoji">${getMoodEmoji(mood.mood)}</span>
        `;
        chartDiv.appendChild(moodDiv);
    });
    
    container.appendChild(chartDiv);
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


