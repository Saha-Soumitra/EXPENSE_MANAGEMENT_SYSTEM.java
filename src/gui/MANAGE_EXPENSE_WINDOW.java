package gui;

import controller.database;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Calendar;

public class MANAGE_EXPENSE_WINDOW extends JFrame {

    private JComboBox<String> monthCombo;
    private JComboBox<Integer> yearCombo;
    private JLabel totalBudgetLabel;
    private JTextField expenseNameField;
    private JTextField expenseAmountField;
    private JTable expensesTable;
    private DefaultTableModel tableModel;

    public MANAGE_EXPENSE_WINDOW() {
        setTitle("Manage Expenses");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);

        initComponents();

        Calendar cal = Calendar.getInstance();
        monthCombo.setSelectedIndex(cal.get(Calendar.MONTH));
        yearCombo.setSelectedItem(cal.get(Calendar.YEAR));

        loadTotalBudget();
        loadExpenses();

        setVisible(true);
    }

    private void initComponents() {
        JLabel titleLabel = new JLabel("Manage Expenses");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setBounds(250, 10, 300, 30);
        add(titleLabel);

        monthCombo = new JComboBox<>(new String[]{
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        });
        monthCombo.setBounds(250, 50, 120, 25);
        add(monthCombo);

        yearCombo = new JComboBox<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int y = currentYear - 2; y <= currentYear + 5; y++) {
            yearCombo.addItem(y);
        }
        yearCombo.setBounds(380, 50, 80, 25);
        add(yearCombo);

        totalBudgetLabel = new JLabel("Total Budget: 0.00 BDT");
        totalBudgetLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalBudgetLabel.setBounds(480, 50, 200, 25);
        add(totalBudgetLabel);

        JLabel expenseNameLabel = new JLabel("Expense Name:");
        expenseNameLabel.setBounds(30, 90, 120, 25);
        add(expenseNameLabel);

        expenseNameField = new JTextField();
        expenseNameField.setBounds(150, 90, 200, 25);
        add(expenseNameField);

        JLabel expenseAmountLabel = new JLabel("Amount (BDT):");
        expenseAmountLabel.setBounds(370, 90, 120, 25);
        add(expenseAmountLabel);

        expenseAmountField = new JTextField();
        expenseAmountField.setBounds(480, 90, 150, 25);
        add(expenseAmountField);

        JButton addExpenseButton = new JButton("Add Expense");
        addExpenseButton.setBounds(270, 130, 140, 30);
        addExpenseButton.setBackground(new Color(40, 167, 69)); // Green
        addExpenseButton.setForeground(Color.WHITE);
        addExpenseButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        add(addExpenseButton);

        tableModel = new DefaultTableModel(new String[]{"Expense Name", "Amount (BDT)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // disable editing for now
            }
        };
        expensesTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(expensesTable);
        scrollPane.setBounds(30, 180, 630, 250);
        add(scrollPane);

        // Listeners
        monthCombo.addActionListener(e -> {
            loadTotalBudget();
            loadExpenses();
        });

        yearCombo.addActionListener(e -> {
            loadTotalBudget();
            loadExpenses();
        });

        addExpenseButton.addActionListener(e -> addExpense());
    }

    private void loadTotalBudget() {
        int month = monthCombo.getSelectedIndex() + 1;
        int year = (Integer) yearCombo.getSelectedItem();

        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT SUM(budget) AS total FROM category_budgets WHERE month = ? AND year = ?")) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                double totalBudget = 0;
                if (rs.next()) {
                    totalBudget = rs.getDouble("total");
                }
                totalBudgetLabel.setText("Total Budget: " + String.format("%.2f BDT", totalBudget));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading total budget: " + e.getMessage());
        }
    }

    private void loadExpenses() {
        tableModel.setRowCount(0);
        int month = monthCombo.getSelectedIndex() + 1;
        int year = (Integer) yearCombo.getSelectedItem();

        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT expense_name, amount FROM expenses WHERE month = ? AND year = ?")) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                            rs.getString("expense_name"),
                            rs.getDouble("amount")
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading expenses: " + e.getMessage());
        }
    }

    private void addExpense() {
        String expenseName = expenseNameField.getText().trim();
        String amountText = expenseAmountField.getText().trim();

        if (expenseName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an expense name.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount.");
            return;
        }

        int month = monthCombo.getSelectedIndex() + 1;
        int year = (Integer) yearCombo.getSelectedItem();

        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO expenses (expense_name, amount, month, year) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, expenseName);
            ps.setDouble(2, amount);
            ps.setInt(3, month);
            ps.setInt(4, year);
            ps.executeUpdate();

            // Refresh table
            loadExpenses();

            // Clear input fields
            expenseNameField.setText("");
            expenseAmountField.setText("");

            JOptionPane.showMessageDialog(this, "Expense added successfully!");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding expense: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MANAGE_EXPENSE_WINDOW::new);
    }
}
