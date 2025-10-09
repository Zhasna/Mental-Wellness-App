package com.journal.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.journal.dao.DBConnection;
import com.journal.utils.SessionUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class StatsServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        
        // Validate session
        if (!SessionUtils.validateSession(request, response)) {
            return;
        }
        
        Long userId = SessionUtils.getUserId(request);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"User ID is required\"}");
            return;
        }

        try {
            Map<String, Object> stats = getStatsForUser(userId);
            response.getWriter().write(gson.toJson(stats));
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\":\"Database error: " + e.getMessage() + "\"}");
        }
    }

    private Map<String, Object> getStatsForUser(Long userId) throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        
        try (Connection conn = DBConnection.getConnection()) {
            // Total entries
            String entriesSql = "SELECT COUNT(*) as count FROM entries WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(entriesSql)) {
                ps.setLong(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        stats.put("totalEntries", rs.getInt("count"));
                    }
                }
            }
            
            // Total goals
            String goalsSql = "SELECT COUNT(*) as count FROM goals WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(goalsSql)) {
                ps.setLong(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        stats.put("totalGoals", rs.getInt("count"));
                    }
                }
            }
            
            // Completed goals
            String completedGoalsSql = "SELECT COUNT(*) as count FROM goals WHERE user_id = ? AND completed = true";
            try (PreparedStatement ps = conn.prepareStatement(completedGoalsSql)) {
                ps.setLong(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        stats.put("completedGoals", rs.getInt("count"));
                    }
                }
            }
            
            // Goals progress percentage
            int totalGoals = (Integer) stats.get("totalGoals");
            int completedGoals = (Integer) stats.get("completedGoals");
            int progress = totalGoals > 0 ? (completedGoals * 100) / totalGoals : 0;
            stats.put("goalsProgress", progress);
            
            // Current mood (most recent)
            String moodSql = "SELECT mood FROM moods WHERE user_id = ? ORDER BY logged_at DESC LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(moodSql)) {
                ps.setLong(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        stats.put("currentMood", rs.getString("mood"));
                    } else {
                        stats.put("currentMood", "😐");
                    }
                }
            }
            
            // Mood distribution
            String moodDistSql = "SELECT mood, COUNT(*) as count FROM moods WHERE user_id = ? GROUP BY mood";
            Map<String, Integer> moodDistribution = new HashMap<>();
            try (PreparedStatement ps = conn.prepareStatement(moodDistSql)) {
                ps.setLong(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        moodDistribution.put(rs.getString("mood"), rs.getInt("count"));
                    }
                }
            }
            stats.put("moodDistribution", moodDistribution);
        }
        
        return stats;
    }
}
