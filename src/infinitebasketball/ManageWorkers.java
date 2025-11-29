package infinitebasketball;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ManageWorkers extends JPanel { // Changed to JPanel

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtFirst, txtLast, txtPhone, txtEmail;
    private JComboBox<String> cmbType;
    private JLabel lblSelectedID;

    public ManageWorkers() {
        initComponents();
        loadData();
        Theme.apply(this);
    }

    private void initComponents() {
        setLayout(new BorderLayout()); // Removed setSize/setTitle

        // 1. TABLE
        String[] columns = {"ID", "First Name", "Last Name", "Phone", "Email", "Type"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        table.getSelectionModel().addListSelectionListener(e -> fillForm());
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // 2. FORM
        JPanel mainFormPanel = new JPanel(new BorderLayout());
        mainFormPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JPanel formPanel = new JPanel(new GridLayout(2, 6, 10, 5));
        
        txtFirst = new JTextField(); txtLast = new JTextField();
        txtPhone = new JTextField(); txtEmail = new JTextField();
        String[] types = {"Volunteer", "Staff"};
        cmbType = new JComboBox<>(types);
        lblSelectedID = new JLabel("-");

        formPanel.add(new JLabel("First Name")); formPanel.add(new JLabel("Last Name"));
        formPanel.add(new JLabel("Phone")); formPanel.add(new JLabel("Email"));
        formPanel.add(new JLabel("Type")); formPanel.add(new JLabel("ID"));

        formPanel.add(txtFirst); formPanel.add(txtLast);
        formPanel.add(txtPhone); formPanel.add(txtEmail);
        formPanel.add(cmbType); formPanel.add(lblSelectedID);

        mainFormPanel.add(formPanel, BorderLayout.CENTER);
        add(mainFormPanel, BorderLayout.NORTH);

        // 3. BUTTONS
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        JButton btnAdd = new JButton("Add New");
        JButton btnUpdate = new JButton("Update Selected");
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnClear = new JButton("Clear Form");

        btnAdd.addActionListener(e -> addWorker());
        btnUpdate.addActionListener(e -> updateWorker());
        btnDelete.addActionListener(e -> deleteWorker());
        btnClear.addActionListener(e -> clearForm());

        btnPanel.add(btnAdd); btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete); btnPanel.add(btnClear);
        add(btnPanel, BorderLayout.SOUTH);
    }

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

    private void fillForm() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        lblSelectedID.setText(model.getValueAt(row, 0).toString());
        txtFirst.setText(model.getValueAt(row, 1).toString());
        txtLast.setText(model.getValueAt(row, 2).toString());
        txtPhone.setText(model.getValueAt(row, 3).toString());
        txtEmail.setText(model.getValueAt(row, 4).toString());
        cmbType.setSelectedItem(model.getValueAt(row, 5).toString());
    }
    
    private void clearForm() {
        table.clearSelection(); lblSelectedID.setText("-");
        txtFirst.setText(""); txtLast.setText(""); txtPhone.setText(""); txtEmail.setText("");
    }

    private void addWorker() {
        try (Connection con = DBConnection.connect();
             PreparedStatement pstmt = con.prepareStatement("INSERT INTO Worker (FirstName, LastName, Phone, Email, WorkerType) VALUES (?, ?, ?, ?, ?)")) {
            pstmt.setString(1, txtFirst.getText()); pstmt.setString(2, txtLast.getText());
            pstmt.setString(3, txtPhone.getText()); pstmt.setString(4, txtEmail.getText());
            pstmt.setString(5, cmbType.getSelectedItem().toString());
            pstmt.executeUpdate(); JOptionPane.showMessageDialog(this, "Worker Added!"); clearForm(); loadData();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void updateWorker() {
        if (lblSelectedID.getText().equals("-")) return;
        String sql = "UPDATE Worker SET FirstName=?, LastName=?, Phone=?, Email=?, WorkerType=? WHERE WorkerID=?";
        try (Connection con = DBConnection.connect(); PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, txtFirst.getText()); pstmt.setString(2, txtLast.getText());
            pstmt.setString(3, txtPhone.getText()); pstmt.setString(4, txtEmail.getText());
            pstmt.setString(5, cmbType.getSelectedItem().toString());
            pstmt.setInt(6, Integer.parseInt(lblSelectedID.getText()));
            pstmt.executeUpdate(); JOptionPane.showMessageDialog(this, "Updated!"); clearForm(); loadData();
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    private void deleteWorker() {
        if (lblSelectedID.getText().equals("-")) return;
        try (Connection con = DBConnection.connect(); PreparedStatement pstmt = con.prepareStatement("DELETE FROM Worker WHERE WorkerID=?")) {
            pstmt.setInt(1, Integer.parseInt(lblSelectedID.getText()));
            pstmt.executeUpdate(); JOptionPane.showMessageDialog(this, "Deleted!"); clearForm(); loadData();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}