
package servlet;

import dao.AssignmentDAO;
import dao.SubmissionDAO;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

@WebServlet("/download-submission")
public class SubmissionDownloadServlet extends HttpServlet {

    private final SubmissionDAO submissionDAO = new SubmissionDAO();
    private final AssignmentDAO assignmentDAO = new AssignmentDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        // 1. Check login
        HttpSession session = request.getSession(false);

        if (session == null
                || session.getAttribute("userId") == null) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Please login first.");
            return;
        }

        // 2. Validate submission ID
        String idText = request.getParameter("submissionId");

        if (idText == null || idText.isBlank()) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Submission ID is required.");
            return;
        }

        int submissionId;

        try {
            submissionId = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid submission ID.");
            return;
        }

        // 3. Find submission
        SubmissionDAO.Submission submission =
                submissionDAO.getSubmissionById(submissionId);

        if (submission == null) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Submission not found.");
            return;
        }

        // 4. Check authorization
        int userId = (Integer) session.getAttribute("userId");
        String role = String.valueOf(session.getAttribute("role"));

        boolean allowed = false;

        if ("STUDENT".equalsIgnoreCase(role)) {
            allowed = submission.studentId == userId;

        } else if ("INSTRUCTOR".equalsIgnoreCase(role)) {

            AssignmentDAO.Assignment assignment =
                    assignmentDAO.getAssignmentById(
                            submission.assignmentId);

            allowed = assignment != null
                    && assignment.instructorId == userId;
        }

        // Admin is not automatically allowed to download submissions.
        if (!allowed) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "You are not allowed to download this submission.");
            return;
        }

        // 5. Validate the stored file path
        String uploadRoot = getServletContext().getRealPath("/");

        if (uploadRoot == null
                || submission.filePath == null
                || submission.filePath.isBlank()) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Submitted file not found.");
            return;
        }

        File rootDirectory =
                new File(uploadRoot).getCanonicalFile();

        File file = new File(
                rootDirectory, submission.filePath).getCanonicalFile();

        String rootPath = rootDirectory.getPath()
                + File.separator;

        if (!file.toPath().startsWith(rootDirectory.toPath())
                || file.equals(rootDirectory)
                || !file.isFile()) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Submitted file not found.");
            return;
        }

        // 6. Prepare download response
        String contentType =
                getServletContext().getMimeType(file.getName());

        response.setContentType(
                contentType != null
                        ? contentType
                        : "application/octet-stream");

        String safeFileName = submission.fileName == null
                ? "submission"
                : submission.fileName
                        .replace("\"", "")
                        .replace("\r", "")
                        .replace("\n", "");

        response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"" + safeFileName + "\"");

        response.setContentLengthLong(file.length());

        // 7. Stream the file
        try (InputStream in = Files.newInputStream(file.toPath());
             OutputStream out = response.getOutputStream()) {

            byte[] buffer = new byte[8192];
            int length;

            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
        }
    }
}
