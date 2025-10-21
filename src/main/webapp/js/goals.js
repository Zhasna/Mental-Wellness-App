document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("goalForm");
    
    // Check authentication
    if (!checkAuth()) {
        return;
    }
    
    // session-based auth handled by server; just ensure we have a session

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
    try {
        const response = await fetch(`/MentalJournalApp/api/goals`);
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
                <h3 class="goal-title ${goal.completed ? 'completed' : ''}">${goal.title}</h3>
                <button class="toggle-btn" onclick="toggleGoal(${goal.id}, ${!goal.completed})" title="Mark as ${goal.completed ? 'incomplete' : 'completed'}">
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
    console.log('toggleGoal called with:', goalId, completed);
    try {
        const response = await fetch('/MentalJournalApp/api/goals', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                goalId: String(goalId),
                completed: completed
            })
        });

        const data = await response.json();
        console.log('Toggle response:', response.status, data);
        
        if (response.ok) {
            // Update UI immediately for better UX
            const goalCard = document.querySelector(`[onclick*="${goalId}"]`).closest('.goal-card');
            if (goalCard) {
                if (completed) {
                    goalCard.classList.add('completed');
                } else {
                    goalCard.classList.remove('completed');
                }
            }
            loadGoals(); // Reload to ensure consistency
        } else {
            alert("Error: " + data.message);
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Failed to update goal. Please try again.');
    }
}

async function deleteGoal(goalId) {
    console.log('deleteGoal called with:', goalId);
    if (!confirm('Are you sure you want to delete this goal?')) {
        return;
    }

    try {
        const response = await fetch(`/MentalJournalApp/api/goals?goalId=${goalId}`, {
            method: 'DELETE'
        });

        const data = await response.json();
        console.log('Delete response:', response.status, data);
        
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
    console.log('editGoal called with:', goalId);
    // Find the goal in the current display
    const goalCard = document.querySelector(`[onclick*="editGoal(${goalId})"]`).closest('.goal-card');
    if (!goalCard) {
        console.error('Goal card not found for ID:', goalId);
        return;
    }
    
    const titleElement = goalCard.querySelector('.goal-title');
    const descriptionElement = goalCard.querySelector('.goal-description');
    
    const currentTitle = titleElement.textContent;
    const currentDescription = descriptionElement.textContent;
    
    // Create inline editing form
    const editForm = document.createElement('div');
    editForm.className = 'edit-form';
    editForm.innerHTML = `
        <div class="form-group">
            <label>Title:</label>
            <input type="text" id="editTitle" value="${currentTitle}" class="edit-input">
        </div>
        <div class="form-group">
            <label>Description:</label>
            <textarea id="editDescription" class="edit-textarea">${currentDescription}</textarea>
        </div>
        <div class="edit-actions">
            <button onclick="saveGoalEdit(${goalId})" class="save-btn">Save</button>
            <button onclick="cancelGoalEdit(${goalId})" class="cancel-btn">Cancel</button>
        </div>
    `;
    
    // Replace the goal content with edit form
    goalCard.innerHTML = '';
    goalCard.appendChild(editForm);
}

async function saveGoalEdit(goalId) {
    const titleInput = document.getElementById('editTitle');
    const descriptionInput = document.getElementById('editDescription');
    
    if (!titleInput || !descriptionInput) return;
    
    const newTitle = titleInput.value.trim();
    const newDescription = descriptionInput.value.trim();
    
    if (!newTitle || !newDescription) {
        alert('Title and description are required');
        return;
    }
    
    try {
        // For now, we'll delete the old goal and create a new one
        // This is a workaround since the servlet doesn't support full updates
        const deleteResponse = await fetch(`/MentalJournalApp/api/goals?goalId=${goalId}`, {
            method: 'DELETE'
        });
        
        if (deleteResponse.ok) {
            // Create new goal with updated content
            const createResponse = await fetch('/MentalJournalApp/api/goals', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    goalTitle: newTitle,
                    goalDescription: newDescription,
                    targetDate: null
                })
            });
            
            if (createResponse.ok) {
                alert('Goal updated successfully!');
                loadGoals(); // Reload the goals list
            } else {
                alert('Failed to update goal. Please try again.');
            }
        } else {
            alert('Failed to update goal. Please try again.');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Failed to update goal. Please try again.');
    }
}

function cancelGoalEdit(goalId) {
    loadGoals(); // Reload to restore original state
}

