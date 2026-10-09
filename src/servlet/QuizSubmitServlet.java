package servlet;

import dao.QuizDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/quiz-submit")
public class QuizSubmitServlet extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null ||
            session.getAttribute("userId") == null) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            response.getWriter().write(
                    "{\"message\":\"Please login first.\"}"
            );

            return;
        }

        String role =
                String.valueOf(session.getAttribute("role"));

        if (!"STUDENT".equalsIgnoreCase(role)) {

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);

            response.getWriter().write(
                    "{\"message\":\"Only students can submit quizzes.\"}"
            );

            return;
        }

        try {

            int studentId =
                    (Integer) session.getAttribute("userId");

            int quizId =
                    Integer.parseInt(
                            request.getParameter("quizId")
                    );

            QuizDAO dao = new QuizDAO();

            // Prevent duplicate attempt
            if (dao.hasStudentAttempted(quizId, studentId)) {

                response.setStatus(
                        HttpServletResponse.SC_CONFLICT
                );

                response.getWriter().write(
                        "{\"message\":\"You have already attempted this quiz.\"}"
                );

                return;
            }

            Map<Integer, String> answers = new HashMap<>();

            String[] questionIds =
                    request.getParameterValues("questionId");

            if (questionIds != null) {

                for (String id : questionIds) {

                    int questionId =
                            Integer.parseInt(id);

                    String answer =
                            request.getParameter(
                                    "answer_" + questionId
                            );

                    if (answer != null) {

                        answers.put(
                                questionId,
                                answer
                        );
                    }
                }
            }

            int score =
                    dao.submitQuiz(
                            quizId,
                            studentId,
                            answers
                    );

            if (score == -1) {

                response.setStatus(
                        HttpServletResponse.SC_CONFLICT
                );

                response.getWriter().write(
                        "{\"message\":\"You have already attempted this quiz.\"}"
                );

                return;
            }

            if (score == -2) {

                response.setStatus(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR
                );

                response.getWriter().write(
                        "{\"message\":\"Unable to submit quiz.\"}"
                );

                return;
            }

            response.getWriter().write(
                    "{\"success\":true,\"score\":" + score + "}"
            );

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            response.getWriter().write(
                    "{\"message\":\"Invalid quiz submission.\"}"
            );
        }
    }
}