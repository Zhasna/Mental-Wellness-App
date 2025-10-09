package com.journal.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

public class DBConnection {
    private static final String JDBC_URL = "jdbc:h2:file:./data/mental_journal;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASS = "";
    private static boolean driverLoaded = false;

    static {
        try {
            // Load the H2 driver
            Class.forName("org.h2.Driver");
            driverLoaded = true;
            System.out.println("H2 Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("H2 Driver not found: " + e.getMessage());
            driverLoaded = false;
        }
        
        // Initialize database tables
        initializeDatabase();
    }
    
    private static void initializeDatabase() {
        if (!driverLoaded) {
            System.err.println("Cannot initialize database - H2 driver not loaded");
            return;
        }
        
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            // users table
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                  id IDENTITY PRIMARY KEY,
                  name VARCHAR(255),
                  email VARCHAR(255) UNIQUE,
                  password VARCHAR(255),
                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """);
            
            // entries table
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS entries (
                  id IDENTITY PRIMARY KEY,
                  user_id BIGINT,
                  entry_date DATE,
                  mood VARCHAR(50),
                  content TEXT,
                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                );
                """);
            
            // goals table
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS goals (
                  id IDENTITY PRIMARY KEY,
                  user_id BIGINT,
                  title VARCHAR(255),
                  description TEXT,
                  target_date DATE,
                  completed BOOLEAN DEFAULT FALSE,
                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                );
                """);
            
            // moods table
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS moods (
                  id IDENTITY PRIMARY KEY,
                  user_id BIGINT,
                  mood VARCHAR(50),
                  logged_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                );
                """);
            
            System.out.println("Database tables created/verified successfully");
        } catch (SQLException ex) {
            System.err.println("Database initialization error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        if (!driverLoaded) {
            // Try to load the driver again
            try {
                Class.forName("org.h2.Driver");
                driverLoaded = true;
                System.out.println("H2 Driver loaded successfully on demand");
            } catch (ClassNotFoundException e) {
                System.err.println("H2 Driver not found on demand: " + e.getMessage());
                throw new SQLException("H2 Driver not available", e);
            }
        }
        return DriverManager.getConnection(JDBC_URL, USER, PASS);
    }
}

