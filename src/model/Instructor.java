package model;

public class Instructor extends User {

    public Instructor(int userId, String name, String email, String password) {
        super(userId, name, email, password);
    }

    @Override
    public void showDashboard() {

        System.out.println("\n================================");
        System.out.println("     INSTRUCTOR DASHBOARD");
        System.out.println("================================");

        System.out.println("1. Course Management");
        System.out.println("2. Assignment Grading");
        System.out.println("3. Student Performance");
        System.out.println("4. Course Enrollment Stats");
        System.out.println("5. Feedback Summary");
        System.out.println("6. Logout");
    }
}