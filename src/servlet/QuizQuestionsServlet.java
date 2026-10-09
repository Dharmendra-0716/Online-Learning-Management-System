package servlet;

import dao.QuizDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet("/quiz-questions")
public class QuizQuestionsServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null ||
            session.getAttribute("userId") == null) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(
                "{\"message\":\"Please login first.\"}"
            );
            return;
        }

        String role = String.valueOf(
                session.getAttribute("role")
        ).trim().toUpperCase();

        if (!"STUDENT".equals(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(
                "{\"message\":\"Access denied.\"}"
            );
            return;
        }

        try {
            int quizId = Integer.parseInt(
                    request.getParameter("quizId")
            );

            int studentId = Integer.parseInt(
                    String.valueOf(
                            session.getAttribute("userId")
                    )
            );

            QuizDAO dao = new QuizDAO();

            // Check student enrollment
       QuizDAO.Quiz quiz = dao.getQuizById(quizId);

if (quiz == null) {
    response.setStatus(
            HttpServletResponse.SC_NOT_FOUND
    );

    response.getWriter().write(
        "{\"message\":\"Quiz not found.\"}"
    );
    return;
}
if (dao.hasStudentAttempted(quizId, studentId)) {
    response.setStatus(HttpServletResponse.SC_CONFLICT);
    response.getWriter().write(
        "{\"message\":\"You have already attempted this quiz.\"}"
    );
    return;
}
if (!dao.isStudentEnrolled(studentId, quiz.courseId)) {
    response.setStatus(
            HttpServletResponse.SC_FORBIDDEN
    );

    response.getWriter().write(
        "{\"message\":\"You are not enrolled in this course.\"}"
    );
    return;
}

            List<QuizDAO.Question> questions =
                    dao.getQuestionsByQuiz(quizId);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            StringBuilder json = new StringBuilder("[");
            
            for (int i = 0; i < questions.size(); i++) {

                QuizDAO.Question q = questions.get(i);

                if (i > 0) {
                    json.append(",");
                }

                json.append("{")
                    .append("\"questionId\":")
                    .append(q.questionId)
                    .append(",")

                    .append("\"questionText\":\"")
                    .append(escapeJson(q.questionText))
                    .append("\",")

                    .append("\"optionA\":\"")
                    .append(escapeJson(q.optionA))
                    .append("\",")

                    .append("\"optionB\":\"")
                    .append(escapeJson(q.optionB))
                    .append("\",")

                    .append("\"optionC\":\"")
                    .append(escapeJson(q.optionC))
                    .append("\",")

                    .append("\"optionD\":\"")
                    .append(escapeJson(q.optionD))
                    .append("\"")

                    .append("}");
            }

            json.append("]");

            response.getWriter().write(
                    json.toString()
            );

        } catch (NumberFormatException e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            response.getWriter().write(
                "{\"message\":\"Invalid quiz ID.\"}"
            );

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().write(
                "{\"message\":\"Unable to load quiz questions.\"}"
            );
        }
    }

    private String escapeJson(String value) {

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