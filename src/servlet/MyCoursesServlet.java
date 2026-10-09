package servlet;

import java.io.IOException;
import java.util.List;

import dao.EnrollmentDAO;
import dao.EnrollmentDAO.EnrolledCourse;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/my-courses")
public class MyCoursesServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        // Check login
        if (session == null || session.getAttribute("userId") == null) {

            sendJson(
                response,
                HttpServletResponse.SC_UNAUTHORIZED,
                "{\"message\":\"Please login first.\"}"
            );

            return;
        }

        // Check student role
        String role = String.valueOf(session.getAttribute("role"));

        if (!"STUDENT".equalsIgnoreCase(role)) {

            sendJson(
                response,
                HttpServletResponse.SC_FORBIDDEN,
                "{\"message\":\"Only students can access My Courses.\"}"
            );

            return;
        }

        int studentId;

        try {

            studentId = Integer.parseInt(
                session.getAttribute("userId").toString()
            );

        } catch (NumberFormatException exception) {

            sendJson(
                response,
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "{\"message\":\"Invalid user session.\"}"
            );

            return;
        }


        // Get enrolled courses
        List<EnrolledCourse> courses =
                enrollmentDAO.getEnrolledCourses(studentId);


        StringBuilder json = new StringBuilder("[");

        for (int i = 0; i < courses.size(); i++) {

            EnrolledCourse course = courses.get(i);

            if (i > 0) {
                json.append(",");
            }

            json.append("{")
                .append("\"courseId\":")
                .append(course.getCourseId())
                .append(",")

                .append("\"title\":\"")
                .append(escapeJson(course.getTitle()))
                .append("\",")

                .append("\"description\":\"")
                .append(escapeJson(course.getDescription()))
                .append("\",")

                .append("\"syllabus\":\"")
                .append(escapeJson(course.getSyllabus()))
                .append("\",")

                .append("\"instructorName\":\"")
                .append(escapeJson(course.getInstructorName()))
                .append("\"")

                .append("}");
        }

        json.append("]");

        response.getWriter().write(json.toString());
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


    private void sendJson(HttpServletResponse response,
                          int status,
                          String json)
            throws IOException {

        response.setStatus(status);
        response.getWriter().write(json);
    }
}