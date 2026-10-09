package servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;

import dao.AssignmentDAO;
import dao.AssignmentDAO.Assignment;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/download-assignment")
public class AssignmentDownloadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final AssignmentDAO assignmentDAO = new AssignmentDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null ||
                session.getAttribute("userId") == null) {

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Please login first."
            );

            return;
        }

        String assignmentIdValue =
                request.getParameter("assignmentId");

        if (assignmentIdValue == null ||
                assignmentIdValue.trim().isEmpty()) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Assignment ID is required."
            );

            return;
        }

        int assignmentId;

        try {
            assignmentId =
                    Integer.parseInt(assignmentIdValue);

        } catch (NumberFormatException exception) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid assignment ID."
            );

            return;
        }

        int userId;

        try {
            userId = Integer.parseInt(
                    String.valueOf(
                            session.getAttribute("userId")
                    )
            );

        } catch (NumberFormatException exception) {

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid user session."
            );

            return;
        }

        String role =
                String.valueOf(session.getAttribute("role"));

        Assignment assignment =
                assignmentDAO.getAssignmentById(assignmentId);

        if (assignment == null) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Assignment not found."
            );

            return;
        }

        /*
         * Only the instructor who owns the assignment
         * or a student enrolled in the course
         * can download the assignment.
         */

        boolean allowed = false;

        if ("INSTRUCTOR".equalsIgnoreCase(role)) {

            allowed =
                    assignment.instructorId == userId;

        } else if ("STUDENT".equalsIgnoreCase(role)) {

            allowed = isStudentEnrolled(
                    userId,
                    assignment.courseId
            );
        }

        if (!allowed) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "You do not have permission to download this assignment."
            );

            return;
        }

        if (assignment.filePath == null ||
                assignment.filePath.isBlank()) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Assignment file not found."
            );

            return;
        }

        String realPath =
                getServletContext().getRealPath(
                        "/" + assignment.filePath
                );

        if (realPath == null) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Assignment file not found."
            );

            return;
        }

        File file = new File(realPath);

        if (!file.exists() || !file.isFile()) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Assignment file not found."
            );

            return;
        }

        response.setContentType(
                getServletContext().getMimeType(
                        file.getName()
                )
        );

        response.setContentLengthLong(file.length());

        response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"" +
                        assignment.fileName +
                        "\""
        );

        try (
                InputStream input =
                        new java.io.FileInputStream(file);

                OutputStream output =
                        response.getOutputStream()
        ) {

            byte[] buffer = new byte[8192];

            int bytesRead;

            while ((bytesRead =
                    input.read(buffer)) != -1) {

                output.write(
                        buffer,
                        0,
                        bytesRead
                );
            }
        }
    }

    private boolean isStudentEnrolled(
            int studentId,
            int courseId) {

        String sql =
                "SELECT 1 FROM enrollments " +
                "WHERE student_id = ? " +
                "AND course_id = ? " +
                "LIMIT 1";

        try (
                java.sql.Connection connection =
                        util.DBConnection.getConnection();

                java.sql.PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, studentId);
            statement.setInt(2, courseId);

            try (
                    java.sql.ResultSet resultSet =
                            statement.executeQuery()
            ) {

                return resultSet.next();
            }

        } catch (Exception exception) {

            exception.printStackTrace();

            return false;
        }
    }
}