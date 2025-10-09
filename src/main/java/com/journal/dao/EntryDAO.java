package com.journal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.journal.models.Entry;

public class EntryDAO {

    public Long createEntry(Entry entry) throws SQLException {
        String sql = "INSERT INTO entries (user_id, entry_date, mood, content) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, entry.getUserId());
            ps.setDate(2, entry.getEntryDate());
            ps.setString(3, entry.getMood());
            ps.setString(4, entry.getContent());
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return null;
    }

    public Entry findById(Long id) throws SQLException {
        String sql = "SELECT * FROM entries WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Entry entry = new Entry();
                    entry.setId(rs.getLong("id"));
                    entry.setUserId(rs.getLong("user_id"));
                    entry.setEntryDate(rs.getDate("entry_date"));
                    entry.setMood(rs.getString("mood"));
                    entry.setContent(rs.getString("content"));
                    entry.setCreatedAt(rs.getTimestamp("created_at"));
                    return entry;
                }
            }
        }
        return null;
    }

    public List<Entry> findByUserId(Long userId) throws SQLException {
        String sql = "SELECT * FROM entries WHERE user_id = ? ORDER BY entry_date DESC";
        List<Entry> entries = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Entry entry = new Entry();
                    entry.setId(rs.getLong("id"));
                    entry.setUserId(rs.getLong("user_id"));
                    entry.setEntryDate(rs.getDate("entry_date"));
                    entry.setMood(rs.getString("mood"));
                    entry.setContent(rs.getString("content"));
                    entry.setCreatedAt(rs.getTimestamp("created_at"));
                    entries.add(entry);
                }
            }
        }
        return entries;
    }

    public boolean updateEntry(Entry entry) throws SQLException {
        String sql = "UPDATE entries SET entry_date = ?, mood = ?, content = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, entry.getEntryDate());
            ps.setString(2, entry.getMood());
            ps.setString(3, entry.getContent());
            ps.setLong(4, entry.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteEntry(Long id) throws SQLException {
        String sql = "DELETE FROM entries WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Entry> getAllEntries() throws SQLException {
        String sql = "SELECT * FROM entries ORDER BY entry_date DESC";
        List<Entry> entries = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Entry entry = new Entry();
                entry.setId(rs.getLong("id"));
                entry.setUserId(rs.getLong("user_id"));
                entry.setEntryDate(rs.getDate("entry_date"));
                entry.setMood(rs.getString("mood"));
                entry.setContent(rs.getString("content"));
                entry.setCreatedAt(rs.getTimestamp("created_at"));
                entries.add(entry);
            }
        }
        return entries;
    }
}
