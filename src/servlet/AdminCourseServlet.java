package servlet;

import dao.AdminDAO;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Admin-only course CRUD endpoint.
 */
@WebServlet("/admin/courses")
public class AdminCourseServlet extends HttpServlet {

    private final AdminDAO adminDAO = new AdminDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!isAdmin(request)) {
            send(response, HttpServletResponse.SC_FORBIDDEN, "Admin access required.");
            return;
        }

        String idText = trim(request.getParameter("id"));
        String title = trim(request.getParameter("title"));
        String description = trim(request.getParameter("description"));
        String syllabus = trim(request.getParameter("syllabus"));
        String instructorText = trim(request.getParameter("instructorId"));

        if (title.isEmpty() || instructorText.isEmpty()) {
            send(response, HttpServletResponse.SC_BAD_REQUEST, "Course title and instructor are required.");
            return;
        }

        try {
            int instructorId = Integer.parseInt(instructorText);

            if (idText.isEmpty()) {
                adminDAO.createCourse(title, description, syllabus, instructorId);
                send(response, HttpServletResponse.SC_OK, "Course created successfully.");
            } else {
                int id = Integer.parseInt(idText);
                adminDAO.updateCourse(id, title, description, syllabus, instructorId);
                send(response, HttpServletResponse.SC_OK, "Course updated successfully.");
            }
        } catch (NumberFormatException e) {
            send(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid course or instructor ID.");
        } catch (Exception e) {
            e.printStackTrace();
            send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to save course.");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!isAdmin(request)) {
            send(response, HttpServletResponse.SC_FORBIDDEN, "Admin access required.");
            return;
        }

        try {
            int id = Integer.parseInt(request.getParameter("id"));

            if (adminDAO.courseHasEnrollments(id)) {
                send(response, HttpServletResponse.SC_CONFLICT,
                        "This course has enrolled students. Remove enrollments before deleting it.");
                return;
            }

            if (!adminDAO.deleteCourse(id)) {
                send(response, HttpServletResponse.SC_NOT_FOUND, "Course not found.");
                return;
            }

            send(response, HttpServletResponse.SC_OK, "Course deleted successfully.");

        } catch (NumberFormatException e) {
            send(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid course ID.");
        } catch (Exception e) {
            e.printStackTrace();
            send(response, HttpServletResponse.SC_CONFLICT,
                    "Course could not be deleted because related records still exist.");
        }
    }

    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null &&
                session.getAttribute("userId") != null &&
                "ADMIN".equalsIgnoreCase(String.valueOf(session.getAttribute("role")));
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private void send(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\":\"" + message.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}");
    }
}
