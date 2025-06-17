
package gui;

import controller.database;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class ADD_TRANSACTIONS_WINDOW extends JFrame {
    private JComboBox<String> typeCombo, categoryCombo;
    private JTextField amountField, quantityField, pricePerUnitField, dateField;
    private JTextArea notesArea;
    private JLabel currencyLabel, quantityLabel, priceLabel;
    private final String userName, userPost;

    public ADD_TRANSACTIONS_WINDOW(String name, String post) {
        this.userName = name;
        this.userPost = post;

        setTitle("Add Transaction");
        setSize(500, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(new Color(230, 245, 255));
        setLayout(null);

        initComponents();
        loadCurrency();
        loadCategories();
        setVisible(true);
    }

    private void initComponents() {
        JLabel title = new JLabel("Add Transaction");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBounds(150, 20, 250, 30);
        add(title);

        // Type
        JLabel typeLabel = new JLabel("Type:");
        typeLabel.setBounds(50, 70, 100, 25);
        add(typeLabel);

        typeCombo = new JComboBox<>(new String[]{"Income", "Expense"});
        typeCombo.setBounds(150, 70, 200, 25);
        typeCombo.addActionListener(e -> toggleIncomeFields());
        add(typeCombo);

        // Category
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setBounds(50, 110, 100, 25);
        add(categoryLabel);

        categoryCombo = new JComboBox<>();
        categoryCombo.setBounds(150, 110, 200, 25);
        add(categoryCombo);

        // Amount
        JLabel amountLabel = new JLabel("Amount:");
        amountLabel.setBounds(50, 150, 100, 25);
        add(amountLabel);

        amountField = new JTextField();
        amountField.setBounds(150, 150, 200, 25);
        amountField.setEditable(true); // auto-calculated for Income
        add(amountField);

        currencyLabel = new JLabel("");
        currencyLabel.setBounds(360, 150, 100, 25);
        add(currencyLabel);

        // Quantity (Income only)
        quantityLabel = new JLabel("Quantity:");
        quantityLabel.setBounds(50, 190, 100, 25);
        add(quantityLabel);

        quantityField = new JTextField();
        quantityField.setBounds(150, 190, 200, 25);
        quantityField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                updateAmount();
            }
        });
        add(quantityField);

        // Price per unit (Income only)
        priceLabel = new JLabel("Price/unit:");
        priceLabel.setBounds(50, 230, 100, 25);
        add(priceLabel);

        pricePerUnitField = new JTextField();
        pricePerUnitField.setBounds(150, 230, 200, 25);
        pricePerUnitField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                updateAmount();
            }
        });
        add(pricePerUnitField);

        // Date
        JLabel dateLabel = new JLabel("Date (YYYY-MM-DD):");
        dateLabel.setBounds(50, 270, 150, 25);
        add(dateLabel);

        dateField = new JTextField(LocalDate.now().toString());
        dateField.setBounds(200, 270, 150, 25);
        add(dateField);

        // Notes
        JLabel notesLabel = new JLabel("Notes:");
        notesLabel.setBounds(50, 310, 100, 25);
        add(notesLabel);

        notesArea = new JTextArea();
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(notesArea);
        scrollPane.setBounds(150, 310, 250, 70);
        add(scrollPane);

        // Add button
        JButton addButton = new JButton("Add Transaction");
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        addButton.setBackground(new Color(52, 152, 219));
        addButton.setForeground(Color.WHITE);
        addButton.setBounds(150, 400, 200, 40);
        addButton.setFocusPainted(false);
        addButton.addActionListener(e -> addTransaction());
        add(addButton);

        toggleIncomeFields(); // initial visibility
    }

    private void toggleIncomeFields() {
        boolean isIncome = "Income".equals(typeCombo.getSelectedItem());
        quantityLabel.setVisible(isIncome);
        amountField.setEditable(!isIncome);
        quantityField.setVisible(isIncome);
        priceLabel.setVisible(isIncome);
        pricePerUnitField.setVisible(isIncome);
        loadCategories();
    }

    private void updateAmount() {
        try {
            int qty = Integer.parseInt(quantityField.getText().trim());
            double price = Double.parseDouble(pricePerUnitField.getText().trim());
            amountField.setText(String.valueOf(qty * price));
        } catch (Exception e) {
            amountField.setText("");
        }
    }

    private void loadCurrency() {
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT currency FROM settings ORDER BY id DESC LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                currencyLabel.setText(rs.getString("currency"));
            }
        } catch (Exception e) {
            currencyLabel.setText("");
        }
    }

    private void loadCategories() {
        categoryCombo.removeAllItems();
        String type = (String) typeCombo.getSelectedItem();
        if (type == null) return;
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT name FROM categories WHERE type = ?")) {
            ps.setString(1, type);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categoryCombo.addItem(rs.getString("name"));
                }
            }
        } catch (Exception e) {
            categoryCombo.addItem("-- Error Loading --");
        }
    }

    private void addTransaction() {
        String type = (String) typeCombo.getSelectedItem();
        String category = (String) categoryCombo.getSelectedItem();
        String amountText = amountField.getText().trim();
        String dateText = dateField.getText().trim();
        String notes = notesArea.getText().trim();
        String currency = currencyLabel.getText().trim();

        if (type == null || category == null || amountText.isEmpty() || dateText.isEmpty() || currency.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields.");
            return;
        }

        double amount;
        Date sqlDate;
        int quantity = 0;

        try {
            amount = Double.parseDouble(amountText);
            sqlDate = Date.valueOf(dateText);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount or date.");
            return;
        }

        if ("Income".equals(type)) {
            try {
                quantity = Integer.parseInt(quantityField.getText().trim());
                double unitPrice = Double.parseDouble(pricePerUnitField.getText().trim());
                if (quantity <= 0 || unitPrice <= 0) throw new NumberFormatException();

                try (Connection conn = database.getConnection()) {
                    PreparedStatement psCheck = conn.prepareStatement(
                            "SELECT quantity FROM inventory WHERE product_name = ?");
                    psCheck.setString(1, category);
                    ResultSet rs = psCheck.executeQuery();

                    if (rs.next()) {
                        int stock = rs.getInt("quantity");
                        if (stock < quantity) {
                            JOptionPane.showMessageDialog(this, "Not enough stock in inventory.");
                            return;
                        }
                        PreparedStatement psUpdate = conn.prepareStatement(
                                "UPDATE inventory SET quantity = quantity - ? WHERE product_name = ?");
                        psUpdate.setInt(1, quantity);
                        psUpdate.setString(2, category);
                        psUpdate.executeUpdate();
                    } else {
                        JOptionPane.showMessageDialog(this, "Item not found in inventory.");
                        return;
                    }
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid quantity or unit price.");
                return;
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
                return;
            }
        }

        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO transactions (type, category, amount, currency, date, notes, name, status, quantity) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, type);
            ps.setString(2, category);
            ps.setDouble(3, amount);
            ps.setString(4, currency);
            ps.setDate(5, sqlDate);
            ps.setString(6, notes);
            ps.setString(7, userName);
            ps.setString(8, userPost);
            ps.setInt(9, quantity);

            if (ps.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, "Transaction added successfully.");
                amountField.setText("");
                notesArea.setText("");
                quantityField.setText("");
                pricePerUnitField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add transaction.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding transaction: " + e.getMessage());
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ADD_TRANSACTIONS_WINDOW("Admin", "Manager"));
    }

}
