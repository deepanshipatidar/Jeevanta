package com.hackathon.dao;

import com.hackathon.model.User;
import com.hackathon.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object for User management.
 * Uses raw JDBC and PreparedStatement for parameterized queries.
 */
public class UserDAO {

    // SQL Queries
    private static final String SQL_AUTHENTICATE = "SELECT id, name, email, password FROM users WHERE email = ? AND password = ?";
    private static final String SQL_INSERT = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
    private static final String SQL_UPDATE = "UPDATE users SET name = ?, email = ?, password = ? WHERE id = ?";
    private static final String SQL_DELETE = "DELETE FROM users WHERE id = ?";

    private Connection getConnection() throws SQLException {
        return DBUtil.getConnection();
    }

    /**
     * Authenticates a user based on email and password.
     * 
     * @return User object if valid, null otherwise.
     */
    public User authenticate(String email, String password) throws SQLException {
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(SQL_AUTHENTICATE)) {

            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password"));
                }
            }
        }
        return null;
    }

    /**
     * Creates a new user in the database.
     * 
     * @return true if successfully inserted.
     */
    public boolean createUser(User user) throws SQLException {
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Updates an existing user's details.
     * 
     * @return true if successfully updated.
     */
    public boolean updateUser(User user) throws SQLException {
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setInt(4, user.getId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Deletes a user by ID.
     * 
     * @return true if successfully deleted.
     */
    public boolean deleteUser(int id) throws SQLException {
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {

            ps.setInt(1, id);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Retrieves the medical profile JSON string for a given patient.
     * The column stores past cases, allergies, chronic conditions, etc.
     * 
     * @param patientId the user's ID
     * @return raw JSON string from the database, or null if not found.
     */
    public String getMedicalProfile(int patientId) throws SQLException {
        String sql = "SELECT medical_profile_json FROM users WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("medical_profile_json");
                }
            }
        }
        return null;
    }
}
