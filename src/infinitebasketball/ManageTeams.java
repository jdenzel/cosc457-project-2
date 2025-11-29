package infinitebasketball;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ManageTeams extends JPanel {

    private JTable teamTable, playerTable;
    private DefaultTableModel teamModel, playerModel;
    
    // Team Inputs
    private JTextField txtTeamName, txtCoach;
    
    // Player Inputs
    private JTextField txtPFirst, txtPLast;
    private JButton btnAddPlayer, btnDelPlayer; 
    
    // Team Stats Labels
    private JLabel lblTeamStats;

    public ManageTeams() {
        initComponents();
        loadTeams();
        Theme.apply(this);
    }

    private void initComponents() {
        setLayout(new GridLayout(2, 1, 10, 10));

        // --- TOP PANEL: TEAMS (MASTER) ---
        JPanel teamPanel = new JPanel(new BorderLayout());
        teamPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.PRIMARY, 1), 
                "TEAMS (Select a team to view roster & stats)", 
                0, 0, 
                Theme.FONT_BOLD, 
                Theme.ACCENT));
        
        String[] teamCols = {"Team ID", "Team Name", "Coach"};
        teamModel = new DefaultTableModel(teamCols, 0);
        teamTable = new JTable(teamModel);
        
        // Listener: When user clicks a team, load that team's players AND stats
        teamTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadTeamDetails();
            }
        });

        teamPanel.add(new JScrollPane(teamTable), BorderLayout.CENTER);

        JPanel teamInputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        txtTeamName = new JTextField(15);
        txtCoach = new JTextField(15);
        JButton btnAddTeam = new JButton("Add Team");
        JButton btnDelTeam = new JButton("Delete Team");
        
        teamInputPanel.add(new JLabel("Team Name:"));
        teamInputPanel.add(txtTeamName);
        teamInputPanel.add(new JLabel("Coach:"));
        teamInputPanel.add(txtCoach);
        teamInputPanel.add(btnAddTeam);
        teamInputPanel.add(btnDelTeam);
        teamPanel.add(teamInputPanel, BorderLayout.SOUTH);

        // --- BOTTOM PANEL: ROSTER & STATS (DETAIL) ---
        JPanel playerPanel = new JPanel(new BorderLayout());
        playerPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.PRIMARY, 1), 
                "TEAM ROSTER & PERFORMANCE", 
                0, 0, 
                Theme.FONT_BOLD, 
                Theme.ACCENT));
        
        // Info Panel for Team Aggregate Stats
        JPanel statsInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        statsInfoPanel.setBackground(Theme.BACKGROUND);
        lblTeamStats = new JLabel("Select a team to see total stats...");
        lblTeamStats.setFont(Theme.FONT_BOLD);
        lblTeamStats.setForeground(Theme.PRIMARY);
        statsInfoPanel.add(lblTeamStats);
        playerPanel.add(statsInfoPanel, BorderLayout.NORTH);
        
        // Updated Player Columns to include Stats
        String[] playerCols = {"ID", "First Name", "Last Name", "GP", "PTS", "REB", "AST"};
        playerModel = new DefaultTableModel(playerCols, 0);
        playerTable = new JTable(playerModel);
        playerPanel.add(new JScrollPane(playerTable), BorderLayout.CENTER);
        
        JPanel playerInputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        txtPFirst = new JTextField(10);
        txtPLast = new JTextField(10);
        btnAddPlayer = new JButton("Add Player");
        btnDelPlayer = new JButton("Delete Player");
        
        togglePlayerInputs(false);
        
        playerInputPanel.add(new JLabel("First:"));
        playerInputPanel.add(txtPFirst);
        playerInputPanel.add(new JLabel("Last:"));
        playerInputPanel.add(txtPLast);
        playerInputPanel.add(btnAddPlayer);
        playerInputPanel.add(btnDelPlayer);
        playerPanel.add(playerInputPanel, BorderLayout.SOUTH);

        add(teamPanel);
        add(playerPanel);

        // Listeners
        btnAddTeam.addActionListener(e -> addTeam());
        btnDelTeam.addActionListener(e -> deleteTeam());
        btnAddPlayer.addActionListener(e -> addPlayer());
        btnDelPlayer.addActionListener(e -> deletePlayer());
    }

    private void togglePlayerInputs(boolean enabled) {
        txtPFirst.setEnabled(enabled);
        txtPLast.setEnabled(enabled);
        btnAddPlayer.setEnabled(enabled);
        btnDelPlayer.setEnabled(enabled);
    }

    private void loadTeams() {
        teamModel.setRowCount(0);
        try (Connection con = DBConnection.connect();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Team")) {
            while (rs.next()) {
                teamModel.addRow(new Object[]{
                    rs.getInt("TeamID"), 
                    rs.getString("TeamName"), 
                    rs.getString("CoachName")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadTeamDetails() {
        int row = teamTable.getSelectedRow();
        if (row == -1) {
            playerModel.setRowCount(0); 
            lblTeamStats.setText("Select a team to see total stats...");
            togglePlayerInputs(false);  
            return;
        }

        togglePlayerInputs(true);
        
        int teamID = (int) teamModel.getValueAt(row, 0); 
        String teamName = (String) teamModel.getValueAt(row, 1);
        
        // 1. Calculate Team Totals
        // We sum up stats from all players on this team across all games
        String sqlTeamStats = "SELECT " +
                              "SUM(s.Points) as TotalPts, " +
                              "SUM(s.Rebounds) as TotalReb, " +
                              "SUM(s.Assists) as TotalAst, " +
                              "COUNT(DISTINCT s.GameID) as GamesPlayed " +
                              "FROM PlayerStats s " +
                              "JOIN Player p ON s.PlayerID = p.PlayerID " +
                              "WHERE p.TeamID = ?";
                              
        try (Connection con = DBConnection.connect();
             PreparedStatement pstmt = con.prepareStatement(sqlTeamStats)) {
            
            pstmt.setInt(1, teamID);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int pts = rs.getInt("TotalPts");
                int reb = rs.getInt("TotalReb");
                int ast = rs.getInt("TotalAst");
                int gp = rs.getInt("GamesPlayed");
                
                // Update the Label
                lblTeamStats.setText(String.format("STATS FOR %s:  Games: %d  |  Points: %d  |  Rebounds: %d  |  Assists: %d", 
                        teamName.toUpperCase(), gp, pts, reb, ast));
            }
        } catch (SQLException e) { e.printStackTrace(); }

        // 2. Load Roster with Individual Stats
        playerModel.setRowCount(0);
        String sqlRoster = "SELECT p.PlayerID, p.FirstName, p.LastName, " +
                           "COUNT(s.GameID) as GP, " +
                           "COALESCE(SUM(s.Points), 0) as PTS, " +
                           "COALESCE(SUM(s.Rebounds), 0) as REB, " +
                           "COALESCE(SUM(s.Assists), 0) as AST " +
                           "FROM Player p " +
                           "LEFT JOIN PlayerStats s ON p.PlayerID = s.PlayerID " +
                           "WHERE p.TeamID = ? " +
                           "GROUP BY p.PlayerID, p.FirstName, p.LastName";
                           
        try (Connection con = DBConnection.connect();
             PreparedStatement pstmt = con.prepareStatement(sqlRoster)) {
            
            pstmt.setInt(1, teamID);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                playerModel.addRow(new Object[]{
                    rs.getInt("PlayerID"),
                    rs.getString("FirstName"),
                    rs.getString("LastName"),
                    rs.getInt("GP"),
                    rs.getInt("PTS"),
                    rs.getInt("REB"),
                    rs.getInt("AST")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void addTeam() {
        try (Connection con = DBConnection.connect();
             PreparedStatement pstmt = con.prepareStatement("INSERT INTO Team (TeamName, CoachName) VALUES (?, ?)")) {
            pstmt.setString(1, txtTeamName.getText());
            pstmt.setString(2, txtCoach.getText());
            pstmt.executeUpdate();
            txtTeamName.setText(""); txtCoach.setText("");
            loadTeams(); 
            JOptionPane.showMessageDialog(this, "Team Added!");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void addPlayer() {
        int row = teamTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a team first!");
            return;
        }
        
        int teamID = (int) teamModel.getValueAt(row, 0);

        try (Connection con = DBConnection.connect();
             PreparedStatement pstmt = con.prepareStatement("INSERT INTO Player (FirstName, LastName, TeamID) VALUES (?, ?, ?)")) {
            pstmt.setString(1, txtPFirst.getText());
            pstmt.setString(2, txtPLast.getText());
            pstmt.setInt(3, teamID); 
            pstmt.executeUpdate();
            
            txtPFirst.setText(""); txtPLast.setText("");
            loadTeamDetails(); 
            JOptionPane.showMessageDialog(this, "Player Added!");
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    private void deleteTeam() {
        int row = teamTable.getSelectedRow();
        if (row == -1) return;
        int id = (int) teamModel.getValueAt(row, 0);
        try (Connection con = DBConnection.connect(); PreparedStatement p = con.prepareStatement("DELETE FROM Team WHERE TeamID=?")) {
            p.setInt(1, id); p.executeUpdate(); 
            loadTeams(); 
            playerModel.setRowCount(0); 
            lblTeamStats.setText("Select a team...");
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Cannot delete team with players!"); }
    }

    private void deletePlayer() {
        int row = playerTable.getSelectedRow();
        if (row == -1) return;
        int id = (int) playerModel.getValueAt(row, 0);
        try (Connection con = DBConnection.connect(); PreparedStatement p = con.prepareStatement("DELETE FROM Player WHERE PlayerID=?")) {
            p.setInt(1, id); p.executeUpdate(); 
            loadTeamDetails(); 
        } catch (SQLException e) { e.printStackTrace(); }
    }
}