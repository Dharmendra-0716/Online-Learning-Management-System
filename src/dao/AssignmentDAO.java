package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import util.DBConnection;

public class AssignmentDAO {

  private void ensureAssignmentFileColumns(Connection connection)
        throws SQLException {

    String checkFileName =
            "SELECT COUNT(*) " +
            "FROM information_schema.columns " +
            "WHERE table_schema = DATABASE() " +
            "AND table_name = 'assignments' " +
            "AND column_name = 'file_name'";

    String checkFilePath =
            "SELECT COUNT(*) " +
            "FROM information_schema.columns " +
            "WHERE table_schema = DATABASE() " +
            "AND table_name = 'assignments' " +
            "AND column_name = 'file_path'";

    try (PreparedStatement statement =
                 connection.prepareStatement(checkFileName);
         ResultSet resultSet =
                 statement.executeQuery()) {

        if (resultSet.next() &&
                resultSet.getInt(1) == 0) {

            try (PreparedStatement addColumn =
                         connection.prepareStatement(
                                 "ALTER TABLE assignments " +
                                 "ADD COLUMN file_name VARCHAR(255) NULL")) {

                addColumn.executeUpdate();
            }
        }
    }

    try (PreparedStatement statement =
                 connection.prepareStatement(checkFilePath);
         ResultSet resultSet =
                 statement.executeQuery()) {

        if (resultSet.next() &&
                resultSet.getInt(1) == 0) {

            try (PreparedStatement addColumn =
                         connection.prepareStatement(
                                 "ALTER TABLE assignments " +
                                 "ADD COLUMN file_path VARCHAR(500) NULL")) {

                addColumn.executeUpdate();
            }
        }
    }
}

    public boolean courseBelongsToInstructor(int courseId, int instructorId) {

        String sql =

                "SELECT 1 FROM courses " +
                "WHERE course_id = ? AND instructor_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, courseId);
            statement.setInt(2, instructorId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException exception) {

            exception.printStackTrace();
            return false;
        }
    }


public boolean createAssignment(
        int courseId,
        int instructorId,
        String title,
        String description,
        Timestamp dueDate,
        int maxMarks,
        String fileName,
        String filePath) {

String sql =
        "INSERT INTO assignments " +
        "(course_id, instructor_id, title, description, due_date, max_marks, file_name, file_path) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    try (Connection connection = DBConnection.getConnection()) {

        // Automatically add file columns if they do not exist
        ensureAssignmentFileColumns(connection);

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, courseId);
            statement.setInt(2, instructorId);
            statement.setString(3, title);
            statement.setString(4, description);
            statement.setTimestamp(5, dueDate);
       statement.setInt(6, maxMarks);
statement.setString(7, fileName);
statement.setString(8, filePath);

return statement.executeUpdate() == 1;
        }

    } catch (SQLException exception) {

        exception.printStackTrace();
        return false;
    }
}


    // Get one assignment by ID
    public Assignment getAssignmentById(int assignmentId) {

String sql =
        "SELECT assignment_id, course_id, instructor_id, " +
        "title, description, due_date, max_marks, " +
        "file_name, file_path, created_at " +
        "FROM assignments " +
        "WHERE assignment_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, assignmentId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    Assignment assignment = new Assignment();

                    assignment.assignmentId =
                            resultSet.getInt("assignment_id");

                    assignment.courseId =
                            resultSet.getInt("course_id");

                    assignment.instructorId =
                            resultSet.getInt("instructor_id");

                    assignment.title =
                            resultSet.getString("title");

                    assignment.description =
                            resultSet.getString("description");

                    assignment.dueDate =
                            resultSet.getTimestamp("due_date");

                    assignment.maxMarks =
        resultSet.getInt("max_marks");

assignment.fileName =
        resultSet.getString("file_name");

assignment.filePath =
        resultSet.getString("file_path");

assignment.createdAt =
        resultSet.getTimestamp("created_at");

                    return assignment;
                }
            }

        } catch (SQLException exception) {

            exception.printStackTrace();
        }

        return null;
    }


    public List<Assignment> getAssignmentsByCourse(int courseId) {

        List<Assignment> assignments =
                new ArrayList<>();

String sql =
        "SELECT assignment_id, course_id, instructor_id, " +
        "title, description, due_date, max_marks, " +
        "file_name, file_path, created_at " +
        "FROM assignments " +
        "WHERE course_id = ? " +
        "ORDER BY due_date ASC, assignment_id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, courseId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    Assignment assignment =
                            new Assignment();

                    assignment.assignmentId =
                            resultSet.getInt("assignment_id");

                    assignment.courseId =
                            resultSet.getInt("course_id");

                    assignment.instructorId =
                            resultSet.getInt("instructor_id");

                    assignment.title =
                            resultSet.getString("title");

                    assignment.description =
                            resultSet.getString("description");

                    assignment.dueDate =
                            resultSet.getTimestamp("due_date");

                    assignment.maxMarks =
                            resultSet.getInt("max_marks");

                    assignment.fileName =
                            resultSet.getString("file_name");

                    assignment.filePath =
                            resultSet.getString("file_path");

                    assignment.createdAt =
                            resultSet.getTimestamp("created_at");

                    assignments.add(assignment);
                }
            }

        } catch (SQLException exception) {

            exception.printStackTrace();
        }

        return assignments;
    }


    public static class Assignment {

        public int assignmentId;

        public int courseId;

        public int instructorId;

        public String title;

        public String description;

        public Timestamp dueDate;

        public int maxMarks;

        public String fileName;

public String filePath;

        public Timestamp createdAt;


        // Getters

        public Timestamp getDueDate() {
            return dueDate;
        }

        public int getAssignmentId() {
            return assignmentId;
        }

        public int getCourseId() {
            return courseId;
        }

        public int getInstructorId() {
            return instructorId;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public int getMaxMarks() {
            return maxMarks;
        }

        public String getFileName() {
    return fileName;
}

public String getFilePath() {
    return filePath;
}

        public Timestamp getCreatedAt() {
            return createdAt;
        }
    }
}