package servlet;

import dao.AdminDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import util.DBConnection;

/**
 * Returns all data required by the Admin Dashboard.
 */
@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private final AdminDAO adminDAO = new AdminDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null ||
                !"ADMIN".equalsIgnoreCase(String.valueOf(session.getAttribute("role")))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"message\":\"Admin access required.\"}");
            return;
        }

        try {
            AdminDAO.Stats stats = adminDAO.getStats();

            StringBuilder json = new StringBuilder("{");
            json.append("\"adminName\":\"").append(escape(String.valueOf(session.getAttribute("userName")))).append("\",");

            json.append("\"stats\":{");
            json.append("\"totalUsers\":").append(stats.totalUsers).append(",");
            json.append("\"totalStudents\":").append(stats.totalStudents).append(",");
            json.append("\"totalInstructors\":").append(stats.totalInstructors).append(",");
            json.append("\"totalCourses\":").append(stats.totalCourses).append(",");
            json.append("\"totalEnrollments\":").append(stats.totalEnrollments).append(",");
            json.append("\"totalSubmissions\":").append(stats.totalSubmissions).append(",");
            json.append("\"gradedSubmissions\":").append(stats.gradedSubmissions).append(",");
            json.append("\"averageMarks\":").append(stats.averageMarks).append(",");
            json.append("\"avgEnrollment\":").append(stats.avgEnrollment).append(",");
            json.append("\"recentRegistrations\":").append(stats.recentRegistrations);
            json.append("},");

            json.append("\"users\":[");
            boolean first = true;
            for (AdminDAO.UserRow u : adminDAO.getUsers()) {
                if (!first) json.append(",");
                first = false;
                json.append("{");
                json.append("\"userId\":").append(u.userId).append(",");
                json.append("\"name\":\"").append(escape(u.name)).append("\",");
                json.append("\"email\":\"").append(escape(u.email)).append("\",");
                json.append("\"role\":\"").append(escape(u.role)).append("\",");
                json.append("\"createdAt\":\"").append(escape(String.valueOf(u.createdAt))).append("\"");
                json.append("}");
            }
            json.append("],");

            json.append("\"courses\":[");
            first = true;
            for (AdminDAO.CourseRow c : adminDAO.getCourses()) {
                if (!first) json.append(",");
                first = false;
                json.append("{");
                json.append("\"courseId\":").append(c.courseId).append(",");
                json.append("\"title\":\"").append(escape(c.title)).append("\",");
                json.append("\"description\":\"").append(escape(c.description)).append("\",");
                json.append("\"syllabus\":\"").append(escape(c.syllabus)).append("\",");
                json.append("\"instructorId\":").append(c.instructorId).append(",");
                json.append("\"instructorName\":\"").append(escape(c.instructorName)).append("\",");
                json.append("\"studentCount\":").append(c.studentCount);
                json.append("}");
            }
            json.append("],");

            json.append("\"activity\":[");
            first = true;
            for (AdminDAO.ActivityRow a : adminDAO.getRecentActivity()) {
                if (!first) json.append(",");
                first = false;
                json.append("{");
                json.append("\"icon\":\"").append(escape(a.icon)).append("\",");
                json.append("\"title\":\"").append(escape(a.title)).append("\",");
                json.append("\"description\":\"").append(escape(a.description)).append("\",");
                json.append("\"time\":\"").append(escape(a.time)).append("\"");
                json.append("}");
            }
            json.append("],");

            json.append("\"settings\":{");
            json.append("\"registration_enabled\":").append(getSetting("registration_enabled", true)).append(",");
            json.append("\"course_creation_enabled\":").append(getSetting("course_creation_enabled", true)).append(",");
            json.append("\"email_notifications_enabled\":").append(getSetting("email_notifications_enabled", true));
            json.append("}");

            json.append("}");
            response.getWriter().write(json.toString());

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\":\"Unable to load admin dashboard.\"}");
        }
    }

    private boolean getSetting(String key, boolean defaultValue) {
        String sql = "SELECT setting_value FROM system_settings WHERE setting_key = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return "true".equalsIgnoreCase(rs.getString(1));
            }
        } catch (Exception ignored) {
            // The setup SQL creates this table. Until then, use a safe default.
        }
        return defaultValue;
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
