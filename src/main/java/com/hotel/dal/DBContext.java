package com.hotel.dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBContext - lớp cha cho mọi Repository.
 * NẾU BẠN ĐÃ CÓ DBContext RIÊNG: chỉ cần đảm bảo nó có method
 * public/protected Connection getConnection() throws SQLException
 * trả về MỘT CONNECTION MỚI mỗi lần gọi (các repository dùng try-with-resources).
 */
public class DBContext {

    private static final String SERVER = env("HMS_DB_SERVER", "localhost");
    private static final String PORT = env("HMS_DB_PORT", "1433");
    private static final String DATABASE = env("HMS_DB_NAME", "SingleHotelManagementDB");
    private static final String USER = env("HMS_DB_USER", env("HMS_DB_USERNAME", "ducnh"));
    private static final String PASSWORD = env("HMS_DB_PASSWORD", "ducnh");

    private static final String DEFAULT_URL =
            "jdbc:sqlserver://" + SERVER + ":" + PORT
            + ";databaseName=" + DATABASE
            + ";encrypt=true;trustServerCertificate=true"
            + ";loginTimeout=5";
    private static final String URL = env("HMS_DB_URL", DEFAULT_URL);

    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
