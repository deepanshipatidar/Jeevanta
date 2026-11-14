package com.hackathon.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Central Database utility providing managed JDBC connections.
 * 
 * Supports environment variable configuration for production / CI/CD:
 * - DB_URL: JDBC connection URL (default: jdbc:mysql://localhost:3306/hackathon_db)
 * - DB_USER: Database username (default: root)
 * - DB_PASS: Database password (default: root)
 */
public class DBUtil {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/hackathon_db?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "root";

    static {
        try {
            // Check for MySQL 8+ driver first, fall back to legacy driver
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                Class.forName("com.mysql.jdbc.Driver");
            }
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("MySQL JDBC Driver not found on classpath: " + e.getMessage());
        }
    }

    /**
     * Obtains a new database connection using environment variables if defined,
     * or standard defaults otherwise.
     *
     * @return an active {@link Connection}
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        String envUrl = System.getenv("DB_URL");
        String envUser = System.getenv("DB_USER");
        String envPass = System.getenv("DB_PASS");

        String url = (envUrl != null && !envUrl.trim().isEmpty()) ? envUrl.trim() : DEFAULT_URL;
        String user = (envUser != null && !envUser.trim().isEmpty()) ? envUser.trim() : DEFAULT_USER;
        String pass = (envPass != null) ? envPass : DEFAULT_PASS;

        return DriverManager.getConnection(url, user, pass);
    }
}

