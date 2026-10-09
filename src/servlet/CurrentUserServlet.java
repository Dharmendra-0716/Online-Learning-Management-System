package servlet;

import java.io.IOException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/current-user")
public class CurrentUserServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            sendJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "{\"success\":false,\"message\":\"Please login first.\"}");
            return;
        }

        Object userId = session.getAttribute("userId");
        String name = String.valueOf(session.getAttribute("userName"));
        String role = String.valueOf(session.getAttribute("role"));

        String json = "{\"userId\":" + userId
                + ",\"name\":\"" + escapeJson(name)
                + "\",\"role\":\"" + escapeJson(role) + "\"}";

        sendJson(response, HttpServletResponse.SC_OK, json);
    }

    private void sendJson(HttpServletResponse response, int status, String json)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
