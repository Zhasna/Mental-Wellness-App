package com.journal.servlets;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.journal.dao.EntryDAO;
import com.journal.models.Entry;
import com.journal.utils.SessionUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class EntryServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private final EntryDAO entryDAO = new EntryDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        
        System.out.println("=== EntryServlet: GET /api/entries ===");
        
        // Validate session
        if (!SessionUtils.validateSession(request, response)) {
            System.err.println("Session validation failed");
            return;
        }
        
        Long userId = SessionUtils.getUserId(request);
        if (userId == null) {
            System.err.println("User ID is null");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"User ID is required\"}");
            return;
        }
        
        System.out.println("Fetching entries for user ID: " + userId);

        try {
            List<Entry> entries = entryDAO.findByUserId(userId);
            System.out.println("Found " + entries.size() + " entries");
            response.getWriter().write(gson.toJson(entries));
        } catch (SQLException e) {
            System.err.println("SQL Error fetching entries: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\":\"Database error: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            System.err.println("General error fetching entries: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\":\"Error: " + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        
        System.out.println("=== EntryServlet: POST /api/entries ===");
        
        // Validate session
        if (!SessionUtils.validateSession(request, response)) {
            System.err.println("Session validation failed");
            return;
        }
        
        try {
            Map<String, String> body = gson.fromJson(request.getReader(), 
                new TypeToken<Map<String, String>>(){}.getType());
            
            Long userId = SessionUtils.getUserId(request);
            String dateStr = body.get("date");
            String mood = body.get("mood");
            String content = body.get("content");
            
            if (dateStr == null || mood == null || content == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"message\":\"All fields are required\"}");
                return;
            }
            
            // Trim and validate content
            content = content.trim();
            if (content.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"message\":\"Entry content cannot be empty\"}");
                return;
            }
            
            java.sql.Date entryDate = java.sql.Date.valueOf(dateStr);
            
            Entry entry = new Entry();
            entry.setUserId(userId);
            entry.setEntryDate(entryDate);
            entry.setMood(mood);
            entry.setContent(content);
            
            Long entryId = entryDAO.createEntry(entry);
            System.out.println("✓ Entry created successfully - ID: " + entryId + ", User: " + userId);
            String jsonResponse = String.format(
                "{\"message\":\"Entry created successfully\",\"entryId\":%d}",
                entryId
            );
            response.getWriter().write(jsonResponse);
            
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Invalid date format. Use YYYY-MM-DD\"}");
        } catch (SQLException e) {
            System.err.println("✗ SQL Error creating entry: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\":\"Database error: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            System.err.println("✗ General error creating entry: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\":\"Error: " + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        
        try {
            Map<String, String> body = gson.fromJson(request.getReader(), 
                new TypeToken<Map<String, String>>(){}.getType());
            
            String entryIdStr = body.get("entryId");
            String dateStr = body.get("date");
            String mood = body.get("mood");
            String content = body.get("content");
            
            if (entryIdStr == null || dateStr == null || mood == null || content == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"message\":\"All fields are required\"}");
                return;
            }
            
            Long entryId = Long.parseLong(entryIdStr);
            java.sql.Date entryDate = java.sql.Date.valueOf(dateStr);
            
            Entry entry = new Entry();
            entry.setId(entryId);
            entry.setEntryDate(entryDate);
            entry.setMood(mood);
            entry.setContent(content);
            
            Long userId = SessionUtils.getUserId(request);
            boolean updated = entryDAO.updateEntryOwned(userId, entry);
            if (updated) {
                response.getWriter().write("{\"message\":\"Entry updated successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"message\":\"Entry not found\"}");
            }
            
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Invalid entry ID format\"}");
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Invalid date format. Use YYYY-MM-DD\"}");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\":\"Database error: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\":\"Error: " + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        
        String entryIdParam = request.getParameter("entryId");
        if (entryIdParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Entry ID is required\"}");
            return;
        }

        try {
            Long entryId = Long.parseLong(entryIdParam);
            Long userId = SessionUtils.getUserId(request);
            boolean deleted = entryDAO.deleteEntryOwned(userId, entryId);
            if (deleted) {
                response.getWriter().write("{\"message\":\"Entry deleted successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"message\":\"Entry not found\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Invalid entry ID format\"}");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\":\"Database error: " + e.getMessage() + "\"}");
        }
    }
}
