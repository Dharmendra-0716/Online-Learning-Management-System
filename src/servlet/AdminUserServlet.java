package servlet;

import dao.AdminDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Admin-only user CRUD endpoint.
 */
@WebServlet("/admin/users")
public class AdminUserServlet extends HttpServlet {

    private final AdminDAO adminDAO = new AdminDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!isAdmin(request)) {
            send(response, HttpServletResponse.SC_FORBIDDEN, "Admin access required.");
            return;
        }

        String idText = request.getParameter("id");
        String name = trim(request.getParameter("name"));
        String email = trim(request.getParameter("email"));
        String role = trim(request.getParameter("role")).toUpperCase();
        String password = request.getParameter("password");

        if (name.isEmpty() || email.isEmpty() ||
                !(role.equals("STUDENT") || role.equals("INSTRUCTOR") || role.equals("ADMIN"))) {
            send(response, HttpServletResponse.SC_BAD_REQUEST, "Valid name, email and role are required.");
            return;
        }

        try {
            int id = idText == null || idText.isBlank() ? -1 : Integer.parseInt(idText);

            if (adminDAO.emailExists(email, id)) {
                send(response, HttpServletResponse.SC_CONFLICT, "Email is already registered.");
                return;
            }

            if (id == -1) {
                if (password == null || password.length() < 6) {
                    send(response, HttpServletResponse.SC_BAD_REQUEST, "Password must contain at least 6 characters.");
                    return;
                }

                String hash = hashPassword(password);
                int createdId = adminDAO.createUser(name, email, role, hash);
                send(response, HttpServletResponse.SC_OK, "User created with ID " + createdId);
            } else {
                if (id == getSessionUserId(request)) {
                    // Do not allow the admin to accidentally remove their own admin role.
                    if (!role.equals("ADMIN")) {
                        send(response, HttpServletResponse.SC_BAD_REQUEST, "You cannot remove your own ADMIN role.");
                        return;
                    }
                }

                adminDAO.updateUser(id, name, email, role);
                send(response, HttpServletResponse.SC_OK, "User updated successfully.");
            }

        } catch (NumberFormatException e) {
            send(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID.");
        } catch (Exception e) {
            e.printStackTrace();
            send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to save user.");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!isAdmin(request)) {
            send(response, HttpServletResponse.SC_FORBIDDEN, "Admin access required.");
            return;
        }

        try {
            int id = Integer.parseInt(request.getParameter("id"));

            if (id == getSessionUserId(request)) {
                send(response, HttpServletResponse.SC_BAD_REQUEST, "You cannot delete your own admin account.");
                return;
            }

            if (!adminDAO.deleteUser(id)) {
                send(response, HttpServletResponse.SC_NOT_FOUND, "User not found.");
                return;
            }

            send(response, HttpServletResponse.SC_OK, "User deleted successfully.");

        } catch (NumberFormatException e) {
            send(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID.");
        } catch (Exception e) {
            e.printStackTrace();
            send(response, HttpServletResponse.SC_CONFLICT,
                    "User could not be deleted because other records still reference this account.");
        }
    }

    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null &&
                session.getAttribute("userId") != null &&
                "ADMIN".equalsIgnoreCase(String.valueOf(session.getAttribute("role")));
    }

    private int getSessionUserId(HttpServletRequest request) {
        return Integer.parseInt(String.valueOf(request.getSession(false).getAttribute("userId")));
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String hashPassword(String password) throws Exception {
        final int iterations = 210_000;
        byte[] salt = new byte[16];
        new java.security.SecureRandom().nextBytes(salt);

        javax.crypto.spec.PBEKeySpec spec =
                new javax.crypto.spec.PBEKeySpec(
                        password.toCharArray(),
                        salt,
                        iterations,
                        256
                );

        byte[] hash = javax.crypto.SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .getEncoded();

        return iterations + ":" +
                java.util.Base64.getEncoder().encodeToString(salt) + ":" +
                java.util.Base64.getEncoder().encodeToString(hash);
    }

    private void send(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\":\"" + escape(message) + "\"}");
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
