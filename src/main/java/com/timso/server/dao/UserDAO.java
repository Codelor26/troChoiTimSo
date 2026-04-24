package com.timso.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;

import com.timso.common.model.*;

public class UserDAO {
    // truy van du lieu tu bang users trong database
    public User login(String email, String password) {
        // username & password from user input
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            // PreparedStatement: tranh SQL injection?
            ps.setString(1, email);
            // setString = dua du lieu vao SQL query
            try (ResultSet rs = ps.executeQuery()) { // ketqu tra ve tu database
                if (rs.next()) {
                    String hashedPassword = rs.getString("password"); // lay password da duoc
                    // hash tu database
                    if (BCrypt.checkpw(password, hashedPassword)) {
                        // ma hoa password user nhap, sau do so sanh voi password da duoc hash trong
                        // database
                        User user = new User();
                        user.setId(rs.getInt("id"));
                        // lay du lieu tu db va gan vao doi tuong user
                        user.setUsername(rs.getString("username"));
                        user.setFullname(rs.getString("full_name"));
                        user.setGender(rs.getString("gender"));
                        user.setDob(rs.getDate("dob"));
                        user.setGold(rs.getInt("gold"));
                        user.setAvatar(rs.getString("avatar"));
                        user.setPlayerName(rs.getString("player_name"));
                        user.setLastNameChange(rs.getTimestamp("last_name_change"));

                        return user;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // chua hash
    // public User login(String email, String password) {
    // String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
    // try (Connection conn = DBConnection.getConnection();
    // PreparedStatement pstmt = conn.prepareStatement(sql)) {

    // pstmt.setString(1, email);
    // pstmt.setString(2, password);

    // ResultSet rs = pstmt.executeQuery();
    // if (rs.next()) {
    // User user = new User();
    // user.setId(rs.getInt("id"));
    // user.setUsername(rs.getString("username"));
    // user.setFullname(rs.getString("full_name"));
    // user.setGender(rs.getString("gender"));
    // user.setDob(rs.getDate("dob"));
    // user.setGold(rs.getInt("gold"));
    // user.setAvatar(rs.getString("avatar"));

    // return user;
    // }
    // } catch (SQLException e) {
    // e.printStackTrace();
    // }
    // return null;
    // }

    public boolean register(User user) {
        String sql = "INSERT INTO users (username, password, full_name, gender, dob) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUserName());
            // pstmt.setString(2, user.getPassword());
            String hashed = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
            pstmt.setString(2, hashed);
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getGender());

            java.util.Date utilDate = user.getDob();
            java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
            pstmt.setDate(5, sqlDate);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePassword(String email, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt());

            ps.setString(1, hashed);
            ps.setString(2, email);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateProfile(String name, String avatar, String email, boolean resetTimer) {
        // Nếu resetTimer = true (có đổi tên), cập nhật last_name_change thành NOW()
        String sql = resetTimer 
            ? "UPDATE users SET player_name = ?, avatar = ?, last_name_change = NOW() WHERE username = ?"
            : "UPDATE users SET avatar = ? WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            if (resetTimer) {
                ps.setString(1, name);
                ps.setString(2, avatar);
                ps.setString(3, email);
            } else {
                ps.setString(1, avatar);
                ps.setString(2, email);
            }

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean checkEmailExists(String email) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
