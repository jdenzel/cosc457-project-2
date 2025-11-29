package infinitebasketball;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class ManageSchedule extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    
    // Game Input Fields
    private JTextField txtDate, txtTime, txtLocation;
    private JComboBox<String> cmbHome, cmbAway;
    
    // Staff Dropdowns
    private JComboBox<String> cmbRef1, cmbRef2, cmbScorekeeper, cmbTimekeeper, cmbCamera;
    
    // UI Containers & Controls
    private JPanel formContainer; // The container that holds the entire form (hidden/shown)
    private JButton btnAction, btnDelete;
    private JButton btnAdd; // Button to trigger adding a new game
    
    private int selectedGameID = -1; // -1 means "Add Mode"
    private int currentMode = 0; // 0: View, 1: Add, 2: Update

    public ManageSchedule() {
        initComponents();
        loadTeams();
        loadWorkers();
        loadGames();
        Theme.apply(this);
        // Start in view mode
        setFormVisibility(false);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10)); // Added spacing

        // --- 1. TOP CONTROL BUTTONS (Add New Game) ---
        JPanel topControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        
        btnAdd = new JButton("Add New Game");
        btnAdd.addActionListener(e -> switchToAddMode());
        
        topControlPanel.add(btnAdd);

        // Control Panel goes to the NORTH (top)
        add(topControlPanel, BorderLayout.NORTH); 
        
        // --- 2. CENTER TABLE (Schedule List) ---
        model = new DefaultTableModel(new String[]{"ID", "Date", "Time", "Location", "Home", "Away"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        
        // Listener: When row clicked, switch to Edit Mode
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                switchToUpdateMode();
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(800, 250));
        add(scrollPane, BorderLayout.CENTER);

        // --- 3. BOTTOM FORM CONTAINER (Hidden by default) ---
        formContainer = new JPanel(new BorderLayout());
        formContainer.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JPanel topFormWrapper = new JPanel(new BorderLayout());
        
        // CLOSE BUTTON (Top Right)
        JButton btnClose = new JButton("X");
        btnClose.setMargin(new Insets(2, 5, 2, 5));
        btnClose.setBackground(new Color(150, 0, 0));
        btnClose.setForeground(Color.WHITE);
        btnClose.addActionListener(e -> setFormVisibility(false));
        
        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        closePanel.setBackground(Theme.BACKGROUND);
        closePanel.add(btnClose);
        
        topFormWrapper.add(closePanel, BorderLayout.NORTH);

        // --- SPLIT FORM COLUMNS ---
        JPanel columnsPanel = new JPanel(new GridLayout(1, 2, 20, 0)); 
        
        // LEFT COLUMN: GAME DETAILS
        JPanel leftPanel = new JPanel(new GridLayout(6, 2, 10, 10)); 
        leftPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.PRIMARY, 1), 
                "Game Details", 0, 0, Theme.FONT_BOLD, Theme.ACCENT));

        txtDate = new JTextField("YYYY-MM-DD");
        txtTime = new JTextField("HH:MM AM/PM");
        txtLocation = new JTextField("CCBC Essex");
        cmbHome = new JComboBox<>();
        cmbAway = new JComboBox<>();

        leftPanel.add(new JLabel("Date:")); leftPanel.add(txtDate);
        leftPanel.add(new JLabel("Time:")); leftPanel.add(txtTime);
        leftPanel.add(new JLabel("Location:")); leftPanel.add(txtLocation);
        leftPanel.add(new JLabel("Home Team:")); leftPanel.add(cmbHome);
        leftPanel.add(new JLabel("Away Team:")); leftPanel.add(cmbAway);
        leftPanel.add(new JLabel("")); leftPanel.add(new JLabel("")); 

        // RIGHT COLUMN: STAFF ASSIGNMENT
        JPanel rightPanel = new JPanel(new GridLayout(6, 2, 10, 10)); 
        rightPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.PRIMARY, 1), 
                "Assign Staff (Must be Unique)", 0, 0, Theme.FONT_BOLD, Theme.ACCENT));

        cmbRef1 = new JComboBox<>(); cmbRef2 = new JComboBox<>();
        cmbScorekeeper = new JComboBox<>(); cmbTimekeeper = new JComboBox<>();
        cmbCamera = new JComboBox<>();

        rightPanel.add(new JLabel("Referee 1:")); rightPanel.add(cmbRef1);
        rightPanel.add(new JLabel("Referee 2:")); rightPanel.add(cmbRef2);
        rightPanel.add(new JLabel("Scorekeeper:")); rightPanel.add(cmbScorekeeper);
        rightPanel.add(new JLabel("Timekeeper:")); rightPanel.add(cmbTimekeeper);
        rightPanel.add(new JLabel("Camera Op:")); rightPanel.add(cmbCamera);
        rightPanel.add(new JLabel("")); rightPanel.add(new JLabel(""));

        columnsPanel.add(leftPanel);
        columnsPanel.add(rightPanel);
        
        topFormWrapper.add(columnsPanel, BorderLayout.CENTER);

        // --- ACTION BUTTONS (At the very bottom) ---
        JPanel formBtnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnAction = new JButton("Schedule Game"); // Universal button for Add/Update
        btnAction.addActionListener(e -> saveGame());
        
        btnDelete = new JButton("Delete Game");
        btnDelete.setBackground(new Color(150, 0, 0));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setVisible(false); // Hidden by default
        btnDelete.addActionListener(e -> deleteGame());

        formBtnPanel.add(btnDelete);
        formBtnPanel.add(btnAction);
        
        topFormWrapper.add(formBtnPanel, BorderLayout.SOUTH);
        
        formContainer.add(topFormWrapper, BorderLayout.CENTER);
        
        add(formContainer, BorderLayout.SOUTH); 
    }

    private void setFormVisibility(boolean visible) {
        formContainer.setVisible(visible);
        
        // TOGGLE ADD BUTTON VISIBILITY
        if(btnAdd != null) {
            btnAdd.setVisible(!visible);
        }

        if (!visible) {
            currentMode = 0; // Back to View Mode
            selectedGameID = -1;
            table.clearSelection();
            // Reset dropdowns/buttons for clean state
            btnDelete.setVisible(false);
            btnAction.setText("Schedule Game");
            clearFormFields();
        }
        revalidate();
        repaint();
    }
    
    private void switchToAddMode() {
        setFormVisibility(true);
        currentMode = 1;
        btnAction.setText("Schedule Game");
        btnDelete.setVisible(false);
        clearFormFields();
        // Set dropdowns to empty selection
        cmbHome.setSelectedIndex(-1); cmbAway.setSelectedIndex(-1);
        cmbRef1.setSelectedIndex(-1); cmbRef2.setSelectedIndex(-1);
        cmbScorekeeper.setSelectedIndex(-1); cmbTimekeeper.setSelectedIndex(-1); cmbCamera.setSelectedIndex(-1);
    }
    
    private void switchToUpdateMode() {
        if (table.getSelectedRow() == -1) return;
        
        setFormVisibility(true);
        currentMode = 2;
        btnAction.setText("Update Game");
        btnDelete.setVisible(true);
        
        loadGameDetails();
    }
    
    private void clearFormFields() {
        txtDate.setText("YYYY-MM-DD");
        txtTime.setText("HH:MM AM/PM");
        txtLocation.setText("CCBC Essex");
        selectedGameID = -1;
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
    
    private void loadWorkers() {
        try (Connection con = DBConnection.connect(); Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery("SELECT WorkerID, FirstName, LastName FROM Worker")) {
            cmbRef1.removeAllItems(); cmbRef2.removeAllItems(); 
            cmbScorekeeper.removeAllItems(); cmbTimekeeper.removeAllItems(); cmbCamera.removeAllItems();
            
            while (rs.next()) {
                String s = rs.getInt("WorkerID") + " - " + rs.getString("FirstName") + " " + rs.getString("LastName");
                cmbRef1.addItem(s); cmbRef2.addItem(s);
                cmbScorekeeper.addItem(s); cmbTimekeeper.addItem(s); cmbCamera.addItem(s);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadGames() {
        model.setRowCount(0);
        String sql = "SELECT g.GameID, g.GameDate, g.GameTime, g.Location, t1.TeamName AS Home, t2.TeamName AS Away FROM Game g JOIN Team t1 ON g.HomeTeamID = t1.TeamID JOIN Team t2 ON g.AwayTeamID = t2.TeamID ORDER BY g.GameDate DESC";
        try (Connection con = DBConnection.connect(); Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) model.addRow(new Object[]{
                rs.getInt("GameID"), rs.getDate("GameDate"), rs.getString("GameTime"), 
                rs.getString("Location"), rs.getString("Home"), rs.getString("Away")
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- LOAD GAME DETAILS FOR EDITING ---
    private void loadGameDetails() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        selectedGameID = (int) model.getValueAt(row, 0);
        
        try (Connection con = DBConnection.connect()) {
            // 1. Get Game Info
            String sqlGame = "SELECT * FROM Game WHERE GameID = ?";
            try (PreparedStatement p = con.prepareStatement(sqlGame)) {
                p.setInt(1, selectedGameID);
                ResultSet rs = p.executeQuery();
                if (rs.next()) {
                    txtDate.setText(rs.getString("GameDate"));
                    txtTime.setText(rs.getString("GameTime"));
                    txtLocation.setText(rs.getString("Location"));
                    
                    int hID = rs.getInt("HomeTeamID");
                    int aID = rs.getInt("AwayTeamID");
                    selectItemByID(cmbHome, hID);
                    selectItemByID(cmbAway, aID);
                }
            }

            // 2. Get Staff Assignments
            // Reset staff dropdowns first
            cmbRef1.setSelectedIndex(-1); cmbRef2.setSelectedIndex(-1);
            cmbScorekeeper.setSelectedIndex(-1); cmbTimekeeper.setSelectedIndex(-1); cmbCamera.setSelectedIndex(-1);
            
            String sqlStaff = "SELECT WorkerID, RoleID FROM GameAssignment WHERE GameID = ?";
            try (PreparedStatement p = con.prepareStatement(sqlStaff)) {
                p.setInt(1, selectedGameID);
                ResultSet rs = p.executeQuery();
                boolean ref1Filled = false;
                
                while(rs.next()) {
                    int wID = rs.getInt("WorkerID");
                    int rID = rs.getInt("RoleID");
                    
                    if (rID == 1) { // Referee
                        if (!ref1Filled) { selectItemByID(cmbRef1, wID); ref1Filled = true; }
                        else { selectItemByID(cmbRef2, wID); }
                    }
                    else if (rID == 2) selectItemByID(cmbScorekeeper, wID);
                    else if (rID == 3) selectItemByID(cmbTimekeeper, wID);
                    else if (rID == 4) selectItemByID(cmbCamera, wID);
                }
            }

        } catch (Exception e) { e.printStackTrace(); }
    }

    // Helper to select item in combobox "ID - Name"
    private void selectItemByID(JComboBox<String> cmb, int id) {
        for (int i = 0; i < cmb.getItemCount(); i++) {
            String item = cmb.getItemAt(i);
            if (item != null && item.startsWith(id + " -")) {
                cmb.setSelectedIndex(i);
                return;
            }
        }
    }

    private void saveGame() {
        if (cmbHome.getSelectedItem() == null || cmbAway.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select both Home and Away teams.", "Missing Teams", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int h = Integer.parseInt(cmbHome.getSelectedItem().toString().split(" - ")[0]);
        int a = Integer.parseInt(cmbAway.getSelectedItem().toString().split(" - ")[0]);
        if (h == a) { JOptionPane.showMessageDialog(this, "Home and Away teams cannot be the same team!", "Team Selection Error", JOptionPane.ERROR_MESSAGE); return; }

        Set<Integer> workerIDs = new HashSet<>();
        int ref1 = getWorkerID(cmbRef1); int ref2 = getWorkerID(cmbRef2);
        int score = getWorkerID(cmbScorekeeper); int time = getWorkerID(cmbTimekeeper); int cam = getWorkerID(cmbCamera);

        if (ref1 == -1 || ref2 == -1 || score == -1 || time == -1 || cam == -1) {
             JOptionPane.showMessageDialog(this, "Please select a worker for every role.", "Missing Staff", JOptionPane.WARNING_MESSAGE);
             return;
        }
        workerIDs.add(ref1); workerIDs.add(ref2); workerIDs.add(score); workerIDs.add(time); workerIDs.add(cam);
        if (workerIDs.size() < 5) { JOptionPane.showMessageDialog(this, "Staff members must be unique!", "Duplicate Staff Assignment", JOptionPane.ERROR_MESSAGE); return; }

        try (Connection con = DBConnection.connect()) {
            con.setAutoCommit(false); 

            if (currentMode == 1) {
                // --- INSERT NEW GAME ---
                String sql = "INSERT INTO Game (GameDate, GameTime, Location, HomeTeamID, AwayTeamID) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement p = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    p.setString(1, txtDate.getText()); p.setString(2, txtTime.getText());
                    p.setString(3, txtLocation.getText()); p.setInt(4, h); p.setInt(5, a);
                    p.executeUpdate();
                    ResultSet rs = p.getGeneratedKeys();
                    if (rs.next()) selectedGameID = rs.getInt(1);
                }
            } else {
                // --- UPDATE EXISTING GAME ---
                String sql = "UPDATE Game SET GameDate=?, GameTime=?, Location=?, HomeTeamID=?, AwayTeamID=? WHERE GameID=?";
                try (PreparedStatement p = con.prepareStatement(sql)) {
                    p.setString(1, txtDate.getText()); p.setString(2, txtTime.getText());
                    p.setString(3, txtLocation.getText()); p.setInt(4, h); p.setInt(5, a);
                    p.setInt(6, selectedGameID);
                    p.executeUpdate();
                }
                // Delete old assignments to re-insert new ones
                try (PreparedStatement p = con.prepareStatement("DELETE FROM GameAssignment WHERE GameID=?")) {
                    p.setInt(1, selectedGameID); p.executeUpdate();
                }
            }

            // --- INSERT ASSIGNMENTS (For both Insert and Update) ---
            String assignSql = "INSERT INTO GameAssignment (GameID, WorkerID, RoleID) VALUES (?, ?, ?)";
            try (PreparedStatement p = con.prepareStatement(assignSql)) {
                p.setInt(1, selectedGameID); p.setInt(2, ref1); p.setInt(3, 1); p.addBatch();
                p.setInt(1, selectedGameID); p.setInt(2, ref2); p.setInt(3, 1); p.addBatch();
                p.setInt(1, selectedGameID); p.setInt(2, score); p.setInt(3, 2); p.addBatch();
                p.setInt(1, selectedGameID); p.setInt(2, time); p.setInt(3, 3); p.addBatch();
                p.setInt(1, selectedGameID); p.setInt(2, cam); p.setInt(3, 4); p.addBatch();
                p.executeBatch();
            }

            con.commit(); 
            loadGames();
            JOptionPane.showMessageDialog(this, "Game Saved Successfully!");
            setFormVisibility(false);

        } catch (Exception e) { 
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteGame() {
        if (selectedGameID == -1) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure? This will delete the game and all associated stats.", "Delete Game", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = DBConnection.connect()) {
            con.setAutoCommit(false);
            // Delete dependencies first (Foreign Keys)
            try (PreparedStatement p = con.prepareStatement("DELETE FROM GameAssignment WHERE GameID=?")) {
                p.setInt(1, selectedGameID); p.executeUpdate();
            }
            try (PreparedStatement p = con.prepareStatement("DELETE FROM PlayerStats WHERE GameID=?")) {
                p.setInt(1, selectedGameID); p.executeUpdate();
            }
            // Delete Game
            try (PreparedStatement p = con.prepareStatement("DELETE FROM Game WHERE GameID=?")) {
                p.setInt(1, selectedGameID); p.executeUpdate();
            }
            con.commit();
            loadGames();
            setFormVisibility(false);
            JOptionPane.showMessageDialog(this, "Game Deleted.");
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }
    
    private int getWorkerID(JComboBox<String> cmb) {
        if (cmb.getSelectedItem() == null) return -1;
        return Integer.parseInt(cmb.getSelectedItem().toString().split(" - ")[0]);
    }
}