package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import util.DBConnection;

public class MaterialDAO {

    public boolean addMaterial(
            int courseId,
            int instructorId,
            String title,
            String description,
            String fileName,
            String filePath,
            String materialType) {

        String query = """
                INSERT INTO materials
                (course_id, instructor_id, title, description,
                 file_name, file_path, material_type)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, courseId);
            statement.setInt(2, instructorId);
            statement.setString(3, title);
            statement.setString(4, description);
            statement.setString(5, fileName);
            statement.setString(6, filePath);
            statement.setString(7, materialType);

            return statement.executeUpdate() > 0;

        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public List<Material> getMaterialsByCourse(int courseId) {

        List<Material> materials = new ArrayList<>();

        String query = """
                SELECT material_id, course_id, instructor_id,
                       title, description, file_name,
                       file_path, material_type, uploaded_at
                FROM materials
                WHERE course_id = ?
                ORDER BY uploaded_at DESC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, courseId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    Material material = new Material();

                    material.setMaterialId(result.getInt("material_id"));
                    material.setCourseId(result.getInt("course_id"));
                    material.setInstructorId(result.getInt("instructor_id"));
                    material.setTitle(result.getString("title"));
                    material.setDescription(result.getString("description"));
                    material.setFileName(result.getString("file_name"));
                    material.setFilePath(result.getString("file_path"));
                    material.setMaterialType(result.getString("material_type"));
                    material.setUploadedAt(
                            result.getTimestamp("uploaded_at")
                    );

                    materials.add(material);
                }
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return materials;
    }

    public Material getMaterialById(int materialId) {

        String query = """
                SELECT material_id, course_id, instructor_id,
                       title, description, file_name,
                       file_path, material_type, uploaded_at
                FROM materials
                WHERE material_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, materialId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    Material material = new Material();

                    material.setMaterialId(result.getInt("material_id"));
                    material.setCourseId(result.getInt("course_id"));
                    material.setInstructorId(result.getInt("instructor_id"));
                    material.setTitle(result.getString("title"));
                    material.setDescription(result.getString("description"));
                    material.setFileName(result.getString("file_name"));
                    material.setFilePath(result.getString("file_path"));
                    material.setMaterialType(result.getString("material_type"));
                    material.setUploadedAt(
                            result.getTimestamp("uploaded_at")
                    );

                    return material;
                }
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return null;
    }

    public static class Material {

        private int materialId;
        private int courseId;
        private int instructorId;
        private String title;
        private String description;
        private String fileName;
        private String filePath;
        private String materialType;
        private java.sql.Timestamp uploadedAt;

        public int getMaterialId() {
            return materialId;
        }

        public void setMaterialId(int materialId) {
            this.materialId = materialId;
        }

        public int getCourseId() {
            return courseId;
        }

        public void setCourseId(int courseId) {
            this.courseId = courseId;
        }

        public int getInstructorId() {
            return instructorId;
        }

        public void setInstructorId(int instructorId) {
            this.instructorId = instructorId;
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

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getMaterialType() {
            return materialType;
        }

        public void setMaterialType(String materialType) {
            this.materialType = materialType;
        }

        public java.sql.Timestamp getUploadedAt() {
            return uploadedAt;
        }

        public void setUploadedAt(java.sql.Timestamp uploadedAt) {
            this.uploadedAt = uploadedAt;
        }
    }
}