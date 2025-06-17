package gui;

import controller.database;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.math.BigDecimal;

public class SETTINGS_WINDOW extends JFrame {

    private JComboBox<String> currencyCombo;
    private JTextField budgetField;

    public SETTINGS_WINDOW() {
        setTitle("App Settings");
        setSize(400, 300);
        setLayout(null);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initComponents();
        loadSettings();
        setVisible(true);
    }

    private void initComponents() {
        JLabel title = new JLabel("Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBounds(140, 20, 200, 30);
        add(title);

        JLabel currencyLabel = new JLabel("Currency:");
        currencyLabel.setBounds(30, 80, 100, 25);
        add(currencyLabel);

        currencyCombo = new JComboBox<>(new String[]{"USD", "EUR", "GBP", "BDT", "INR"});
        currencyCombo.setBounds(140, 80, 200, 25);
        add(currencyCombo);

        JLabel budgetLabel = new JLabel("Monthly Budget:");
        budgetLabel.setBounds(30, 130, 120, 25);
        add(budgetLabel);

        budgetField = new JTextField();
        budgetField.setBounds(140, 130, 200, 25);
        add(budgetField);

        JButton saveButton = new JButton("Save");
        saveButton.setBounds(140, 190, 120, 35);
        saveButton.setBackground(new Color(46, 204, 113));
        saveButton.setForeground(Color.WHITE);
        add(saveButton);

        saveButton.addActionListener(e -> saveSettings());
    }

    private void loadSettings() {
        try (Connection conn = database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT currency, budget FROM settings ORDER BY id DESC LIMIT 1")) {
            if (rs.next()) {
                currencyCombo.setSelectedItem(rs.getString("currency"));
                budgetField.setText(rs.getBigDecimal("budget").toString());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading settings: " + ex.getMessage());
        }
    }

    private void saveSettings() {
        String currency = (String) currencyCombo.getSelectedItem();
        String budgetText = budgetField.getText().trim();

        if (currency == null || budgetText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fill both currency and budget.");
            return;
        }

        BigDecimal budget;
        try {
            budget = new BigDecimal(budgetText);
            if (budget.compareTo(BigDecimal.ZERO) < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid budget. Enter a positive number.");
            return;
        }

        try (Connection conn = database.getConnection()) {
            conn.setAutoCommit(false);
            conn.createStatement().executeUpdate("DELETE FROM settings");
            PreparedStatement ps = conn.prepareStatement("INSERT INTO settings (currency, budget) VALUES (?, ?)");
            ps.setString(1, currency);
            ps.setBigDecimal(2, budget);
            ps.executeUpdate();
            conn.commit();

            JOptionPane.showMessageDialog(this, "Settings updated.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving settings: " + ex.getMessage());
        }
    }
}
