package com.hackathon.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
 * REST endpoint: {@code POST /api/book-bed}
 *
 * Accepts a JSON payload and atomically books one bed at the specified hospital.
 *
 * <p>Request body:
 * <pre>{ "hospitalId": 1, "userId": 123 }</pre>
 *
 * <p>Success response (200):
 * <pre>{ "success": true, "message": "Bed booked successfully at hospital 1." }</pre>
 *
 * <p>Failure response (409 / 400 / 500):
 * <pre>{ "success": false, "message": "..." }</pre>
 */
@WebServlet("/api/book-bed")
public class BookBedServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final HospitalDAO hospitalDAO = new HospitalDAO();
    private final Gson        gson        = new Gson();

    // ─── DTO for the incoming JSON body ────────────────────────────────

    /**
     * Private inner class used solely for deserialising the request JSON.
     * Kept as a nested DTO because it's tightly coupled to this endpoint.
     */
    private static class BookBedRequest {
        int hospitalId;
        int userId;
    }

    // ─── POST /api/book-bed ────────────────────────────────────────────

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        PrintWriter out = resp.getWriter();

        try {
            // ── 1. Read & parse the JSON body ──────────────────────────
            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = req.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
            }

            BookBedRequest bookReq;
            try {
                bookReq = gson.fromJson(body.toString(), BookBedRequest.class);
            } catch (JsonSyntaxException e) {
                sendError(out, resp, HttpServletResponse.SC_BAD_REQUEST,
                        "Malformed JSON in request body.");
                return;
            }

            // ── 2. Validate input ──────────────────────────────────────
            if (bookReq == null || bookReq.hospitalId <= 0 || bookReq.userId <= 0) {
                sendError(out, resp, HttpServletResponse.SC_BAD_REQUEST,
                        "Missing or invalid 'hospitalId' / 'userId'. Both must be positive integers.");
                return;
            }

            // ── 3. Attempt the booking ─────────────────────────────────
            boolean success = hospitalDAO.bookBed(bookReq.hospitalId, bookReq.userId);

            if (success) {
                resp.setStatus(HttpServletResponse.SC_OK);

                Map<String, Object> result = new LinkedHashMap<>();
                result.put("success", true);
                result.put("message", "Bed booked successfully at hospital " + bookReq.hospitalId + ".");
                out.print(gson.toJson(result));

            } else {
                // Either the hospital doesn't exist or has 0 beds
                sendError(out, resp, HttpServletResponse.SC_CONFLICT,
                        "Booking failed — hospital " + bookReq.hospitalId
                                + " not found or no beds available.");
            }

        } catch (SQLException e) {
            sendError(out, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database error: " + e.getMessage());
            e.printStackTrace(); // Log to server console

        } catch (Exception e) {
            sendError(out, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }

        out.flush();
    }

    // ─── Helper ────────────────────────────────────────────────────────

    private void sendError(PrintWriter out, HttpServletResponse resp, int status, String message) {
        resp.setStatus(status);
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("success", false);
        error.put("message", message);
        out.print(gson.toJson(error));
    }
}
