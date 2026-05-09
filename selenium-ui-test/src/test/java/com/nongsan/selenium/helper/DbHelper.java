package com.nongsan.selenium.helper;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Helper thao tac voi DB de verify data va rollback fixture.
 */
public class DbHelper {
    private static Properties props = new Properties();
    
    static {
        try (InputStream in = DbHelper.class.getResourceAsStream("/selenium.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            props.getProperty("db.url"),
            props.getProperty("db.user"),
            props.getProperty("db.password")
        );
    }

    // --- Category Helper ---

    public static boolean categoryExistsByName(String name) {
        String sql = "SELECT COUNT(*) FROM categories WHERE category_name = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void rollbackCategoryByName(String name) {
        String sql = "DELETE FROM categories WHERE category_name = ? ORDER BY category_id DESC LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            int rows = ps.executeUpdate();
            System.out.println("Rollback category '" + name + "': " + rows + " row(s) deleted.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void rollbackDuplicateCategoryByName(String name) {
        // Chỉ xóa nếu có nhiều hơn 1 bản ghi (giữ lại bản ghi gốc)
        String checkSql = "SELECT COUNT(*) FROM categories WHERE category_name = ?";
        String deleteSql = "DELETE FROM categories WHERE category_name = ? ORDER BY category_id DESC LIMIT 1";
        
        try (Connection conn = getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setString(1, name);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 1) {
                    try (PreparedStatement delPs = conn.prepareStatement(deleteSql)) {
                        delPs.setString(1, name);
                        int rows = delPs.executeUpdate();
                        System.out.println("Rollback duplicate category '" + name + "': " + rows + " row(s) deleted.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- Product Helper ---

    public static boolean productExistsByName(String name) {
        String sql = "SELECT COUNT(*) FROM products WHERE name = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void rollbackProductByName(String name) {
        String sql = "DELETE FROM products WHERE name = ? ORDER BY product_id DESC LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            int rows = ps.executeUpdate();
            System.out.println("Rollback product '" + name + "': " + rows + " row(s) deleted.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
