package servlet;

import java.io.IOException;
import java.util.List;

import dao.AssignmentDAO;
import dao.AssignmentDAO.Assignment;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import util.DBConnection;

@WebServlet("/assignments")
public class AssignmentListServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final AssignmentDAO assignmentDAO = new AssignmentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            sendJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "{\"success\":false,\"message\":\"Please login first.\"}");
            return;
        }

        String courseIdValue = request.getParameter("courseId");
        if (courseIdValue == null || courseIdValue.trim().isEmpty()) {
            sendJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    "{\"success\":false,\"message\":\"Course ID is required.\"}");
            return;
        }

        int courseId;
        try {
            courseId = Integer.parseInt(courseIdValue);
        } catch (NumberFormatException exception) {
            sendJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    "{\"success\":false,\"message\":\"Invalid course ID.\"}");
            return;
        }

        String role = String.valueOf(session.getAttribute("role"));

int userId;

try {
    userId = Integer.parseInt(
            String.valueOf(session.getAttribute("userId"))
    );
} catch (NumberFormatException exception) {

    sendJson(
            response,
            HttpServletResponse.SC_UNAUTHORIZED,
            "{\"success\":false,\"message\":\"Invalid user session.\"}"
    );

    return;
}

boolean allowed = false;

try (Connection con = DBConnection.getConnection()) {

    if ("STUDENT".equalsIgnoreCase(role)) {

        String sql = """
                SELECT 1
                FROM enrollments
                WHERE student_id = ?
                  AND course_id = ?
                LIMIT 1
                """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, courseId);

            try (ResultSet rs = ps.executeQuery()) {
                allowed = rs.next();
            }
        }

    } else if ("INSTRUCTOR".equalsIgnoreCase(role)) {

        String sql = """
                SELECT 1
                FROM courses
                WHERE course_id = ?
                  AND instructor_id = ?
                LIMIT 1
                """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, courseId);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                allowed = rs.next();
            }
        }
    }

} catch (Exception exception) {

    exception.printStackTrace();

    sendJson(
            response,
            HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            "{\"success\":false,\"message\":\"Unable to verify course access.\"}"
    );

    return;
}

if (!allowed) {

    sendJson(
            response,
            HttpServletResponse.SC_FORBIDDEN,
            "{\"success\":false,\"message\":\"You do not have access to this course.\"}"
    );

    return;
}

        List<Assignment> assignments = assignmentDAO.getAssignmentsByCourse(courseId);
        StringBuilder json = new StringBuilder("[");

        for (int i = 0; i < assignments.size(); i++) {
            Assignment a = assignments.get(i);
            if (i > 0) json.append(',');

            json.append('{')
                    .append("\"assignmentId\":").append(a.assignmentId).append(',')
                    .append("\"courseId\":").append(a.courseId).append(',')
                    .append("\"instructorId\":").append(a.instructorId).append(',')
                    .append("\"title\":\"").append(escapeJson(a.title)).append("\",")
                    .append("\"description\":\"").append(escapeJson(a.description)).append("\",")
                    .append("\"dueDate\":\"").append(escapeJson(String.valueOf(a.dueDate))).append("\",")
.append("\"maxMarks\":").append(a.maxMarks).append(',')
.append("\"fileName\":\"").append(escapeJson(a.fileName)).append("\",")
.append("\"filePath\":\"").append(escapeJson(a.filePath)).append("\",")
.append("\"createdAt\":\"").append(escapeJson(String.valueOf(a.createdAt))).append("\"")
                    .append('}');
        }

        json.append(']');
        sendJson(response, HttpServletResponse.SC_OK, json.toString());
    }

    private void sendJson(HttpServletResponse response, int status, String json)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
