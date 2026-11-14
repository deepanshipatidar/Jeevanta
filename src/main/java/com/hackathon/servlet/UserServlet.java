package com.hackathon.servlet;

import com.google.gson.Gson;
import com.hackathon.dao.UserDAO;
import com.hackathon.model.User;

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
 * REST API Controller for User Management.
 * Handles Login, Create, Update, and Delete operations purely returning JSON.
 */
@WebServlet("/api/users")
public class UserServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final Gson gson = new Gson();

    /**
     * DTO for incoming JSON payload in POST/PUT.
     */
    private static class UserRequestPayload {
        String action; // "login" or "create"
        User user;     // Used for create/update containing id, name, email, password
        String email;  // Used for login
        String password; // Used for login
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setupResponse(resp);
        PrintWriter out = resp.getWriter();

        try {
            UserRequestPayload payload = parseJson(req);
            if (payload == null) {
                sendError(out, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
                return;
            }

            // Route based on 'action'
            if ("login".equalsIgnoreCase(payload.action)) {
                
                User authenticatedUser = userDAO.authenticate(payload.email, payload.password);
                
                if (authenticatedUser != null) {
                    // Avoid sending back the password in the response for security
                    authenticatedUser.setPassword(null);
                    sendSuccess(out, resp, "Login successful.", authenticatedUser);
                } else {
                    sendError(out, resp, HttpServletResponse.SC_UNAUTHORIZED, "Invalid credentials.");
                }

            } else {
                
                // Default to createUser
                if (payload.user == null || payload.user.getEmail() == null || payload.user.getPassword() == null) {
                    sendError(out, resp, HttpServletResponse.SC_BAD_REQUEST, "Missing user details for creation.");
                    return;
                }

                boolean success = userDAO.createUser(payload.user);
                if (success) {
                    sendSuccess(out, resp, "User created successfully.", null);
                } else {
                    sendError(out, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create user.");
                }
            }

        } catch (SQLException e) {
            sendError(out, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(out, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setupResponse(resp);
        PrintWriter out = resp.getWriter();

        try {
            UserRequestPayload payload = parseJson(req);
            if (payload == null || payload.user == null || payload.user.getId() <= 0) {
                sendError(out, resp, HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid user details for update.");
                return;
            }

            boolean success = userDAO.updateUser(payload.user);
            if (success) {
                sendSuccess(out, resp, "User updated successfully.", null);
            } else {
                sendError(out, resp, HttpServletResponse.SC_NOT_FOUND, "User not found or update failed.");
            }

        } catch (SQLException e) {
            sendError(out, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(out, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setupResponse(resp);
        PrintWriter out = resp.getWriter();

        try {
            String idParam = req.getParameter("id");
            if (idParam == null || idParam.trim().isEmpty()) {
                sendError(out, resp, HttpServletResponse.SC_BAD_REQUEST, "Missing user ID for deletion.");
                return;
            }

            int id = Integer.parseInt(idParam);
            boolean success = userDAO.deleteUser(id);
            
            if (success) {
                sendSuccess(out, resp, "User deleted successfully.", null);
            } else {
                sendError(out, resp, HttpServletResponse.SC_NOT_FOUND, "User not found.");
            }

        } catch (NumberFormatException e) {
            sendError(out, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format.");
        } catch (SQLException e) {
            sendError(out, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(out, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        } finally {
            out.flush();
        }
    }

    // ─── Helper Methods ──────────────────────────────────────────────────

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setupResponse(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private void setupResponse(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
    }

    private UserRequestPayload parseJson(HttpServletRequest req) throws IOException {
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }
        if (body.length() == 0) return null;
        return gson.fromJson(body.toString(), UserRequestPayload.class);
    }

    private void sendSuccess(PrintWriter out, HttpServletResponse resp, String message, User user) {
        resp.setStatus(HttpServletResponse.SC_OK);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", message);
        if (user != null) {
            result.put("user", user);
        }
        out.print(gson.toJson(result));
    }

    private void sendError(PrintWriter out, HttpServletResponse resp, int status, String message) {
        resp.setStatus(status);
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("success", false);
        error.put("message", message);
        out.print(gson.toJson(error));
    }
}
