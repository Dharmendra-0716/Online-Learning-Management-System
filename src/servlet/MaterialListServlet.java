package servlet;

import java.io.IOException;
import java.util.List;

import dao.MaterialDAO;
import dao.MaterialDAO.Material;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.http.HttpSession;

import util.DBConnection;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/materials")
public class MaterialListServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final MaterialDAO materialDAO =
            new MaterialDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);

if (session == null ||
    session.getAttribute("userId") == null) {

    response.setStatus(
            HttpServletResponse.SC_UNAUTHORIZED
    );

    response.getWriter().write(
            "{\"message\":\"Please login first.\"}"
    );

    return;
}

String role =
        String.valueOf(session.getAttribute("role"));

int userId;

try {

    userId = Integer.parseInt(
            String.valueOf(
                    session.getAttribute("userId")
            )
    );

} catch (NumberFormatException exception) {

    response.setStatus(
            HttpServletResponse.SC_UNAUTHORIZED
    );

    response.getWriter().write(
            "{\"message\":\"Invalid user session.\"}"
    );

    return;
}

        String courseIdParameter =
                request.getParameter("courseId");

        if (courseIdParameter == null) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            response.getWriter().write(
                    "{\"message\":\"Course ID is required.\"}"
            );

            return;
        }

        int courseId;

        try {

            courseId =
                    Integer.parseInt(courseIdParameter);

        } catch (NumberFormatException exception) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            response.getWriter().write(
                    "{\"message\":\"Invalid course ID.\"}"
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
            """;

        try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

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
            """;

        try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setInt(1, courseId);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                allowed = rs.next();
            }
        }
    }

} catch (Exception exception) {

    exception.printStackTrace();

    response.setStatus(
            HttpServletResponse.SC_INTERNAL_SERVER_ERROR
    );

    response.getWriter().write(
            "{\"message\":\"Unable to verify course access.\"}"
    );

    return;
}

if (!allowed) {

    response.setStatus(
            HttpServletResponse.SC_FORBIDDEN
    );

    response.getWriter().write(
        "{\"message\":\"You do not have access to this course.\"}"
);

    return;
}

        List<Material> materials =
                materialDAO.getMaterialsByCourse(courseId);

        StringBuilder json =
                new StringBuilder("[");

        for (int i = 0; i < materials.size(); i++) {

            Material material =
                    materials.get(i);

            if (i > 0) {
                json.append(",");
            }

            json.append("{")
                    .append("\"materialId\":")
                    .append(material.getMaterialId())
                    .append(",")

                    .append("\"title\":\"")
                    .append(escapeJson(material.getTitle()))
                    .append("\",")

                    .append("\"description\":\"")
                    .append(escapeJson(material.getDescription()))
                    .append("\",")

                    .append("\"fileName\":\"")
                    .append(escapeJson(material.getFileName()))
                    .append("\",")

                    .append("\"materialType\":\"")
                    .append(escapeJson(material.getMaterialType()))
                    .append("\"")

                    .append("}");
        }

        json.append("]");

        response.getWriter().write(
                json.toString()
        );
    }

    private String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}