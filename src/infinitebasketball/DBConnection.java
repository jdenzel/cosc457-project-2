package infinitebasketball;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    
    // Credentials from your uploaded file
    private static final String USER = "jmabila2";
    private static final String PASS = "COSC*8m1pr";
    
    // Using Port 3360 as specified in your COSC457.java example
    // Added serverTimezone to prevent common MySQL driver errors
    private static final String URL = "jdbc:mysql://triton.towson.edu:3360/jmabila2db?serverTimezone=EST";

    public static Connection connect() {
        try {
            // Load the driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(URL, USER, PASS);
            return con;
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Connection Failed: " + e.getMessage());
            return null;
        }
    }
}