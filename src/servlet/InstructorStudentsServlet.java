package servlet;

import dao.EnrollmentDAO;
import dao.EnrollmentDAO.EnrolledStudent;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet("/instructor-students")
public class InstructorStudentsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final EnrollmentDAO enrollmentDAO =
            new EnrollmentDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // =========================
        // CHECK LOGIN
        // =========================

        HttpSession session =
                request.getSession(false);

        if (session == null ||
                session.getAttribute("userId") == null) {

            sendJson(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "{\"message\":\"Please login first.\"}"
            );

            return;
        }

        // =========================
        // CHECK INSTRUCTOR ROLE
        // =========================

        String role =
                String.valueOf(
                        session.getAttribute("role")
                );

        if (!"INSTRUCTOR".equalsIgnoreCase(role)) {

            sendJson(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "{\"message\":\"Only instructors can view students.\"}"
            );

            return;
        }

        // =========================
        // GET INSTRUCTOR ID
        // =========================

        int instructorId;

        try {

            instructorId = Integer.parseInt(
                    String.valueOf(
                            session.getAttribute("userId")
                    )
            );

        } catch (NumberFormatException exception) {

            sendJson(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "{\"message\":\"Invalid user session.\"}"
            );

            return;
        }

        // =========================
        // GET STUDENTS
        // =========================

        List<EnrolledStudent> students =
                enrollmentDAO.getStudentsByInstructor(
                        instructorId
                );

        // =========================
        // BUILD JSON
        // =========================

        StringBuilder json =
                new StringBuilder("[");

        for (int i = 0; i < students.size(); i++) {

            EnrolledStudent student =
                    students.get(i);

            if (i > 0) {
                json.append(",");
            }

            json.append("{")

                    .append("\"studentId\":")
                    .append(student.getStudentId())
                    .append(",")

                    .append("\"studentName\":\"")
                    .append(escapeJson(
                            student.getStudentName()))
                    .append("\",")

                    .append("\"email\":\"")
                    .append(escapeJson(
                            student.getEmail()))
                    .append("\",")

                    .append("\"courseId\":")
                    .append(student.getCourseId())
                    .append(",")

                    .append("\"courseTitle\":\"")
                    .append(escapeJson(
                            student.getCourseTitle()))
                    .append("\",")

                    .append("\"enrollmentDate\":\"")
                    .append(escapeJson(
                            String.valueOf(
                                    student.getEnrollmentDate())))
                    .append("\"")

                    .append("}");
        }

        json.append("]");

        response.getWriter().write(
                json.toString()
        );
    }


    // =========================
    // JSON ESCAPE
    // =========================

    private String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }


    // =========================
    // SEND JSON
    // =========================

    private void sendJson(
            HttpServletResponse response,
            int status,
            String json)
            throws IOException {

        response.setStatus(status);
        response.getWriter().write(json);
    }
}