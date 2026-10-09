package dao;

import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizDAO {

    // =========================
    // Quiz Model
    // =========================

    public static class Quiz {

        public int quizId;
        public int courseId;
        public int instructorId;
        public String title;
        public String description;
        public Timestamp createdAt;

        public Quiz(
                int quizId,
                int courseId,
                int instructorId,
                String title,
                String description,
                Timestamp createdAt) {

            this.quizId = quizId;
            this.courseId = courseId;
            this.instructorId = instructorId;
            this.title = title;
            this.description = description;
            this.createdAt = createdAt;
        }
    }


    // =========================
    // Question Model
    // =========================

    public static class Question {

        public int questionId;
        public int quizId;
        public String questionText;
        public String optionA;
        public String optionB;
        public String optionC;
        public String optionD;
        public String correctOption;

        public Question(
                int questionId,
                int quizId,
                String questionText,
                String optionA,
                String optionB,
                String optionC,
                String optionD,
                String correctOption) {

            this.questionId = questionId;
            this.quizId = quizId;
            this.questionText = questionText;
            this.optionA = optionA;
            this.optionB = optionB;
            this.optionC = optionC;
            this.optionD = optionD;
            this.correctOption = correctOption;
        }
    }


    // =========================
    // Create Quiz
    // =========================

    public int createQuiz(
            int courseId,
            int instructorId,
            String title,
            String description) {

        String sql = """
                INSERT INTO quizzes
                (course_id, instructor_id, title, description)
                VALUES (?, ?, ?, ?)
                """;

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps =
                        con.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {

            ps.setInt(1, courseId);
            ps.setInt(2, instructorId);
            ps.setString(3, title);
            ps.setString(4, description);

            int rows = ps.executeUpdate();

            if (rows == 0) {
                return -1;
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }


    // =========================
    // Add Question
    // =========================

    public boolean addQuestion(
            int quizId,
            String questionText,
            String optionA,
            String optionB,
            String optionC,
            String optionD,
            String correctOption) {

        String sql = """
                INSERT INTO quiz_questions
                (quiz_id, question_text, option_a, option_b,
                 option_c, option_d, correct_option)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setInt(1, quizId);
            ps.setString(2, questionText);
            ps.setString(3, optionA);
            ps.setString(4, optionB);
            ps.setString(5, optionC);
            ps.setString(6, optionD);
            ps.setString(7, correctOption);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    // =========================
    // Get Quiz By ID
    // =========================

    public Quiz getQuizById(int quizId) {

        String sql = """
                SELECT quiz_id, course_id, instructor_id,
                       title, description, created_at
                FROM quizzes
                WHERE quiz_id = ?
                """;

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setInt(1, quizId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    return new Quiz(
                            rs.getInt("quiz_id"),
                            rs.getInt("course_id"),
                            rs.getInt("instructor_id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getTimestamp("created_at")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    // =========================
    // Get Quizzes By Course
    // =========================

    public List<Quiz> getQuizzesByCourse(int courseId) {

        List<Quiz> quizzes = new ArrayList<>();

        String sql = """
                SELECT quiz_id, course_id, instructor_id,
                       title, description, created_at
                FROM quizzes
                WHERE course_id = ?
                ORDER BY created_at DESC
                """;

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setInt(1, courseId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    quizzes.add(
                            new Quiz(
                                    rs.getInt("quiz_id"),
                                    rs.getInt("course_id"),
                                    rs.getInt("instructor_id"),
                                    rs.getString("title"),
                                    rs.getString("description"),
                                    rs.getTimestamp("created_at")
                            )
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return quizzes;
    }


    // =========================
    // Get Questions By Quiz
    // =========================

    public List<Question> getQuestionsByQuiz(int quizId) {

        List<Question> questions = new ArrayList<>();

        String sql = """
                SELECT question_id, quiz_id, question_text,
                       option_a, option_b, option_c,
                       option_d, correct_option
                FROM quiz_questions
                WHERE quiz_id = ?
                ORDER BY question_id
                """;

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setInt(1, quizId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    questions.add(
                            new Question(
                                    rs.getInt("question_id"),
                                    rs.getInt("quiz_id"),
                                    rs.getString("question_text"),
                                    rs.getString("option_a"),
                                    rs.getString("option_b"),
                                    rs.getString("option_c"),
                                    rs.getString("option_d"),
                                    rs.getString("correct_option")
                            )
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return questions;
    }


    // =========================
    // Check Course Ownership
    // =========================

    public boolean isQuizOwnedByInstructor(
            int quizId,
            int instructorId) {

        String sql = """
                SELECT quiz_id
                FROM quizzes
                WHERE quiz_id = ?
                AND instructor_id = ?
                """;

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setInt(1, quizId);
            ps.setInt(2, instructorId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    // =========================
    // Check Student Enrollment
    // =========================

    public boolean isStudentEnrolled(
            int studentId,
            int courseId) {

        String sql = """
                SELECT enrollment_id
                FROM enrollments
                WHERE student_id = ?
                AND course_id = ?
                """;

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setInt(1, studentId);
            ps.setInt(2, courseId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    // =========================
    // Get Quizzes For Student
    // =========================

    public List<Quiz> getQuizzesForStudent(int studentId) {

        List<Quiz> quizzes = new ArrayList<>();

        String sql = """
                SELECT DISTINCT
                       q.quiz_id,
                       q.course_id,
                       q.instructor_id,
                       q.title,
                       q.description,
                       q.created_at
                FROM quizzes q
                INNER JOIN enrollments e
                    ON q.course_id = e.course_id
                WHERE e.student_id = ?
                ORDER BY q.created_at DESC
                """;

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    quizzes.add(
                            new Quiz(
                                    rs.getInt("quiz_id"),
                                    rs.getInt("course_id"),
                                    rs.getInt("instructor_id"), 
                                    rs.getString("title"),
                                    rs.getString("description"),
                                    rs.getTimestamp("created_at")
                            )
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return quizzes;
    }
    public List<Quiz> getQuizzesByInstructor(int instructorId) {

    List<Quiz> quizzes = new ArrayList<>();

    String sql = """
        SELECT quiz_id, course_id, instructor_id,
               title, description, created_at
        FROM quizzes
        WHERE instructor_id = ?
        ORDER BY created_at DESC
        """;

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, instructorId);

        try (ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                quizzes.add(
                    new Quiz(
                        rs.getInt("quiz_id"),
                        rs.getInt("course_id"),
                        rs.getInt("instructor_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getTimestamp("created_at")
                    )
                );
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    return quizzes;
}
    public boolean hasStudentAttempted(int quizId, int studentId) {

    String sql = "SELECT 1 FROM quiz_attempts WHERE quiz_id = ? AND student_id = ?";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, quizId);
        ps.setInt(2, studentId);

        try (ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }

    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}

public int submitQuiz(int quizId, int studentId, java.util.Map<Integer, String> answers) {

    String checkSql =
            "SELECT 1 FROM quiz_attempts WHERE quiz_id = ? AND student_id = ?";

    String questionSql =
            "SELECT question_id, correct_option FROM quiz_questions WHERE quiz_id = ?";

    String insertSql =
            "INSERT INTO quiz_attempts " +
            "(quiz_id, student_id, score, total_marks) VALUES (?, ?, ?, ?)";

    try (Connection con = DBConnection.getConnection()) {

        // Check duplicate attempt
        try (PreparedStatement check = con.prepareStatement(checkSql)) {

            check.setInt(1, quizId);
            check.setInt(2, studentId);

            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    return -1;
                }
            }
        }

        int score = 0;
        int total = 0;

        try (PreparedStatement ps = con.prepareStatement(questionSql)) {

            ps.setInt(1, quizId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    total++;

                    int questionId = rs.getInt("question_id");
                    String correct = rs.getString("correct_option");

                    String answer = answers.get(questionId);

                    if (answer != null &&
                        answer.equalsIgnoreCase(correct)) {

                        score++;
                    }
                }
            }
        }

        try (PreparedStatement ps = con.prepareStatement(insertSql)) {

            ps.setInt(1, quizId);
            ps.setInt(2, studentId);
            ps.setInt(3, score);
            ps.setInt(4, total);

            ps.executeUpdate();
        }

        return score;

    } catch (Exception e) {

        e.printStackTrace();
        return -2;
    }
}
public java.util.Map<String, Object> getQuizResult(int quizId, int studentId) {

    java.util.Map<String, Object> result =
        new java.util.HashMap<>();

    String sql =
        "SELECT score, total_marks, attempted_at " +
        "FROM quiz_attempts " +
        "WHERE quiz_id = ? AND student_id = ?";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, quizId);
        ps.setInt(2, studentId);

        try (ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {

                result.put("score", rs.getInt("score"));
                result.put("totalMarks", rs.getInt("total_marks"));
                result.put(
                    "attemptedAt",
                    rs.getTimestamp("attempted_at").toString()
                );

            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    return result;
}
public List<String[]> getInstructorQuizResults(
        int quizId,
        int instructorId) {

    List<String[]> results = new ArrayList<>();

    String sql = """
        SELECT
            u.name,
            qa.score,
            qa.total_marks,
            qa.attempted_at
        FROM quizzes q
        INNER JOIN enrollments e
            ON q.course_id = e.course_id
        INNER JOIN users u
            ON e.student_id = u.user_id
        LEFT JOIN quiz_attempts qa
            ON qa.quiz_id = q.quiz_id
            AND qa.student_id = u.user_id
        WHERE q.quiz_id = ?
        AND q.instructor_id = ?
        ORDER BY u.name
        """;

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, quizId);
        ps.setInt(2, instructorId);

        try (ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                String name =
                    rs.getString("name");

                String score =
                    rs.getObject("score") == null
                    ? null
                    : String.valueOf(rs.getInt("score"));

                String total =
                    rs.getObject("total_marks") == null
                    ? null
                    : String.valueOf(
                        rs.getInt("total_marks")
                    );

                String attemptedAt =
                    rs.getTimestamp("attempted_at") == null
                    ? null
                    : rs.getTimestamp(
                        "attempted_at"
                    ).toString();

                results.add(
                    new String[] {
                        name,
                        score,
                        total,
                        attemptedAt
                    }
                );
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    return results;
}
}