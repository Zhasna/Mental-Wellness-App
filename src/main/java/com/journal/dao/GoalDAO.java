package com.journal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.journal.models.Goal;

public class GoalDAO {

    public Long createGoal(Goal goal) throws SQLException {
        String sql = "INSERT INTO goals (user_id, title, description, target_date, completed) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, goal.getUserId());
            ps.setString(2, goal.getTitle());
            ps.setString(3, goal.getDescription());
            ps.setDate(4, goal.getTargetDate());
            ps.setBoolean(5, goal.getCompleted());
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return null;
    }

    public Goal findById(Long id) throws SQLException {
        String sql = "SELECT * FROM goals WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Goal goal = new Goal();
                    goal.setId(rs.getLong("id"));
                    goal.setUserId(rs.getLong("user_id"));
                    goal.setTitle(rs.getString("title"));
                    goal.setDescription(rs.getString("description"));
                    goal.setTargetDate(rs.getDate("target_date"));
                    goal.setCompleted(rs.getBoolean("completed"));
                    goal.setCreatedAt(rs.getTimestamp("created_at"));
                    return goal;
                }
            }
        }
        return null;
    }

    public List<Goal> findByUserId(Long userId) throws SQLException {
        String sql = "SELECT * FROM goals WHERE user_id = ? ORDER BY created_at DESC";
        List<Goal> goals = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Goal goal = new Goal();
                    goal.setId(rs.getLong("id"));
                    goal.setUserId(rs.getLong("user_id"));
                    goal.setTitle(rs.getString("title"));
                    goal.setDescription(rs.getString("description"));
                    goal.setTargetDate(rs.getDate("target_date"));
                    goal.setCompleted(rs.getBoolean("completed"));
                    goal.setCreatedAt(rs.getTimestamp("created_at"));
                    goals.add(goal);
                }
            }
        }
        return goals;
    }

    public boolean updateGoalOwned(Long userId, Goal goal) throws SQLException {
        String sql = "UPDATE goals SET title = ?, description = ?, target_date = ?, completed = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, goal.getTitle());
            ps.setString(2, goal.getDescription());
            ps.setDate(3, goal.getTargetDate());
            ps.setBoolean(4, goal.getCompleted());
            ps.setLong(5, goal.getId());
            ps.setLong(6, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateGoalCompletionOwned(Long userId, Long id, boolean completed) throws SQLException {
        String sql = "UPDATE goals SET completed = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, completed);
            ps.setLong(2, id);
            ps.setLong(3, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteGoalOwned(Long userId, Long id) throws SQLException {
        String sql = "DELETE FROM goals WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        }
    }
}
