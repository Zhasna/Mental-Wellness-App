package com.journal.servlets;

import java.io.IOException;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class TestServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        
        try {
            System.out.println("TestServlet: Received POST request");
            System.out.println("TestServlet: Content-Type: " + request.getContentType());
            System.out.println("TestServlet: Content-Length: " + request.getContentLength());
            
            // Read the raw request body
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            String rawBody = sb.toString();
            System.out.println("TestServlet: Raw body: " + rawBody);
            
            // Try to parse as JSON
            Map<String, String> body = gson.fromJson(rawBody, 
                new TypeToken<Map<String, String>>(){}.getType());
            
            System.out.println("TestServlet: Parsed body: " + body);
            
            response.getWriter().write("{\"message\":\"Test successful\", \"received\": " + gson.toJson(body) + "}");
            
        } catch (Exception e) {
            System.out.println("TestServlet: Exception: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\":\"Error: " + e.getMessage() + "\"}");
        }
    }
} 