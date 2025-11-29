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
            System.out.println("Inserting complete sample dataset with advanced stats...");

            // 1. WORKERS
            stmt.executeUpdate("INSERT INTO Worker (FirstName, LastName, Phone, Email, WorkerType) VALUES " +
                    "('John', 'Smith', '410-555-0101', 'jsmith@example.com', 'Staff')," +
                    "('Sarah', 'Connor', '410-555-0102', 'sarah@example.com', 'Volunteer')," +
                    "('Mike', 'Jordan', '410-555-2323', 'mike@nba.com', 'Staff')," +
                    "('Emily', 'Blunt', '443-555-9999', 'emily@movie.com', 'Volunteer')," +
                    "('Chris', 'Paul', '410-555-3333', 'cp3@pointgod.com', 'Staff')");

            // 2. TEAMS
            stmt.executeUpdate("INSERT INTO Team (TeamName, CoachName) VALUES " +
                    "('Towson Tigers', 'Coach Miller')," +
                    "('Baltimore Ravens', 'Coach Harbaugh')," +
                    "('Essex Knights', 'Coach Robinson')," +
                    "('Loyola Greyhounds', 'Coach Smith')");

            // 3. PLAYERS (7 per team)
            // Towson (Team 1)
            stmt.executeUpdate("INSERT INTO Player (FirstName, LastName, TeamID) VALUES " +
                    "('Davon', 'Banks', 1), ('Marcus', 'Lee', 1), ('Tyrell', 'Jones', 1), ('Cam', 'Holden', 1), ('Charles', 'Thompson', 1), ('Jason', 'Gibson', 1), ('Nicolas', 'Timberlake', 1)");
            // Ravens (Team 2)
            stmt.executeUpdate("INSERT INTO Player (FirstName, LastName, TeamID) VALUES " +
                    "('Lamar', 'Jackson', 2), ('Zay', 'Flowers', 2), ('Mark', 'Andrews', 2), ('Roquan', 'Smith', 2), ('Kyle', 'Hamilton', 2), ('Justin', 'Tucker', 2), ('Odell', 'Beckham', 2)");
            // Essex (Team 3)
            stmt.executeUpdate("INSERT INTO Player (FirstName, LastName, TeamID) VALUES " +
                    "('Kevin', 'Durant', 3), ('Carmelo', 'Anthony', 3), ('Rudy', 'Gay', 3), ('Muggsy', 'Bogues', 3), ('Reggie', 'Lewis', 3), ('Sam', 'Cassell', 3), ('Juan', 'Dixon', 3)");
            // Loyola (Team 4)
            stmt.executeUpdate("INSERT INTO Player (FirstName, LastName, TeamID) VALUES " +
                    "('Santi', 'Aldama', 4), ('Andre', 'Walker', 4), ('Cam', 'Spencer', 4), ('Jaylin', 'Andrews', 4), ('Kenny', 'Jones', 4), ('Golden', 'Dike', 4), ('Alonso', 'Faure', 4)");

            // 4. GAMES
            stmt.executeUpdate("INSERT INTO Game (GameDate, GameTime, Location, HomeTeamID, AwayTeamID, HomeScore, AwayScore) VALUES " +
                    "('2023-11-01', '3:00 PM', 'CCBC Essex Gym A', 1, 2, 85, 82)," + 
                    "('2023-11-05', '5:30 PM', 'Towson Arena', 3, 4, 92, 88)," + 
                    "('2023-11-10', '1:00 PM', 'Loyola Center', 2, 3, 0, 0)");

            // 5. GAME ASSIGNMENTS
            stmt.executeUpdate("INSERT INTO GameAssignment (GameID, WorkerID, RoleID) VALUES " +
                    "(1, 1, 1), (1, 2, 2), (1, 3, 3)," + 
                    "(2, 3, 1), (2, 4, 4)");

            // 6. PLAYER STATS (DETAILED)
            // Format: GameID, PlayerID, Pts, Reb, Ast, Stl, Blk, Fouls, FGM, FGA, 3PM, 3PA
            
            // Game 1: Towson (ID 1) vs Ravens (ID 2)
            // Towson Players
            stmt.executeUpdate("INSERT INTO PlayerStats (GameID, PlayerID, Points, Rebounds, Assists, Steals, Blocks, Fouls, FGM, FGA, ThreePM, ThreePA) VALUES " +
                    "(1, 1, 24, 5, 2, 1, 0, 3, 9, 18, 2, 6)," +  // Davon Banks: 24 pts, 9/18 FG
                    "(1, 2, 10, 12, 1, 0, 2, 4, 5, 8, 0, 0)," +  // Marcus Lee: Double-Double
                    "(1, 3, 15, 3, 5, 2, 0, 2, 6, 12, 3, 5)," +  // Tyrell Jones: 3 threes
                    "(1, 4, 8, 4, 1, 1, 0, 1, 3, 7, 1, 3)," +    // Cam Holden
                    "(1, 5, 6, 8, 0, 0, 1, 5, 3, 5, 0, 0)," +    // Charles Thompson
                    "(1, 6, 12, 1, 4, 1, 0, 2, 4, 9, 4, 8)," +   // Jason Gibson
                    "(1, 7, 10, 2, 2, 0, 0, 1, 4, 8, 2, 4)");    // Nicolas Timberlake

            // Ravens Players
            stmt.executeUpdate("INSERT INTO PlayerStats (GameID, PlayerID, Points, Rebounds, Assists, Steals, Blocks, Fouls, FGM, FGA, ThreePM, ThreePA) VALUES " +
                    "(1, 8, 30, 6, 8, 3, 1, 1, 12, 22, 2, 5)," + // Lamar Jackson: MVP performance
                    "(1, 9, 14, 2, 3, 1, 0, 2, 5, 11, 2, 4)," +  // Zay Flowers
                    "(1, 10, 18, 9, 2, 0, 1, 3, 7, 13, 0, 1)," + // Mark Andrews
                    "(1, 11, 6, 10, 1, 2, 0, 4, 3, 6, 0, 0)," +  // Roquan Smith
                    "(1, 12, 8, 3, 2, 1, 0, 2, 3, 8, 2, 5)," +   // Kyle Hamilton
                    "(1, 13, 4, 1, 0, 0, 0, 0, 2, 3, 0, 0)," +   // Justin Tucker
                    "(1, 14, 2, 2, 1, 0, 0, 1, 1, 4, 0, 2)");    // Odell Beckham

            // Game 2: Essex (ID 3) vs Loyola (ID 4)
            // Essex Players (KD, Melo)
            stmt.executeUpdate("INSERT INTO PlayerStats (GameID, PlayerID, Points, Rebounds, Assists, Steals, Blocks, Fouls, FGM, FGA, ThreePM, ThreePA) VALUES " +
                    "(2, 15, 35, 8, 4, 1, 2, 2, 14, 24, 4, 8)," + // Kevin Durant
                    "(2, 16, 28, 5, 2, 0, 0, 3, 10, 20, 3, 7)," + // Carmelo Anthony
                    "(2, 17, 12, 6, 1, 1, 1, 2, 5, 10, 1, 3)");   // Rudy Gay

            System.out.println("Sample data with ADVANCED STATS inserted successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}