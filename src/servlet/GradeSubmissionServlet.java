package servlet;

import dao.AssignmentDAO;
import dao.SubmissionDAO;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/grade-submission")
public class GradeSubmissionServlet extends HttpServlet {

    private final SubmissionDAO submissionDAO = new SubmissionDAO();
    private final AssignmentDAO assignmentDAO = new AssignmentDAO();

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(
                    "{\"message\":\"Please login first.\"}");
            return;
        }

        if (!"INSTRUCTOR".equalsIgnoreCase(
                String.valueOf(session.getAttribute("role")))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(
                    "{\"message\":\"Only instructors can grade submissions.\"}");
            return;
        }

        String submissionText =
                request.getParameter("submissionId");

        String marksText =
                request.getParameter("marks");

        String feedback =
                request.getParameter("feedback");

        if (submissionText == null || marksText == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                    "{\"message\":\"Submission ID and marks are required.\"}");
            return;
        }

        int submissionId;
        int marks;

        try {
            submissionId = Integer.parseInt(submissionText);
            marks = Integer.parseInt(marksText);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                    "{\"message\":\"Invalid submission ID or marks.\"}");
            return;
        }

        SubmissionDAO.Submission submission =
                submissionDAO.getSubmissionById(submissionId);

        if (submission == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(
                    "{\"message\":\"Submission not found.\"}");
            return;
        }

        AssignmentDAO.Assignment assignment =
                assignmentDAO.getAssignmentById(
                        submission.assignmentId);

        int instructorId =
                (Integer) session.getAttribute("userId");

        if (assignment == null ||
                assignment.instructorId != instructorId) {

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(
                    "{\"message\":\"You do not own this assignment.\"}");
            return;
        }

        if (submission.marks != null) {

    response.setStatus(
            HttpServletResponse.SC_CONFLICT);

    response.getWriter().write(
            "{\"message\":\"This submission has already been graded.\"}");

    return;
}

        if (marks < 0 || marks > assignment.maxMarks) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                    "{\"message\":\"Marks must be between 0 and " +
                    assignment.maxMarks + ".\"}");
            return;
        }

        if (feedback == null) {
            feedback = "";
        }

        boolean updated =
                submissionDAO.gradeSubmission(
                        submissionId,
                        marks,
                        feedback.trim());

        if (!updated) {
            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(
                    "{\"message\":\"Could not save evaluation.\"}");
            return;
        }

        response.getWriter().write(
                "{\"message\":\"Marks and feedback saved successfully.\"}");
    }
}
