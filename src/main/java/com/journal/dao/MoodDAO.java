package com.journal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.journal.models.Mood;

public class MoodDAO {

    public Long createMood(Mood mood) throws SQLException {
        String sql = "INSERT INTO moods (user_id, mood) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, mood.getUserId());
            ps.setString(2, mood.getMood());
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return null;
    }

    public List<Mood> findByUserId(Long userId) throws SQLException {
        String sql = "SELECT * FROM moods WHERE user_id = ? ORDER BY logged_at DESC";
        List<Mood> moods = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Mood mood = new Mood();
                    mood.setId(rs.getLong("id"));
                    mood.setUserId(rs.getLong("user_id"));
                    mood.setMood(rs.getString("mood"));
                    mood.setLoggedAt(rs.getTimestamp("logged_at"));
                    moods.add(mood);
                }
            }
        }
        return moods;
    }

    public List<Mood> getRecentMoods(Long userId, int limit) throws SQLException {
        String sql = "SELECT * FROM moods WHERE user_id = ? ORDER BY logged_at DESC LIMIT ?";
        List<Mood> moods = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Mood mood = new Mood();
                    mood.setId(rs.getLong("id"));
                    mood.setUserId(rs.getLong("user_id"));
                    mood.setMood(rs.getString("mood"));
                    mood.setLoggedAt(rs.getTimestamp("logged_at"));
                    moods.add(mood);
                }
            }
        }
        return moods;
    }

    public boolean deleteMood(Long id) throws SQLException {
        String sql = "DELETE FROM moods WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
