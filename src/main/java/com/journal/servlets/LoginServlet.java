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
import jakarta.servlet.http.HttpSession;

public class LoginServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private static final java.util.concurrent.ConcurrentHashMap<String, java.util.Deque<Long>> ATTEMPTS = new java.util.concurrent.ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 10 * 60 * 1000L; // 10 minutes

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        
        try {
            Map<String, String> body = gson.fromJson(request.getReader(), 
                new TypeToken<Map<String, String>>(){}.getType());
            
            String email = body.get("email");
            String password = body.get("password");
            
            // Simple rate limiting by client IP
            String clientKey = request.getRemoteAddr();
            long now = System.currentTimeMillis();
            ATTEMPTS.compute(clientKey, (k, q) -> {
                if (q == null) q = new java.util.ArrayDeque<>();
                // purge old attempts
                while (!q.isEmpty() && now - q.peekFirst() > WINDOW_MS) {
                    q.pollFirst();
                }
                if (q.size() >= MAX_ATTEMPTS) {
                    return q; // will be handled below
                }
                return q;
            });
            java.util.Deque<Long> queue = ATTEMPTS.get(clientKey);
            if (queue.size() >= MAX_ATTEMPTS) {
                response.setStatus(429);
                response.getWriter().write("{\"message\":\"Too many login attempts. Please try again later.\"}");
                return;
            }
            
            if (email == null || password == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"message\":\"Email and password are required\"}");
                return;
            }
            
            // Validate user credentials
            String sql = "SELECT id, name, email, password_hash FROM users WHERE email = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String hashedPassword = rs.getString("password_hash");
                        if (PasswordUtils.verifyPassword(password, hashedPassword)) {
                            // reset attempts on success
                            java.util.Deque<Long> q = ATTEMPTS.get(clientKey);
                            if (q != null) q.clear();
                            // Login successful - Prevent session fixation by invalidating any existing session
                            HttpSession existing = request.getSession(false);
                            if (existing != null) {
                                existing.invalidate();
                            }
                            HttpSession session = request.getSession(true);
                            session.setAttribute("userId", rs.getLong("id"));
                            session.setAttribute("username", rs.getString("name"));
                            session.setAttribute("email", rs.getString("email"));
                            
                            // Set session timeout to 30 minutes
                            session.setMaxInactiveInterval(30 * 60);
                            
                            // Create response JSON manually to avoid type issues
                            Long userId = rs.getLong("id");
                            String userName = rs.getString("name");
                            String userEmail = rs.getString("email");
                            
                            String jsonResponse = String.format(
                                "{\"message\":\"Login successful\",\"userId\":%d,\"username\":\"%s\",\"email\":\"%s\"}",
                                userId,
                                userName.replace("\"", "\\\""),
                                userEmail.replace("\"", "\\\"")
                            );
                            response.getWriter().write(jsonResponse);
                        } else {
                            // record failed attempt
                            ATTEMPTS.compute(clientKey, (k, q) -> {
                                if (q == null) q = new java.util.ArrayDeque<>();
                                q.addLast(System.currentTimeMillis());
                                return q;
                            });
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"message\":\"Invalid credentials\"}");
                        }
                    } else {
                        // record failed attempt
                        ATTEMPTS.compute(clientKey, (k, q) -> {
                            if (q == null) q = new java.util.ArrayDeque<>();
                            q.addLast(System.currentTimeMillis());
                            return q;
                        });
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("{\"message\":\"Invalid credentials\"}");
                    }
                }
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\":\"Database error: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\":\"Error: " + e.getMessage() + "\"}");
        }
    }
}

