package servlet;

import dao.QuizDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/create-quiz")
public class CreateQuizServlet extends HttpServlet {

    private final QuizDAO quizDAO = new QuizDAO();

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // =========================
        // Check Login
        // =========================
        HttpSession session = request.getSession(false);

        if (session == null ||
            session.getAttribute("userId") == null) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            response.getWriter().write(
                    "{\"message\":\"Please login first.\"}"
            );

            return;
        }

        // =========================
        // Check Instructor Role
        // =========================
        String role =
                String.valueOf(session.getAttribute("role"))
                      .trim()
                      .toUpperCase();

        if (!"INSTRUCTOR".equals(role)) {

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);

            response.getWriter().write(
                    "{\"message\":\"Only instructors can create quizzes.\"}"
            );

            return;
        }

        // =========================
        // Get Instructor ID
        // =========================
        int instructorId;

        try {

            instructorId =
                    Integer.parseInt(
                            String.valueOf(
                                    session.getAttribute("userId")
                            )
                    );

        } catch (NumberFormatException e) {

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().write(
                    "{\"message\":\"Invalid instructor session.\"}"
            );

            return;
        }

        // =========================
        // Get Form Data
        // =========================
        String courseIdParam =
                request.getParameter("courseId");

        String title =
                request.getParameter("title");

        String description =
                request.getParameter("description");

        // =========================
        // Validate Course ID
        // =========================
        int courseId;

        try {

            courseId = Integer.parseInt(courseIdParam);

        } catch (Exception e) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            response.getWriter().write(
                    "{\"message\":\"Invalid course ID.\"}"
            );

            return;
        }

        // =========================
        // Validate Quiz Details
        // =========================
        if (title == null ||
            title.trim().isEmpty()) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            response.getWriter().write(
                    "{\"message\":\"Quiz title is required.\"}"
            );

            return;
        }

        if (description == null) {
            description = "";
        }

        // =========================
        // Verify Course Ownership
        // =========================
        if (!isCourseOwnedByInstructor(courseId, instructorId)) {

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);

            response.getWriter().write(
                    "{\"message\":\"You can create quizzes only for your own courses.\"}"
            );

            return;
        }

        // =========================
        // Create Quiz
        // =========================
        int quizId =
                quizDAO.createQuiz(
                        courseId,
                        instructorId,
                        title.trim(),
                        description.trim()
                );

        if (quizId == -1) {

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().write(
                    "{\"message\":\"Failed to create quiz.\"}"
            );

            return;
        }

        // =========================
        // Success Response
        // =========================
        response.setStatus(HttpServletResponse.SC_CREATED);

        response.getWriter().write(
                "{"
                + "\"message\":\"Quiz created successfully.\","
                + "\"quizId\":" + quizId
                + "}"
        );
    }

    // ==========================================
    // Verify Course Ownership
    // ==========================================
    private boolean isCourseOwnedByInstructor(int courseId,
                                               int instructorId) {

        String sql =
                "SELECT course_id FROM courses " +
                "WHERE course_id = ? AND instructor_id = ?";

        try (
                java.sql.Connection con =
                        util.DBConnection.getConnection();

                java.sql.PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setInt(1, courseId);
            ps.setInt(2, instructorId);

            try (java.sql.ResultSet rs = ps.executeQuery()) {

                return rs.next();
            }

        } catch (java.sql.SQLException e) {

            e.printStackTrace();
            return false;
        }
    }
}