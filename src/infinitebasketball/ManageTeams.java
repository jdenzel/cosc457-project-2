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
    // Removed cmbTeamSelect because we now use the table selection
    private JButton btnAddPlayer, btnDelPlayer; 

    public ManageTeams() {
        initComponents();
        loadTeams();
        // We don't load players initially; we wait for a team selection
        Theme.apply(this);
    }

    private void initComponents() {
        setLayout(new GridLayout(2, 1, 10, 10));

        // --- TOP PANEL: TEAMS (MASTER) ---
        JPanel teamPanel = new JPanel(new BorderLayout());
        teamPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.PRIMARY, 1), 
                "TEAMS (Select a team to view/edit roster)", 
                0, 0, 
                Theme.FONT_BOLD, 
                Theme.ACCENT));
        
        String[] teamCols = {"Team ID", "Team Name", "Coach"};
        teamModel = new DefaultTableModel(teamCols, 0);
        teamTable = new JTable(teamModel);
        
        // ADD LISTENER: When user clicks a team, load that team's players
        teamTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadPlayersForSelectedTeam();
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

        // --- BOTTOM PANEL: PLAYERS (DETAIL) ---
        JPanel playerPanel = new JPanel(new BorderLayout());
        playerPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.PRIMARY, 1), 
                "ROSTER (Players on selected team)", 
                0, 0, 
                Theme.FONT_BOLD, 
                Theme.ACCENT));
        
        // Removed 'Team ID' column since it's redundant here
        String[] playerCols = {"Player ID", "First Name", "Last Name"};
        playerModel = new DefaultTableModel(playerCols, 0);
        playerTable = new JTable(playerModel);
        playerPanel.add(new JScrollPane(playerTable), BorderLayout.CENTER);
        
        JPanel playerInputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        txtPFirst = new JTextField(10);
        txtPLast = new JTextField(10);
        btnAddPlayer = new JButton("Add Player");
        btnDelPlayer = new JButton("Delete Player");
        
        // Disable player inputs until a team is picked
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

    private void loadPlayersForSelectedTeam() {
        int row = teamTable.getSelectedRow();
        if (row == -1) {
            playerModel.setRowCount(0); // Clear table
            togglePlayerInputs(false);  // Disable buttons
            return;
        }

        // Enable inputs now that a team is selected
        togglePlayerInputs(true);
        
        int teamID = (int) teamModel.getValueAt(row, 0); // Get ID from selected row
        
        playerModel.setRowCount(0);
        try (Connection con = DBConnection.connect();
             PreparedStatement pstmt = con.prepareStatement("SELECT * FROM Player WHERE TeamID = ?")) {
            
            pstmt.setInt(1, teamID);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                playerModel.addRow(new Object[]{
                    rs.getInt("PlayerID"),
                    rs.getString("FirstName"),
                    rs.getString("LastName")
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
        
        // Auto-detect Team ID from the selection
        int teamID = (int) teamModel.getValueAt(row, 0);

        try (Connection con = DBConnection.connect();
             PreparedStatement pstmt = con.prepareStatement("INSERT INTO Player (FirstName, LastName, TeamID) VALUES (?, ?, ?)")) {
            pstmt.setString(1, txtPFirst.getText());
            pstmt.setString(2, txtPLast.getText());
            pstmt.setInt(3, teamID); // Use the detected ID
            pstmt.executeUpdate();
            
            txtPFirst.setText(""); txtPLast.setText("");
            loadPlayersForSelectedTeam(); // Refresh only the roster part
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
            playerModel.setRowCount(0); // Clear players view since team is gone
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Cannot delete team with players!"); }
    }

    private void deletePlayer() {
        int row = playerTable.getSelectedRow();
        if (row == -1) return;
        int id = (int) playerModel.getValueAt(row, 0);
        try (Connection con = DBConnection.connect(); PreparedStatement p = con.prepareStatement("DELETE FROM Player WHERE PlayerID=?")) {
            p.setInt(1, id); p.executeUpdate(); 
            loadPlayersForSelectedTeam(); // Refresh list
        } catch (SQLException e) { e.printStackTrace(); }
    }
}