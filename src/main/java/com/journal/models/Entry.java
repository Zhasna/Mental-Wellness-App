package com.journal.models;

import java.sql.Date;
import java.sql.Timestamp;

public class Entry {
    private Long id;
    private Long userId;
    private Date entryDate;
    private String mood;
    private String content;
    private Timestamp createdAt;

    public Entry() {}

    public Entry(Long id, Long userId, Date entryDate, String mood, String content, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.entryDate = entryDate;
        this.mood = mood;
        this.content = content;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public Date getEntryDate() { return entryDate; }
    public void setEntryDate(Date entryDate) { this.entryDate = entryDate; }
    
    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
