package com.timso.server.dao.test;

import java.sql.Connection;
import java.sql.SQLException;
import com.timso.server.dao.DBConnection;

public class TestConnection {
    public static void main(String[] args) {
        try {
            Connection conn = DBConnection.getConnection();

            if (conn != null) {
                System.out.println("Kết nối Database thành công!");
                conn.close(); // Nhớ đóng kết nối sau khi test xong
            }
        } catch (SQLException e) {
            System.out.println("Kết nối thất bại! Hãy kiểm tra XAMPP hoặc tên Database.");
            e.printStackTrace();
        }
    }
}

// success