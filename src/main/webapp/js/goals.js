document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("goalForm");
    
    // Check authentication
    if (!checkAuth()) {
        return;
    }
    
    const userId = sessionStorage.getItem('userId');
    if (!userId) {
        alert('User not authenticated');
        window.location.href = 'login.html';
        return;
    }

    if (form) {
        form.addEventListener("submit", async (e) => {
            e.preventDefault();

            const title = document.getElementById("goalTitle").value.trim();
            const description = document.getElementById("goalDescription").value.trim();
            const targetDate = document.getElementById("targetDate").value;

            // Client-side validation
            if (!title || !description) {
                alert('Title and description are required');
                return;
            }

            if (title.length > 255) {
                alert('Title must be less than 255 characters');
                return;
            }

            try {
                const response = await fetch('/MentalJournalApp/api/goals', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        userId: userId,
                        goalTitle: title,
                        goalDescription: description,
                        targetDate: targetDate || null
                    })
                });

                const data = await response.json();
                
                if (response.ok) {
                    alert("Goal created successfully!");
                    form.reset();
                    loadGoals(); // Reload the goals list
                } else {
                    alert("Error: " + data.message);
                }
            } catch (error) {
                console.error('Error:', error);
                alert('Failed to create goal. Please try again.');
            }
        });
    }

    // Load existing goals
    loadGoals();
});

async function loadGoals() {
    const userId = sessionStorage.getItem('userId');
    if (!userId) return;
    
    try {
        const response = await fetch(`/MentalJournalApp/api/goals?userId=${userId}`);
        if (response.ok) {
            const goals = await response.json();
            displayGoals(goals);
        } else {
            console.error('Failed to load goals');
        }
    } catch (error) {
        console.error('Error loading goals:', error);
    }
}

function displayGoals(goals) {
    const goalsContainer = document.getElementById('goalsContainer');
    if (!goalsContainer) return;

    goalsContainer.innerHTML = '';
    
    if (goals.length === 0) {
        goalsContainer.innerHTML = '<p class="no-goals">No goals yet. Create your first goal above!</p>';
        return;
    }
    
    goals.forEach(goal => {
        const goalDiv = document.createElement('div');
        goalDiv.className = `goal-card ${goal.completed ? 'completed' : ''}`;
        goalDiv.innerHTML = `
            <div class="goal-header">
                <h3 class="goal-title">${goal.title}</h3>
                <button class="toggle-btn" onclick="toggleGoal(${goal.id}, ${!goal.completed})">
                    ${goal.completed ? '✅' : '⭕'}
                </button>
            </div>
            <div class="goal-description">${goal.description}</div>
            ${goal.targetDate ? `<div class="goal-target-date">Target: ${goal.targetDate}</div>` : ''}
            <div class="goal-actions">
                <button onclick="editGoal(${goal.id})" class="edit-btn">Edit</button>
                <button onclick="deleteGoal(${goal.id})" class="delete-btn">Delete</button>
            </div>
        `;
        goalsContainer.appendChild(goalDiv);
    });
}

async function toggleGoal(goalId, completed) {
    try {
        // First get the goal details
        const userId = sessionStorage.getItem('userId');
        const goalsResponse = await fetch(`/MentalJournalApp/api/goals?userId=${userId}`);
        const goals = await goalsResponse.json();
        const goal = goals.find(g => g.id === goalId);
        
        if (!goal) {
            alert('Goal not found');
            return;
        }

        const response = await fetch('/MentalJournalApp/api/goals', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                id: goalId,
                title: goal.title,
                description: goal.description,
                targetDate: goal.targetDate,
                completed: completed
            })
        });

        const data = await response.json();
        
        if (response.ok) {
            loadGoals(); // Reload the goals list
        } else {
            alert("Error: " + data.message);
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Failed to update goal. Please try again.');
    }
}

async function deleteGoal(goalId) {
    if (!confirm('Are you sure you want to delete this goal?')) {
        return;
    }

    try {
        const response = await fetch(`/MentalJournalApp/api/goals?id=${goalId}`, {
            method: 'DELETE'
        });

        const data = await response.json();
        
        if (response.ok) {
            loadGoals(); // Reload the goals list
        } else {
            alert("Error: " + data.message);
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Failed to delete goal. Please try again.');
    }
}

function editGoal(goalId) {
    // Simple implementation - could be enhanced with a modal
    const newTitle = prompt('Enter new title:');
    const newDescription = prompt('Enter new description:');
    
    if (newTitle && newDescription) {
        updateGoal(goalId, newTitle, newDescription);
    }
}

async function updateGoal(goalId, title, description) {
    try {
        const response = await fetch('/MentalJournalApp/api/goals', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                id: goalId,
                title: title,
                description: description
            })
        });

        const data = await response.json();
        
        if (response.ok) {
            loadGoals(); // Reload the goals list
        } else {
            alert("Error: " + data.message);
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Failed to update goal. Please try again.');
    }
}

