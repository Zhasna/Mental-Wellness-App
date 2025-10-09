package com.journal.servlets;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.journal.dao.DBConnection;
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
            List<Entry> entries = entryDAO.findByUserId(userId);
            response.getWriter().write(gson.toJson(entries));
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\":\"Database error: " + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        
        // Validate session
        if (!SessionUtils.validateSession(request, response)) {
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
            response.getWriter().write("{\"message\":\"Entry created successfully\", \"entryId\":" + entryId + "}");
            
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
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        
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
            
            boolean updated = entryDAO.updateEntry(entry);
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
        response.setContentType("application/json");
        
        String entryIdParam = request.getParameter("entryId");
        if (entryIdParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Entry ID is required\"}");
            return;
        }

        try {
            Long entryId = Long.parseLong(entryIdParam);
            boolean deleted = entryDAO.deleteEntry(entryId);
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
