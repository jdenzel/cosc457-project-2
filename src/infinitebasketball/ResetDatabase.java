package infinitebasketball;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResetDatabase {

    public static void main(String[] args) {
        Connection con = DBConnection.connect();
        if (con == null) return;

        try {
            Statement stmt = con.createStatement();
            
            // 1. Disable Foreign Key Checks 
            // (Crucial: prevents errors when dropping tables that are linked to others)
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            
            // 2. Get a list of ALL tables in your database
            ResultSet rs = stmt.executeQuery("SHOW TABLES");
            List<String> tables = new ArrayList<>();
            
            while (rs.next()) {
                tables.add(rs.getString(1));
            }
            
            // 3. Drop each table found
            if (tables.isEmpty()) {
                System.out.println("Database is already empty!");
            } else {
                for (String table : tables) {
                    System.out.println("Dropping table: " + table);
                    stmt.executeUpdate("DROP TABLE " + table);
                }
                System.out.println("--------------------------------");
                System.out.println("All tables dropped successfully.");
                System.out.println("Run SetupDatabase.java to rebuild them.");
            }

            // 4. Re-enable Foreign Key Checks
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}