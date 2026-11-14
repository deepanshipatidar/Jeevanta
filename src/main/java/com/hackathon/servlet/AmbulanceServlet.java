package com.hackathon.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hackathon.dao.HospitalDAO;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * REST endpoint: {@code POST /api/book-ambulance}
 *
 * Books a bed at the selected hospital AND dispatches an ambulance.
 * Returns the ambulance/driver details so the React frontend can
 * display them on the live tracking map.
 *
 * <p>Request body:
 * <pre>{ "hospitalId": 1, "userId": 123, "userLat": 23.25, "userLng": 77.41 }</pre>
 *
 * <p>Success response (200):
 * <pre>{
 *   "success": true,
 *   "message": "Ambulance dispatched...",
 *   "ambulance": {
 *     "driverName": "Rahul Sharma",
 *     "driverPhone": "+91 9876543220",
 *     "vehicleNumber": "MP09 XX 1111",
 *     "ambulanceType": "Advanced Life Support (ALS)",
 *     "estimatedArrival": "8 minutes"
 *   }
 * }</pre>
 */
@WebServlet("/api/book-ambulance")
public class AmbulanceServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final HospitalDAO hospitalDAO = new HospitalDAO();
    private final Gson gson = new Gson();

    // ─── DTO for the incoming JSON body ─────────────────────────────────

    private static class AmbulanceRequest {
        int hospitalId;
        int userId;
        double userLat;
        double userLng;
    }

    // ─── Simulated ambulance/driver pool ────────────────────────────────
    // In a production system this would come from a database table.

    private static final String[][] DRIVER_POOL = {
        {"Rahul Sharma",   "+91 9876543220", "MP09 XX 1111", "Advanced Life Support (ALS)"},
        {"Priya Verma",    "+91 9876543221", "MP09 YY 2222", "Basic Life Support (BLS)"},
        {"Amit Patel",     "+91 9876543222", "MP09 ZZ 3333", "Advanced Life Support (ALS)"},
        {"Sneha Gupta",    "+91 9876543223", "MP09 AA 4444", "Patient Transport (PT)"},
        {"Vikram Singh",   "+91 9876543224", "MP09 BB 5555", "Advanced Life Support (ALS)"},
    };

    // ─── POST /api/book-ambulance ───────────────────────────────────────

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        PrintWriter out = resp.getWriter();

        try {
            // ── 1. Read & parse the JSON body ───────────────────────────
            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = req.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
            }

            AmbulanceRequest ambReq;
            try {
                ambReq = gson.fromJson(body.toString(), AmbulanceRequest.class);
            } catch (JsonSyntaxException e) {
                sendError(out, resp, HttpServletResponse.SC_BAD_REQUEST,
                        "Malformed JSON in request body.");
                return;
            }

            // ── 2. Validate input ───────────────────────────────────────
            if (ambReq == null || ambReq.hospitalId <= 0 || ambReq.userId <= 0) {
                sendError(out, resp, HttpServletResponse.SC_BAD_REQUEST,
                        "Missing or invalid 'hospitalId' / 'userId'.");
                return;
            }

            // ── 3. Book the bed (same atomic transaction) ───────────────
            boolean bedBooked = hospitalDAO.bookBed(ambReq.hospitalId, ambReq.userId);

            if (!bedBooked) {
                sendError(out, resp, HttpServletResponse.SC_CONFLICT,
                        "Booking failed — hospital " + ambReq.hospitalId
                                + " not found or no beds available.");
                return;
            }

            // ── 4. Assign an ambulance/driver ───────────────────────────
            // Pick a driver based on hospitalId (deterministic for demo)
            String[] driver = DRIVER_POOL[ambReq.hospitalId % DRIVER_POOL.length];

            Map<String, String> ambulanceDetails = new LinkedHashMap<>();
            ambulanceDetails.put("driverName",    driver[0]);
            ambulanceDetails.put("driverPhone",   driver[1]);
            ambulanceDetails.put("vehicleNumber", driver[2]);
            ambulanceDetails.put("ambulanceType", driver[3]);
            ambulanceDetails.put("estimatedArrival", "8 minutes");

            // ── 5. Build success response ───────────────────────────────
            resp.setStatus(HttpServletResponse.SC_OK);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("message", "Bed reserved & ambulance dispatched from hospital " + ambReq.hospitalId + ".");
            result.put("ambulance", ambulanceDetails);
            out.print(gson.toJson(result));

        } catch (SQLException e) {
            sendError(out, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database error: " + e.getMessage());
            e.printStackTrace();

        } catch (Exception e) {
            sendError(out, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }

        out.flush();
    }

    // ─── Helper ─────────────────────────────────────────────────────────

    private void sendError(PrintWriter out, HttpServletResponse resp, int status, String message) {
        resp.setStatus(status);
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("success", false);
        error.put("message", message);
        out.print(gson.toJson(error));
    }
}
