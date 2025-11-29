package infinitebasketball;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class ViewReports extends JPanel { // Changed to JPanel

    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> cmbReportType;

    public ViewReports() {
        initComponents();
        loadReport("All Games");
        Theme.apply(this);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String[] reports = {"All Games", "League Standings", "Player Rosters", "Staff Directory", "Game Staff Assignments"};
        cmbReportType = new JComboBox<>(reports);
        cmbReportType.addActionListener(e -> loadReport((String) cmbReportType.getSelectedItem()));
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadReport((String) cmbReportType.getSelectedItem()));
        
        topPanel.add(new JLabel("Report:")); topPanel.add(cmbReportType); topPanel.add(btnRefresh);
        add(topPanel, BorderLayout.NORTH);

        table = new JTable();
        table.setDefaultEditor(Object.class, null);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void loadReport(String reportName) {
        String sql = "";
        switch (reportName) {
            case "All Games": sql = "SELECT g.GameID, g.GameDate, g.Location, t1.TeamName AS Home, g.HomeScore, t2.TeamName AS Away, g.AwayScore FROM Game g JOIN Team t1 ON g.HomeTeamID = t1.TeamID JOIN Team t2 ON g.AwayTeamID = t2.TeamID ORDER BY g.GameDate"; break;
            case "Player Rosters": sql = "SELECT p.FirstName, p.LastName, t.TeamName FROM Player p JOIN Team t ON p.TeamID = t.TeamID ORDER BY t.TeamName"; break;
            case "Staff Directory": sql = "SELECT FirstName, LastName, Phone, Email, WorkerType FROM Worker ORDER BY WorkerType"; break;
            case "Game Staff Assignments": sql = "SELECT g.GameDate, g.Location, r.RoleName, w.LastName FROM GameAssignment ga JOIN Game g ON ga.GameID = g.GameID JOIN Worker w ON ga.WorkerID = w.WorkerID JOIN Role r ON ga.RoleID = r.RoleID ORDER BY g.GameDate"; break;
            case "League Standings": sql = "SELECT t.TeamName, SUM(CASE WHEN (g.HomeTeamID=t.TeamID AND g.HomeScore>g.AwayScore) OR (g.AwayTeamID=t.TeamID AND g.AwayScore>g.HomeScore) THEN 1 ELSE 0 END) AS Wins, SUM(CASE WHEN (g.HomeTeamID=t.TeamID AND g.HomeScore<g.AwayScore) OR (g.AwayTeamID=t.TeamID AND g.AwayScore<g.HomeScore) THEN 1 ELSE 0 END) AS Losses FROM Team t LEFT JOIN Game g ON t.TeamID=g.HomeTeamID OR t.TeamID=g.AwayTeamID GROUP BY t.TeamID, t.TeamName ORDER BY Wins DESC"; break;
        }

        try (Connection con = DBConnection.connect(); Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            Vector<String> columnNames = new Vector<>();
            for (int i = 1; i <= columnCount; i++) columnNames.add(metaData.getColumnLabel(i));
            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= columnCount; i++) row.add(rs.getObject(i));
                data.add(row);
            }
            model = new DefaultTableModel(data, columnNames);
            table.setModel(model);
            Theme.apply(this); // Re-apply theme to new table
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }
}