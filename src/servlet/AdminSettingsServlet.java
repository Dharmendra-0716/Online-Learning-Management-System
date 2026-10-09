package servlet;

import util.DBConnection;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Admin-only system settings endpoint.
 */
@WebServlet("/admin/settings")
public class AdminSettingsServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!isAdmin(request)) {
            send(response, HttpServletResponse.SC_FORBIDDEN, "Admin access required.");
            return;
        }

        String key = request.getParameter("key");
        String value = request.getParameter("value");

        if (!isAllowedKey(key) || !("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value))) {
            send(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid setting.");
            return;
        }

        String sql =
                "INSERT INTO system_settings (setting_key, setting_value) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, key);
            ps.setString(2, value.toLowerCase());
            ps.executeUpdate();

            send(response, HttpServletResponse.SC_OK, "Setting updated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to update setting. Run admin_setup.sql first.");
        }
    }

    private boolean isAllowedKey(String key) {
        return "registration_enabled".equals(key) ||
                "course_creation_enabled".equals(key) ||
                "email_notifications_enabled".equals(key);
    }

    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null &&
                session.getAttribute("userId") != null &&
                "ADMIN".equalsIgnoreCase(String.valueOf(session.getAttribute("role")));
    }

    private void send(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\":\"" + message.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}");
    }
}
