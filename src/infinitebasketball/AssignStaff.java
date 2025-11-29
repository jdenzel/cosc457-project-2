package infinitebasketball;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.sql.*;

public class AssignStaff extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> cmbGame, cmbWorker, cmbRole;

    public AssignStaff() {
        initComponents();
        loadDropdowns();
        // Initial load (will show empty or first game's staff)
        if (cmbGame.getItemCount() > 0) {
            loadAssignmentsForGame();
        }
        Theme.apply(this);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // --- TOP PANEL (Table of Assignments) ---
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.PRIMARY, 1), 
                "CURRENT STAFF ASSIGNMENTS", 
                0, 0, 
                Theme.FONT_BOLD, 
                Theme.ACCENT));

        model = new DefaultTableModel(new String[]{"ID", "Worker Name", "Role", "Phone"}, 0);
        table = new JTable(model);
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);

        // --- BOTTOM PANEL (Form) ---
        JPanel mainForm = new JPanel(new BorderLayout());
        mainForm.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50)); 
        
        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        cmbGame = new JComboBox<>();
        cmbWorker = new JComboBox<>();
        cmbRole = new JComboBox<>();
        
        // Listener: When Game changes, reload the table
        cmbGame.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                loadAssignmentsForGame();
            }
        });

        form.add(new JLabel("Select Game:"));
        form.add(cmbGame);
        form.add(new JLabel("Select Worker:"));
        form.add(cmbWorker);
        form.add(new JLabel("Assign Role:"));
        form.add(cmbRole);
        
        JButton btnAssign = new JButton("Assign Staff Member");
        btnAssign.addActionListener(e -> assign());
        
        JButton btnDelete = new JButton("Remove Assignment");
        btnDelete.addActionListener(e -> removeAssignment());
        
        form.add(btnDelete); // Place delete button
        form.add(btnAssign); // Place add button

        mainForm.add(form, BorderLayout.CENTER);
        add(mainForm, BorderLayout.SOUTH);
    }

    private void loadDropdowns() {
        try (Connection con = DBConnection.connect(); Statement stmt = con.createStatement()) {
            
            // 1. Load Games (Ordered by Date)
            ResultSet rsGame = stmt.executeQuery(
                "SELECT GameID, Location, GameDate, t1.TeamName as Home, t2.TeamName as Away " + 
                "FROM Game g " +
                "JOIN Team t1 ON g.HomeTeamID = t1.TeamID " + 
                "JOIN Team t2 ON g.AwayTeamID = t2.TeamID " +
                "ORDER BY GameDate"
            );
            cmbGame.removeAllItems();
            while(rsGame.next()) {
                // Format: "ID - Date: Home vs Away"
                String item = rsGame.getInt("GameID") + " - " + 
                              rsGame.getDate("GameDate") + ": " + 
                              rsGame.getString("Home") + " vs " + rsGame.getString("Away");
                cmbGame.addItem(item);
            }
            
            // 2. Load Workers
            ResultSet rsWorker = stmt.executeQuery("SELECT WorkerID, FirstName, LastName FROM Worker ORDER BY LastName");
            cmbWorker.removeAllItems();
            while(rsWorker.next()) {
                cmbWorker.addItem(rsWorker.getInt("WorkerID") + " - " + rsWorker.getString("FirstName") + " " + rsWorker.getString("LastName"));
            }
            
            // 3. Load Roles
            ResultSet rsRole = stmt.executeQuery("SELECT * FROM Role ORDER BY RoleName");
            cmbRole.removeAllItems();
            while(rsRole.next()) {
                cmbRole.addItem(rsRole.getInt("RoleID") + " - " + rsRole.getString("RoleName"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadAssignmentsForGame() {
        if (cmbGame.getSelectedItem() == null) return;
        
        int gameID = Integer.parseInt(cmbGame.getSelectedItem().toString().split(" - ")[0]);
        model.setRowCount(0);
        
        String sql = "SELECT a.AssignmentID, w.FirstName, w.LastName, w.Phone, r.RoleName " +
                     "FROM GameAssignment a " +
                     "JOIN Worker w ON a.WorkerID = w.WorkerID " +
                     "JOIN Role r ON a.RoleID = r.RoleID " +
                     "WHERE a.GameID = ?";
                     
        try (Connection con = DBConnection.connect();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setInt(1, gameID);
            ResultSet rs = pstmt.executeQuery();
            
            while(rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("AssignmentID"),
                    rs.getString("FirstName") + " " + rs.getString("LastName"),
                    rs.getString("RoleName"),
                    rs.getString("Phone")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void assign() {
        if (cmbGame.getSelectedItem() == null || cmbWorker.getSelectedItem() == null) return;

        try {
            int gID = Integer.parseInt(cmbGame.getSelectedItem().toString().split(" - ")[0]);
            int wID = Integer.parseInt(cmbWorker.getSelectedItem().toString().split(" - ")[0]);
            int rID = Integer.parseInt(cmbRole.getSelectedItem().toString().split(" - ")[0]);

            Connection con = DBConnection.connect();
            
            // --- VALIDATION: Check if worker is already working this game ---
            String checkSql = "SELECT COUNT(*) FROM GameAssignment WHERE GameID = ? AND WorkerID = ?";
            PreparedStatement checkStmt = con.prepareStatement(checkSql);
            checkStmt.setInt(1, gID);
            checkStmt.setInt(2, wID);
            ResultSet rsCheck = checkStmt.executeQuery();
            rsCheck.next();
            if (rsCheck.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "This worker is already assigned to this game! \nCannot assign multiple roles per game.");
                return;
            }
            // ---------------------------------------------------------------

            String sql = "INSERT INTO GameAssignment (GameID, WorkerID, RoleID) VALUES (?, ?, ?)";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, gID); 
            pstmt.setInt(2, wID); 
            pstmt.setInt(3, rID);
            pstmt.executeUpdate(); 
            
            loadAssignmentsForGame(); 
            JOptionPane.showMessageDialog(this, "Staff Assigned!");
            
        } catch (Exception e) { 
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); 
        }
    }
    
    private void removeAssignment() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an assignment from the table to remove.");
            return;
        }
        
        int assignID = (int) model.getValueAt(row, 0);
        
        try (Connection con = DBConnection.connect();
             PreparedStatement pstmt = con.prepareStatement("DELETE FROM GameAssignment WHERE AssignmentID = ?")) {
            
            pstmt.setInt(1, assignID);
            pstmt.executeUpdate();
            loadAssignmentsForGame();
            JOptionPane.showMessageDialog(this, "Assignment Removed.");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}