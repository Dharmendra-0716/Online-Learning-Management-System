package main;

import model.Admin;
import model.Instructor;
import model.Student;
import model.User;

public class LMSApplication {

    public static void main(String[] args) {

        User admin = new Admin(
                1,
                "Admin",
                "admin@lms.com",
                "admin123"
        );

        User instructor = new Instructor(
                2,
                "John",
                "john@lms.com",
                "john123"
        );

        User student = new Student(
                3,
                "Dhoni",
                "dhoni@lms.com",
                "dhoni123"
        );

        admin.showDashboard();

        instructor.showDashboard();

        student.showDashboard();
    }
}