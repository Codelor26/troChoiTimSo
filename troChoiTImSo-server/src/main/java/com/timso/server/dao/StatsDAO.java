package com.timso.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsDAO {
    public boolean updateGameStats(int userId, boolean isWin, boolean isDraw, int points) {
        String sql;
        if (isDraw) {
            sql = "INSERT INTO game_stats (user_id, total_games, draws, total_points) "
                    + "VALUES (?, 1, 1, ?) "
                    + "ON DUPLICATE KEY UPDATE "
                    + "total_games = total_games + 1, "
                    + "draws = draws + 1, "
                    + "total_points = total_points + ?";
        } else if (isWin) {
            sql = "INSERT INTO game_stats (user_id, total_games, wins, total_points) "
                    + "VALUES (?, 1, 1, ?) "
                    + "ON DUPLICATE KEY UPDATE "
                    + "total_games = total_games + 1, "
                    + "wins = wins + 1, "
                    + "total_points = total_points + ?";
        } else {
            sql = "INSERT INTO game_stats (user_id, total_games, losses, total_points) "
                    + "VALUES (?, 1, 1, ?) "
                    + "ON DUPLICATE KEY UPDATE "
                    + "total_games = total_games + 1, "
                    + "losses = losses + 1, "
                    + "total_points = total_points + ?";
        }

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, points);
            pstmt.setInt(3, points);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean initGameStats(int userId) {
        String sql = "INSERT INTO game_stats (user_id, total_games, wins, losses, draws, total_points) "
                + "VALUES (?, 0, 0, 0, 0, 0) "
                + "ON DUPLICATE KEY UPDATE user_id = user_id";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Map<String, Object>> getLeaderboard() {
        List<Map<String, Object>> leaderboard = new ArrayList<>();
        String sql = "SELECT u.id, u.username, u.player_name, u.avatar, "
                + "COALESCE(SUM(gs.total_games), 0) as total_games, "
                + "COALESCE(SUM(gs.wins), 0) as wins, "
                + "COALESCE(SUM(gs.losses), 0) as losses, "
                + "COALESCE(SUM(gs.draws), 0) as draws, "
                + "COALESCE(SUM(gs.total_points), 0) as total_points, "
                + "ROUND(COALESCE(SUM(gs.wins), 0) * 100.0 / NULLIF(SUM(gs.total_games), 0), 2) as win_rate "
                + "FROM users u "
                + "LEFT JOIN game_stats gs ON u.id = gs.user_id "
                + "GROUP BY u.id, u.username, u.player_name, u.avatar "
                + "ORDER BY total_points DESC, win_rate DESC "
                + "LIMIT 10";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> player = new HashMap<>();
                player.put("playerName", rs.getString("player_name") != null && !rs.getString("player_name").isEmpty()
                        ? rs.getString("player_name")
                        : rs.getString("username"));
                player.put("avatar", rs.getString("avatar") != null ? rs.getString("avatar")
                        : "/icon/Martin-Berube-Character-Devil.256.png");
                player.put("totalGames", rs.getInt("total_games"));
                player.put("wins", rs.getInt("wins"));
                player.put("losses", rs.getInt("losses"));
                player.put("draws", rs.getInt("draws"));
                player.put("totalPoints", rs.getInt("total_points"));
                player.put("winRate", rs.getDouble("win_rate"));
                leaderboard.add(player);

                System.out.println("Player: " + player.get("playerName") + " - Games: " + player.get("totalGames")
                        + " - Points: " + player.get("totalPoints"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return leaderboard;
    }

    public int getUserRank(int userId) {
        String sql = "SELECT COUNT(*) + 1 as rank FROM ("
                + "SELECT gs.user_id, SUM(gs.total_points) as total_points "
                + "FROM game_stats gs "
                + "GROUP BY gs.user_id) AS ranked "
                + "WHERE total_points > (SELECT COALESCE(SUM(total_points), 0) FROM game_stats WHERE user_id = ?)";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("rank");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Map<String, Object> getUserStats(int userId) {
        Map<String, Object> stats = new HashMap<>();
        String sql = "SELECT SUM(total_games) as total_games, "
                + "SUM(wins) as wins, "
                + "SUM(losses) as losses, "
                + "SUM(draws) as draws, "
                + "SUM(total_points) as total_points, "
                + "ROUND(SUM(wins) * 100.0 / NULLIF(SUM(total_games), 0), 2) as win_rate "
                + "FROM game_stats WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                stats.put("totalGames", rs.getInt("total_games"));
                stats.put("wins", rs.getInt("wins"));
                stats.put("losses", rs.getInt("losses"));
                stats.put("draws", rs.getInt("draws"));
                stats.put("totalPoints", rs.getInt("total_points"));
                stats.put("winRate", rs.getDouble("win_rate"));
            } else {
                stats.put("totalGames", 0);
                stats.put("wins", 0);
                stats.put("losses", 0);
                stats.put("draws", 0);
                stats.put("totalPoints", 0);
                stats.put("winRate", 0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }
}