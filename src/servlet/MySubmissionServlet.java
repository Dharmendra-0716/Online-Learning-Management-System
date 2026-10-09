package servlet;

import dao.AssignmentDAO;
import dao.EnrollmentDAO;
import dao.SubmissionDAO;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/my-submission")
public class MySubmissionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final SubmissionDAO submissionDAO =
            new SubmissionDAO();

    private final AssignmentDAO assignmentDAO =
            new AssignmentDAO();

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

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED);

            response.getWriter().write(
                    "{\"message\":\"Please login first.\"}");

            return;
        }

        // =========================
        // CHECK STUDENT ROLE
        // =========================

        if (!"STUDENT".equalsIgnoreCase(
                String.valueOf(session.getAttribute("role")))) {

            response.setStatus(
                    HttpServletResponse.SC_FORBIDDEN);

            response.getWriter().write(
                    "{\"message\":\"Only students can view their submissions.\"}");

            return;
        }

        // =========================
        // GET ASSIGNMENT ID
        // =========================

        String assignmentText =
                request.getParameter("assignmentId");

        if (assignmentText == null ||
                assignmentText.isBlank()) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST);

            response.getWriter().write(
                    "{\"message\":\"Assignment ID is required.\"}");

            return;
        }

        int assignmentId;

        try {

            assignmentId =
                    Integer.parseInt(assignmentText);

        } catch (NumberFormatException exception) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST);

            response.getWriter().write(
                    "{\"message\":\"Invalid assignment ID.\"}");

            return;
        }

        // =========================
        // GET STUDENT ID FROM SESSION
        // =========================

        int studentId;

        try {

            studentId = Integer.parseInt(
                    String.valueOf(
                            session.getAttribute("userId")
                    )
            );

        } catch (NumberFormatException exception) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED);

            response.getWriter().write(
                    "{\"message\":\"Invalid user session.\"}");

            return;
        }

        // =========================
        // CHECK ASSIGNMENT
        // =========================

        AssignmentDAO.Assignment assignment =
                assignmentDAO.getAssignmentById(
                        assignmentId);

        if (assignment == null) {

            response.setStatus(
                    HttpServletResponse.SC_NOT_FOUND);

            response.getWriter().write(
                    "{\"message\":\"Assignment not found.\"}");

            return;
        }

        // =========================
        // CHECK COURSE ENROLLMENT
        // =========================

        boolean enrolled =
                enrollmentDAO.isEnrolled(
                        studentId,
                        assignment.getCourseId());

        if (!enrolled) {

            response.setStatus(
                    HttpServletResponse.SC_FORBIDDEN);

            response.getWriter().write(
                    "{\"message\":\"You are not enrolled in this course.\"}");

            return;
        }

        // =========================
        // GET OWN SUBMISSION
        // =========================

        SubmissionDAO.Submission submission =
                submissionDAO.getStudentSubmission(
                        assignmentId,
                        studentId);

        // Student has not submitted yet
        if (submission == null) {

            response.getWriter().write("null");

            return;
        }

        // =========================
        // PREPARE JSON
        // =========================

        String marks =
                submission.marks == null
                        ? "null"
                        : String.valueOf(
                                submission.marks);

        String feedback =
                submission.feedback == null
                        ? "null"
                        : "\"" +
                          escape(
                                  submission.feedback)
                          + "\"";

        String json =
                "{"
                + "\"submissionId\":"
                + submission.submissionId
                + ","

                + "\"fileName\":\""
                + escape(submission.fileName)
                + "\","

                + "\"submittedAt\":\""
                + escape(
                        String.valueOf(
                                submission.submittedAt))
                + "\","

                + "\"marks\":"
                + marks
                + ","

                + "\"feedback\":"
                + feedback

                + "}";

        response.getWriter().write(json);
    }


    // =========================
    // JSON ESCAPE
    // =========================

    private String escape(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}