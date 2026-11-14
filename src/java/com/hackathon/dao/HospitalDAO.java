package com.hackathon.dao;

import com.hackathon.model.Hospital;
import com.hackathon.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for hospital-related database operations.
 *
 * Uses raw JDBC with {@link PreparedStatement} for parameterized queries.
 * All connections are obtained via {@link #getConnection()} and closed
 * per-method to avoid leaking resources.
 */
public class HospitalDAO {

    // ─── SQL ───────────────────────────────────────────────────────────
    private static final String SQL_GET_AVAILABLE =
            "SELECT id, name, lat, lng, available_beds FROM hospitals WHERE available_beds > 0 ORDER BY name";

    private static final String SQL_LOCK_ROW =
            "SELECT available_beds FROM hospitals WHERE id = ? FOR UPDATE";

    private static final String SQL_DECREMENT_BED =
            "UPDATE hospitals SET available_beds = available_beds - 1 WHERE id = ?";

    // ─── Connection Helper ─────────────────────────────────────────────
    private Connection getConnection() throws SQLException {
        return DBUtil.getConnection();
    }

    // ═══════════════════════════════════════════════════════════════════
    // PUBLIC API
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Returns every hospital that has at least one available bed.
     *
     * @return list of {@link Hospital} objects (never {@code null})
     * @throws SQLException if a database access error occurs
     */
    public List<Hospital> getAllAvailableHospitals() throws SQLException {
        List<Hospital> hospitals = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_GET_AVAILABLE);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Hospital h = new Hospital();
                h.setId(rs.getInt("id"));
                h.setName(rs.getString("name"));
                h.setLat(rs.getDouble("lat"));
                h.setLng(rs.getDouble("lng"));
                h.setAvailableBeds(rs.getInt("available_beds"));
                hospitals.add(h);
            }
        }

        return hospitals;
    }

    /**
     * Atomically books one bed at the given hospital for the given user.
     *
     * <p>Uses a database transaction with {@code SELECT ... FOR UPDATE} to
     * acquire a row-level lock, preventing two concurrent requests from
     * double-booking the last bed.
     *
     * @param hospitalId the ID of the hospital to book
     * @param userId     the ID of the user making the booking (logged for auditing)
     * @return {@code true} if a bed was successfully booked, {@code false}
     *         if the hospital doesn't exist or has zero available beds
     * @throws SQLException if a database access error occurs
     */
    public boolean bookBed(int hospitalId, int userId) throws SQLException {
        Connection conn = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false); // BEGIN TRANSACTION

            // Step 1: Lock the row and read the current bed count
            int currentBeds;
            try (PreparedStatement lockStmt = conn.prepareStatement(SQL_LOCK_ROW)) {
                lockStmt.setInt(1, hospitalId);
                try (ResultSet rs = lockStmt.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false; // Hospital ID not found
                    }
                    currentBeds = rs.getInt("available_beds");
                }
            }

            // Step 2: Check availability
            if (currentBeds <= 0) {
                conn.rollback();
                return false; // No beds available
            }

            // Step 3: Decrement the bed count
            try (PreparedStatement updateStmt = conn.prepareStatement(SQL_DECREMENT_BED)) {
                updateStmt.setInt(1, hospitalId);
                updateStmt.executeUpdate();
            }

            conn.commit(); // COMMIT TRANSACTION
            System.out.printf("[DAO] Bed booked — hospitalId=%d, userId=%d, bedsRemaining=%d%n",
                    hospitalId, userId, currentBeds - 1);
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            throw e; // Re-throw so the servlet can handle it
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }
}
