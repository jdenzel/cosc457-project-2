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
            
            System.out.println("Inserting sample data...");

            // 1. ADD WORKERS
            stmt.executeUpdate("INSERT INTO Worker (FirstName, LastName, Phone, Email, WorkerType) VALUES " +
                    "('John', 'Smith', '410-555-0101', 'jsmith@example.com', 'Staff')," +
                    "('Sarah', 'Connor', '410-555-0102', 'sarah@example.com', 'Volunteer')," +
                    "('Mike', 'Jordan', '410-555-2323', 'mike@nba.com', 'Staff')," +
                    "('Emily', 'Blunt', '443-555-9999', 'emily@movie.com', 'Volunteer')," +
                    "('Chris', 'Paul', '410-555-3333', 'cp3@pointgod.com', 'Staff')");

            // 2. ADD TEAMS
            stmt.executeUpdate("INSERT INTO Team (TeamName, CoachName) VALUES " +
                    "('Towson Tigers', 'Coach Miller')," +
                    "('Baltimore Ravens', 'Coach Harbaugh')," +
                    "('Essex Knights', 'Coach Robinson')," +
                    "('Loyola Greyhounds', 'Coach Smith')");

            // 3. ADD PLAYERS (7 Players per Team)
            
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

            // 4. SCHEDULE GAMES
            stmt.executeUpdate("INSERT INTO Game (GameDate, Location, HomeTeamID, AwayTeamID, HomeScore, AwayScore) VALUES " +
                    "('2023-11-01', 'CCBC Essex Gym A', 1, 2, 85, 82)," + 
                    "('2023-11-05', 'Towson Arena', 3, 4, 0, 0)," + 
                    "('2023-11-10', 'Loyola Center', 2, 3, 0, 0)");

            // 5. ASSIGN STAFF TO GAMES
            stmt.executeUpdate("INSERT INTO GameAssignment (GameID, WorkerID, RoleID) VALUES " +
                    "(1, 1, 1)," + // John Smith is Ref for Game 1
                    "(1, 2, 2)," + // Sarah Connor is Scorekeeper for Game 1
                    "(2, 3, 1)");  // Mike Jordan is Ref for Game 2

            System.out.println("Sample data inserted successfully with 7 players per team!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}