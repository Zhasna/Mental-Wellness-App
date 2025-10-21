// Meditation Timer JavaScript
document.addEventListener("DOMContentLoaded", () => {
    if (!checkAuth()) return;
    
    // Initialize timer
    initializeTimer();
    // Force clean state and default duration
    if (completionTimeoutId) { clearTimeout(completionTimeoutId); completionTimeoutId = null; }
    if (endSound) { endSound.pause(); endSound.currentTime = 0; }
    if (completionMessage) completionMessage.style.display = 'none';
    setTimer(5);
});

// Timer state variables
let timerInterval = null;
let totalSeconds = 0;
let remainingSeconds = 0;
let isRunning = false;
let isPaused = false;
let backgroundSoundEnabled = false;
let completionTimeoutId = null;

// DOM elements
const timerDisplay = document.getElementById('timerDisplay');
const timerStatus = document.getElementById('timerStatus');
const timerCircle = document.querySelector('.timer-circle');
const startBtn = document.getElementById('startBtn');
const pauseBtn = document.getElementById('pauseBtn');
const resetBtn = document.getElementById('resetBtn');
const soundBtn = document.getElementById('soundBtn');
const customMinutesInput = document.getElementById('customMinutes');
const completionMessage = document.getElementById('completionMessage');
const backgroundSound = document.getElementById('backgroundSound');
const endSound = document.getElementById('endSound');

// Initialize timer functionality
function initializeTimer() {
    // Set up preset buttons
    const presetButtons = document.querySelectorAll('.preset-btn');
    presetButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const minutes = parseInt(btn.dataset.minutes);
            setTimer(minutes);
            updatePresetButtons(btn);
        });
    });

    // Set up custom input
    customMinutesInput.addEventListener('input', () => {
        const minutes = parseInt(customMinutesInput.value) || 0;
        if (minutes > 0 && minutes <= 120) {
            setTimer(minutes);
            clearPresetSelection();
        }
    });

    // Set up control buttons
    startBtn.addEventListener('click', startTimer);
    pauseBtn.addEventListener('click', pauseTimer);
    resetBtn.addEventListener('click', resetTimer);

    // Set up sound button
    soundBtn.addEventListener('click', toggleBackgroundSound);

    // Ensure clean initial UI state
    if (completionMessage) completionMessage.style.display = 'none';
    updateStatus('Ready to begin');
    remainingSeconds = totalSeconds = 0;
    timerDisplay.textContent = '00:00';
}

// Set timer duration
function setTimer(minutes) {
    totalSeconds = minutes * 60;
    remainingSeconds = totalSeconds;
    updateDisplay();
    updateStatus('Ready to begin');
    resetTimerState();
}

// Update timer display
function updateDisplay() {
    const minutes = Math.floor(remainingSeconds / 60);
    const seconds = remainingSeconds % 60;
    timerDisplay.textContent = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
}

// Update timer status
function updateStatus(status) {
    timerStatus.textContent = status;
}

// Update preset button selection
function updatePresetButtons(selectedBtn) {
    const presetButtons = document.querySelectorAll('.preset-btn');
    presetButtons.forEach(btn => btn.classList.remove('active'));
    selectedBtn.classList.add('active');
}

// Clear preset selection
function clearPresetSelection() {
    const presetButtons = document.querySelectorAll('.preset-btn');
    presetButtons.forEach(btn => btn.classList.remove('active'));
}

// Reset timer state
function resetTimerState() {
    isRunning = false;
    isPaused = false;
    timerCircle.classList.remove('running', 'paused');
    startBtn.disabled = false;
    pauseBtn.disabled = true;
    resetBtn.disabled = false;
    startBtn.textContent = 'Start';
}

// Start timer
function startTimer() {
    if (remainingSeconds <= 0) return;
    
    isRunning = true;
    isPaused = false;
    
    timerCircle.classList.add('running');
    timerCircle.classList.remove('paused');
    
    startBtn.disabled = true;
    pauseBtn.disabled = false;
    resetBtn.disabled = false;
    
    updateStatus('Meditation in progress...');

    // Stop any lingering end chime and hide completion message
    if (endSound) { endSound.pause(); endSound.currentTime = 0; }
    if (completionTimeoutId) { clearTimeout(completionTimeoutId); completionTimeoutId = null; }
    if (completionMessage) completionMessage.style.display = 'none';
    
    // Start background sound if enabled
    if (backgroundSoundEnabled) {
        playBackgroundSound();
    }
    
    // Start countdown
    timerInterval = setInterval(() => {
        remainingSeconds--;
        updateDisplay();
        
        if (remainingSeconds <= 0) {
            completeTimer();
        }
    }, 1000);
}

// Pause timer
function pauseTimer() {
    if (!isRunning) return;
    
    isRunning = false;
    isPaused = true;
    
    clearInterval(timerInterval);
    timerInterval = null;
    
    timerCircle.classList.remove('running');
    timerCircle.classList.add('paused');
    
    startBtn.disabled = false;
    pauseBtn.disabled = true;
    
    updateStatus('Paused');
    
    // Pause background sound
    pauseBackgroundSound();
}

// Reset timer
function resetTimer() {
    clearInterval(timerInterval);
    timerInterval = null;
    
    remainingSeconds = totalSeconds;
    updateDisplay();
    updateStatus('Ready to begin');
    resetTimerState();
    
    // Stop background sound
    stopBackgroundSound();

    // Stop end chime if playing
    if (endSound) { endSound.pause(); endSound.currentTime = 0; }

    // Clear any pending completion popup
    if (completionTimeoutId) { clearTimeout(completionTimeoutId); completionTimeoutId = null; }
    
    // Hide completion message
    completionMessage.style.display = 'none';
}

// Complete timer
function completeTimer() {
    clearInterval(timerInterval);
    timerInterval = null;
    
    isRunning = false;
    isPaused = false;
    
    timerCircle.classList.remove('running', 'paused');
    updateStatus('Session complete!');
    
    // Stop background sound
    stopBackgroundSound();
    
    // Play end sound
    playEndSound();
    
    // Show completion message
    completionTimeoutId = setTimeout(() => {
        showCompletionMessage();
        completionTimeoutId = null;
    }, 500);
    
    // Reset button states
    startBtn.disabled = false;
    pauseBtn.disabled = true;
    startBtn.textContent = 'Start';
}

// Show completion message
function showCompletionMessage() {
    completionMessage.style.display = 'flex';
}

// Play background sound
function playBackgroundSound() {
    if (backgroundSoundEnabled && backgroundSound) {
        backgroundSound.currentTime = 0;
        backgroundSound.play().catch(e => {
            console.log('Background sound play failed:', e);
        });
    }
}

// Pause background sound
function pauseBackgroundSound() {
    if (backgroundSound) {
        backgroundSound.pause();
    }
}

// Stop background sound
function stopBackgroundSound() {
    if (backgroundSound) {
        backgroundSound.pause();
        backgroundSound.currentTime = 0;
    }
}

// Play end sound
function playEndSound() {
    if (endSound) {
        endSound.currentTime = 0;
        endSound.play().catch(e => {
            console.log('End sound play failed:', e);
        });
    }
}

// Toggle background sound
function toggleBackgroundSound() {
    backgroundSoundEnabled = !backgroundSoundEnabled;
    
    const soundIcon = soundBtn.querySelector('.sound-icon');
    const soundText = soundBtn.querySelector('.sound-text');
    
    if (backgroundSoundEnabled) {
        soundIcon.textContent = '🔊';
        soundText.textContent = 'Background Sound ON';
        soundBtn.classList.add('active');
        
        // If timer is running, start background sound
        if (isRunning) {
            playBackgroundSound();
        }
    } else {
        soundIcon.textContent = '🔇';
        soundText.textContent = 'Background Sound';
        soundBtn.classList.remove('active');
        
        // Stop background sound
        stopBackgroundSound();
    }
}

// Global reset function for completion message
window.resetTimer = resetTimer;

// Handle page visibility change (pause when tab is not active)
document.addEventListener('visibilitychange', () => {
    if (document.hidden && isRunning) {
        // Optionally pause timer when tab is not visible
        // pauseTimer();
    }
});

// Handle beforeunload (warn if timer is running)
window.addEventListener('beforeunload', (e) => {
    if (isRunning) {
        e.preventDefault();
        e.returnValue = 'Your meditation session is still running. Are you sure you want to leave?';
    }
});

// Keyboard shortcuts
document.addEventListener('keydown', (e) => {
    if (e.code === 'Space') {
        e.preventDefault();
        if (isRunning) {
            pauseTimer();
        } else if (remainingSeconds > 0) {
            startTimer();
        }
    } else if (e.code === 'Escape') {
        resetTimer();
    }
});

