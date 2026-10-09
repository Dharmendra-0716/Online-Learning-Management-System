package servlet;

import java.io.IOException;

import dao.UserDAO;
import dao.UserDAO.LoginResult;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // Basic validation
        if (email == null || password == null
                || email.trim().isEmpty()
                || password.isEmpty()) {

            sendJson(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Email and password are required."
            );

            return;
        }

        email = email.trim().toLowerCase();

        // Check login credentials
        LoginResult user =
                userDAO.loginUser(email, password);

        if (user == null) {

            sendJson(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid email or password."
            );

            return;
        }

        // Create session
        HttpSession session =
                request.getSession(true);

String role = user.getRole().trim().toUpperCase();

session.setAttribute("userId", user.getUserId());
session.setAttribute("userName", user.getName());
session.setAttribute("role", role);

        String dashboard;

        switch (role) {

            case "ADMIN":
                dashboard = "pages/admin-dashboard.html";
                break;

            case "INSTRUCTOR":
                dashboard = "pages/instructor-dashboard.html";
                break;

            case "STUDENT":
                dashboard = "pages/student-dashboard.html";
                break;

            default:

                sendJson(
                        response,
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Invalid user role."
                );

                return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(
                "{\"message\":\"Login successful.\","
                        + "\"dashboard\":\""
                        + dashboard
                        + "\"}"
        );
    }

    private void sendJson(
            HttpServletResponse response,
            int status,
            String message)
            throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(
                "{\"message\":\""
                        + message
                        + "\"}"
        );
    }
}