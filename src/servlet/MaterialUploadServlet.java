package servlet;

import dao.MaterialDAO;
import dao.CourseDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/upload-material")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 20 * 1024 * 1024,
        maxRequestSize = 25 * 1024 * 1024
)
public class MaterialUploadServlet extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        HttpSession session =
                request.getSession(false);

        // Login check
        if (session == null ||
                session.getAttribute("userId") == null) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED);

            out.print("""
                    {
                        "success": false,
                        "message": "Please login first."
                    }
                    """);

            return;
        }

        int instructorId =
                (Integer) session.getAttribute("userId");

        String role =
                (String) session.getAttribute("role");

        // Instructor check
        if (!"INSTRUCTOR".equalsIgnoreCase(role)) {

            response.setStatus(
                    HttpServletResponse.SC_FORBIDDEN);

            out.print("""
                    {
                        "success": false,
                        "message": "Only instructors can upload materials."
                    }
                    """);

            return;
        }

        try {

            String courseIdText =
                    request.getParameter("courseId");

            String title =
                    request.getParameter("title");

            String description =
                    request.getParameter("description");

            Part filePart =
                    request.getPart("file");


            if (courseIdText == null ||
                    courseIdText.isBlank()) {

                response.setStatus(
                        HttpServletResponse.SC_BAD_REQUEST);

                out.print("""
                        {
                            "success": false,
                            "message": "Course is required."
                        }
                        """);

                return;
            }


            if (title == null ||
                    title.isBlank()) {

                response.setStatus(
                        HttpServletResponse.SC_BAD_REQUEST);

                out.print("""
                        {
                            "success": false,
                            "message": "Material title is required."
                        }
                        """);

                return;
            }


            if (filePart == null ||
                    filePart.getSize() == 0) {

                response.setStatus(
                        HttpServletResponse.SC_BAD_REQUEST);

                out.print("""
                        {
                            "success": false,
                            "message": "Please select a file."
                        }
                        """);

                return;
            }


            int courseId =
                    Integer.parseInt(courseIdText);


            /*
             * IMPORTANT:
             * Check whether this instructor actually owns
             * the selected course.
             */

            CourseDAO courseDAO =
                    new CourseDAO();

            CourseDAO.Course course =
                    courseDAO.getCourseById(courseId);


            if (course == null) {

                response.setStatus(
                        HttpServletResponse.SC_NOT_FOUND);

                out.print("""
                        {
                            "success": false,
                            "message": "Course not found."
                        }
                        """);

                return;
            }


            if (course.getInstructorId()
                    != instructorId) {

                response.setStatus(
                        HttpServletResponse.SC_FORBIDDEN);

                out.print("""
                        {
                            "success": false,
                            "message": "You can only upload materials to your own courses."
                        }
                        """);

                return;
            }


            /*
             * Create uploads directory.
             */

            String uploadDirectory =
                    getServletContext()
                            .getRealPath("/uploads");


            File uploadFolder =
                    new File(uploadDirectory);


            if (!uploadFolder.exists()) {

                uploadFolder.mkdirs();

            }


            /*
             * Original filename.
             */

            String originalFileName =
                    filePart.getSubmittedFileName();


            if (originalFileName == null ||
                    originalFileName.isBlank()) {

                response.setStatus(
                        HttpServletResponse.SC_BAD_REQUEST);

                out.print("""
                        {
                            "success": false,
                            "message": "Invalid file."
                        }
                        """);

                return;
            }


            /*
             * Remove unsafe characters from filename.
             */

            String safeFileName =
                    originalFileName
                            .replaceAll(
                                    "[^a-zA-Z0-9._-]",
                                    "_");


            /*
             * Generate unique stored filename.
             */

            String storedFileName =
                    System.currentTimeMillis()
                    + "_"
                    + safeFileName;


            File destination =
                    new File(
                            uploadFolder,
                            storedFileName);


            /*
             * Save actual file.
             */

            filePart.write(
                    destination.getAbsolutePath());


            String filePath =
                    "uploads/"
                    + storedFileName;


            String materialType =
                    filePart.getContentType();


            /*
             * Save material information in database.
             */

            MaterialDAO materialDAO =
                    new MaterialDAO();


            materialDAO.addMaterial(
                    courseId,
                    instructorId,
                    title.trim(),
                    description == null
                            ? ""
                            : description.trim(),
                    originalFileName,
                    filePath,
                    materialType
            );


            out.print("""
                    {
                        "success": true,
                        "message": "Material uploaded successfully."
                    }
                    """);


        } catch (NumberFormatException exception) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST);

            out.print("""
                    {
                        "success": false,
                        "message": "Invalid course ID."
                    }
                    """);


        } catch (Exception exception) {

            exception.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            out.print("""
                    {
                        "success": false,
                        "message": "Unable to upload material."
                    }
                    """);
        }
    }
}