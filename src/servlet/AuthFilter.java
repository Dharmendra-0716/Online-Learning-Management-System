
package servlet;

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebFilter("/pages/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest =
                (HttpServletRequest) request;
        HttpServletResponse httpResponse =
                (HttpServletResponse) response;

        httpResponse.setHeader(
                "Cache-Control",
                "no-cache, no-store, must-revalidate");
        httpResponse.setHeader("Pragma", "no-cache");
        httpResponse.setDateHeader("Expires", 0);

        HttpSession session = httpRequest.getSession(false);

        boolean loggedIn = session != null
                && session.getAttribute("userId") != null;

        String role = session == null ? ""
                : String.valueOf(session.getAttribute("role"));

        String path = httpRequest.getRequestURI();

        if (!loggedIn) {
            httpResponse.sendRedirect(
                    httpRequest.getContextPath() + "/login.html");
        } else if (path.endsWith("/admin-dashboard.html")
                && !"ADMIN".equalsIgnoreCase(role)) {
            httpResponse.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Admin access required.");
        } else {
            chain.doFilter(request, response);
        }
    }
}
