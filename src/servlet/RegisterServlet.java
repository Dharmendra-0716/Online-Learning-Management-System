package servlet;

import java.io.IOException;
import java.util.Locale;
import java.util.regex.Pattern;

import dao.UserDAO;
import dao.UserDAO.RegistrationResult;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        request.setCharacterEncoding("UTF-8");

        String name = value(request.getParameter("name"));
        String email = value(request.getParameter("email"))
                .toLowerCase(Locale.ROOT);

        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        String validationError =
                validate(name, email, password, confirmPassword);

        if (validationError != null) {
            writeJson(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    validationError
            );
            return;
        }

        RegistrationResult result =
                userDAO.registerStudent(name, email, password);

        switch (result) {

            case SUCCESS:
                writeJson(
                        response,
                        HttpServletResponse.SC_CREATED,
                        "Account created successfully."
                );
                break;

            case EMAIL_ALREADY_EXISTS:
                writeJson(
                        response,
                        HttpServletResponse.SC_CONFLICT,
                        "An account already exists for this email address."
                );
                break;

            case DATABASE_ERROR:
                writeJson(
                        response,
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "We could not create your account. Please try again later."
                );
                break;
        }
    }

    private String validate(
            String name,
            String email,
            String password,
            String confirmPassword) {

        if (name.length() < 2 || name.length() > 100) {
            return "Enter a name between 2 and 100 characters.";
        }

        if (!EMAIL_PATTERN.matcher(email).matches()
                || email.length() > 254) {
            return "Enter a valid email address.";
        }

        if (password == null || password.length() < 6) {
            return "Password must contain at least 6 characters.";
        }

        if (!password.equals(confirmPassword)) {
            return "Passwords do not match.";
        }

        return null;
    }

    private String value(String input) {
        return input == null ? "" : input.trim();
    }

    private void writeJson(
            HttpServletResponse response,
            int status,
            String message) throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String escapedMessage = message
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");

        response.getWriter().write(
                "{\"message\":\"" + escapedMessage + "\"}"
        );
    }
}