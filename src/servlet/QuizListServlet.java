package servlet;

import dao.QuizDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/quiz-list")
public class QuizListServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        PrintWriter out = response.getWriter();

        if (session == null ||
            session.getAttribute("userId") == null) {

            response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
            );

            out.print(
                "{\"message\":\"Please login first.\"}"
            );

            return;
        }

        String role =
            String.valueOf(
                session.getAttribute("role")
            )
            .trim()
            .toUpperCase();

        if (!"STUDENT".equals(role)) {

            response.setStatus(
                HttpServletResponse.SC_FORBIDDEN
            );

            out.print(
                "{\"message\":\"Student access only.\"}"
            );

            return;
        }

        int studentId;

        try {

            studentId =
                Integer.parseInt(
                    String.valueOf(
                        session.getAttribute("userId")
                    )
                );

        } catch (Exception e) {

            response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
            );

            out.print(
                "{\"message\":\"Invalid student session.\"}"
            );

            return;
        }


        try {

            QuizDAO quizDAO =
                new QuizDAO();

            List<QuizDAO.Quiz> quizzes =
                quizDAO.getQuizzesForStudent(
                    studentId
                );


            StringBuilder json =
                new StringBuilder();

            json.append("[");


            for (int i = 0;
                 i < quizzes.size();
                 i++) {

                QuizDAO.Quiz quiz =
                    quizzes.get(i);

                if (i > 0) {
                    json.append(",");
                }

                json.append("{");

                json.append(
                    "\"quizId\":"
                )
                .append(quiz.quizId)
                .append(",");

                json.append(
                    "\"courseId\":"
                )
                .append(quiz.courseId)
                .append(",");

                json.append(
                    "\"title\":\""
                )
                .append(escapeJson(
                    quiz.title
                ))
                .append("\",");

                json.append(
                    "\"description\":\""
                )
                .append(escapeJson(
                    quiz.description
                ))
                .append("\"");
                boolean attempted =
    quizDAO.hasStudentAttempted(
        quiz.quizId,
        studentId
    );

json.append(",");

json.append(
    "\"attempted\":"
)
.append(attempted);

if (attempted) {

    java.util.Map<String, Object> result =
        quizDAO.getQuizResult(
            quiz.quizId,
            studentId
        );

    json.append(",");

    json.append("\"score\":")
        .append(result.get("score"));

    json.append(",");

    json.append("\"totalMarks\":")
        .append(result.get("totalMarks"));

}
                


                json.append("}");
            }

            json.append("]");

            out.print(json.toString());


        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            out.print(
                "{\"message\":\"Unable to load quizzes.\"}"
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