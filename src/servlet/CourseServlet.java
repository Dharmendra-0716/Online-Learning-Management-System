package servlet;

import java.io.IOException;
import java.util.List;

import dao.CourseDAO;
import dao.CourseDAO.Course;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/courses")
public class CourseServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final CourseDAO courseDAO = new CourseDAO();

    // =========================================================
    // GET COURSES
    // =========================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String courseIdParameter =
                request.getParameter("courseId");

        // Get one course
        if (courseIdParameter != null
                && !courseIdParameter.isBlank()) {

            int courseId;

            try {

                courseId =
                        Integer.parseInt(courseIdParameter);

            } catch (NumberFormatException exception) {

                sendJson(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "{\"message\":\"Invalid course ID.\"}"
                );

                return;
            }

            Course course =
                    courseDAO.getCourseById(courseId);

            if (course == null) {

                sendJson(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "{\"message\":\"Course not found.\"}"
                );

                return;
            }

            response.getWriter()
                    .write(courseToJson(course));

            return;
        }

        // Get all courses
        List<Course> courses =
                courseDAO.getAllCourses();

        StringBuilder json =
                new StringBuilder("[");

        for (int i = 0;
             i < courses.size();
             i++) {

            if (i > 0) {
                json.append(",");
            }

            json.append(
                    courseToJson(courses.get(i))
            );
        }

        json.append("]");

        response.getWriter()
                .write(json.toString());
    }


    // =========================================================
    // CREATE COURSE
    // =========================================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session =
                request.getSession(false);

        // Login check
        if (session == null
                || session.getAttribute("userId") == null) {

            sendJson(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "{\"message\":\"Please login first.\"}"
            );

            return;
        }

        // Role check
        String role =
                (String) session.getAttribute("role");

        if (!"INSTRUCTOR".equalsIgnoreCase(role)) {

            sendJson(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "{\"message\":\"Only instructors can create courses.\"}"
            );

            return;
        }

        // Get logged-in instructor ID
        Integer instructorId =
                (Integer) session.getAttribute("userId");

        if (instructorId == null) {

            sendJson(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "{\"message\":\"Invalid instructor session.\"}"
            );

            return;
        }

        // Get form data
        String title =
                request.getParameter("title");

        String description =
                request.getParameter("description");

        String syllabus =
                request.getParameter("syllabus");

        // Validation
        if (title == null
                || title.isBlank()) {

            sendJson(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "{\"message\":\"Course title is required.\"}"
            );

            return;
        }

        if (description == null) {
            description = "";
        }

        if (syllabus == null) {
            syllabus = "";
        }

        // Create course
        boolean created =
                courseDAO.createCourse(
                        title.trim(),
                        description.trim(),
                        syllabus.trim(),
                        instructorId
                );

        if (!created) {

            sendJson(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "{\"message\":\"Unable to create course.\"}"
            );

            return;
        }

        sendJson(
                response,
                HttpServletResponse.SC_OK,
                "{\"message\":\"Course created successfully.\"}"
        );
    }


    // =========================================================
    // COURSE → JSON
    // =========================================================

    private String courseToJson(Course course) {

        return "{"
                + "\"courseId\":"
                + course.getCourseId()
                + ","
                + "\"title\":\""
                + escapeJson(course.getTitle())
                + "\","
                + "\"description\":\""
                + escapeJson(course.getDescription())
                + "\","
                + "\"syllabus\":\""
                + escapeJson(course.getSyllabus())
                + "\","
                + "\"instructorId\":"
                + course.getInstructorId()
                + ","
                + "\"instructorName\":\""
                + escapeJson(course.getInstructorName())
                + "\""
                + "}";
    }


    // =========================================================
    // JSON ESCAPE
    // =========================================================

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


    // =========================================================
    // JSON RESPONSE
    // =========================================================

    private void sendJson(
            HttpServletResponse response,
            int status,
            String json)
            throws IOException {

        response.setStatus(status);
        response.getWriter().write(json);
    }
}