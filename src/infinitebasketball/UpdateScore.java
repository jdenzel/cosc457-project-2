package infinitebasketball;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class UpdateScore extends JPanel { // Changed to JPanel

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtHomeScore, txtAwayScore;
    private JLabel lblMatchup;
    private int selectedGameID = -1;

    public UpdateScore() {
        initComponents();
        loadGames();
        Theme.apply(this);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new String[]{"ID", "Date", "Home", "Away", "H-Score", "A-Score"}, 0);
        table = new JTable(model);
        table.getSelectionModel().addListSelectionListener(e -> pickGame());
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel panel = new JPanel(new FlowLayout());
        lblMatchup = new JLabel("Select game...");
        txtHomeScore = new JTextField(3); txtAwayScore = new JTextField(3);
        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(e -> saveScore());

        panel.add(lblMatchup); panel.add(new JLabel("Home:")); panel.add(txtHomeScore);
        panel.add(new JLabel("Away:")); panel.add(txtAwayScore); panel.add(btnSave);
        add(panel, BorderLayout.SOUTH);
    }

    private void loadGames() {
        model.setRowCount(0);
        String sql = "SELECT g.GameID, g.GameDate, t1.TeamName AS Home, t2.TeamName AS Away, g.HomeScore, g.AwayScore FROM Game g JOIN Team t1 ON g.HomeTeamID = t1.TeamID JOIN Team t2 ON g.AwayTeamID = t2.TeamID";
        try (Connection con = DBConnection.connect(); Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) model.addRow(new Object[]{rs.getInt("GameID"), rs.getDate("GameDate"), rs.getString("Home"), rs.getString("Away"), rs.getInt("HomeScore"), rs.getInt("AwayScore")});
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void pickGame() {
        int r = table.getSelectedRow(); if (r == -1) return;
        selectedGameID = (int) model.getValueAt(r, 0);
        lblMatchup.setText(model.getValueAt(r, 2) + " vs " + model.getValueAt(r, 3));
        txtHomeScore.setText(model.getValueAt(r, 4).toString());
        txtAwayScore.setText(model.getValueAt(r, 5).toString());
    }

    private void saveScore() {
        if (selectedGameID == -1) return;
        try (Connection con = DBConnection.connect(); PreparedStatement p = con.prepareStatement("UPDATE Game SET HomeScore=?, AwayScore=? WHERE GameID=?")) {
            p.setInt(1, Integer.parseInt(txtHomeScore.getText())); p.setInt(2, Integer.parseInt(txtAwayScore.getText())); p.setInt(3, selectedGameID);
            p.executeUpdate(); loadGames(); JOptionPane.showMessageDialog(this, "Updated!");
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }
}