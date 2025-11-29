package infinitebasketball;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ManageSchedule extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtDate, txtTime, txtLocation; // Added txtTime
    private JComboBox<String> cmbHome, cmbAway;

    public ManageSchedule() {
        initComponents();
        loadTeams();
        loadGames();
        Theme.apply(this);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Update table columns to include Time
        model = new DefaultTableModel(new String[]{"ID", "Date", "Time", "Location", "Home", "Away"}, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Form Panel
        JPanel mainForm = new JPanel(new BorderLayout());
        mainForm.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Changed grid to fit the extra Time field
        JPanel form = new JPanel(new GridLayout(2, 6, 10, 5));
        
        txtDate = new JTextField("2023-10-01", 8);
        txtTime = new JTextField("3:00 PM", 8); // New Field
        txtLocation = new JTextField("CCBC Essex", 10);
        cmbHome = new JComboBox<>();
        cmbAway = new JComboBox<>();
        JButton btnAdd = new JButton("Schedule");

        form.add(new JLabel("Date:")); 
        form.add(new JLabel("Time:")); // New Label
        form.add(new JLabel("Location:")); 
        form.add(new JLabel("Home Team:")); 
        form.add(new JLabel("Away Team:")); 
        form.add(new JLabel("")); // Spacer

        form.add(txtDate);
        form.add(txtTime); // Add field to panel
        form.add(txtLocation);
        form.add(cmbHome);
        form.add(cmbAway);
        
        btnAdd.addActionListener(e -> addGame());
        form.add(btnAdd);
        
        mainForm.add(form, BorderLayout.CENTER);
        add(mainForm, BorderLayout.SOUTH);
    }

    private void loadTeams() {
        try (Connection con = DBConnection.connect(); Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM Team")) {
            cmbHome.removeAllItems(); cmbAway.removeAllItems();
            while (rs.next()) {
                String s = rs.getInt("TeamID") + " - " + rs.getString("TeamName");
                cmbHome.addItem(s); cmbAway.addItem(s);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadGames() {
        model.setRowCount(0);
        // Updated query to fetch GameTime
        String sql = "SELECT g.GameID, g.GameDate, g.GameTime, g.Location, t1.TeamName AS Home, t2.TeamName AS Away " +
                     "FROM Game g " +
                     "JOIN Team t1 ON g.HomeTeamID = t1.TeamID " +
                     "JOIN Team t2 ON g.AwayTeamID = t2.TeamID";
                     
        try (Connection con = DBConnection.connect(); Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("GameID"), 
                    rs.getDate("GameDate"), 
                    rs.getString("GameTime"), // Show Time in table
                    rs.getString("Location"), 
                    rs.getString("Home"), 
                    rs.getString("Away")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void addGame() {
        try {
            int h = Integer.parseInt(cmbHome.getSelectedItem().toString().split(" - ")[0]);
            int a = Integer.parseInt(cmbAway.getSelectedItem().toString().split(" - ")[0]);
            if (h == a) { JOptionPane.showMessageDialog(this, "Teams must be different!"); return; }
            
            // Updated INSERT to include GameTime
            String sql = "INSERT INTO Game (GameDate, GameTime, Location, HomeTeamID, AwayTeamID) VALUES (?, ?, ?, ?, ?)";
            
            try (Connection con = DBConnection.connect(); PreparedStatement p = con.prepareStatement(sql)) {
                p.setString(1, txtDate.getText());
                p.setString(2, txtTime.getText()); // Save Time
                p.setString(3, txtLocation.getText()); 
                p.setInt(4, h); 
                p.setInt(5, a);
                
                p.executeUpdate(); 
                loadGames(); 
                JOptionPane.showMessageDialog(this, "Scheduled!");
            }
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }
}