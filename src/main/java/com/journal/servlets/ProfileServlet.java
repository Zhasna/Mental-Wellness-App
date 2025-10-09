package com.journal.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.journal.dao.DBConnection;
import com.journal.utils.PasswordUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ProfileServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        
        String userIdParam = request.getParameter("userId");
        if (userIdParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"User ID is required\"}");
            return;
        }

        try {
            Long userId = Long.parseLong(userIdParam);
            String sql = "SELECT id, username, email FROM users WHERE id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        response.getWriter().write(gson.toJson(Map.of(
                            "id", rs.getLong("id"),
                            "username", rs.getString("username"),
                            "email", rs.getString("email")
                        )));
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        response.getWriter().write("{\"message\":\"User not found\"}");
                    }
                }
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Invalid user ID format\"}");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\":\"Database error: " + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        
        try {
            Map<String, String> body = gson.fromJson(request.getReader(), 
                new TypeToken<Map<String, String>>(){}.getType());
            
            String userIdStr = body.get("userId");
            String username = body.get("profileName");
            String email = body.get("profileEmail");
            String currentPassword = body.get("currentPassword");
            String newPassword = body.get("newPassword");
            
            if (userIdStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"message\":\"User ID is required\"}");
                return;
            }
            
            Long userId = Long.parseLong(userIdStr);
            
            // Build update query dynamically
            StringBuilder sql = new StringBuilder("UPDATE users SET ");
            boolean hasUpdates = false;
            
            if (username != null && !username.trim().isEmpty()) {
                sql.append("username = ?, ");
                hasUpdates = true;
            }
            if (email != null && !email.trim().isEmpty()) {
                sql.append("email = ?, ");
                hasUpdates = true;
            }
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                if (currentPassword == null || currentPassword.trim().isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"message\":\"Current password is required to change password\"}");
                    return;
                }
                sql.append("password = ?, ");
                hasUpdates = true;
            }
            
            if (!hasUpdates) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"message\":\"No fields to update\"}");
                return;
            }
            
            // Remove trailing comma and space
            sql.setLength(sql.length() - 2);
            sql.append(" WHERE id = ?");
            
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                
                int paramIndex = 1;
                if (username != null && !username.trim().isEmpty()) {
                    ps.setString(paramIndex++, username);
                }
                if (email != null && !email.trim().isEmpty()) {
                    ps.setString(paramIndex++, email);
                }
                if (newPassword != null && !newPassword.trim().isEmpty()) {
                    // Verify current password first
                    String verifySql = "SELECT password FROM users WHERE id = ?";
                    try (PreparedStatement verifyPs = conn.prepareStatement(verifySql)) {
                        verifyPs.setLong(1, userId);
                        try (ResultSet rs = verifyPs.executeQuery()) {
                            if (rs.next()) {
                                String storedPassword = rs.getString("password");
                                if (!PasswordUtils.verifyPassword(currentPassword, storedPassword)) {
                                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                    response.getWriter().write("{\"message\":\"Current password is incorrect\"}");
                                    return;
                                }
                            } else {
                                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                                response.getWriter().write("{\"message\":\"User not found\"}");
                                return;
                            }
                        }
                    }
                    ps.setString(paramIndex++, PasswordUtils.hashPassword(newPassword));
                }
                ps.setLong(paramIndex, userId);
                
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    response.getWriter().write("{\"message\":\"Profile updated successfully\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("{\"message\":\"User not found\"}");
                }
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Invalid user ID format\"}");
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) { // Unique constraint violation
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.getWriter().write("{\"message\":\"Email already exists\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"message\":\"Database error: " + e.getMessage() + "\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\":\"Error: " + e.getMessage() + "\"}");
        }
    }
}
