package com.hackathon.servlet;

import com.google.gson.Gson;
import com.hackathon.dao.DoctorDAO;
import com.hackathon.model.Doctor;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST endpoint: {@code GET /doctors}
 *
 * Returns a JSON array of all doctors.
 */
@WebServlet("/doctors")
public class DoctorServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final Gson      gson      = new Gson();

    // ─── GET /doctors ────────────────────────────────────────────

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        
        // Add CORS Headers if frontend is running on a different port
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        PrintWriter out = resp.getWriter();

        try {
            List<Doctor> doctors = doctorDAO.getAllDoctors();

            resp.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(doctors));

        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            Map<String, Object> error = new LinkedHashMap<>();
            error.put("success", false);
            error.put("message", "Database error while fetching doctors.");
            error.put("detail", e.getMessage());
            out.print(gson.toJson(error));

            e.printStackTrace(); // Log to server console
        }

        out.flush();
    }
    
    // Handle OPTIONS requests for CORS preflight
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
