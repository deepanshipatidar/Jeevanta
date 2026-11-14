package com.hackathon.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.hackathon.dao.UserDAO;
import com.hackathon.service.GeminiService;

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
 * REST endpoint: {@code POST /api/ai-consult}
 *
 * Accepts a JSON payload with a patient's ID and medical query,
 * fetches their medical profile from the database, sends both
 * to the Gemini AI API, and returns the AI's JSON response.
 *
 * <p>Request body:
 * <pre>{ "patientId": 1, "query": "Can I take Amoxicillin?" }</pre>
 *
 * <p>Success response (200): The raw AI JSON containing analysis,
 * criticalWarning, and suggestedSpecialist fields.
 */
@WebServlet("/api/ai-consult")
public class AIConsultServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final UserDAO userDAO = new UserDAO();
    private final Gson gson = new Gson();

    // ─── DTO for the incoming JSON body ─────────────────────────────────

    private static class ConsultRequest {
        int patientId;
        String query;
    }

    // ─── POST /api/ai-consult ───────────────────────────────────────────

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

            ConsultRequest consultReq;
            try {
                consultReq = gson.fromJson(body.toString(), ConsultRequest.class);
            } catch (JsonSyntaxException e) {
                sendError(out, resp, HttpServletResponse.SC_BAD_REQUEST,
                        "Malformed JSON in request body.");
                return;
            }

            // ── 2. Validate input ───────────────────────────────────────
            if (consultReq == null || consultReq.patientId <= 0
                    || consultReq.query == null || consultReq.query.trim().isEmpty()) {
                sendError(out, resp, HttpServletResponse.SC_BAD_REQUEST,
                        "Missing or invalid 'patientId' / 'query'.");
                return;
            }

            // ── 3. Fetch the patient's medical profile from DB ──────────
            String medicalProfile = userDAO.getMedicalProfile(consultReq.patientId);

            if (medicalProfile == null || medicalProfile.trim().isEmpty()) {
                // Patient exists but has no medical profile yet — use a safe default
                medicalProfile = "{\"pastCases\": [], \"allergies\": [], \"chronicConditions\": []}";
            }

            // ── 4. Call Gemini AI with the query + medical history ───────
            String aiResponse = GeminiService.getMedicalAdvice(
                    consultReq.query.trim(), medicalProfile);

            // ── 5. Return the AI's JSON response directly to React ──────
            // The AI is instructed to return valid JSON, so we parse it
            // and wrap it in our standard envelope for consistency.
            JsonObject aiJson;
            try {
                aiJson = gson.fromJson(aiResponse, JsonObject.class);
            } catch (JsonSyntaxException e) {
                // AI returned non-JSON text — wrap it as analysis
                aiJson = new JsonObject();
                aiJson.addProperty("analysis", aiResponse);
                aiJson.addProperty("criticalWarning", (String) null);
                aiJson.addProperty("suggestedSpecialist", "General Physician");
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("analysis", aiJson.has("analysis")
                    ? aiJson.get("analysis").getAsString() : "No analysis available.");
            result.put("criticalWarning", aiJson.has("criticalWarning") && !aiJson.get("criticalWarning").isJsonNull()
                    ? aiJson.get("criticalWarning").getAsString() : null);
            result.put("suggestedSpecialist", aiJson.has("suggestedSpecialist")
                    ? aiJson.get("suggestedSpecialist").getAsString() : "General Physician");

            resp.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(result));

        } catch (SQLException e) {
            sendError(out, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database error: " + e.getMessage());
            e.printStackTrace();

        } catch (IOException e) {
            sendError(out, resp, HttpServletResponse.SC_BAD_GATEWAY,
                    "AI Service error: " + e.getMessage());
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
