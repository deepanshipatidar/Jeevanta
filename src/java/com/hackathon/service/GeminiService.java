package com.hackathon.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Utility class to call the Google Gemini API (default: gemini-1.5-flash)
 * using standard {@link HttpURLConnection}.
 *
 * <p>Configuration:
 * Set the {@code GEMINI_API_KEY} environment variable before starting the application.
 */
public class GeminiService {

    private static final String DEFAULT_MODEL = "gemini-1.5-flash";

    private static final String SYSTEM_PROMPT = "You are a medical triage assistant. Cross-reference the patient's query with their medical history. "
            + "If a medicine conflicts with their allergies or chronic conditions, flag it as a CRITICAL WARNING. "
            + "Suggest the type of specialist doctor they should see. "
            + "You MUST output strictly in JSON format without markdown: "
            + "{\"analysis\": \"string\", \"criticalWarning\": \"string or null\", \"suggestedSpecialist\": \"string\"}.";

    private static final Gson gson = new Gson();

    /**
     * Resolves the Gemini API key from environment variable or system property.
     */
    private static String getApiKey() {
        String key = System.getenv("GEMINI_API_KEY");
        if (key != null && !key.trim().isEmpty()) {
            return key.trim();
        }
        key = System.getProperty("gemini.api.key");
        if (key != null && !key.trim().isEmpty()) {
            return key.trim();
        }
        // Fallback placeholder key if not provided via environment
        return "YOUR_GEMINI_API_KEY_HERE";
    }

    /**
     * Resolves the model name from environment or defaults to gemini-1.5-flash.
     */
    private static String getModelName() {
        String model = System.getenv("GEMINI_MODEL");
        if (model != null && !model.trim().isEmpty()) {
            return model.trim();
        }
        return DEFAULT_MODEL;
    }

    /**
     * Sends the patient's query and their medical history to Gemini
     * and returns the raw AI response text.
     *
     * @param patientQuery       the user's medical question
     * @param medicalHistoryJson the raw JSON string from the database containing allergies, conditions, etc.
     * @return the AI-generated JSON string
     * @throws IOException if the HTTP call fails or API key is not configured
     */
    public static String getMedicalAdvice(String patientQuery, String medicalHistoryJson) throws IOException {
        String apiKey = getApiKey();
        if ("YOUR_GEMINI_API_KEY_HERE".equals(apiKey)) {
            // Return a safe mock response when no API key is configured
            return "{\"analysis\": \"API key not configured. Please set GEMINI_API_KEY environment variable. Simulation: Consultation noted.\", \"criticalWarning\": null, \"suggestedSpecialist\": \"General Physician\"}";
        }

        String model = getModelName();
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;

        // ── Build the user message combining query + history ────────────
        String userMessage = "Patient's Medical History:\n" + medicalHistoryJson
                + "\n\nPatient's Query:\n" + patientQuery;

        // ── Construct the Gemini API request body ───────────────────────
        JsonObject systemTextPart = new JsonObject();
        systemTextPart.addProperty("text", SYSTEM_PROMPT);

        JsonArray systemParts = new JsonArray();
        systemParts.add(systemTextPart);

        JsonObject systemInstruction = new JsonObject();
        systemInstruction.add("parts", systemParts);

        JsonObject userTextPart = new JsonObject();
        userTextPart.addProperty("text", userMessage);

        JsonArray userParts = new JsonArray();
        userParts.add(userTextPart);

        JsonObject content = new JsonObject();
        content.add("parts", userParts);

        JsonArray contents = new JsonArray();
        contents.add(content);

        JsonObject requestBody = new JsonObject();
        requestBody.add("system_instruction", systemInstruction);
        requestBody.add("contents", contents);

        String jsonPayload = gson.toJson(requestBody);

        // ── Make the HTTP POST request ──────────────────────────────────
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // ── Read the response ───────────────────────────────────────────
        int responseCode = conn.getResponseCode();
        StringBuilder responseBuilder = new StringBuilder();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        responseCode >= 200 && responseCode < 300
                                ? conn.getInputStream()
                                : conn.getErrorStream(),
                        StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                responseBuilder.append(line);
            }
        } finally {
            conn.disconnect();
        }

        if (responseCode < 200 || responseCode >= 300) {
            throw new IOException("Gemini API returned HTTP " + responseCode + ": " + responseBuilder);
        }

        // ── Extract the text from the Gemini response ───────────────────
        JsonObject responseJson = gson.fromJson(responseBuilder.toString(), JsonObject.class);

        String aiText = responseJson
                .getAsJsonArray("candidates")
                .get(0).getAsJsonObject()
                .getAsJsonObject("content")
                .getAsJsonArray("parts")
                .get(0).getAsJsonObject()
                .get("text").getAsString();

        return aiText.trim();
    }
}
