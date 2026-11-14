package com.hackathon.servlet;

import com.google.gson.Gson;
import com.hackathon.dao.HospitalDAO;
import com.hackathon.model.Hospital;

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
 * REST endpoint: {@code GET /api/hospitals}
 *
 * Returns a JSON array of all hospitals that have at least one available bed.
 *
 * <p>Example response:
 * <pre>
 * [
 *   { "id": 1, "name": "City General", "lat": 28.61, "lng": 77.20, "availableBeds": 12 },
 *   ...
 * ]
 * </pre>
 */
@WebServlet("/api/hospitals")
public class HospitalServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final HospitalDAO hospitalDAO = new HospitalDAO();
    private final Gson        gson        = new Gson();

    // ─── GET /api/hospitals ────────────────────────────────────────────

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        PrintWriter out = resp.getWriter();

        try {
            List<Hospital> hospitals = hospitalDAO.getAllAvailableHospitals();

            resp.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(hospitals));

        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            Map<String, Object> error = new LinkedHashMap<>();
            error.put("success", false);
            error.put("message", "Database error while fetching hospitals.");
            error.put("detail", e.getMessage());
            out.print(gson.toJson(error));

            e.printStackTrace(); // Log to server console
        }

        out.flush();
    }
}
