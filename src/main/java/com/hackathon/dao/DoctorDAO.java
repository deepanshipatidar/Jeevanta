package com.hackathon.dao;

import com.hackathon.model.Doctor;
import com.hackathon.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO {

    // ─── SQL ───────────────────────────────────────────────────────────
    private static final String SQL_GET_ALL =
            "SELECT id, name, specialization, hospital, experience, rating, phone, fee, available FROM doctors";

    // ─── Connection Helper ─────────────────────────────────────────────
    private Connection getConnection() throws SQLException {
        return DBUtil.getConnection();
    }

    /**
     * Returns a list of all doctors in the system.
     *
     * @return list of {@link Doctor} objects
     * @throws SQLException if a database access error occurs
     */
    public List<Doctor> getAllDoctors() throws SQLException {
        List<Doctor> doctors = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_GET_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Doctor d = new Doctor();
                d.setId(rs.getInt("id"));
                d.setName(rs.getString("name"));
                d.setSpecialization(rs.getString("specialization"));
                d.setHospital(rs.getString("hospital"));
                d.setExperience(rs.getInt("experience"));
                d.setRating(rs.getDouble("rating"));
                d.setPhone(rs.getString("phone"));
                d.setFee(rs.getDouble("fee"));
                d.setAvailable(rs.getBoolean("available"));
                doctors.add(d);
            }
        }

        return doctors;
    }
}
