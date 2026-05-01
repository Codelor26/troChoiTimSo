package com.timso.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mindrot.jbcrypt.BCrypt;

import com.timso.common.model.*;

public class UserDAO {
    public User login(String email, String password) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs.getString("password");
                    if (BCrypt.checkpw(password, hashedPassword)) {
                        User user = new User();
                        user.setId(rs.getInt("id"));
                        user.setUsername(rs.getString("username"));
                        user.setFullname(rs.getString("full_name"));
                        user.setGender(rs.getString("gender"));
                        user.setDob(rs.getDate("dob"));
                        user.setGold(rs.getInt("gold"));
                        user.setAvatar(rs.getString("avatar"));
                        user.setPlayerName(rs.getString("player_name"));

                        return user;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUserByUsername(String username) {
        String sql = "SELECT id, username, full_name, player_name, avatar, gold, gender, dob FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setFullname(rs.getString("full_name"));
                user.setPlayerName(rs.getString("player_name"));
                user.setAvatar(rs.getString("avatar"));
                user.setGold(rs.getInt("gold"));
                user.setGender(rs.getString("gender"));
                user.setDob(rs.getDate("dob"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean register(User user) {
        String sql = "INSERT INTO users (username, password, full_name, gender, dob) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUserName());
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

    public boolean updateProfile(String name, String avatar, String email) {
        String sql = "UPDATE users SET player_name = ?, avatar = ? WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, avatar);
            ps.setString(3, email);

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

    public boolean checkUsernameExistsExcluding(String username, int excludeId) {
        String sql = "SELECT id FROM users WHERE username = ? AND id != ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUserInfo(int id, String username, String fullName,
            String gender, java.sql.Date dob, String newPassword) {
        boolean changePass = newPassword != null && !newPassword.isEmpty();
        String sql = changePass
                ? "UPDATE users SET username=?, full_name=?, gender=?, dob=?, password=? WHERE id=?"
                : "UPDATE users SET username=?, full_name=?, gender=?, dob=? WHERE id=" + id;
        if (!changePass) {
            sql = "UPDATE users SET username=?, full_name=?, gender=?, dob=? WHERE id=?";
        }
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, fullName);
            ps.setString(3, gender);
            ps.setDate(4, dob);
            if (changePass) {
                String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                ps.setString(5, hashed);
                ps.setInt(6, id);
            } else {
                ps.setInt(5, id);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addGold(String username, int amount) {
        String sql = "UPDATE users SET gold = gold + ? WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, amount);
            pstmt.setString(2, username);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Added " + amount + " gold to user: " + username);
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deductGold(String username, int amount) {
        String sql = "UPDATE users SET gold = gold - ? WHERE username = ? AND gold >= ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, amount);
            pstmt.setString(2, username);
            pstmt.setInt(3, amount);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getUserGold(String username) {
        String sql = "SELECT gold FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("gold");
            }
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public boolean updateSkill(String username, String skillType, int quantity) {
        String sql = "";
        switch (skillType) {
            case "light":
                sql = "UPDATE users SET light_skill = light_skill + ? WHERE username = ?";
                break;
            case "dark":
                sql = "UPDATE users SET dark_skill = dark_skill + ? WHERE username = ?";
                break;
            case "freeze":
                sql = "UPDATE users SET freeze_skill = freeze_skill + ? WHERE username = ?";
                break;
            default:
                return false;
        }

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantity);
            pstmt.setString(2, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, Integer> getUserSkills(String username) {
        Map<String, Integer> skills = new HashMap<>();
        String sql = "SELECT light_skill, dark_skill, freeze_skill FROM users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                skills.put("light", rs.getInt("light_skill"));
                skills.put("dark", rs.getInt("dark_skill"));
                skills.put("freeze", rs.getInt("freeze_skill"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return skills;
    }

    public boolean useSkill(String username, String skillType) {
        String sql = "";
        switch (skillType) {
            case "light":
                sql = "UPDATE users SET light_skill = light_skill - 1 WHERE username = ? AND light_skill > 0";
                break;
            case "dark":
                sql = "UPDATE users SET dark_skill = dark_skill - 1 WHERE username = ? AND dark_skill > 0";
                break;
            case "freeze":
                sql = "UPDATE users SET freeze_skill = freeze_skill - 1 WHERE username = ? AND freeze_skill > 0";
                break;
            default:
                return false;
        }

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
