package dao;

import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO used by the administrator dashboard.
 * Keeps SQL/database work separate from the Servlets.
 */
public class AdminDAO {

    public static class UserRow {
        public int userId;
        public String name;
        public String email;
        public String role;
        public Timestamp createdAt;
    }

    public static class CourseRow {
        public int courseId;
        public String title;
        public String description;
        public String syllabus;
        public int instructorId;
        public String instructorName;
        public int studentCount;
    }

    public static class Stats {
        public int totalUsers;
        public int totalStudents;
        public int totalInstructors;
        public int totalCourses;
        public int totalEnrollments;
        public int totalSubmissions;
        public int gradedSubmissions;
        public double averageMarks;
        public int avgEnrollment;
        public int recentRegistrations;
    }

    public static class ActivityRow {
        public String icon;
        public String title;
        public String description;
        public Timestamp timestamp;
        public String time;
    }

    public List<UserRow> getUsers() throws SQLException {
        List<UserRow> list = new ArrayList<>();
        String sql = "SELECT user_id, name, email, role, created_at FROM users ORDER BY created_at DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                UserRow user = new UserRow();
                user.userId = rs.getInt("user_id");
                user.name = rs.getString("name");
                user.email = rs.getString("email");
                user.role = rs.getString("role");
                user.createdAt = rs.getTimestamp("created_at");
                list.add(user);
            }
        }
        return list;
    }

    public boolean emailExists(String email, int exceptId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ? AND user_id <> ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, exceptId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public int createUser(String name, String email, String role, String passwordHash) throws SQLException {
        String sql = "INSERT INTO users (role, password_hash, name, email) VALUES (?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, role);
            ps.setString(2, passwordHash);
            ps.setString(3, name);
            ps.setString(4, email);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public boolean updateUser(int id, String name, String email, String role) throws SQLException {
        String sql = "UPDATE users SET name = ?, email = ?, role = ? WHERE user_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, role);
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteUser(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<CourseRow> getCourses() throws SQLException {
        List<CourseRow> list = new ArrayList<>();

        String sql =
                "SELECT c.course_id, c.title, c.description, c.syllabus, " +
                "c.instructor_id, u.name AS instructor_name, " +
                "COUNT(e.enrollment_id) AS student_count " +
                "FROM courses c " +
                "LEFT JOIN users u ON u.user_id = c.instructor_id " +
                "LEFT JOIN enrollments e ON e.course_id = c.course_id " +
                "GROUP BY c.course_id, c.title, c.description, c.syllabus, c.instructor_id, u.name " +
                "ORDER BY c.course_id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                CourseRow course = new CourseRow();
                course.courseId = rs.getInt("course_id");
                course.title = rs.getString("title");
                course.description = rs.getString("description");
                course.syllabus = rs.getString("syllabus");
                course.instructorId = rs.getInt("instructor_id");
                course.instructorName = rs.getString("instructor_name");
                course.studentCount = rs.getInt("student_count");
                list.add(course);
            }
        }
        return list;
    }

    public boolean createCourse(String title, String description, String syllabus, int instructorId) throws SQLException {
        String sql = "INSERT INTO courses (title, description, syllabus, instructor_id) VALUES (?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setString(3, syllabus);
            ps.setInt(4, instructorId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateCourse(int id, String title, String description, String syllabus, int instructorId) throws SQLException {
        String sql = "UPDATE courses SET title = ?, description = ?, syllabus = ?, instructor_id = ? WHERE course_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setString(3, syllabus);
            ps.setInt(4, instructorId);
            ps.setInt(5, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean courseHasEnrollments(int courseId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE course_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public boolean deleteCourse(int id) throws SQLException {
        String sql = "DELETE FROM courses WHERE course_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public Stats getStats() throws SQLException {
        Stats s = new Stats();

        try (Connection c = DBConnection.getConnection()) {
            s.totalUsers = count(c, "SELECT COUNT(*) FROM users");
            s.totalStudents = count(c, "SELECT COUNT(*) FROM users WHERE UPPER(role)='STUDENT'");
            s.totalInstructors = count(c, "SELECT COUNT(*) FROM users WHERE UPPER(role)='INSTRUCTOR'");
            s.totalCourses = count(c, "SELECT COUNT(*) FROM courses");
            s.totalEnrollments = count(c, "SELECT COUNT(*) FROM enrollments");
            s.totalSubmissions = count(c, "SELECT COUNT(*) FROM submissions");
            s.gradedSubmissions = count(c, "SELECT COUNT(*) FROM submissions WHERE marks IS NOT NULL");
            s.recentRegistrations = count(c, "SELECT COUNT(*) FROM users WHERE created_at >= NOW() - INTERVAL 30 DAY");

            String avgSql = "SELECT COALESCE(AVG(marks),0) FROM submissions WHERE marks IS NOT NULL";
            try (PreparedStatement ps = c.prepareStatement(avgSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) s.averageMarks = Math.round(rs.getDouble(1) * 10.0) / 10.0;
            }

            s.avgEnrollment = s.totalCourses == 0 ? 0 : Math.round((float) s.totalEnrollments / s.totalCourses);
        }

        return s;
    }

    private int count(Connection c, String sql) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public List<ActivityRow> getRecentActivity() throws SQLException {
        List<ActivityRow> list = new ArrayList<>();

        addUserActivities(list);
        addCourseActivities(list);
        addEnrollmentActivities(list);
        addMaterialActivities(list);
        addAssignmentActivities(list);
        addSubmissionActivities(list);

        list.sort((a, b) -> {
            if (a.timestamp == null && b.timestamp == null) return 0;
            if (a.timestamp == null) return 1;
            if (b.timestamp == null) return -1;
            return b.timestamp.compareTo(a.timestamp);
        });
        if (list.size() > 8) {
            return new ArrayList<>(list.subList(0, 8));
        }
        return list;
    }

    private void addUserActivities(List<ActivityRow> list) throws SQLException {
        String sql = "SELECT name, role, created_at FROM users ORDER BY created_at DESC LIMIT 4";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ActivityRow a = new ActivityRow();
                a.icon = "👤";
                a.title = "New user registered";
                a.description = rs.getString("name") + " joined as " + rs.getString("role");
                a.timestamp = rs.getTimestamp("created_at");
                a.time = String.valueOf(a.timestamp);
                list.add(a);
            }
        }
    }

    private void addCourseActivities(List<ActivityRow> list) throws SQLException {
        String sql = "SELECT c.title, u.name, c.course_id FROM courses c LEFT JOIN users u ON u.user_id=c.instructor_id ORDER BY c.course_id DESC LIMIT 4";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ActivityRow a = new ActivityRow();
                a.icon = "📚";
                a.title = "Course available";
                a.description = rs.getString("title") + " • Instructor: " + rs.getString("name");
                a.timestamp = null;
                a.time = "Course ID " + rs.getInt("course_id");
                list.add(a);
            }
        }
    }

    private void addEnrollmentActivities(List<ActivityRow> list) throws SQLException {
        String sql = "SELECT e.enrollment_date, u.name, c.title FROM enrollments e JOIN users u ON u.user_id=e.student_id JOIN courses c ON c.course_id=e.course_id ORDER BY e.enrollment_date DESC LIMIT 4";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ActivityRow a = new ActivityRow();
                a.icon = "🎓";
                a.title = "Student enrolled";
                a.description = rs.getString("name") + " enrolled in " + rs.getString("title");
                a.timestamp = rs.getTimestamp("enrollment_date");
                a.time = String.valueOf(a.timestamp);
                list.add(a);
            }
        }
    }

    private void addMaterialActivities(List<ActivityRow> list) throws SQLException {
        String sql = "SELECT m.uploaded_at, m.title, c.title AS course_title FROM materials m JOIN courses c ON c.course_id=m.course_id ORDER BY m.uploaded_at DESC LIMIT 4";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ActivityRow a = new ActivityRow();
                a.icon = "📤";
                a.title = "Material uploaded";
                a.description = rs.getString("title") + " • " + rs.getString("course_title");
                a.timestamp = rs.getTimestamp("uploaded_at");
                a.time = String.valueOf(a.timestamp);
                list.add(a);
            }
        }
    }

    private void addAssignmentActivities(List<ActivityRow> list) throws SQLException {
        String sql = "SELECT created_at, title FROM assignments ORDER BY created_at DESC LIMIT 4";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ActivityRow a = new ActivityRow();
                a.icon = "📝";
                a.title = "Assignment created";
                a.description = rs.getString("title");
                a.timestamp = rs.getTimestamp("created_at");
                a.time = String.valueOf(a.timestamp);
                list.add(a);
            }
        }
    }

    private void addSubmissionActivities(List<ActivityRow> list) throws SQLException {
        String sql = "SELECT s.submitted_at, u.name, a.title FROM submissions s JOIN users u ON u.user_id=s.student_id JOIN assignments a ON a.assignment_id=s.assignment_id ORDER BY s.submitted_at DESC LIMIT 4";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ActivityRow a = new ActivityRow();
                a.icon = "📝";
                a.title = "Assignment submitted";
                a.description = rs.getString("name") + " • " + rs.getString("title");
                a.timestamp = rs.getTimestamp("submitted_at");
                a.time = String.valueOf(a.timestamp);
                list.add(a);
            }
        }
    }
}
