package com.journal.servlets;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.journal.dao.MoodDAO;
import com.journal.models.Mood;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class MoodServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private final MoodDAO moodDAO = new MoodDAO();

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
            List<Mood> moods = moodDAO.findByUserId(userId);
            response.getWriter().write(gson.toJson(moods));
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Invalid user ID format\"}");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\":\"Database error: " + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        
        try {
            Map<String, String> body = gson.fromJson(request.getReader(), 
                new TypeToken<Map<String, String>>(){}.getType());
            
            String userIdStr = body.get("userId");
            String moodType = body.get("mood");
            
            if (userIdStr == null || moodType == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"message\":\"User ID and mood are required\"}");
                return;
            }
            
            Long userId = Long.parseLong(userIdStr);
            Mood mood = new Mood();
            mood.setUserId(userId);
            mood.setMood(moodType);
            
            Long moodId = moodDAO.createMood(mood);
            response.getWriter().write("{\"message\":\"Mood recorded successfully\", \"moodId\":" + moodId + "}");
            
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Invalid user ID format\"}");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\":\"Database error: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\":\"Error: " + e.getMessage() + "\"}");
        }
    }
}
