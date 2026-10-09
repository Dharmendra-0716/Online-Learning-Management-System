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

@WebServlet("/instructor-quiz-list")
public class InstructorQuizListServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session =
            request.getSession(false);

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
            String.valueOf(
                session.getAttribute("role")
            );

        if (!"INSTRUCTOR".equalsIgnoreCase(role)) {

            response.setStatus(
                HttpServletResponse.SC_FORBIDDEN
            );

            response.getWriter().write(
                "{\"message\":\"Only instructors can view quizzes.\"}"
            );

            return;
        }

        try {

            int instructorId =
                (Integer) session.getAttribute("userId");

            QuizDAO dao =
                new QuizDAO();

            List<QuizDAO.Quiz> quizzes =
                dao.getQuizzesByInstructor(
                    instructorId
                );

            StringBuilder json =
                new StringBuilder();

            json.append("[");

            for (int i = 0;
                 i < quizzes.size();
                 i++) {

                if (i > 0) {
                    json.append(",");
                }

                QuizDAO.Quiz quiz =
                    quizzes.get(i);

                json.append("{");

                json.append("\"quizId\":")
                    .append(quiz.quizId)
                    .append(",");

                json.append("\"courseId\":")
                    .append(quiz.courseId)
                    .append(",");

                json.append("\"title\":\"")
                    .append(escapeJson(quiz.title))
                    .append("\",");

                json.append("\"description\":\"")
                    .append(
                        escapeJson(
                            quiz.description
                        )
                    )
                    .append("\"");

                json.append("}");
            }

            json.append("]");

            response.getWriter()
                .write(json.toString());

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().write(
                "{\"message\":\"Unable to load instructor quizzes.\"}"
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
            .replace("\n", "\\n")
            .replace("\r", "\\r");
    }
}