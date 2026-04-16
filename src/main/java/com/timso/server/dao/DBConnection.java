package com.timso.server.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // mo ket noi den mysql cua xampp thong qua jdbc
    private static final String URL = "jdbc:mysql://localhost:3306/game_tim_so";
    private static final String USER = "root";
    private static final String PASS = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
