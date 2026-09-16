package model;

public class Admin extends User {

    public Admin(int userId, String name, String email, String password) {
        super(userId, name, email, password);
    }

    @Override
    public void showDashboard() {

        System.out.println("\n================================");
        System.out.println("       ADMIN DASHBOARD");
        System.out.println("================================");

        System.out.println("1. User Management");
        System.out.println("2. Course Management");
        System.out.println("3. Performance Analytics");
        System.out.println("4. System Settings");
        System.out.println("5. Activity Monitoring");
        System.out.println("6. Logout");
    }
}