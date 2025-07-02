// SETTINGS_WINDOW.java
package gui;

import controller.database;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;

public class SETTINGS_WINDOW extends JFrame {

    private JTable budgetTable;
    private DefaultTableModel tableModel;
    private JLabel totalBudgetLabel;
    private JTextField totalBudgetField;
    private JComboBox<String> monthCombo;
    private JComboBox<Integer> yearCombo;

    public SETTINGS_WINDOW() {
        setTitle("Application Settings");
        setSize(850, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);

        initComponents();

        Calendar cal = Calendar.getInstance();
        monthCombo.setSelectedIndex(cal.get(Calendar.MONTH));
        yearCombo.setSelectedItem(cal.get(Calendar.YEAR));

        loadBudgetsAndPrices();
        setVisible(true);
    }

    private void initComponents() {
        JLabel title = new JLabel("Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBounds(360, 20, 200, 30);
        add(title);

        monthCombo = new JComboBox<>(new String[]{
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        });
        monthCombo.setBounds(350, 70, 120, 25);
        add(monthCombo);

        yearCombo = new JComboBox<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int y = currentYear - 2; y <= currentYear + 5; y++) {
            yearCombo.addItem(y);
        }
        yearCombo.setBounds(480, 70, 80, 25);
        add(yearCombo);

        JLabel budgetLabel = new JLabel("Set Total Budget (BDT):");
        budgetLabel.setBounds(30, 110, 160, 25);
        add(budgetLabel);

        totalBudgetField = new JTextField();
        totalBudgetField.setBounds(190, 110, 150, 25);
        add(totalBudgetField);

        tableModel = new DefaultTableModel(new String[]{
                "Category", "Company", "Buying Price (BDT)", "Selling Price (BDT)"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };

        budgetTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(budgetTable);
        scrollPane.setBounds(30, 150, 780, 300);
        add(scrollPane);

        JButton saveButton = new JButton("Save Settings");
        saveButton.setBounds(360, 470, 140, 35);
        saveButton.setBackground(new Color(0, 123, 255));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        add(saveButton);

        totalBudgetLabel = new JLabel("Current Total Budget: 0.00 BDT");
        totalBudgetLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalBudgetLabel.setBounds(30, 520, 350, 25);
        add(totalBudgetLabel);

        saveButton.addActionListener(e -> saveBudgetsAndPrices());
        monthCombo.addActionListener(e -> loadBudgetsAndPrices());
        yearCombo.addActionListener(e -> loadBudgetsAndPrices());
    }

    private void loadBudgetsAndPrices() {
        tableModel.setRowCount(0);
        int selectedMonth = monthCombo.getSelectedIndex() + 1;
        int selectedYear = (Integer) yearCombo.getSelectedItem();

        try (Connection conn = database.getConnection();
             Statement stmt = conn.createStatement()) {

            double totalBudget = 0;
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT SUM(budget) AS total FROM category_budgets " +
                            "WHERE month = " + selectedMonth + " AND year = " + selectedYear)) {
                if (rs.next()) {
                    totalBudget = rs.getDouble("total");
                }
            }

            totalBudgetField.setText(String.format("%.2f", totalBudget));
            totalBudgetLabel.setText("Current Total Budget: " + String.format("%.2f BDT", totalBudget));

            try (ResultSet rsCategories = stmt.executeQuery("SELECT name FROM categories")) {
                while (rsCategories.next()) {
                    String category = rsCategories.getString("name");
                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT company_name, price, selling_price FROM companies WHERE category_name = ?")) {
                        ps.setString(1, category);
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                tableModel.addRow(new Object[]{
                                        category,
                                        rs.getString("company_name"),
                                        rs.getDouble("price"),
                                        rs.getDouble("selling_price")
                                });
                            }
                        }
                    }
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }

    private void saveBudgetsAndPrices() {
        int selectedMonth = monthCombo.getSelectedIndex() + 1;
        int selectedYear = (Integer) yearCombo.getSelectedItem();
        String budgetText = totalBudgetField.getText().trim();
        double totalBudget;

        try {
            totalBudget = Double.parseDouble(budgetText);
            if (totalBudget < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid total budget value.");
            return;
        }

        try (Connection conn = database.getConnection()) {
            conn.setAutoCommit(false);

            // Clear previous month-year budgets
            try (PreparedStatement del = conn.prepareStatement(
                    "DELETE FROM category_budgets WHERE month = ? AND year = ?")) {
                del.setInt(1, selectedMonth);
                del.setInt(2, selectedYear);
                del.executeUpdate();
            }

            List<String> categories = new ArrayList<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT name FROM categories")) {
                while (rs.next()) {
                    categories.add(rs.getString("name"));
                }
            }

            double perCategory = totalBudget / categories.size();
            try (PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO category_budgets (category_name, budget, month, year) VALUES (?, ?, ?, ?)")) {
                for (String cat : categories) {
                    ins.setString(1, cat);
                    ins.setDouble(2, perCategory);
                    ins.setInt(3, selectedMonth);
                    ins.setInt(4, selectedYear);
                    ins.addBatch();
                }
                ins.executeBatch();
            }

            // Update selling prices
            try (PreparedStatement upd = conn.prepareStatement(
                    "UPDATE companies SET selling_price = ? WHERE category_name = ? AND company_name = ?")) {
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String cat = tableModel.getValueAt(i, 0).toString();
                    String comp = tableModel.getValueAt(i, 1).toString();
                    double sellPrice = Double.parseDouble(tableModel.getValueAt(i, 3).toString());
                    if (sellPrice < 0) throw new NumberFormatException();
                    upd.setDouble(1, sellPrice);
                    upd.setString(2, cat);
                    upd.setString(3, comp);
                    upd.addBatch();
                }
                upd.executeBatch();
            }

            conn.commit();
            totalBudgetLabel.setText("Current Total Budget: " + String.format("%.2f BDT", totalBudget));
            JOptionPane.showMessageDialog(this, "✅ Settings saved successfully!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving settings: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SETTINGS_WINDOW::new);
    }
}
