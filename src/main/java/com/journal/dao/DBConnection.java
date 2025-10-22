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
    private static final String JDBC_URL = "jdbc:h2:file:" + DB_PATH + ";AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=false;CASE_INSENSITIVE_IDENTIFIERS=TRUE;AUTO_SERVER_PORT=9090";
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
        
        System.out.println("=== INITIALIZING DATABASE ===");
        System.out.println("DB_PATH: " + DB_PATH);
        System.out.println("JDBC URL: " + JDBC_URL);
        
        // Check if the directory exists and is writable
        java.io.File dbDir = new java.io.File(DB_PATH).getParentFile();
        System.out.println("Database directory: " + dbDir.getAbsolutePath());
        System.out.println("Directory exists: " + dbDir.exists());
        System.out.println("Directory is writable: " + dbDir.canWrite());
        
        // Try to create directory if it doesn't exist
        if (!dbDir.exists()) {
            System.out.println("Creating database directory...");
            boolean created = dbDir.mkdirs();
            System.out.println("Directory created: " + created);
        }
        
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
            
            System.out.println("✓ Database tables created/verified successfully");
            
            // Verify database files are created
            java.io.File dbFile = new java.io.File(DB_PATH + ".mv.db");
            System.out.println("Database file path: " + dbFile.getAbsolutePath());
            System.out.println("Database file exists: " + dbFile.exists());
            if (dbFile.exists()) {
                System.out.println("Database file size: " + dbFile.length() + " bytes");
                System.out.println("Database file last modified: " + new java.util.Date(dbFile.lastModified()));
            }
            
            // List all files in the data directory
            java.io.File dataDir = new java.io.File(DB_PATH).getParentFile();
            if (dataDir.exists() && dataDir.isDirectory()) {
                System.out.println("Files in data directory:");
                java.io.File[] files = dataDir.listFiles();
                if (files != null && files.length > 0) {
                    for (java.io.File f : files) {
                        System.out.println("  - " + f.getName() + " (" + f.length() + " bytes)");
                    }
                } else {
                    System.out.println("  (directory is empty)");
                }
            }
            
            System.out.println("==============================");
        } catch (SQLException ex) {
            System.err.println("✗ Database initialization error: " + ex.getMessage());
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

