package infinitebasketball;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.*;

public class UpdateScore extends JPanel {

    private JTable gamesTable;
    private DefaultTableModel gamesModel;
    private JTable homeTable, awayTable;
    private DefaultTableModel homeModel, awayModel;
    private JLabel lblHomeTeam, lblAwayTeam, lblTotalScore, lblGameResult; // Added lblGameResult
    private int selectedGameID = -1;
    private int homeTeamID = -1;
    private int awayTeamID = -1;
    private String homeTeamName = "";
    private String awayTeamName = "";

    public UpdateScore() {
        initComponents();
        loadGames();
        Theme.apply(this);
        
        // --- AUTO-REFRESH LISTENER ---
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                loadGames();
            }
        });
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
            public boolean isCellEditable(int row, int column) { return false; }
        };
        gamesTable = new JTable(gamesModel);
        gamesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) pickGame();
        });
        
        JScrollPane scrollGames = new JScrollPane(gamesTable);
        scrollGames.setPreferredSize(new Dimension(800, 200));
        topPanel.add(scrollGames, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);

        // --- CENTER: PLAYER STATS ---
        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Home Team
        JPanel homePanel = new JPanel(new BorderLayout());
        lblHomeTeam = new JLabel("Home Team Stats", SwingConstants.CENTER);
        lblHomeTeam.setFont(Theme.FONT_BOLD);
        lblHomeTeam.setForeground(Theme.PRIMARY);
        homePanel.add(lblHomeTeam, BorderLayout.NORTH);
        
        homeModel = new DefaultTableModel(new String[]{"ID", "Player Name", "PTS", "REB", "AST", "STL", "PF"}, 0);
        homeTable = new JTable(homeModel);
        homePanel.add(new JScrollPane(homeTable), BorderLayout.CENTER);
        
        // Away Team
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
        
        JPanel infoPanel = new JPanel(new GridLayout(2, 1)); // Changed to grid for 2 labels
        infoPanel.setBackground(Theme.BACKGROUND); // Ensure background matches theme
        
        // Score Label
        lblTotalScore = new JLabel("Calculated Score: Home 0 - 0 Away", SwingConstants.CENTER);
        lblTotalScore.setFont(Theme.FONT_HEADER);
        lblTotalScore.setForeground(Theme.TEXT_MAIN);
        
        // Result Label (Winner/Loser)
        lblGameResult = new JLabel("Result: TBD", SwingConstants.CENTER);
        lblGameResult.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblGameResult.setForeground(Theme.ACCENT); // Gold color for emphasis
        
        infoPanel.add(lblTotalScore);
        infoPanel.add(lblGameResult);
        
        JButton btnSave = new JButton("SAVE ALL STATS & UPDATE SCORE");
        btnSave.setPreferredSize(new Dimension(300, 50));
        btnSave.addActionListener(e -> saveAllStats());
        
        bottomPanel.add(infoPanel, BorderLayout.CENTER); // Changed position
        bottomPanel.add(btnSave, BorderLayout.SOUTH);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadGames() {
        gamesModel.setRowCount(0);
        homeModel.setRowCount(0);
        awayModel.setRowCount(0);
        lblHomeTeam.setText("Home Team Stats");
        lblAwayTeam.setText("Away Team Stats");
        lblTotalScore.setText("Calculated Score: Home 0 - 0 Away");
        lblGameResult.setText(""); // Clear result
        selectedGameID = -1;

        String sql = "SELECT g.GameID, g.GameDate, t1.TeamName AS Home, t2.TeamName AS Away, " +
                     "g.HomeScore, g.AwayScore " +
                     "FROM Game g " +
                     "JOIN Team t1 ON g.HomeTeamID = t1.TeamID " +
                     "JOIN Team t2 ON g.AwayTeamID = t2.TeamID ORDER BY g.GameDate DESC"; 
                     
        try (Connection con = DBConnection.connect(); Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
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
        homeTeamName = (String) gamesModel.getValueAt(r, 2);
        awayTeamName = (String) gamesModel.getValueAt(r, 3);
        
        lblHomeTeam.setText(homeTeamName + " (Home)");
        lblAwayTeam.setText(awayTeamName + " (Away)");
        
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
        String sql = "SELECT p.PlayerID, p.FirstName, p.LastName, " +
                     "COALESCE(s.Points, 0) as PTS, COALESCE(s.Rebounds, 0) as REB, " +
                     "COALESCE(s.Assists, 0) as AST, COALESCE(s.Steals, 0) as STL, " +
                     "COALESCE(s.Fouls, 0) as PF " +
                     "FROM Player p " +
                     "LEFT JOIN PlayerStats s ON p.PlayerID = s.PlayerID AND s.GameID = ? " +
                     "WHERE p.TeamID = ?";
        try (Connection con = DBConnection.connect(); PreparedStatement p = con.prepareStatement(sql)) {
            p.setInt(1, selectedGameID); p.setInt(2, teamID);
            ResultSet rs = p.executeQuery();
            while(rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("PlayerID"), rs.getString("FirstName") + " " + rs.getString("LastName"),
                    rs.getInt("PTS"), rs.getInt("REB"), rs.getInt("AST"), 
                    rs.getInt("STL"), rs.getInt("PF")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void saveAllStats() {
        if (selectedGameID == -1) { JOptionPane.showMessageDialog(this, "Select a game first!"); return; }
        
        try (Connection con = DBConnection.connect()) {
            con.setAutoCommit(false);
            
            // Delete old stats
            try (PreparedStatement del = con.prepareStatement("DELETE FROM PlayerStats WHERE GameID = ?")) {
                del.setInt(1, selectedGameID); del.executeUpdate();
            }

            // Insert new stats
            String insertSql = "INSERT INTO PlayerStats (GameID, PlayerID, Points, Rebounds, Assists, Steals, Fouls) VALUES (?,?,?,?,?,?,?)";
            try (PreparedStatement ins = con.prepareStatement(insertSql)) {
                saveTableToBatch(ins, homeModel);
                saveTableToBatch(ins, awayModel);
                ins.executeBatch();
            }

            // Update Game Score
            int hScore = calculateTotalPoints(homeModel);
            int aScore = calculateTotalPoints(awayModel);
            try (PreparedStatement upd = con.prepareStatement("UPDATE Game SET HomeScore=?, AwayScore=? WHERE GameID=?")) {
                upd.setInt(1, hScore); upd.setInt(2, aScore); upd.setInt(3, selectedGameID);
                upd.executeUpdate();
            }

            con.commit();
            JOptionPane.showMessageDialog(this, "Stats Saved & Scores Updated!");
            loadGames(); 
            updateTotalScoreLabel(); 

        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error saving: " + e.getMessage()); }
    }

    private void saveTableToBatch(PreparedStatement p, DefaultTableModel model) throws SQLException {
        for (int i = 0; i < model.getRowCount(); i++) {
            p.setInt(1, selectedGameID);
            p.setInt(2, Integer.parseInt(model.getValueAt(i, 0).toString()));
            p.setInt(3, Integer.parseInt(model.getValueAt(i, 2).toString()));
            p.setInt(4, Integer.parseInt(model.getValueAt(i, 3).toString()));
            p.setInt(5, Integer.parseInt(model.getValueAt(i, 4).toString()));
            p.setInt(6, Integer.parseInt(model.getValueAt(i, 5).toString()));
            p.setInt(7, Integer.parseInt(model.getValueAt(i, 6).toString()));
            p.addBatch();
        }
    }

    private int calculateTotalPoints(DefaultTableModel model) {
        int total = 0;
        for (int i = 0; i < model.getRowCount(); i++) total += Integer.parseInt(model.getValueAt(i, 2).toString());
        return total;
    }
    
    private void updateTotalScoreLabel() {
        int h = calculateTotalPoints(homeModel);
        int a = calculateTotalPoints(awayModel);
        lblTotalScore.setText("Calculated Score: Home " + h + " - " + a + " Away");
        
        // --- NEW LOGIC: DETERMINE WINNER/LOSER ---
        if (h > a) {
            lblGameResult.setText("WINNER: " + homeTeamName + "  |  LOSER: " + awayTeamName);
            lblGameResult.setForeground(Color.GREEN); // Optional: Green for Winner context
        } else if (a > h) {
            lblGameResult.setText("WINNER: " + awayTeamName + "  |  LOSER: " + homeTeamName);
            lblGameResult.setForeground(Color.GREEN);
        } else {
            lblGameResult.setText("RESULT: TIE GAME");
            lblGameResult.setForeground(Color.YELLOW);
        }
    }
}