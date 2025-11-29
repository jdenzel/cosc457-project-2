package infinitebasketball;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class AddSampleData {

    public static void main(String[] args) {
        Connection con = DBConnection.connect();
        if (con == null) return;

        try {
            Statement stmt = con.createStatement();
            
            System.out.println("Inserting complete sample dataset...");

            // 1. ROLES (Usually static, but ensuring they exist)
            // Using INSERT IGNORE to skip if they already exist from SetupDatabase
            stmt.executeUpdate("INSERT IGNORE INTO Role (RoleID, RoleName) VALUES (1, 'Referee'), (2, 'Scorekeeper'), (3, 'Timekeeper'), (4, 'Camera Operator')");

            // 2. WORKERS
            stmt.executeUpdate("INSERT INTO Worker (FirstName, LastName, Phone, Email, WorkerType) VALUES " +
                    "('John', 'Smith', '410-555-0101', 'jsmith@example.com', 'Staff')," +
                    "('Sarah', 'Connor', '410-555-0102', 'sarah@example.com', 'Volunteer')," +
                    "('Mike', 'Jordan', '410-555-2323', 'mike@nba.com', 'Staff')," +
                    "('Emily', 'Blunt', '443-555-9999', 'emily@movie.com', 'Volunteer')," +
                    "('Chris', 'Paul', '410-555-3333', 'cp3@pointgod.com', 'Staff')");

            // 3. TEAMS
            stmt.executeUpdate("INSERT INTO Team (TeamName, CoachName) VALUES " +
                    "('Towson Tigers', 'Coach Miller')," +
                    "('Baltimore Ravens', 'Coach Harbaugh')," +
                    "('Essex Knights', 'Coach Robinson')," +
                    "('Loyola Greyhounds', 'Coach Smith')");

            // 4. PLAYERS (7 per team)
            // Team 1: Towson Tigers
            stmt.executeUpdate("INSERT INTO Player (FirstName, LastName, TeamID) VALUES " +
                    "('Davon', 'Banks', 1), ('Marcus', 'Lee', 1), ('Tyrell', 'Jones', 1), " +
                    "('Cam', 'Holden', 1), ('Charles', 'Thompson', 1), ('Jason', 'Gibson', 1), ('Nicolas', 'Timberlake', 1)");

            // Team 2: Baltimore Ravens
            stmt.executeUpdate("INSERT INTO Player (FirstName, LastName, TeamID) VALUES " +
                    "('Lamar', 'Jackson', 2), ('Zay', 'Flowers', 2), ('Mark', 'Andrews', 2), " +
                    "('Roquan', 'Smith', 2), ('Kyle', 'Hamilton', 2), ('Justin', 'Tucker', 2), ('Odell', 'Beckham', 2)");
            
            // Team 3: Essex Knights
            stmt.executeUpdate("INSERT INTO Player (FirstName, LastName, TeamID) VALUES " +
                    "('Kevin', 'Durant', 3), ('Carmelo', 'Anthony', 3), ('Rudy', 'Gay', 3), " +
                    "('Muggsy', 'Bogues', 3), ('Reggie', 'Lewis', 3), ('Sam', 'Cassell', 3), ('Juan', 'Dixon', 3)");

            // Team 4: Loyola Greyhounds
            stmt.executeUpdate("INSERT INTO Player (FirstName, LastName, TeamID) VALUES " +
                    "('Santi', 'Aldama', 4), ('Andre', 'Walker', 4), ('Cam', 'Spencer', 4), " +
                    "('Jaylin', 'Andrews', 4), ('Kenny', 'Jones', 4), ('Golden', 'Dike', 4), ('Alonso', 'Faure', 4)");

            // 5. GAMES
            // Game 1: Tigers vs Ravens
            // Game 2: Knights vs Greyhounds
            // Game 3: Ravens vs Knights
            stmt.executeUpdate("INSERT INTO Game (GameDate, GameTime, Location, HomeTeamID, AwayTeamID, HomeScore, AwayScore) VALUES " +
                    "('2023-11-01', '3:00 PM', 'CCBC Essex Gym A', 1, 2, 85, 82)," + 
                    "('2023-11-05', '5:30 PM', 'Towson Arena', 3, 4, 0, 0)," + 
                    "('2023-11-10', '1:00 PM', 'Loyola Center', 2, 3, 0, 0)");

            // 6. GAME ASSIGNMENTS (Linking Workers to Games)
            // Game 1 Staffing
            stmt.executeUpdate("INSERT INTO GameAssignment (GameID, WorkerID, RoleID) VALUES " +
                    "(1, 1, 1)," + // John Smith (Staff) is Referee
                    "(1, 2, 2)," + // Sarah Connor (Vol) is Scorekeeper
                    "(1, 3, 3)");  // Mike Jordan (Staff) is Timekeeper

            // Game 2 Staffing
            stmt.executeUpdate("INSERT INTO GameAssignment (GameID, WorkerID, RoleID) VALUES " +
                    "(2, 3, 1)," + // Mike Jordan is Ref
                    "(2, 4, 4)");  // Emily Blunt is Camera Operator

            // 7. PLAYER STATS
            // Stats for Game 1 (Tigers vs Ravens)
            // Tigers Players (ID 1, 2, 3...)
            stmt.executeUpdate("INSERT INTO PlayerStats (GameID, PlayerID, Points, Rebounds, Assists, Steals, Fouls) VALUES " +
                    "(1, 1, 24, 5, 2, 1, 3)," +  // Davon
                    "(1, 2, 10, 12, 1, 0, 4)," + // Marcus
                    "(1, 3, 15, 3, 5, 2, 2)");   // Tyrell
            
            // Ravens Players (ID 8, 9, 10...)
            stmt.executeUpdate("INSERT INTO PlayerStats (GameID, PlayerID, Points, Rebounds, Assists, Steals, Fouls) VALUES " +
                    "(1, 8, 30, 2, 8, 3, 1)," +  // Lamar
                    "(1, 9, 12, 4, 1, 0, 2)");   // Zay

            System.out.println("All tables populated successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}