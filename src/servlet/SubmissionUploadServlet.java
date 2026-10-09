package servlet;

import dao.AssignmentDAO;
import dao.AssignmentDAO.Assignment;
import dao.SubmissionDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import util.DBConnection;

@WebServlet("/submit-assignment")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 10 * 1024 * 1024,
        maxRequestSize = 15 * 1024 * 1024
)
public class SubmissionUploadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final SubmissionDAO submissionDAO =
            new SubmissionDAO();

    private final AssignmentDAO assignmentDAO =
            new AssignmentDAO();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // ================= SESSION CHECK =================

        HttpSession session =
                request.getSession(false);

        if (session == null ||
                session.getAttribute("userId") == null) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED);

            response.getWriter().write(
                    "{\"message\":\"Please login first.\"}");

            return;
        }

        // ================= ROLE CHECK =================

        String role =
                String.valueOf(
                        session.getAttribute("role"));

        if (!"STUDENT".equalsIgnoreCase(role)) {

            response.setStatus(
                    HttpServletResponse.SC_FORBIDDEN);

            response.getWriter().write(
                    "{\"message\":\"Only students can submit assignments.\"}");

            return;
        }

        // ================= STUDENT ID =================

        int studentId;

        try {

            studentId =
                    Integer.parseInt(
                            String.valueOf(
                                    session.getAttribute("userId")));

        } catch (NumberFormatException e) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED);

            response.getWriter().write(
                    "{\"message\":\"Invalid user session.\"}");

            return;
        }

        // ================= REQUEST DATA =================

        String assignmentIdText =
                request.getParameter("assignmentId");

        Part filePart =
                request.getPart("file");

        if (assignmentIdText == null ||
                assignmentIdText.isBlank() ||
                filePart == null ||
                filePart.getSize() == 0) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST);

            response.getWriter().write(
                    "{\"message\":\"Assignment and file are required.\"}");

            return;
        }

        // ================= ASSIGNMENT ID =================

        int assignmentId;

        try {

            assignmentId =
                    Integer.parseInt(assignmentIdText);

        } catch (NumberFormatException e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST);

            response.getWriter().write(
                    "{\"message\":\"Invalid assignment ID.\"}");

            return;
        }

        // ================= GET ASSIGNMENT =================

        Assignment assignment =
                assignmentDAO.getAssignmentById(
                        assignmentId);

        if (assignment == null) {

            response.setStatus(
                    HttpServletResponse.SC_NOT_FOUND);

            response.getWriter().write(
                    "{\"message\":\"Assignment not found.\"}");

            return;
        }

        // ================= ENROLLMENT CHECK =================

        boolean enrolled = false;

        try (Connection connection =
                     DBConnection.getConnection()) {

            String sql = """
                    SELECT 1
                    FROM enrollments
                    WHERE student_id = ?
                      AND course_id = ?
                    LIMIT 1
                    """;

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setInt(1, studentId);

                statement.setInt(
                        2,
                        assignment.getCourseId());

                try (ResultSet result =
                             statement.executeQuery()) {

                    enrolled = result.next();
                }
            }

        } catch (Exception exception) {

            exception.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            response.getWriter().write(
                    "{\"message\":\"Unable to verify course enrollment.\"}");

            return;
        }

        if (!enrolled) {

            response.setStatus(
                    HttpServletResponse.SC_FORBIDDEN);

            response.getWriter().write(
                    "{\"message\":\"You are not enrolled in this course.\"}");

            return;
        }

        // ================= DUE DATE CHECK =================

        LocalDateTime now =
                LocalDateTime.now();

        if (!now.isBefore(
                assignment.getDueDate()
                        .toLocalDateTime())) {

            response.setStatus(
                    HttpServletResponse.SC_CONFLICT);

            response.getWriter().write(
                    "{\"message\":\"Submission is closed. The due date has passed.\"}");

            return;
        }

        // ================= EXISTING SUBMISSION =================

        SubmissionDAO.Submission existingSubmission =
                submissionDAO.getStudentSubmission(
                        assignmentId,
                        studentId);

        // ================= GRADED CHECK =================

        if (existingSubmission != null &&
                existingSubmission.getMarks() != null) {

            response.setStatus(
                    HttpServletResponse.SC_CONFLICT);

            response.getWriter().write(
                    "{\"message\":\"This submission has already been graded and cannot be edited.\"}");

            return;
        }

        // ================= FILE NAME =================

        String originalFileName =
                Paths.get(
                        filePart.getSubmittedFileName())
                        .getFileName()
                        .toString();

        if (originalFileName.isBlank()) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST);

            response.getWriter().write(
                    "{\"message\":\"Invalid file.\"}");

            return;
        }

        // ================= SERVER FILE NAME =================

        String storedFileName =
                System.currentTimeMillis()
                        + "_"
                        + originalFileName.replaceAll(
                                "[^a-zA-Z0-9._-]",
                                "_");

        // ================= UPLOAD DIRECTORY =================

        String uploadDirectory =
                getServletContext()
                        .getRealPath("/uploads");

        File uploadDir =
                new File(uploadDirectory);

        if (!uploadDir.exists() &&
                !uploadDir.mkdirs()) {

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            response.getWriter().write(
                    "{\"message\":\"Unable to create upload directory.\"}");

            return;
        }

        // ================= SAVE FILE =================

        File destination =
                new File(
                        uploadDir,
                        storedFileName);

        filePart.write(
                destination.getAbsolutePath());

        String filePath =
                "uploads/" + storedFileName;

        // ================= CREATE / UPDATE =================

        boolean saved;

        if (existingSubmission == null) {

            saved =
                    submissionDAO.createSubmission(
                            assignmentId,
                            studentId,
                            originalFileName,
                            filePath);

        } else {

            saved =
                    submissionDAO.updateSubmission(
                            assignmentId,
                            studentId,
                            originalFileName,
                            filePath);
        }

        // ================= DATABASE FAILURE =================

        if (!saved) {

            destination.delete();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            response.getWriter().write(
                    "{\"message\":\"Could not save submission.\"}");

            return;
        }

        // ================= DELETE OLD FILE =================

        if (existingSubmission != null &&
                existingSubmission.getFilePath() != null) {

            String oldPath =
                    getServletContext().getRealPath(
                            "/" +
                            existingSubmission.getFilePath());

            if (oldPath != null) {

                File oldFile =
                        new File(oldPath);

                if (oldFile.exists()) {
                    oldFile.delete();
                }
            }
        }

        // ================= SUCCESS =================

        if (existingSubmission == null) {

            response.getWriter().write(
                    "{\"message\":\"Assignment submitted successfully.\"}");

        } else {

            response.getWriter().write(
                    "{\"message\":\"Submission updated successfully.\"}");
        }
    }
}