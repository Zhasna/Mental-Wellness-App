package com.journal.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

public class DBConnection {
    // Use environment variable for DB path if available (for Render deployment)
    // Otherwise default to local ./data directory
    private static final String DB_PATH;
    
    static {
        String envPath = System.getenv("DB_PATH");
        System.out.println("=== DATABASE CONFIGURATION ===");
        System.out.println("DB_PATH environment variable: " + (envPath != null ? envPath : "NOT SET"));
        
        if (envPath != null && !envPath.trim().isEmpty()) {
            DB_PATH = envPath;
            System.out.println("Using environment DB path: " + DB_PATH);
        } else {
            DB_PATH = "./data/mental_journal";
            System.out.println("WARNING: DB_PATH not set, using default: " + DB_PATH);
            System.out.println("This will NOT persist on Render! Set DB_PATH environment variable.");
        }
        System.out.println("==============================");
    }
    private static final String JDBC_URL = "jdbc:h2:file:" + DB_PATH + ";AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=false;CASE_INSENSITIVE_IDENTIFIERS=TRUE";
    private static final String USER = "sa";
    private static final String PASS = "";
    private static boolean driverLoaded = false;

    static {
        System.out.println("Loading H2 Driver...");
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
        
        System.out.println("Initializing database at: " + DB_PATH);
        System.out.println("JDBC URL: " + JDBC_URL);
        
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            // users table
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                  name VARCHAR(255),
                  email VARCHAR(255) UNIQUE,
                  password_hash VARCHAR(255),
                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """);

            // Backward compatibility: if legacy column `password` exists, ensure `password_hash` exists and copy data
            try {
                st.executeUpdate("ALTER TABLE users ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255)");
                // Copy data from legacy column if present and password_hash is null
                st.executeUpdate("UPDATE users SET password_hash = password WHERE password_hash IS NULL");
            } catch (SQLException ignored) {
                // Ignore if ALTER not supported or column already exists
            }
            
            // entries table
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS entries (
                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
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
                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
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
                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                  user_id BIGINT,
                  mood VARCHAR(50),
                  logged_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                );
                """);
            
            System.out.println("Database tables created/verified successfully at: " + DB_PATH);
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

