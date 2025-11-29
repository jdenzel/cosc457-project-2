package infinitebasketball;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class UpdateScore extends JPanel {

    private JTable gamesTable;
    private DefaultTableModel gamesModel;
    private JTable homeTable, awayTable;
    private DefaultTableModel homeModel, awayModel;
    private JLabel lblHomeTeam, lblAwayTeam, lblTotalScore;
    private int selectedGameID = -1;
    private int homeTeamID = -1;
    private int awayTeamID = -1;

    public UpdateScore() {
        initComponents();
        loadGames();
        Theme.apply(this);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // --- TOP: GAMES LIST ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.PRIMARY, 1), 
                "1. SELECT A GAME", 
                0, 0, Theme.FONT_BOLD, Theme.ACCENT));
        
        gamesModel = new DefaultTableModel(new String[]{"ID", "Date", "Home Team", "Away Team", "H-Score", "A-Score"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; } // Read-only
        };
        gamesTable = new JTable(gamesModel);
        gamesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) pickGame();
        });
        // Make games table smaller (top 30% of screen)
        JScrollPane scrollGames = new JScrollPane(gamesTable);
        scrollGames.setPreferredSize(new Dimension(800, 200));
        topPanel.add(scrollGames, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);

        // --- CENTER: PLAYER STATS (Split Pane) ---
        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Home Team Panel
        JPanel homePanel = new JPanel(new BorderLayout());
        lblHomeTeam = new JLabel("Home Team Stats", SwingConstants.CENTER);
        lblHomeTeam.setFont(Theme.FONT_BOLD);
        lblHomeTeam.setForeground(Theme.PRIMARY);
        homePanel.add(lblHomeTeam, BorderLayout.NORTH);
        
        homeModel = new DefaultTableModel(new String[]{"ID", "Player Name", "PTS", "REB", "AST", "STL", "PF"}, 0);
        homeTable = new JTable(homeModel);
        homePanel.add(new JScrollPane(homeTable), BorderLayout.CENTER);
        
        // Away Team Panel
        JPanel awayPanel = new JPanel(new BorderLayout());
        lblAwayTeam = new JLabel("Away Team Stats", SwingConstants.CENTER);
        lblAwayTeam.setFont(Theme.FONT_BOLD);
        lblAwayTeam.setForeground(Theme.PRIMARY);
        awayPanel.add(lblAwayTeam, BorderLayout.NORTH);
        
        awayModel = new DefaultTableModel(new String[]{"ID", "Player Name", "PTS", "REB", "AST", "STL", "PF"}, 0);
        awayTable = new JTable(awayModel);
        awayPanel.add(new JScrollPane(awayTable), BorderLayout.CENTER);

        statsPanel.add(homePanel);
        statsPanel.add(awayPanel);
        add(statsPanel, BorderLayout.CENTER);

        // --- BOTTOM: ACTIONS ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, Theme.PRIMARY));
        
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        lblTotalScore = new JLabel("Calculated Score: Home 0 - 0 Away");
        lblTotalScore.setFont(Theme.FONT_HEADER);
        infoPanel.add(lblTotalScore);
        
        JButton btnSave = new JButton("SAVE ALL STATS & UPDATE SCORE");
        btnSave.setPreferredSize(new Dimension(300, 50));
        btnSave.addActionListener(e -> saveAllStats());
        
        bottomPanel.add(infoPanel, BorderLayout.NORTH);
        bottomPanel.add(btnSave, BorderLayout.SOUTH);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadGames() {
        gamesModel.setRowCount(0);
        String sql = "SELECT g.GameID, g.GameDate, t1.TeamName AS Home, t2.TeamName AS Away, " +
                     "g.HomeScore, g.AwayScore, t1.TeamID, t2.TeamID " +
                     "FROM Game g " +
                     "JOIN Team t1 ON g.HomeTeamID = t1.TeamID " +
                     "JOIN Team t2 ON g.AwayTeamID = t2.TeamID ORDER BY g.GameDate";
        try (Connection con = DBConnection.connect(); Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                // Hidden columns for TeamIDs are managed by the selection logic, not the table model directly
                // We store ID, Date, Names, Scores. 
                // We'll fetch TeamIDs again when selected or store in a separate list if needed.
                // For simplicity, we'll re-fetch or assume order.
                // Actually, let's put TeamIDs in the table but hide them? 
                // Easier approach: just query again on selection or store in parallel list. 
                // For this simple app, re-querying single game details is fine.
                
                gamesModel.addRow(new Object[]{
                    rs.getInt("GameID"), rs.getDate("GameDate"),
                    rs.getString("Home"), rs.getString("Away"),
                    rs.getInt("HomeScore"), rs.getInt("AwayScore")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void pickGame() {
        int r = gamesTable.getSelectedRow();
        if (r == -1) return;
        
        selectedGameID = (int) gamesModel.getValueAt(r, 0);
        String homeName = (String) gamesModel.getValueAt(r, 2);
        String awayName = (String) gamesModel.getValueAt(r, 3);
        
        lblHomeTeam.setText(homeName + " (Home)");
        lblAwayTeam.setText(awayName + " (Away)");
        
        // Fetch Team IDs
        try (Connection con = DBConnection.connect(); 
             PreparedStatement p = con.prepareStatement("SELECT HomeTeamID, AwayTeamID FROM Game WHERE GameID=?")) {
            p.setInt(1, selectedGameID);
            ResultSet rs = p.executeQuery();
            if (rs.next()) {
                homeTeamID = rs.getInt("HomeTeamID");
                awayTeamID = rs.getInt("AwayTeamID");
                
                loadPlayerStats(homeTeamID, homeModel);
                loadPlayerStats(awayTeamID, awayModel);
                updateTotalScoreLabel();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadPlayerStats(int teamID, DefaultTableModel model) {
        model.setRowCount(0);
        // This query gets ALL players for the team. 
        // It LEFT JOINs with PlayerStats to see if they already have stats for THIS game.
        // If not, it returns NULL (which we treat as 0).
        String sql = "SELECT p.PlayerID, p.FirstName, p.LastName, " +
                     "COALESCE(s.Points, 0) as PTS, " +
                     "COALESCE(s.Rebounds, 0) as REB, " +
                     "COALESCE(s.Assists, 0) as AST, " +
                     "COALESCE(s.Steals, 0) as STL, " +
                     "COALESCE(s.Fouls, 0) as PF " +
                     "FROM Player p " +
                     "LEFT JOIN PlayerStats s ON p.PlayerID = s.PlayerID AND s.GameID = ? " +
                     "WHERE p.TeamID = ?";
                     
        try (Connection con = DBConnection.connect(); PreparedStatement p = con.prepareStatement(sql)) {
            p.setInt(1, selectedGameID);
            p.setInt(2, teamID);
            ResultSet rs = p.executeQuery();
            while(rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("PlayerID"),
                    rs.getString("FirstName") + " " + rs.getString("LastName"),
                    rs.getInt("PTS"), rs.getInt("REB"), rs.getInt("AST"), 
                    rs.getInt("STL"), rs.getInt("PF")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void saveAllStats() {
        if (selectedGameID == -1) return;
        
        try (Connection con = DBConnection.connect()) {
            con.setAutoCommit(false); // Transaction for safety

            // 1. Delete old stats for this game to prevent duplicates
            // (Simpler than doing Update/Insert checks for every player)
            String deleteSql = "DELETE FROM PlayerStats WHERE GameID = ?";
            try (PreparedStatement del = con.prepareStatement(deleteSql)) {
                del.setInt(1, selectedGameID);
                del.executeUpdate();
            }

            // 2. Insert new stats from both tables
            String insertSql = "INSERT INTO PlayerStats (GameID, PlayerID, Points, Rebounds, Assists, Steals, Fouls) VALUES (?,?,?,?,?,?,?)";
            try (PreparedStatement ins = con.prepareStatement(insertSql)) {
                // Save Home Team
                saveTableToBatch(ins, homeModel);
                // Save Away Team
                saveTableToBatch(ins, awayModel);
                ins.executeBatch();
            }

            // 3. Update Game Total Score
            int hScore = calculateTotalPoints(homeModel);
            int aScore = calculateTotalPoints(awayModel);
            String updateGameSql = "UPDATE Game SET HomeScore=?, AwayScore=? WHERE GameID=?";
            try (PreparedStatement upd = con.prepareStatement(updateGameSql)) {
                upd.setInt(1, hScore);
                upd.setInt(2, aScore);
                upd.setInt(3, selectedGameID);
                upd.executeUpdate();
            }

            con.commit();
            JOptionPane.showMessageDialog(this, "Stats Saved & Scores Updated!");
            loadGames(); // Refresh top table
            updateTotalScoreLabel(); // Refresh label

        } catch (Exception e) { 
            e.printStackTrace(); 
            JOptionPane.showMessageDialog(this, "Error saving: " + e.getMessage());
        }
    }

    private void saveTableToBatch(PreparedStatement p, DefaultTableModel model) throws SQLException {
        for (int i = 0; i < model.getRowCount(); i++) {
            // Check if player actually played (has non-zero stats)? 
            // For now, we save everyone even if 0s to keep it simple.
            p.setInt(1, selectedGameID);
            p.setInt(2, Integer.parseInt(model.getValueAt(i, 0).toString())); // PlayerID
            p.setInt(3, Integer.parseInt(model.getValueAt(i, 2).toString())); // PTS
            p.setInt(4, Integer.parseInt(model.getValueAt(i, 3).toString())); // REB
            p.setInt(5, Integer.parseInt(model.getValueAt(i, 4).toString())); // AST
            p.setInt(6, Integer.parseInt(model.getValueAt(i, 5).toString())); // STL
            p.setInt(7, Integer.parseInt(model.getValueAt(i, 6).toString())); // PF
            p.addBatch();
        }
    }

    private int calculateTotalPoints(DefaultTableModel model) {
        int total = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            total += Integer.parseInt(model.getValueAt(i, 2).toString()); // Column 2 is PTS
        }
        return total;
    }
    
    private void updateTotalScoreLabel() {
        int h = calculateTotalPoints(homeModel);
        int a = calculateTotalPoints(awayModel);
        lblTotalScore.setText("Calculated Score: Home " + h + " - " + a + " Away");
    }
}