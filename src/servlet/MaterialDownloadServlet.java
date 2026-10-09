package servlet;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.http.HttpSession;

import util.DBConnection;

import dao.MaterialDAO;
import dao.MaterialDAO.Material;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/download-material")
public class MaterialDownloadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final MaterialDAO materialDAO =
            new MaterialDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {
        
        HttpSession session =
        request.getSession(false);

if (session == null ||
    session.getAttribute("userId") == null) {

    response.sendError(
            HttpServletResponse.SC_UNAUTHORIZED,
            "Please login first."
    );

    return;
}

String role =
        String.valueOf(
                session.getAttribute("role")
        );

int userId;

try {

    userId = Integer.parseInt(
            String.valueOf(
                    session.getAttribute("userId")
            )
    );

} catch (NumberFormatException exception) {

    response.sendError(
            HttpServletResponse.SC_UNAUTHORIZED,
            "Invalid user session."
    );

    return;
}

        String materialIdParameter =
                request.getParameter("materialId");

        if (materialIdParameter == null) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Material ID is required."
            );

            return;
        }

        int materialId;

        try {

            materialId =
                    Integer.parseInt(materialIdParameter);

        } catch (NumberFormatException exception) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid material ID."
            );

            return;
        }

        Material material =
                materialDAO.getMaterialById(materialId);

        if (material == null) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Material not found."
            );

            return;
        }

        boolean allowed = false;

try (Connection con = DBConnection.getConnection()) {

    if ("STUDENT".equalsIgnoreCase(role)) {

        String sql = """
            SELECT 1
            FROM materials m
            INNER JOIN enrollments e
                ON e.course_id = m.course_id
            WHERE m.material_id = ?
              AND e.student_id = ?
            """;

        try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setInt(1, materialId);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                allowed = rs.next();
            }
        }

    } else if ("INSTRUCTOR".equalsIgnoreCase(role)) {

        String sql = """
            SELECT 1
            FROM materials m
            INNER JOIN courses c
                ON c.course_id = m.course_id
            WHERE m.material_id = ?
              AND c.instructor_id = ?
            """;

        try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setInt(1, materialId);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                allowed = rs.next();
            }
        }
    }

} catch (Exception exception) {

    exception.printStackTrace();

    response.sendError(
            HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            "Unable to verify material access."
    );

    return;
}

if (!allowed) {

    response.sendError(
            HttpServletResponse.SC_FORBIDDEN,
            "You are not allowed to download this material."
    );

    return;
}

        String realPath =
                getServletContext().getRealPath(
                        "/" + material.getFilePath()
                );

        File file =
                new File(realPath);

        if (!file.exists()) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "File not found."
            );

            return;
        }

        response.setContentType(
                material.getMaterialType()
        );

        response.setContentLengthLong(
                file.length()
        );

        response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"" +
                material.getFileName() +
                "\""
        );

        try (OutputStream output =
                     response.getOutputStream()) {

            Files.copy(
                    file.toPath(),
                    output
            );
        }
    }
}