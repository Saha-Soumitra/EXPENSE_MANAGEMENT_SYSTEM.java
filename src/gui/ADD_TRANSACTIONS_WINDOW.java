package gui;

import controller.database;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class ADD_TRANSACTIONS_WINDOW extends JFrame {
    private JComboBox<String> categoryCombo, companyCombo;
    private JTextField amountField, quantityField, pricePerUnitField, dateField;
    private JTextArea notesArea;
    private JLabel currencyLabel, quantityLabel, priceLabel;
    private final String userName, userPost;
    private double sellingPricePerUnit = 0.0;  // selling price fetched from companies table

    public ADD_TRANSACTIONS_WINDOW(String name, String post) {
        this.userName = name;
        this.userPost = post;

        setTitle("Add Transaction");
        setSize(550, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(new Color(230, 245, 255));
        setLayout(null);

        initComponents();
        loadCurrency();
        loadCategories();
        // companyCombo is populated dynamically after category selection
        setVisible(true);
    }

    private void initComponents() {
        JLabel title = new JLabel("Add Transaction");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBounds(180, 20, 250, 30);
        add(title);

        // Category label & combo
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setBounds(50, 70, 100, 25);
        add(categoryLabel);

        categoryCombo = new JComboBox<>();
        categoryCombo.setBounds(150, 70, 200, 25);
        categoryCombo.addActionListener(e -> {
            loadCompaniesForCategory();
            loadSellingPrice();
        });
        add(categoryCombo);

        // Company label & combo
        JLabel companyLabel = new JLabel("Company:");
        companyLabel.setBounds(50, 110, 100, 25);
        add(companyLabel);

        companyCombo = new JComboBox<>();
        companyCombo.setBounds(150, 110, 200, 25);
        companyCombo.addActionListener(e -> loadSellingPrice());
        add(companyCombo);

        // Quantity
        quantityLabel = new JLabel("Quantity:");
        quantityLabel.setBounds(50, 150, 100, 25);
        add(quantityLabel);

        quantityField = new JTextField();
        quantityField.setBounds(150, 150, 200, 25);
        quantityField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                updateAmount();
            }
        });
        add(quantityField);

        // Price/unit (disabled, fetched from companies)
        priceLabel = new JLabel("Price/unit:");
        priceLabel.setBounds(50, 190, 100, 25);
        add(priceLabel);

        pricePerUnitField = new JTextField();
        pricePerUnitField.setBounds(150, 190, 200, 25);
        pricePerUnitField.setEditable(false); // user cannot edit
        add(pricePerUnitField);

        // Amount (calculated)
        JLabel amountLabel = new JLabel("Amount:");
        amountLabel.setBounds(50, 230, 100, 25);
        add(amountLabel);

        amountField = new JTextField();
        amountField.setBounds(150, 230, 200, 25);
        amountField.setEditable(false);
        add(amountField);

        currencyLabel = new JLabel("");
        currencyLabel.setBounds(360, 230, 100, 25);
        add(currencyLabel);

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
        scrollPane.setBounds(150, 310, 300, 80);
        add(scrollPane);

        // Add button
        JButton addButton = new JButton("Add Transaction");
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        addButton.setBackground(new Color(52, 152, 219));
        addButton.setForeground(Color.WHITE);
        addButton.setBounds(180, 410, 200, 40);
        addButton.setFocusPainted(false);
        addButton.addActionListener(e -> addTransaction());
        add(addButton);
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
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT DISTINCT name FROM categories ORDER BY name ASC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categoryCombo.addItem(rs.getString("name"));
            }
        } catch (Exception e) {
            categoryCombo.addItem("-- Error Loading Categories --");
        }
    }

    private void loadCompaniesForCategory() {
        companyCombo.removeAllItems();
        String selectedCategory = (String) categoryCombo.getSelectedItem();
        if (selectedCategory == null) return;

        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT DISTINCT company_name FROM companies WHERE category_name = ? ORDER BY company_name ASC")) {
            ps.setString(1, selectedCategory);
            try (ResultSet rs = ps.executeQuery()) {
                boolean hasCompany = false;
                while (rs.next()) {
                    companyCombo.addItem(rs.getString("company_name"));
                    hasCompany = true;
                }
                if (!hasCompany) {
                    companyCombo.addItem("-- No Companies Found --");
                }
            }
        } catch (Exception e) {
            companyCombo.addItem("-- Error Loading Companies --");
        }
    }

    private void loadSellingPrice() {
        String selectedCategory = (String) categoryCombo.getSelectedItem();
        String selectedCompany = (String) companyCombo.getSelectedItem();
        if (selectedCategory == null || selectedCompany == null
                || selectedCompany.startsWith("--")) {
            sellingPricePerUnit = 0.0;
            pricePerUnitField.setText("");
            updateAmount();
            return;
        }

        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT selling_price FROM companies WHERE category_name = ? AND company_name = ? LIMIT 1")) {
            ps.setString(1, selectedCategory);
            ps.setString(2, selectedCompany);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    sellingPricePerUnit = rs.getDouble("selling_price");
                    pricePerUnitField.setText(String.format("%.2f", sellingPricePerUnit));
                } else {
                    sellingPricePerUnit = 0.0;
                    pricePerUnitField.setText("");
                }
                updateAmount();
            }
        } catch (Exception e) {
            sellingPricePerUnit = 0.0;
            pricePerUnitField.setText("");
            updateAmount();
        }
    }

    private void updateAmount() {
        try {
            int qty = Integer.parseInt(quantityField.getText().trim());
            double price = sellingPricePerUnit;
            amountField.setText(String.format("%.2f", qty * price));
        } catch (Exception e) {
            amountField.setText("");
        }
    }

    private void addTransaction() {
        String category = (String) categoryCombo.getSelectedItem();
        String company = (String) companyCombo.getSelectedItem();
        String amountText = amountField.getText().trim();
        String dateText = dateField.getText().trim();
        String notes = notesArea.getText().trim();
        String currency = currencyLabel.getText().trim();

        if (category == null || company == null || amountText.isEmpty() || dateText.isEmpty() || currency.isEmpty()
                || company.startsWith("--")) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields.");
            return;
        }

        double amount;
        Date sqlDate;
        int quantity = 0;

        try {
            amount = Double.parseDouble(amountText);
            sqlDate = Date.valueOf(dateText);
            quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0 || sellingPricePerUnit <= 0) throw new NumberFormatException();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid quantity, amount or date.");
            return;
        }

        // Inventory check & update
        try (Connection conn = database.getConnection()) {
            PreparedStatement psCheck = conn.prepareStatement(
                    "SELECT quantity FROM inventory WHERE product_name = ? AND company_name = ?");
            psCheck.setString(1, category);
            psCheck.setString(2, company);
            ResultSet rs = psCheck.executeQuery();

            if (rs.next()) {
                int stock = rs.getInt("quantity");
                if (stock < quantity) {
                    JOptionPane.showMessageDialog(this, "Not enough stock in inventory.");
                    return;
                }
                PreparedStatement psUpdate = conn.prepareStatement(
                        "UPDATE inventory SET quantity = quantity - ? WHERE product_name = ? AND company_name = ?");
                psUpdate.setInt(1, quantity);
                psUpdate.setString(2, category);
                psUpdate.setString(3, company);
                psUpdate.executeUpdate();
            } else {
                JOptionPane.showMessageDialog(this, "Item not found in inventory.");
                return;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
            return;
        }

        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO transactions (category, company_name, amount, currency, date, notes, name, status, quantity) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, category);
            ps.setString(2, company);
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
                pricePerUnitField.setText(String.format("%.2f", sellingPricePerUnit));
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
