package servlet;

import dao.QuizDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/add-quiz-question")
public class AddQuizQuestionServlet extends HttpServlet {

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
                    "{\"message\":\"Only instructors can add quiz questions.\"}"
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
        // Get Parameters
        // =========================
        String quizIdParam =
                request.getParameter("quizId");

        String questionText =
                request.getParameter("questionText");

        String optionA =
                request.getParameter("optionA");

        String optionB =
                request.getParameter("optionB");

        String optionC =
                request.getParameter("optionC");

        String optionD =
                request.getParameter("optionD");

        String correctOption =
                request.getParameter("correctOption");

        // =========================
        // Validate Quiz ID
        // =========================
        int quizId;

        try {

            quizId = Integer.parseInt(quizIdParam);

        } catch (Exception e) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            response.getWriter().write(
                    "{\"message\":\"Invalid quiz ID.\"}"
            );

            return;
        }

        // =========================
        // Validate Fields
        // =========================
        if (questionText == null ||
            questionText.trim().isEmpty() ||
            optionA == null ||
            optionA.trim().isEmpty() ||
            optionB == null ||
            optionB.trim().isEmpty() ||
            optionC == null ||
            optionC.trim().isEmpty() ||
            optionD == null ||
            optionD.trim().isEmpty()) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            response.getWriter().write(
                    "{\"message\":\"Question and all four options are required.\"}"
            );

            return;
        }

        // =========================
        // Validate Correct Option
        // =========================
        if (correctOption == null) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            response.getWriter().write(
                    "{\"message\":\"Correct option is required.\"}"
            );

            return;
        }

        correctOption =
                correctOption.trim().toUpperCase();

        if (!correctOption.equals("A") &&
            !correctOption.equals("B") &&
            !correctOption.equals("C") &&
            !correctOption.equals("D")) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            response.getWriter().write(
                    "{\"message\":\"Correct option must be A, B, C, or D.\"}"
            );

            return;
        }

        // =========================
        // Get Quiz
        // =========================
        QuizDAO.Quiz quiz =
                quizDAO.getQuizById(quizId);

        if (quiz == null) {

            response.setStatus(HttpServletResponse.SC_NOT_FOUND);

            response.getWriter().write(
                    "{\"message\":\"Quiz not found.\"}"
            );

            return;
        }

        // =========================
        // Verify Quiz Ownership
        // =========================
        if (!quizDAO.isQuizOwnedByInstructor(
                quizId,
                instructorId)) {

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);

            response.getWriter().write(
                    "{\"message\":\"You can add questions only to your own quizzes.\"}"
            );

            return;
        }

        // =========================
        // Add Question
        // =========================
        boolean added =
                quizDAO.addQuestion(
                        quizId,
                        questionText.trim(),
                        optionA.trim(),
                        optionB.trim(),
                        optionC.trim(),
                        optionD.trim(),
                        correctOption
                );

        if (!added) {

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().write(
                    "{\"message\":\"Failed to add question.\"}"
            );

            return;
        }

        // =========================
        // Success
        // =========================
        response.setStatus(HttpServletResponse.SC_CREATED);

        response.getWriter().write(
                "{"
                + "\"message\":\"Question added successfully.\","
                + "\"quizId\":" + quizId
                + "}"
        );
    }
}