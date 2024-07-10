package org.example.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionUtil {

    private static String url = "jdbc:postgresql://localhost:5432/project_0_db";

    private static String username = "postgres";

    private static String password = "password";

    /**
     * @return an active connection to the database
     */
    public static Connection c = null;

    public static Connection getConnection() {
        if(c != null)
            return c;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection(url,username, password);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.exit(0);
        }
        return c;
    }
}
