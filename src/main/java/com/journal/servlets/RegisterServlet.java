package com.journal.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.journal.dao.DBConnection;
import com.journal.dao.UserDAO;
import com.journal.utils.PasswordUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RegisterServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        
        try {
            // Debug: Log the request
            System.out.println("RegisterServlet: Received POST request");
            
            Map<String, String> body = gson.fromJson(request.getReader(), 
                new TypeToken<Map<String, String>>(){}.getType());
            
            // Debug: Log the parsed body
            System.out.println("RegisterServlet: Parsed body: " + body);
            
            String username = body.get("username");
            String email = body.get("email");
            String password = body.get("password");
            
            // Debug: Log individual fields
            System.out.println("RegisterServlet: username=" + username);
            System.out.println("RegisterServlet: email=" + email);
            System.out.println("RegisterServlet: password=" + (password != null ? "[PROVIDED]" : "null"));
            
            if (username == null || email == null || password == null) {
                System.out.println("RegisterServlet: Missing fields - username=" + username + ", email=" + email + ", password=" + (password != null ? "provided" : "null"));
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"message\":\"All fields are required\"}");
                return;
            }
            
            // Hash the password
            String hashedPassword = PasswordUtils.hashPassword(password);
            
            // Insert user into database - use 'name' column instead of 'username'
            String sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, email);
                ps.setString(3, hashedPassword);
                
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("RegisterServlet: User registered successfully");
                    response.getWriter().write("{\"message\":\"User registered successfully\"}");
                } else {
                    System.out.println("RegisterServlet: Failed to register user");
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().write("{\"message\":\"Failed to register user\"}");
                }
            }
        } catch (SQLException e) {
            System.out.println("RegisterServlet: SQL Exception: " + e.getMessage());
            if (e.getSQLState().equals("23505")) { // Unique constraint violation
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.getWriter().write("{\"message\":\"Email already exists\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"message\":\"Database error: " + e.getMessage() + "\"}");
            }
        } catch (Exception e) {
            System.out.println("RegisterServlet: Exception: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\":\"Error: " + e.getMessage() + "\"}");
        }
    }
}
