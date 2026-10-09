package servlet;

import dao.QuizDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

@WebServlet("/quiz-result")
public class QuizResultServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null ||
            session.getAttribute("userId") == null) {

            response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
            );

            response.getWriter().write(
                "{\"message\":\"Please login first.\"}"
            );
            return;
        }

        String role =
            String.valueOf(session.getAttribute("role"));

        if (!"STUDENT".equalsIgnoreCase(role)) {

            response.setStatus(
                HttpServletResponse.SC_FORBIDDEN
            );

            response.getWriter().write(
                "{\"message\":\"Only students can view quiz results.\"}"
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

            QuizDAO.Quiz quiz =
                dao.getQuizById(quizId);

            if (quiz == null) {

                response.setStatus(
                    HttpServletResponse.SC_NOT_FOUND
                );

                response.getWriter().write(
                    "{\"message\":\"Quiz not found.\"}"
                );
                return;
            }

            if (!dao.isStudentEnrolled(
                    studentId,
                    quiz.courseId)) {

                response.setStatus(
                    HttpServletResponse.SC_FORBIDDEN
                );

                response.getWriter().write(
                    "{\"message\":\"You are not enrolled in this course.\"}"
                );
                return;
            }

            Map<String, Object> result =
                dao.getQuizResult(
                    quizId,
                    studentId
                );

            if (result.isEmpty()) {

                response.setStatus(
                    HttpServletResponse.SC_NOT_FOUND
                );

                response.getWriter().write(
                    "{\"message\":\"Quiz has not been attempted yet.\"}"
                );
                return;
            }

            response.getWriter().write(
                "{"
                + "\"quizId\":" + quizId + ","
                + "\"score\":" + result.get("score") + ","
                + "\"totalMarks\":" + result.get("totalMarks") + ","
                + "\"attemptedAt\":\""
                + result.get("attemptedAt")
                + "\""
                + "}"
            );

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                HttpServletResponse.SC_BAD_REQUEST
            );

            response.getWriter().write(
                "{\"message\":\"Invalid quiz result request.\"}"
            );
        }
    }
}
