document.addEventListener("DOMContentLoaded", async () => {
    if (!checkAuth()) return;
    
    try {
        // Load dashboard statistics
        const response = await fetch(`/api/stats`);
        if (response.ok) {
            const stats = await response.json();
            
            // Update stats cards
            // stats.totalEntries is backend-calculated; if needed client-side, ensure gratitude is excluded elsewhere
            document.getElementById("totalEntries").textContent = stats.totalEntries || 0;
            document.getElementById("currentMood").textContent = stats.currentMood || "😐";
            document.getElementById("goalsProgress").textContent = (stats.goalsProgress || 0) + "%";
            document.getElementById("totalGoals").textContent = stats.totalGoals || 0;
            document.getElementById("completedGoals").textContent = stats.completedGoals || 0;
            
            // Update username in header
            if (stats.username) {
                document.getElementById("userName").textContent = stats.username;
            }
            
            // Display mood distribution
            displayMoodDistribution(stats.moodDistribution || {});
            
            // Create mood chart
            createMoodChart(stats.moodDistribution || {});
            
        } else {
            console.error('Failed to load dashboard stats');
        }
    } catch (error) {
        console.error('Error loading dashboard data:', error);
    }
});

function displayMoodDistribution(moodDistribution) {
    const container = document.getElementById('moodDistribution');
    if (!container) return;
    
    container.innerHTML = '<h3>Mood Distribution</h3>';
    
    if (Object.keys(moodDistribution).length === 0) {
        container.innerHTML += '<p class="no-data">No mood data available yet.</p>';
        return;
    }
    
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
    
    const totalMoods = Object.values(moodDistribution).reduce((sum, count) => sum + count, 0);
    
    Object.entries(moodDistribution).forEach(([mood, count]) => {
        const percentage = totalMoods > 0 ? Math.round((count / totalMoods) * 100) : 0;
        
        const moodDiv = document.createElement('div');
        moodDiv.className = 'mood-item';
        moodDiv.innerHTML = `
            <div class="emoji">${moodEmojis[mood] || '😐'}</div>
            <div class="mood-name">${mood.charAt(0).toUpperCase() + mood.slice(1)}</div>
            <div class="count">${count}</div>
            <div class="percentage">${percentage}%</div>
        `;
        container.appendChild(moodDiv);
    });
}

function createMoodChart(moodDistribution) {
    console.log('Creating mood chart with data:', moodDistribution);
    
    const canvas = document.getElementById('moodChart');
    console.log('Canvas element:', canvas);
    
    if (!canvas) {
        console.error('Canvas element not found');
        return;
    }
    
    if (typeof Chart === 'undefined') {
        console.error('Chart.js library not loaded');
        return;
    }
    
    console.log('Chart.js version:', Chart.version);
    
    const ctx = canvas.getContext('2d');
    
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
    
    const labels = Object.keys(moodDistribution).map(mood => 
        `${moodEmojis[mood] || '😐'} ${mood.charAt(0).toUpperCase() + mood.slice(1)}`
    );
    const data = Object.values(moodDistribution);
    
    console.log('Chart labels:', labels);
    console.log('Chart data:', data);
    
    if (data.length === 0) {
        console.log('No mood data available for chart');
        // Show a message instead of empty chart
        const chartContainer = document.querySelector('.chart-wrapper');
        if (chartContainer) {
            chartContainer.innerHTML = '<p class="no-data">No mood data available yet. Start logging your moods to see trends!</p>';
        }
        return;
    }
    
    try {
        const chart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: data,
                backgroundColor: [
                    '#E8B4B8', // Soft rose
                    '#D4A5A5', // Muted rose
                    '#F4A261', // Warm orange
                    '#A8D5BA', // Soft green
                    '#C89595', // Rose button
                    '#B88585', // Button hover
                    '#8B5A3C', // Warm brown
                    '#F5E6E8', // Light rose
                    '#FFF8F3', // Soft cream
                    '#6B4C4C'  // Heading color
                ],
                borderWidth: 2,
                borderColor: '#FFFFFF'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        padding: 20,
                        usePointStyle: true,
                        font: {
                            family: 'Inter',
                            size: 12
                        }
                    }
                },
                tooltip: {
                    backgroundColor: 'rgba(0, 0, 0, 0.8)',
                    titleColor: '#FFFFFF',
                    bodyColor: '#FFFFFF',
                    borderColor: '#E8B4B8',
                    borderWidth: 1,
                    cornerRadius: 8,
                    displayColors: true
                }
            },
            cutout: '60%',
            animation: {
                animateRotate: true,
                duration: 1000
            }
        }
        });
        console.log('Chart created successfully:', chart);
    } catch (error) {
        console.error('Error creating chart:', error);
    }
}