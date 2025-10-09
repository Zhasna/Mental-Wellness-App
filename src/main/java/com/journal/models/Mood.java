package com.journal.models;

import java.sql.Timestamp;

public class Mood {
    private Long id;
    private Long userId;
    private String mood;
    private Timestamp loggedAt;

    public Mood() {}

    public Mood(Long id, Long userId, String mood, Timestamp loggedAt) {
        this.id = id;
        this.userId = userId;
        this.mood = mood;
        this.loggedAt = loggedAt;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }
    
    public Timestamp getLoggedAt() { return loggedAt; }
    public void setLoggedAt(Timestamp loggedAt) { this.loggedAt = loggedAt; }
}
