package dao;

import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubmissionDAO {

    public boolean createSubmission(int assignmentId, int studentId,
                                    String fileName, String filePath) {

        String sql = """
            INSERT INTO submissions
            (assignment_id, student_id, file_name, file_path)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, assignmentId);
            ps.setInt(2, studentId);
            ps.setString(3, fileName);
            ps.setString(4, filePath);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasSubmitted(int assignmentId, int studentId) {

        String sql = """
            SELECT submission_id
            FROM submissions
            WHERE assignment_id = ? AND student_id = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, assignmentId);
            ps.setInt(2, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateSubmission(int assignmentId, int studentId,
                                String fileName, String filePath) {

    String sql = """
        UPDATE submissions
        SET file_name = ?, file_path = ?, submitted_at = CURRENT_TIMESTAMP
        WHERE assignment_id = ? AND student_id = ?
        """;

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, fileName);
        ps.setString(2, filePath);
        ps.setInt(3, assignmentId);
        ps.setInt(4, studentId);

        return ps.executeUpdate() > 0;

    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

    public Submission getStudentSubmission(int assignmentId, int studentId) {

        String sql = """
            SELECT submission_id, assignment_id, student_id,
                   file_name, file_path, submitted_at, marks, feedback
            FROM submissions
            WHERE assignment_id = ? AND student_id = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, assignmentId);
            ps.setInt(2, studentId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapSubmission(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Submission getSubmissionById(int submissionId) {

        String sql = """
            SELECT s.submission_id, s.assignment_id, s.student_id,
                   s.file_name, s.file_path, s.submitted_at,
                   s.marks, s.feedback,
                   u.name AS student_name,
                   u.email AS student_email
            FROM submissions s
            JOIN users u ON s.student_id = u.user_id
            WHERE s.submission_id = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, submissionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapSubmission(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Submission> getSubmissionsByAssignment(int assignmentId) {

        List<Submission> submissions = new ArrayList<>();

        String sql = """
            SELECT s.submission_id, s.assignment_id, s.student_id,
                   s.file_name, s.file_path, s.submitted_at,
                   s.marks, s.feedback,
                   u.name AS student_name,
                   u.email AS student_email
            FROM submissions s
            JOIN users u ON s.student_id = u.user_id
            WHERE s.assignment_id = ?
            ORDER BY s.submitted_at DESC
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, assignmentId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    submissions.add(mapSubmission(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return submissions;
    }

    public boolean gradeSubmission(int submissionId, int marks,
                                   String feedback) {

        String sql = """
            UPDATE submissions
            SET marks = ?, feedback = ?
            WHERE submission_id = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, marks);
            ps.setString(2, feedback);
            ps.setInt(3, submissionId);

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Submission mapSubmission(ResultSet rs) throws SQLException {

        Submission s = new Submission();

        s.submissionId = rs.getInt("submission_id");
        s.assignmentId = rs.getInt("assignment_id");
        s.studentId = rs.getInt("student_id");
        s.fileName = rs.getString("file_name");
        s.filePath = rs.getString("file_path");
        s.submittedAt = rs.getTimestamp("submitted_at");

        int marks = rs.getInt("marks");
        s.marks = rs.wasNull() ? null : marks;

        s.feedback = rs.getString("feedback");

        try {
            s.studentName = rs.getString("student_name");
            s.studentEmail = rs.getString("student_email");
        } catch (SQLException ignored) {
            // Columns are not present in the student-only query.
        }

        return s;
    }

    public static class Submission {

        public int submissionId;
        public int assignmentId;
        public int studentId;

        public String fileName;
        public String filePath;
        public Timestamp submittedAt;

        public Integer marks;
        public String feedback;

        public String studentName;
        public String studentEmail;

        public Integer getMarks() {
    return marks;
}

public String getFilePath() {
    return filePath;
}
    }
}

