package com.journal.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
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

public class RegisterServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        
        try {
            System.out.println("RegisterServlet: POST /api/register");

            Map<String, String> body = gson.fromJson(request.getReader(),
                new TypeToken<Map<String, String>>(){}.getType());

            String username = body.get("username");
            String email = body.get("email");
            String password = body.get("password");

            if (username == null || email == null || password == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"message\":\"All fields are required\"}");
                return;
            }

            username = username.trim();
            email = email.trim();
            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"message\":\"All fields are required\"}");
                return;
            }
            if (!email.contains("@") || email.length() < 5) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"message\":\"Invalid email\"}");
                return;
            }
            if (password.length() < 8) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"message\":\"Password must be at least 8 characters\"}");
                return;
            }

            String hashedPassword = PasswordUtils.hashPassword(password);

            String sql = "INSERT INTO users (name, email, password_hash) VALUES (?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, email);
                ps.setString(3, hashedPassword);

                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    response.getWriter().write("{\"message\":\"User registered successfully\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().write("{\"message\":\"Failed to register user\"}");
                }
            }
        } catch (SQLException e) {
            if (e.getSQLState() != null && e.getSQLState().equals("23505")) { // Unique constraint violation
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
