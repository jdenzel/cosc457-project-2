package infinitebasketball;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ManagePlayerStats extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> cmbGame, cmbPlayer;
    private JTextField txtPts, txtReb, txtAst, txtStl, txtFouls;

    public ManagePlayerStats() {
        initComponents();
        loadDropdowns();
        loadStats();
        Theme.apply(this);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // --- TOP: TABLE ---
        model = new DefaultTableModel(new String[]{"StatID", "Game", "Player", "PTS", "REB", "AST", "STL", "PF"}, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- BOTTOM: INPUT FORM ---
        JPanel form = new JPanel(new GridLayout(3, 6, 5, 5));
        form.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.PRIMARY, 1), 
                "Add Player Statistics", 0, 0, Theme.FONT_BOLD, Theme.ACCENT));

        cmbGame = new JComboBox<>();
        cmbPlayer = new JComboBox<>();
        txtPts = new JTextField("0");
        txtReb = new JTextField("0");
        txtAst = new JTextField("0");
        txtStl = new JTextField("0");
        txtFouls = new JTextField("0");
        JButton btnAdd = new JButton("Save Stats");

        // Row 1 Headers
        form.add(new JLabel("Select Game:"));
        form.add(new JLabel("Select Player:"));
        form.add(new JLabel("Points"));
        form.add(new JLabel("Rebounds"));
        form.add(new JLabel("Assists"));
        form.add(new JLabel("Action"));

        // Row 2 Inputs
        form.add(cmbGame);
        form.add(cmbPlayer);
        form.add(txtPts);
        form.add(txtReb);
        form.add(txtAst);
        form.add(btnAdd);
        
        // Row 3 (Extra inputs)
        form.add(new JLabel("Steals:")); form.add(txtStl);
        form.add(new JLabel("Fouls:"));  form.add(txtFouls);
        form.add(new JLabel("")); form.add(new JLabel("")); 

        btnAdd.addActionListener(e -> addStats());
        add(form, BorderLayout.SOUTH);
    }

    private void loadDropdowns() {
        try (Connection con = DBConnection.connect(); Statement stmt = con.createStatement()) {
            // Games
            ResultSet rs = stmt.executeQuery("SELECT GameID, Location, GameDate FROM Game ORDER BY GameDate DESC");
            cmbGame.removeAllItems();
            while(rs.next()) cmbGame.addItem(rs.getInt("GameID") + " - " + rs.getString("Location"));
            
            // Players
            rs = stmt.executeQuery("SELECT PlayerID, FirstName, LastName FROM Player ORDER BY LastName");
            cmbPlayer.removeAllItems();
            while(rs.next()) cmbPlayer.addItem(rs.getInt("PlayerID") + " - " + rs.getString("FirstName") + " " + rs.getString("LastName"));
            
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadStats() {
        model.setRowCount(0);
        String sql = "SELECT s.StatID, g.GameDate, p.LastName, s.Points, s.Rebounds, s.Assists, s.Steals, s.Fouls " +
                     "FROM PlayerStats s " +
                     "JOIN Game g ON s.GameID = g.GameID " +
                     "JOIN Player p ON s.PlayerID = p.PlayerID " +
                     "ORDER BY g.GameDate DESC";
        
        try (Connection con = DBConnection.connect(); Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("StatID"), rs.getDate("GameDate"), rs.getString("LastName"),
                    rs.getInt("Points"), rs.getInt("Rebounds"), rs.getInt("Assists"),
                    rs.getInt("Steals"), rs.getInt("Fouls")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void addStats() {
        try {
            int gID = Integer.parseInt(cmbGame.getSelectedItem().toString().split(" - ")[0]);
            int pID = Integer.parseInt(cmbPlayer.getSelectedItem().toString().split(" - ")[0]);
            
            String sql = "INSERT INTO PlayerStats (GameID, PlayerID, Points, Rebounds, Assists, Steals, Fouls) VALUES (?,?,?,?,?,?,?)";
            try (Connection con = DBConnection.connect(); PreparedStatement p = con.prepareStatement(sql)) {
                p.setInt(1, gID); p.setInt(2, pID);
                p.setInt(3, Integer.parseInt(txtPts.getText()));
                p.setInt(4, Integer.parseInt(txtReb.getText()));
                p.setInt(5, Integer.parseInt(txtAst.getText()));
                p.setInt(6, Integer.parseInt(txtStl.getText()));
                p.setInt(7, Integer.parseInt(txtFouls.getText()));
                
                p.executeUpdate();
                loadStats();
                JOptionPane.showMessageDialog(this, "Stats Saved!");
            }
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }
}