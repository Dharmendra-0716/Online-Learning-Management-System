package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import util.DBConnection;

public class EnrollmentDAO {

    // =========================
    // ENROLL STUDENT
    // =========================

    public EnrollmentResult enrollStudent(int studentId, int courseId) {

        String checkQuery = """
                SELECT 1 FROM enrollments
                WHERE student_id = ? AND course_id = ?
                LIMIT 1
                """;

        String insertQuery = """
                INSERT INTO enrollments (student_id, course_id)
                VALUES (?, ?)
                """;

        try (Connection connection = DBConnection.getConnection()) {

            if (connection == null) {
                return EnrollmentResult.DATABASE_ERROR;
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(checkQuery)) {

                statement.setInt(1, studentId);
                statement.setInt(2, courseId);

                try (ResultSet result = statement.executeQuery()) {

                    if (result.next()) {
                        return EnrollmentResult.ALREADY_ENROLLED;
                    }
                }
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(insertQuery)) {

                statement.setInt(1, studentId);
                statement.setInt(2, courseId);

                statement.executeUpdate();

                return EnrollmentResult.SUCCESS;
            }

        } catch (Exception exception) {

            exception.printStackTrace();

            return EnrollmentResult.DATABASE_ERROR;
        }
    }


    // =========================
    // GET ENROLLED COURSES
    // =========================

    public List<EnrolledCourse> getEnrolledCourses(int studentId) {

        List<EnrolledCourse> courses =
                new ArrayList<>();

        String query = """
                SELECT
                    c.course_id,
                    c.title,
                    c.description,
                    c.syllabus,
                    u.name AS instructor_name
                FROM enrollments e
                JOIN courses c
                    ON e.course_id = c.course_id
                JOIN users u
                    ON c.instructor_id = u.user_id
                WHERE e.student_id = ?
                ORDER BY e.enrollment_date DESC
                """;

        try (Connection connection =
                     DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(query)) {

            statement.setInt(1, studentId);

            try (ResultSet result =
                         statement.executeQuery()) {

                while (result.next()) {

                    EnrolledCourse course =
                            new EnrolledCourse();

                    course.setCourseId(
                            result.getInt("course_id"));

                    course.setTitle(
                            result.getString("title"));

                    course.setDescription(
                            result.getString("description"));

                    course.setSyllabus(
                            result.getString("syllabus"));

                    course.setInstructorName(
                            result.getString("instructor_name"));

                    courses.add(course);
                }
            }

        } catch (Exception exception) {

            exception.printStackTrace();
        }

        return courses;
    }


    // =========================
    // CHECK ENROLLMENT
    // =========================

    public boolean isEnrolled(
            int studentId,
            int courseId) {

        String query = """
                SELECT 1
                FROM enrollments
                WHERE student_id = ?
                  AND course_id = ?
                LIMIT 1
                """;

        try (Connection connection =
                     DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(query)) {

            statement.setInt(1, studentId);
            statement.setInt(2, courseId);

            try (ResultSet result =
                         statement.executeQuery()) {

                return result.next();
            }

        } catch (Exception exception) {

            exception.printStackTrace();

            return false;
        }
    }


    // =========================
    // GET STUDENTS BY INSTRUCTOR
    // =========================

    public List<EnrolledStudent> getStudentsByInstructor(
            int instructorId) {

        List<EnrolledStudent> students =
                new ArrayList<>();

        String query = """
                SELECT
                    u.user_id,
                    u.name,
                    u.email,
                    c.course_id,
                    c.title,
                    e.enrollment_date
                FROM enrollments e
                JOIN users u
                    ON e.student_id = u.user_id
                JOIN courses c
                    ON e.course_id = c.course_id
                WHERE c.instructor_id = ?
                ORDER BY e.enrollment_date DESC
                """;

        try (Connection connection =
                     DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(query)) {

            statement.setInt(1, instructorId);

            try (ResultSet result =
                         statement.executeQuery()) {

                while (result.next()) {

                    EnrolledStudent student =
                            new EnrolledStudent();

                    student.setStudentId(
                            result.getInt("user_id"));

                    student.setStudentName(
                            result.getString("name"));

                    student.setEmail(
                            result.getString("email"));

                    student.setCourseId(
                            result.getInt("course_id"));

                    student.setCourseTitle(
                            result.getString("title"));

                    student.setEnrollmentDate(
                            result.getTimestamp(
                                    "enrollment_date"));

                    students.add(student);
                }
            }

        } catch (Exception exception) {

            exception.printStackTrace();
        }

        return students;
    }


    // =========================
    // ENROLLMENT RESULT
    // =========================

    public enum EnrollmentResult {
        SUCCESS,
        ALREADY_ENROLLED,
        DATABASE_ERROR
    }


    // =========================
    // ENROLLED COURSE MODEL
    // =========================

    public static class EnrolledCourse {

        private int courseId;
        private String title;
        private String description;
        private String syllabus;
        private String instructorName;

        public int getCourseId() {
            return courseId;
        }

        public void setCourseId(int courseId) {
            this.courseId = courseId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getSyllabus() {
            return syllabus;
        }

        public void setSyllabus(String syllabus) {
            this.syllabus = syllabus;
        }

        public String getInstructorName() {
            return instructorName;
        }

        public void setInstructorName(String instructorName) {
            this.instructorName = instructorName;
        }
    }


    // =========================
    // ENROLLED STUDENT MODEL
    // =========================

    public static class EnrolledStudent {

        private int studentId;
        private String studentName;
        private String email;
        private int courseId;
        private String courseTitle;
        private java.sql.Timestamp enrollmentDate;

        public int getStudentId() {
            return studentId;
        }

        public void setStudentId(int studentId) {
            this.studentId = studentId;
        }

        public String getStudentName() {
            return studentName;
        }

        public void setStudentName(String studentName) {
            this.studentName = studentName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public int getCourseId() {
            return courseId;
        }

        public void setCourseId(int courseId) {
            this.courseId = courseId;
        }

        public String getCourseTitle() {
            return courseTitle;
        }

        public void setCourseTitle(String courseTitle) {
            this.courseTitle = courseTitle;
        }

        public java.sql.Timestamp getEnrollmentDate() {
            return enrollmentDate;
        }

        public void setEnrollmentDate(
                java.sql.Timestamp enrollmentDate) {

            this.enrollmentDate = enrollmentDate;
        }
    }
}