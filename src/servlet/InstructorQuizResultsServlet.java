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

@WebServlet("/instructor-quiz-results")
public class InstructorQuizResultsServlet extends HttpServlet {

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
                "{\"message\":\"Only instructors can view quiz results.\"}"
            );

            return;
        }

        try {

            int instructorId =
                (Integer) session.getAttribute("userId");

            int quizId =
                Integer.parseInt(
                    request.getParameter("quizId")
                );

            QuizDAO dao =
                new QuizDAO();

            List<String[]> results =
                dao.getInstructorQuizResults(
                    quizId,
                    instructorId
                );

            StringBuilder json =
                new StringBuilder();

            json.append("[");

            for (int i = 0;
                 i < results.size();
                 i++) {

                if (i > 0) {
                    json.append(",");
                }

                String[] row =
                    results.get(i);

                String name =
                    row[0];

                String score =
                    row[1];

                String total =
                    row[2];

                String attemptedAt =
                    row[3];

                json.append("{");

                json.append("\"studentName\":\"")
                    .append(escapeJson(name))
                    .append("\",");

                if (score == null) {
                    json.append("\"attempted\":false,");
                    json.append("\"score\":null,");
                    json.append("\"totalMarks\":null,");
                    json.append("\"attemptedAt\":null");
                } else {

                    json.append("\"attempted\":true,");

                    json.append("\"score\":")
                        .append(score)
                        .append(",");

                    json.append("\"totalMarks\":")
                        .append(total)
                        .append(",");

                    json.append("\"attemptedAt\":\"")
                        .append(escapeJson(attemptedAt))
                        .append("\"");
                }

                json.append("}");
            }

            json.append("]");

            response.getWriter()
                .write(json.toString());

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                HttpServletResponse.SC_BAD_REQUEST
            );

            response.getWriter().write(
                "{\"message\":\"Unable to load quiz results.\"}"
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