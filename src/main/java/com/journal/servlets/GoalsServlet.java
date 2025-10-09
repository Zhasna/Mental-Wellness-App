package com.journal.servlets;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.journal.dao.GoalDAO;
import com.journal.models.Goal;
import com.journal.utils.SessionUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class GoalsServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private final GoalDAO goalDAO = new GoalDAO();

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
            List<Goal> goals = goalDAO.findByUserId(userId);
            response.getWriter().write(gson.toJson(goals));
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
            String title = body.get("goalTitle");
            String description = body.get("goalDescription");
            String targetDateStr = body.get("targetDate");
            
            // Trim and validate inputs
            if (title != null) title = title.trim();
            if (description != null) description = description.trim();
            
            if (title == null || description == null || title.isEmpty() || description.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"message\":\"Title and description are required\"}");
                return;
            }
            
            Goal goal = new Goal();
            goal.setUserId(userId);
            goal.setTitle(title);
            goal.setDescription(description);
            goal.setCompleted(false);
            
            if (targetDateStr != null && !targetDateStr.trim().isEmpty()) {
                goal.setTargetDate(Date.valueOf(targetDateStr));
            }
            
            Long goalId = goalDAO.createGoal(goal);
            response.getWriter().write("{\"message\":\"Goal created successfully\", \"goalId\":" + goalId + "}");
            
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
            
            String goalIdStr = body.get("goalId");
            String completedStr = body.get("completed");
            
            if (goalIdStr == null || completedStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"message\":\"Goal ID and completion status are required\"}");
                return;
            }
            
            Long goalId = Long.parseLong(goalIdStr);
            boolean completed = Boolean.parseBoolean(completedStr);
            
            boolean updated = goalDAO.updateGoalCompletion(goalId, completed);
            if (updated) {
                response.getWriter().write("{\"message\":\"Goal updated successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"message\":\"Goal not found\"}");
            }
            
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Invalid goal ID format\"}");
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
        
        String goalIdParam = request.getParameter("goalId");
        if (goalIdParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Goal ID is required\"}");
            return;
        }

        try {
            Long goalId = Long.parseLong(goalIdParam);
            boolean deleted = goalDAO.deleteGoal(goalId);
            if (deleted) {
                response.getWriter().write("{\"message\":\"Goal deleted successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"message\":\"Goal not found\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Invalid goal ID format\"}");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\":\"Database error: " + e.getMessage() + "\"}");
        }
    }
}
