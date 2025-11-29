package infinitebasketball;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ManageWorkers extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    
    // Input Fields and Containers
    private JTextField txtFirst, txtLast, txtPhone, txtEmail;
    private JComboBox<String> cmbType;
    private JLabel lblSelectedID;
    private JPanel formContainer; // Panel to hold and hide/show the form
    
    // Action Buttons
    private JButton btnAction, btnDelete;
    private JButton btnAdd; // Need to reference this for visibility toggle
    
    // State Tracking
    private int currentMode = 0; // 0: View, 1: Add, 2: Update
    private int selectedWorkerID = -1;

    public ManageWorkers() {
        initComponents();
        loadData();
        Theme.apply(this);
        // Start in view mode
        setFormVisibility(false);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10)); // Added spacing

        // 1. TABLE
        String[] columns = {"ID", "First Name", "Last Name", "Phone", "Email", "Type"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        
        // Listener: When row is clicked, switches to UPDATE Mode
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                switchToUpdateMode();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // 2. FORM CONTAINER (Hidden by default)
        formContainer = new JPanel(new BorderLayout());
        formContainer.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Panel to hold the form and the close button
        JPanel topFormWrapper = new JPanel(new BorderLayout());
        
        // CLOSE BUTTON (Top Right)
        JButton btnClose = new JButton("X");
        btnClose.setMargin(new Insets(2, 5, 2, 5)); // Make it small
        btnClose.setBackground(new Color(150, 0, 0));
        btnClose.setForeground(Color.WHITE);
        btnClose.addActionListener(e -> setFormVisibility(false));
        
        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        closePanel.setBackground(Theme.BACKGROUND);
        closePanel.add(btnClose);
        
        topFormWrapper.add(closePanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(2, 6, 10, 5));
        
        txtFirst = new JTextField(); txtLast = new JTextField();
        txtPhone = new JTextField(); txtEmail = new JTextField();
        String[] types = {"Volunteer", "Staff"};
        cmbType = new JComboBox<>(types);
        lblSelectedID = new JLabel("-"); // Used to track ID being edited

        // Labels
        formPanel.add(new JLabel("First Name")); formPanel.add(new JLabel("Last Name"));
        formPanel.add(new JLabel("Phone")); formPanel.add(new JLabel("Email"));
        formPanel.add(new JLabel("Type")); formPanel.add(new JLabel("ID"));

        // Inputs
        formPanel.add(txtFirst); formPanel.add(txtLast);
        formPanel.add(txtPhone); formPanel.add(txtEmail);
        formPanel.add(cmbType); formPanel.add(lblSelectedID);

        topFormWrapper.add(formPanel, BorderLayout.CENTER);

        // 3. ACTION BUTTONS (For form)
        JPanel formBtnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnAction = new JButton("Submit"); // Universal button for Add/Update
        btnAction.addActionListener(e -> submitForm());
        
        btnDelete = new JButton("Delete Selected");
        btnDelete.setBackground(new Color(150, 0, 0));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.addActionListener(e -> deleteWorker());

        formBtnPanel.add(btnAction);
        formBtnPanel.add(btnDelete);
        topFormWrapper.add(formBtnPanel, BorderLayout.SOUTH);
        
        formContainer.add(topFormWrapper, BorderLayout.CENTER);
        
        // Reworked: Form container goes to the SOUTH (bottom)
        add(formContainer, BorderLayout.SOUTH); 

        // 4. TOP CONTROL BUTTONS (Now placed at the North, above the table)
        JPanel topControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        
        // Initialize btnAdd here, ensuring it exists before any visibility calls
        btnAdd = new JButton("Add New Worker"); 
        // REMOVED: JButton btnClear = new JButton("Clear Selection");
        // We rely on the X button and the list selection listener now.

        btnAdd.addActionListener(e -> switchToAddMode());
        // REMOVED: btnClear.addActionListener(e -> setFormVisibility(false));
        
        topControlPanel.add(btnAdd);
        // REMOVED: topControlPanel.add(btnClear);

        // Control Panel goes to the NORTH (top)
        add(topControlPanel, BorderLayout.NORTH); 
    }

    private void setFormVisibility(boolean visible) {
        formContainer.setVisible(visible);
        
        // TOGGLE ADD BUTTON VISIBILITY
        if(btnAdd != null) {
            btnAdd.setVisible(!visible);
        }

        if (!visible) {
            currentMode = 0; // Back to View Mode
            selectedWorkerID = -1;
            clearFormFields();
            table.clearSelection();
        }
        // Force re-layout after hiding/showing component
        revalidate();
        repaint();
    }
    
    private void switchToAddMode() {
        setFormVisibility(true);
        currentMode = 1;
        btnAction.setText("Submit Add");
        btnDelete.setVisible(false); // Can't delete until worker is selected
        lblSelectedID.setText("-");
        table.clearSelection();
        clearFormFields();
    }
    
    private void switchToUpdateMode() {
        if (table.getSelectedRow() == -1) return; 
        
        setFormVisibility(true);
        currentMode = 2;
        btnAction.setText("Submit Update");
        btnDelete.setVisible(true); 
        
        fillFormFieldsFromTable();
    }

    private void clearFormFields() {
        txtFirst.setText(""); txtLast.setText("");
        txtPhone.setText(""); txtEmail.setText("");
        lblSelectedID.setText("-");
        selectedWorkerID = -1;
    }

    private void fillFormFieldsFromTable() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        selectedWorkerID = (int) model.getValueAt(row, 0); // Get ID from table
        
        // Fill form fields
        lblSelectedID.setText(String.valueOf(selectedWorkerID));
        txtFirst.setText(model.getValueAt(row, 1).toString());
        txtLast.setText(model.getValueAt(row, 2).toString());
        txtPhone.setText(model.getValueAt(row, 3).toString());
        txtEmail.setText(model.getValueAt(row, 4).toString());
        cmbType.setSelectedItem(model.getValueAt(row, 5).toString());
    }

    // --- SUBMIT LOGIC ---
    private void submitForm() {
        if (currentMode == 1) {
            addWorker();
        } else if (currentMode == 2) {
            updateWorker();
        } else {
            JOptionPane.showMessageDialog(this, "Please select an action (Add New or Select Worker).");
        }
    }
    
    // --- DATABASE OPERATIONS (mostly unchanged) ---
    private void loadData() {
        model.setRowCount(0);
        try (Connection con = DBConnection.connect();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Worker")) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("WorkerID"), rs.getString("FirstName"), rs.getString("LastName"),
                    rs.getString("Phone"), rs.getString("Email"), rs.getString("WorkerType")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void addWorker() {
        try (Connection con = DBConnection.connect();
             PreparedStatement pstmt = con.prepareStatement("INSERT INTO Worker (FirstName, LastName, Phone, Email, WorkerType) VALUES (?, ?, ?, ?, ?)")) {
            
            pstmt.setString(1, txtFirst.getText()); pstmt.setString(2, txtLast.getText());
            pstmt.setString(3, txtPhone.getText()); pstmt.setString(4, txtEmail.getText());
            pstmt.setString(5, cmbType.getSelectedItem().toString());

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Worker Added!");
            setFormVisibility(false); // Hide form after success
            loadData();
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Error adding worker: " + e.getMessage()); }
    }

    private void updateWorker() {
        if (selectedWorkerID == -1) return;
        
        String sql = "UPDATE Worker SET FirstName=?, LastName=?, Phone=?, Email=?, WorkerType=? WHERE WorkerID=?";
        try (Connection con = DBConnection.connect();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, txtFirst.getText()); pstmt.setString(2, txtLast.getText());
            pstmt.setString(3, txtPhone.getText()); pstmt.setString(4, txtEmail.getText());
            pstmt.setString(5, cmbType.getSelectedItem().toString());
            pstmt.setInt(6, selectedWorkerID);

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Worker Updated!");
            setFormVisibility(false); // Hide form after success
            loadData();
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Error updating worker: " + e.getMessage()); }
    }
    
    private void deleteWorker() {
        if (selectedWorkerID == -1) return;
        
        int confirm = JOptionPane.showConfirmDialog(this, "Delete Worker ID " + selectedWorkerID + "? This cannot be undone.", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        
        try (Connection con = DBConnection.connect(); PreparedStatement pstmt = con.prepareStatement("DELETE FROM Worker WHERE WorkerID=?")) {
            pstmt.setInt(1, selectedWorkerID);
            pstmt.executeUpdate(); 
            JOptionPane.showMessageDialog(this, "Worker Deleted!"); 
            setFormVisibility(false); 
            loadData();
        } catch (SQLException e) { 
            JOptionPane.showMessageDialog(this, "Cannot delete worker: Still assigned to games.", "Deletion Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}