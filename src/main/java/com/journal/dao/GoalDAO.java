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

    public boolean updateGoal(Goal goal) throws SQLException {
        String sql = "UPDATE goals SET title = ?, description = ?, target_date = ?, completed = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, goal.getTitle());
            ps.setString(2, goal.getDescription());
            ps.setDate(3, goal.getTargetDate());
            ps.setBoolean(4, goal.getCompleted());
            ps.setLong(5, goal.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateGoalCompletion(Long id, boolean completed) throws SQLException {
        String sql = "UPDATE goals SET completed = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, completed);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteGoal(Long id) throws SQLException {
        String sql = "DELETE FROM goals WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
