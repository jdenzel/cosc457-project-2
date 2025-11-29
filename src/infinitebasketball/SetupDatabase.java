package infinitebasketball;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class SetupDatabase {

    public static void main(String[] args) {
        init();
    }

    public static void init() {
        System.out.println("Checking database structure...");
        Connection con = DBConnection.connect();
        
        if (con == null) {
            System.err.println("Setup aborted: No database connection.");
            return; 
        }

        try {
            Statement stmt = con.createStatement();

            // 1. Role Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Role (RoleID INT PRIMARY KEY AUTO_INCREMENT, RoleName VARCHAR(50))");
            stmt.executeUpdate("INSERT IGNORE INTO Role (RoleID, RoleName) VALUES (1, 'Referee'), (2, 'Scorekeeper'), (3, 'Timekeeper')");

            // 2. Team Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Team (TeamID INT PRIMARY KEY AUTO_INCREMENT, TeamName VARCHAR(50), CoachName VARCHAR(50))");

            // 3. Worker Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Worker (WorkerID INT PRIMARY KEY AUTO_INCREMENT, FirstName VARCHAR(50), LastName VARCHAR(50), Phone VARCHAR(20), Email VARCHAR(100), WorkerType VARCHAR(20))");

            // 4. Player Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Player (PlayerID INT PRIMARY KEY AUTO_INCREMENT, FirstName VARCHAR(50), LastName VARCHAR(50), TeamID INT, FOREIGN KEY (TeamID) REFERENCES Team(TeamID))");

            // 5. Game Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Game (GameID INT PRIMARY KEY AUTO_INCREMENT, GameDate DATE, GameTime VARCHAR(20), Location VARCHAR(100), HomeTeamID INT, AwayTeamID INT, HomeScore INT DEFAULT 0, AwayScore INT DEFAULT 0, FOREIGN KEY (HomeTeamID) REFERENCES Team(TeamID), FOREIGN KEY (AwayTeamID) REFERENCES Team(TeamID))");

            // 6. Game Assignment Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS GameAssignment (AssignmentID INT PRIMARY KEY AUTO_INCREMENT, GameID INT, WorkerID INT, RoleID INT, FOREIGN KEY (GameID) REFERENCES Game(GameID), FOREIGN KEY (WorkerID) REFERENCES Worker(WorkerID), FOREIGN KEY (RoleID) REFERENCES Role(RoleID))");

            // --- 7. PLAYER STATS TABLE (NEW) ---
            String sqlStats = "CREATE TABLE IF NOT EXISTS PlayerStats (" +
                              "StatID INT PRIMARY KEY AUTO_INCREMENT, " +
                              "GameID INT, " +
                              "PlayerID INT, " +
                              "Points INT DEFAULT 0, " +
                              "Rebounds INT DEFAULT 0, " +
                              "Assists INT DEFAULT 0, " +
                              "Steals INT DEFAULT 0, " +
                              "Fouls INT DEFAULT 0, " +
                              "FOREIGN KEY (GameID) REFERENCES Game(GameID), " +
                              "FOREIGN KEY (PlayerID) REFERENCES Player(PlayerID))";
            stmt.executeUpdate(sqlStats);
            
            System.out.println("Database checks complete. PlayerStats table ready.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}