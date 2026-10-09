package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import util.DBConnection;

public class CourseDAO {

    // =========================
    // GET ALL COURSES
    // =========================

    public List<Course> getAllCourses() {

        List<Course> courses = new ArrayList<>();

        String query = """
                SELECT
                    c.course_id,
                    c.title,
                    c.description,
                    c.syllabus,
                    c.instructor_id,
                    u.name AS instructor_name
                FROM courses c
                JOIN users u
                    ON c.instructor_id = u.user_id
                ORDER BY c.course_id DESC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(query);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                Course course = new Course();

                course.setCourseId(
                        result.getInt("course_id")
                );

                course.setTitle(
                        result.getString("title")
                );

                course.setDescription(
                        result.getString("description")
                );

                course.setSyllabus(
                        result.getString("syllabus")
                );

                course.setInstructorId(
                        result.getInt("instructor_id")
                );

                course.setInstructorName(
                        result.getString("instructor_name")
                );

                courses.add(course);
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return courses;
    }


    // =========================
    // GET COURSE BY ID
    // =========================

    public Course getCourseById(int courseId) {

        String query = """
                SELECT
                    c.course_id,
                    c.title,
                    c.description,
                    c.syllabus,
                    c.instructor_id,
                    u.name AS instructor_name
                FROM courses c
                JOIN users u
                    ON c.instructor_id = u.user_id
                WHERE c.course_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(query)) {

            statement.setInt(1, courseId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    Course course = new Course();

                    course.setCourseId(
                            result.getInt("course_id")
                    );

                    course.setTitle(
                            result.getString("title")
                    );

                    course.setDescription(
                            result.getString("description")
                    );

                    course.setSyllabus(
                            result.getString("syllabus")
                    );

                    course.setInstructorId(
                            result.getInt("instructor_id")
                    );

                    course.setInstructorName(
                            result.getString("instructor_name")
                    );

                    return course;
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    // =========================
// GET COURSES BY INSTRUCTOR
// =========================

public List<Course> getCoursesByInstructor(int instructorId) {

    List<Course> courses = new ArrayList<>();

    String query = """
            SELECT
                c.course_id,
                c.title,
                c.description,
                c.syllabus,
                c.instructor_id,
                u.name AS instructor_name
            FROM courses c
            JOIN users u
                ON c.instructor_id = u.user_id
            WHERE c.instructor_id = ?
            ORDER BY c.course_id DESC
            """;

    try (Connection connection = DBConnection.getConnection();
         PreparedStatement statement =
                 connection.prepareStatement(query)) {

        statement.setInt(1, instructorId);

        try (ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                Course course = new Course();

                course.setCourseId(
                        result.getInt("course_id")
                );

                course.setTitle(
                        result.getString("title")
                );

                course.setDescription(
                        result.getString("description")
                );

                course.setSyllabus(
                        result.getString("syllabus")
                );

                course.setInstructorId(
                        result.getInt("instructor_id")
                );

                course.setInstructorName(
                        result.getString("instructor_name")
                );

                courses.add(course);
            }
        }

    } catch (Exception e) {

        e.printStackTrace();
    }

    return courses;
}

    // =========================
// CREATE COURSE
// =========================

public boolean createCourse(
        String title,
        String description,
        String syllabus,
        int instructorId) {

    String query = """
            INSERT INTO courses
                (title, description, syllabus, instructor_id)
            VALUES
                (?, ?, ?, ?)
            """;

    try (Connection connection = DBConnection.getConnection();
         PreparedStatement statement =
                 connection.prepareStatement(query)) {

        statement.setString(1, title);
        statement.setString(2, description);
        statement.setString(3, syllabus);
        statement.setInt(4, instructorId);

        return statement.executeUpdate() == 1;

    } catch (Exception e) {

        e.printStackTrace();
        return false;
    }
}


    // =========================
    // COURSE MODEL
    // =========================

    public static class Course {

        private int courseId;
        private String title;
        private String description;
        private String syllabus;
        private int instructorId;
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


        public int getInstructorId() {
            return instructorId;
        }

        public void setInstructorId(int instructorId) {
            this.instructorId = instructorId;
        }


        public String getInstructorName() {
            return instructorName;
        }

        public void setInstructorName(String instructorName) {
            this.instructorName = instructorName;
        }
    }
}