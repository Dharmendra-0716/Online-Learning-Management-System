package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import util.DBConnection;
import util.PasswordHasher;

/**
 * Handles database operations related to users.
 */
public class UserDAO {

    // =========================
    // REGISTER STUDENT
    // =========================

    public RegistrationResult registerStudent(
            String name,
            String email,
            String password) {

        String checkQuery =
                "SELECT 1 FROM users WHERE email = ? LIMIT 1";

        String insertQuery = """
                INSERT INTO users
                (name, email, password_hash, role)
                VALUES (?, ?, ?, 'STUDENT')
                """;

        try (Connection connection = DBConnection.getConnection()) {

            if (connection == null) {
                return RegistrationResult.DATABASE_ERROR;
            }

            // Check whether email already exists
            try (PreparedStatement checkStatement =
                         connection.prepareStatement(checkQuery)) {

                checkStatement.setString(1, email);

                try (ResultSet result =
                             checkStatement.executeQuery()) {

                    if (result.next()) {
                        return RegistrationResult.EMAIL_ALREADY_EXISTS;
                    }
                }
            }

            // Hash password and create student
            try (PreparedStatement insertStatement =
                         connection.prepareStatement(insertQuery)) {

                insertStatement.setString(1, name);
                insertStatement.setString(2, email);
                insertStatement.setString(
                        3,
                        PasswordHasher.hash(password)
                );

                insertStatement.executeUpdate();

                return RegistrationResult.SUCCESS;
            }

        } catch (SQLException | IllegalStateException exception) {

            exception.printStackTrace();

            return RegistrationResult.DATABASE_ERROR;
        }
    }


    // =========================
    // LOGIN USER
    // =========================

    public LoginResult loginUser(
            String email,
            String password) {

        String query = """
                SELECT user_id, name, password_hash, role
                FROM users
                WHERE email = ?
                """;

        try (Connection connection = DBConnection.getConnection()) {

            if (connection == null) {
                return null;
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(query)) {

                statement.setString(1, email);

                try (ResultSet result =
                             statement.executeQuery()) {

                    // Email not found
                    if (!result.next()) {
                        return null;
                    }

                    int userId =
                            result.getInt("user_id");

                    String name =
                            result.getString("name");

                    String storedPassword =
                            result.getString("password_hash");

                    String role =
                            result.getString("role");

                    // Verify entered password
                    boolean passwordCorrect =
                            PasswordHasher.verify(
                                    password,
                                    storedPassword
                            );

                    if (!passwordCorrect) {
                        return null;
                    }

                    return new LoginResult(
                            userId,
                            name,
                            role
                    );
                }
            }

        } catch (SQLException | IllegalStateException exception) {

            exception.printStackTrace();

            return null;
        }
    }


    // =========================
    // REGISTRATION RESULT
    // =========================

    public enum RegistrationResult {

        SUCCESS,

        EMAIL_ALREADY_EXISTS,

        DATABASE_ERROR
    }


    // =========================
    // LOGIN RESULT
    // =========================

    public static class LoginResult {

        private final int userId;
        private final String name;
        private final String role;


        public LoginResult(
                int userId,
                String name,
                String role) {

            this.userId = userId;
            this.name = name;
            this.role = role;
        }


        public int getUserId() {
            return userId;
        }


        public String getName() {
            return name;
        }


        public String getRole() {
            return role;
        }
    }
}