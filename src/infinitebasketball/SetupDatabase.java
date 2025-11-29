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
            String sqlRole = "CREATE TABLE IF NOT EXISTS Role (" +
                             "RoleID INT PRIMARY KEY AUTO_INCREMENT, " +
                             "RoleName VARCHAR(50))";
            stmt.executeUpdate(sqlRole);
            
            stmt.executeUpdate("INSERT IGNORE INTO Role (RoleID, RoleName) VALUES (1, 'Referee')");
            stmt.executeUpdate("INSERT IGNORE INTO Role (RoleID, RoleName) VALUES (2, 'Scorekeeper')");
            stmt.executeUpdate("INSERT IGNORE INTO Role (RoleID, RoleName) VALUES (3, 'Timekeeper')");
            stmt.executeUpdate("INSERT IGNORE INTO Role (RoleID, RoleName) VALUES (4, 'Camera Operator')");

            // 2. Team Table
            String sqlTeam = "CREATE TABLE IF NOT EXISTS Team (" +
                             "TeamID INT PRIMARY KEY AUTO_INCREMENT, " +
                             "TeamName VARCHAR(50), " +
                             "CoachName VARCHAR(50))";
            stmt.executeUpdate(sqlTeam);

            // 3. Worker Table
            String sqlWorker = "CREATE TABLE IF NOT EXISTS Worker (" +
                               "WorkerID INT PRIMARY KEY AUTO_INCREMENT, " +
                               "FirstName VARCHAR(50), " +
                               "LastName VARCHAR(50), " +
                               "Phone VARCHAR(20), " +
                               "Email VARCHAR(100), " +
                               "WorkerType VARCHAR(20))";
            stmt.executeUpdate(sqlWorker);

            // 4. Player Table
            String sqlPlayer = "CREATE TABLE IF NOT EXISTS Player (" +
                               "PlayerID INT PRIMARY KEY AUTO_INCREMENT, " +
                               "FirstName VARCHAR(50), " +
                               "LastName VARCHAR(50), " +
                               "TeamID INT, " +
                               "FOREIGN KEY (TeamID) REFERENCES Team(TeamID))";
            stmt.executeUpdate(sqlPlayer);

            // 5. Game Table (UPDATED WITH GameTime)
            String sqlGame = "CREATE TABLE IF NOT EXISTS Game (" +
                             "GameID INT PRIMARY KEY AUTO_INCREMENT, " +
                             "GameDate DATE, " +
                             "GameTime VARCHAR(20), " + // New Column
                             "Location VARCHAR(100), " +
                             "HomeTeamID INT, " +
                             "AwayTeamID INT, " +
                             "HomeScore INT, " +
                             "AwayScore INT, " +
                             "FOREIGN KEY (HomeTeamID) REFERENCES Team(TeamID), " +
                             "FOREIGN KEY (AwayTeamID) REFERENCES Team(TeamID))";
            stmt.executeUpdate(sqlGame);
            
            // Check if column exists, if not add it (For existing databases)
            try {
                stmt.executeUpdate("ALTER TABLE Game ADD COLUMN GameTime VARCHAR(20)");
            } catch (SQLException e) {
                // Column likely already exists, ignore error
            }

            // 6. Game Assignment Table
            String sqlAssign = "CREATE TABLE IF NOT EXISTS GameAssignment (" +
                               "AssignmentID INT PRIMARY KEY AUTO_INCREMENT, " +
                               "GameID INT, " +
                               "WorkerID INT, " +
                               "RoleID INT, " +
                               "FOREIGN KEY (GameID) REFERENCES Game(GameID), " +
                               "FOREIGN KEY (WorkerID) REFERENCES Worker(WorkerID), " +
                               "FOREIGN KEY (RoleID) REFERENCES Role(RoleID))";
            stmt.executeUpdate(sqlAssign);
            
            System.out.println("Database checks complete. GameTime column ensured.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}