package gui;

import controller.database;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class MANAGE_CATEGORIES_WINDOW extends JFrame {

    private JTextField categoryNameField;
    private JComboBox<String> typeComboBox;
    private JTable categoryTable;
    private DefaultTableModel tableModel;

    public MANAGE_CATEGORIES_WINDOW() {
        setTitle("Manage Categories");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        getContentPane().setBackground(new Color(200, 230, 255));

        initComponents();
        loadCategories((String) typeComboBox.getSelectedItem());
        setVisible(true);
    }

    private void initComponents() {
        JLabel titleLabel = new JLabel("Manage Categories");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setBounds(200, 10, 250, 30);
        add(titleLabel);

        JLabel nameLabel = new JLabel("Category Name:");
        nameLabel.setBounds(30, 60, 120, 25);
        add(nameLabel);

        categoryNameField = new JTextField();
        categoryNameField.setBounds(150, 60, 170, 25);
        add(categoryNameField);

        JLabel typeLabel = new JLabel("Category Type:");
        typeLabel.setBounds(330, 60, 100, 25);
        add(typeLabel);

        String[] types = {"Income", "Expense"};
        typeComboBox = new JComboBox<>(types);
        typeComboBox.setBounds(430, 60, 120, 25);
        add(typeComboBox);

        typeComboBox.addActionListener(e -> loadCategories((String) typeComboBox.getSelectedItem()));

        JButton addButton = new JButton("Add");
        addButton.setBounds(150, 100, 100, 30);
        addButton.setBackground(new Color(46, 204, 113));
        addButton.setForeground(Color.WHITE);
        add(addButton);

        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.setBounds(270, 100, 150, 30);
        deleteButton.setBackground(new Color(231, 76, 60));
        deleteButton.setForeground(Color.WHITE);
        add(deleteButton);

        tableModel = new DefaultTableModel(new String[]{"Name", "Type"}, 0);
        categoryTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(categoryTable);
        scrollPane.setBounds(30, 150, 520, 230);
        add(scrollPane);

        addButton.addActionListener(e -> addCategory());
        deleteButton.addActionListener(e -> deleteSelectedCategory());
    }

    private void loadCategories(String typeFilter) {
        tableModel.setRowCount(0);
        try (Connection conn = database.getConnection()) {
            String sql = "SELECT name, type FROM categories WHERE type = ? ORDER BY name DESC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, typeFilter);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{rs.getString("name"), rs.getString("type")});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading categories: " + ex.getMessage());
        }
    }

    private void addCategory() {
        String rawName = categoryNameField.getText().trim();

        if (rawName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a category name.");
            return;
        }

        String name = rawName.toUpperCase();
        String type = (String) typeComboBox.getSelectedItem();

        try (Connection conn = database.getConnection()) {
            // First, check if the category already exists
            String checkSql = "SELECT COUNT(*) FROM categories WHERE UPPER(name) = ? AND type = ?";
            PreparedStatement checkPs = conn.prepareStatement(checkSql);
            checkPs.setString(1, name);
            checkPs.setString(2, type);
            ResultSet rs = checkPs.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "This category already exists.");
                return;
            }

            // Proceed to insert
            String sql = "INSERT INTO categories (name, type) VALUES (?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, type);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                if ("Income".equalsIgnoreCase(type)) {
                    try {
                        String inventorySql = "INSERT INTO inventory (product_name, quantity) VALUES (?, 0)";
                        PreparedStatement invPs = conn.prepareStatement(inventorySql);
                        invPs.setString(1, name);
                        invPs.executeUpdate();
                    } catch (SQLException invEx) {
                        if (invEx.getErrorCode() != 1062) { // Ignore duplicate
                            JOptionPane.showMessageDialog(this, "Error syncing to inventory: " + invEx.getMessage());
                        }
                    }
                }

                JOptionPane.showMessageDialog(this, "Category added.");
                categoryNameField.setText("");
                loadCategories(type);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add category.");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void deleteSelectedCategory() {
        int row = categoryTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a category to delete.");
            return;
        }

        String name = (String) tableModel.getValueAt(row, 0);
        String type = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this, "Delete this category?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = database.getConnection()) {
            String sql = "DELETE FROM categories WHERE name = ? AND type = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, type);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                if ("Income".equalsIgnoreCase(type)) {
                    String invSql = "DELETE FROM inventory WHERE UPPER(product_name) = ?";
                    PreparedStatement invPs = conn.prepareStatement(invSql);
                    invPs.setString(1, name.toUpperCase());
                    invPs.executeUpdate();
                }

                JOptionPane.showMessageDialog(this, "Category deleted.");
                loadCategories((String) typeComboBox.getSelectedItem());
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete category.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MANAGE_CATEGORIES_WINDOW::new);
    }
}
