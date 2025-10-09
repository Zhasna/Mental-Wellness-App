package com.journal.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class SessionUtils {
    
    public static boolean validateSession(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        // Check for valid server-side session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\":\"Authentication required\"}");
            return false;
        }
        
        return true;
    }
    
    public static Long getUserId(HttpServletRequest request) {
        // First try to get from server-side session
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            return (Long) session.getAttribute("userId");
        }
        
        // Fallback to request parameters (for backward compatibility)
        String userId = request.getParameter("userId");
        if (userId != null) {
            try {
                return Long.parseLong(userId);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    public static boolean validateUserOwnership(HttpServletRequest request, Long resourceUserId) {
        Long sessionUserId = getUserId(request);
        return sessionUserId != null && sessionUserId.equals(resourceUserId);
    }
}