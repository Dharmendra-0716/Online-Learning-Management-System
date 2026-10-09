package servlet;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import dao.AssignmentDAO;
import dao.CourseDAO;
import dao.CourseDAO.Course;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.Part;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 20 * 1024 * 1024,
        maxRequestSize = 25 * 1024 * 1024
)
@WebServlet("/create-assignment")
public class CreateAssignmentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final AssignmentDAO assignmentDAO =
            new AssignmentDAO();

    private final CourseDAO courseDAO =
            new CourseDAO();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session =
                request.getSession(false);

        // Check login
        if (session == null ||
                session.getAttribute("userId") == null) {

            sendJson(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Please login first."
            );

            return;
        }

        // Check instructor role
        String role =
                String.valueOf(
                        session.getAttribute("role")
                );

        if (!"INSTRUCTOR".equalsIgnoreCase(role)) {

            sendJson(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Only instructors can create assignments."
            );

            return;
        }

        try {

            int instructorId =
                    Integer.parseInt(
                            session.getAttribute("userId")
                                    .toString()
                    );

            String courseIdParam =
                    request.getParameter("courseId");

            String title =
                    request.getParameter("title");

            String description =
                    request.getParameter("description");

            String dueDateParam =
                    request.getParameter("dueDate");

            String maxMarksParam =
                    request.getParameter("maxMarks");

            Part assignmentFilePart =
                    request.getPart("assignmentFile");

        if (assignmentFilePart == null ||
        assignmentFilePart.getSize() == 0) {

    sendJson(
            response,
            HttpServletResponse.SC_BAD_REQUEST,
            "Please select an assignment file."
    );

    return;
}

            // Validate required fields
            if (courseIdParam == null ||
                    title == null ||
                    description == null ||
                    dueDateParam == null ||
                    maxMarksParam == null ||
                    title.trim().isEmpty() ||
                    description.trim().isEmpty()) {

                sendJson(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "All assignment fields are required."
                );

                return;
            }

            int courseId =
                    Integer.parseInt(
                            courseIdParam.trim()
                    );

            int maxMarks =
                    Integer.parseInt(
                            maxMarksParam.trim()
                    );

            if (maxMarks <= 0) {

                sendJson(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Maximum marks must be greater than 0."
                );

                return;
            }

            // Check course
            Course course =
                    courseDAO.getCourseById(courseId);

            if (course == null) {

                sendJson(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Course not found."
                );

                return;
            }

            // Instructor can create assignment
            // only for their own course
            if (course.getInstructorId() != instructorId) {

                sendJson(
                        response,
                        HttpServletResponse.SC_FORBIDDEN,
                        "You can only create assignments for your own courses."
                );

                return;
            }

            // Save assignment file
String uploadDirectory =
        getServletContext().getRealPath("/uploads");

java.io.File uploadFolder =
        new java.io.File(uploadDirectory);

if (!uploadFolder.exists()) {
    uploadFolder.mkdirs();
}

String originalFileName =
        assignmentFilePart.getSubmittedFileName();

if (originalFileName == null ||
        originalFileName.isBlank()) {

    sendJson(
            response,
            HttpServletResponse.SC_BAD_REQUEST,
            "Invalid assignment file."
    );

    return;
}

String safeFileName =
        originalFileName.replaceAll(
                "[^a-zA-Z0-9._-]",
                "_"
        );

String storedFileName =
        System.currentTimeMillis()
        + "_"
        + safeFileName;

java.io.File destination =
        new java.io.File(
                uploadFolder,
                storedFileName
        );

assignmentFilePart.write(
        destination.getAbsolutePath()
);

String assignmentFilePath =
        "uploads/" + storedFileName;

            // Convert HTML datetime-local
            // Example:
            // 2026-10-30T19:58
            LocalDateTime localDateTime =
                    LocalDateTime.parse(
                            dueDateParam
                    );

            Timestamp dueDate =
                    Timestamp.valueOf(
                            localDateTime
                    );

boolean created =
        assignmentDAO.createAssignment(
                courseId,
                instructorId,
                title.trim(),
                description.trim(),
                dueDate,
                maxMarks,
                originalFileName,
                assignmentFilePath
        );

            if (!created) {

                sendJson(
                        response,
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Unable to create assignment."
                );

                return;
            }

            sendJson(
                    response,
                    HttpServletResponse.SC_OK,
                    "Assignment created successfully."
            );

        } catch (NumberFormatException e) {

            sendJson(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid course or marks value."
            );

        } catch (Exception e) {

            e.printStackTrace();

            sendJson(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Server error while creating assignment."
            );
        }
    }

    private void sendJson(
            HttpServletResponse response,
            int status,
            String message)
            throws IOException {

        response.setStatus(status);

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        String safeMessage =
                message.replace(
                        "\"",
                        "\\\""
                );

        response.getWriter().write(
                "{\"message\":\""
                        + safeMessage
                        + "\"}"
        );
    }
}