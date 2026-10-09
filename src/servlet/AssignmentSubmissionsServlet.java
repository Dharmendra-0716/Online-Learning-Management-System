package servlet;

import dao.AssignmentDAO;
import dao.SubmissionDAO;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet("/assignment-submissions")
public class AssignmentSubmissionsServlet extends HttpServlet {

    private final SubmissionDAO submissionDAO = new SubmissionDAO();
    private final AssignmentDAO assignmentDAO = new AssignmentDAO();

    @Override
    protected void doGet(HttpServletRequest request,
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
                    "{\"message\":\"Only instructors can view submissions.\"}");
            return;
        }

        String idText = request.getParameter("assignmentId");

        if (idText == null || idText.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                    "{\"message\":\"Assignment ID is required.\"}");
            return;
        }

        int assignmentId;

        try {
            assignmentId = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                    "{\"message\":\"Invalid assignment ID.\"}");
            return;
        }

        int instructorId = (Integer) session.getAttribute("userId");

        AssignmentDAO.Assignment assignment =
                assignmentDAO.getAssignmentById(assignmentId);

        if (assignment == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(
                    "{\"message\":\"Assignment not found.\"}");
            return;
        }

        if (assignment.instructorId != instructorId) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(
                    "{\"message\":\"You do not own this assignment.\"}");
            return;
        }

        List<SubmissionDAO.Submission> submissions =
                submissionDAO.getSubmissionsByAssignment(assignmentId);

        StringBuilder json = new StringBuilder("[");
        boolean first = true;

        for (SubmissionDAO.Submission s : submissions) {

            if (!first) {
                json.append(",");
            }
            first = false;

            json.append("{")
                .append("\"submissionId\":").append(s.submissionId).append(",")
                .append("\"studentId\":").append(s.studentId).append(",")
                .append("\"studentName\":\"").append(escape(s.studentName)).append("\",")
                .append("\"studentEmail\":\"").append(escape(s.studentEmail)).append("\",")
                .append("\"fileName\":\"").append(escape(s.fileName)).append("\",")
                .append("\"submittedAt\":\"").append(escape(String.valueOf(s.submittedAt))).append("\",")
                .append("\"marks\":")
                .append(s.marks == null ? "null" : s.marks)
                .append(",")
                .append("\"feedback\":")
                .append(s.feedback == null ? "null" : "\"" + escape(s.feedback) + "\"")
                .append("}");
        }

        json.append("]");

        response.getWriter().write(json.toString());
    }

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
