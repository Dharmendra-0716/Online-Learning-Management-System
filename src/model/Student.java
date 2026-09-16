package model;

public class Student extends User {

    public Student(int userId, String name, String email, String password) {
        super(userId, name, email, password);
    }

    @Override
    public void showDashboard() {

        System.out.println("\n================================");
        System.out.println("       STUDENT DASHBOARD");
        System.out.println("================================");

        System.out.println("1. Course Enrollment");
        System.out.println("2. Material Access");
        System.out.println("3. Assignment Submission");
        System.out.println("4. Progress Tracking");
        System.out.println("5. Feedback & Grades");
        System.out.println("6. Logout");
    }
}