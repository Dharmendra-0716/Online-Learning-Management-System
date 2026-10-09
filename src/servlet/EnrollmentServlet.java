package servlet;

import java.io.IOException;

import dao.EnrollmentDAO;
import dao.EnrollmentDAO.EnrollmentResult;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/enroll")
public class EnrollmentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final EnrollmentDAO enrollmentDAO =
            new EnrollmentDAO();


    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");


        // =========================
        // CHECK LOGIN
        // =========================

        HttpSession session =
                request.getSession(false);

        if (session == null
                || session.getAttribute("userId") == null) {

            sendJson(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Please login first."
            );

            return;
        }


        // =========================
        // GET STUDENT ID
        // =========================

        Object userIdObject =
                session.getAttribute("userId");

        int studentId;

        try {

            studentId =
                    Integer.parseInt(
                            userIdObject.toString()
                    );

        } catch (NumberFormatException exception) {

            sendJson(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Invalid user session."
            );

            return;
        }


        // =========================
        // CHECK ROLE
        // =========================

        String role =
                String.valueOf(
                        session.getAttribute("role")
                );

        if (!"STUDENT".equalsIgnoreCase(role)) {

            sendJson(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Only students can enroll in courses."
            );

            return;
        }


        // =========================
        // GET COURSE ID
        // =========================

        String courseIdParameter =
                request.getParameter("courseId");


        if (courseIdParameter == null
                || courseIdParameter.trim().isEmpty()) {

            sendJson(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Course ID is required."
            );

            return;
        }


        int courseId;

        try {

            courseId =
                    Integer.parseInt(
                            courseIdParameter
                    );

        } catch (NumberFormatException exception) {

            sendJson(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid course ID."
            );

            return;
        }


        // =========================
        // ENROLL
        // =========================

        EnrollmentResult result =
                enrollmentDAO.enrollStudent(
                        studentId,
                        courseId
                );


        // =========================
        // HANDLE RESULT
        // =========================

        switch (result) {

            case SUCCESS:

                sendJson(
                        response,
                        HttpServletResponse.SC_OK,
                        "Enrolled successfully."
                );

                break;


            case ALREADY_ENROLLED:

                sendJson(
                        response,
                        HttpServletResponse.SC_CONFLICT,
                        "You are already enrolled in this course."
                );

                break;


            case DATABASE_ERROR:

                sendJson(
                        response,
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Unable to enroll in the course."
                );

                break;
        }
    }


    // =========================
    // JSON RESPONSE
    // =========================

    private void sendJson(
            HttpServletResponse response,
            int status,
            String message)
            throws IOException {

        response.setStatus(status);

        response.getWriter().write(
                "{\"message\":\""
                        + escapeJson(message)
                        + "\"}"
        );
    }


    private String escapeJson(String value) {

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}