package gui;

import controller.database;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Calendar;

public class INVENTORY_WINDOW extends JFrame {

    private JTable categoryTable;
    private DefaultTableModel categoryModel;

    public INVENTORY_WINDOW() {
        setTitle("Inventory Dashboard");
        setSize(650, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Table model
        categoryModel = new DefaultTableModel(new String[]{"Category", "Quantity"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        // Table UI
        categoryTable = new JTable(categoryModel);
        categoryTable.setRowHeight(28);
        categoryTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        categoryTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        categoryTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public void setValue(Object val) {
                try {
                    int qty = Integer.parseInt(val.toString());
                    setText(String.valueOf(qty));
                    setForeground(qty < 100 ? Color.RED : Color.BLACK);
                } catch (Exception e) {
                    setText("N/A");
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(categoryTable);
        add(scrollPane, BorderLayout.CENTER);

        JLabel infoLabel = new JLabel("Double-click a category to manage. Quantities update automatically.");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        infoLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        add(infoLabel, BorderLayout.SOUTH);

        categoryTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && categoryTable.getSelectedRow() != -1) {
                    String cat = categoryModel.getValueAt(categoryTable.getSelectedRow(), 0).toString();
                    showCompanyDetails(cat);
                }
            }
        });

        addWindowFocusListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
                loadCategoryInventory();
            }
        });

        loadCategoryInventory();
        setVisible(true);
    }

    private void loadCategoryInventory() {
        categoryModel.setRowCount(0);
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT name, quantity FROM categories ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categoryModel.addRow(new Object[]{
                        rs.getString("name"),
                        rs.getInt("quantity")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading categories: " + e.getMessage());
        }
    }

    private void updateCategoryQuantity(String cat, int qty) {
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE categories SET quantity = ? WHERE name = ?")) {
            ps.setInt(1, qty);
            ps.setString(2, cat);
            ps.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating category qty: " + e.getMessage());
        }
    }

    private void showCompanyDetails(String categoryName) {
        JFrame detail = new JFrame("Manage Inventory - " + categoryName);
        detail.setSize(600, 420);
        detail.setLocationRelativeTo(this);
        detail.setLayout(new BorderLayout());

        DefaultTableModel compModel = new DefaultTableModel(
                new String[]{"Company", "Price (BDT)", "Quantity"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable compTable = new JTable(compModel);
        compTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        compTable.setRowHeight(26);
        compTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT company_name, price, quantity FROM companies WHERE category_name = ? ORDER BY company_name")) {
            ps.setString(1, categoryName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    compModel.addRow(new Object[]{
                            rs.getString("company_name"),
                            rs.getDouble("price"),
                            rs.getInt("quantity")
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading companies: " + e.getMessage());
        }

        JButton buyButton = new JButton("Buy Selected");
        buyButton.setBackground(new Color(46, 204, 113));
        buyButton.setForeground(Color.WHITE);
        buyButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        buyButton.addActionListener(e -> {
            int row = compTable.getSelectedRow();
            if (row < 0) return;

            String company = compModel.getValueAt(row, 0).toString();
            double price = (double) compModel.getValueAt(row, 1);
            int currentQty = (int) compModel.getValueAt(row, 2);

            try (Connection conn = database.getConnection()) {
                conn.setAutoCommit(false);

                String input = JOptionPane.showInputDialog(detail,
                        String.format("Enter quantity to buy (BDT %.2f each):", price));
                if (input == null) return;

                int qtyToBuy;
                try {
                    qtyToBuy = Integer.parseInt(input);
                    if (qtyToBuy <= 0) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(detail, "Invalid quantity.");
                    return;
                }

                double cost = qtyToBuy * price;

                // Update company quantity
                int newQty = currentQty + qtyToBuy;
                try (PreparedStatement psUpdateCompany = conn.prepareStatement(
                        "UPDATE companies SET quantity = ? WHERE category_name = ? AND company_name = ?")) {
                    psUpdateCompany.setInt(1, newQty);
                    psUpdateCompany.setString(2, categoryName);
                    psUpdateCompany.setString(3, company);
                    psUpdateCompany.executeUpdate();
                }

                // Update category quantity
                int totalCatQty = 0;
                try (PreparedStatement psTotal = conn.prepareStatement(
                        "SELECT SUM(quantity) FROM companies WHERE category_name = ?")) {
                    psTotal.setString(1, categoryName);
                    try (ResultSet rs = psTotal.executeQuery()) {
                        if (rs.next()) totalCatQty = rs.getInt(1);
                    }
                }

                updateCategoryQuantity(categoryName, totalCatQty);
                conn.commit();

                compModel.setValueAt(newQty, row, 2);
                loadCategoryInventory();

                JOptionPane.showMessageDialog(detail,
                        "✅ Purchase successful!\nCost: BDT " + cost);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(detail, "Error during purchase: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        bottomPanel.add(buyButton);

        detail.add(new JScrollPane(compTable), BorderLayout.CENTER);
        detail.add(bottomPanel, BorderLayout.SOUTH);
        detail.setVisible(true);
    }

    private String getMonthName(int month) {
        return new java.text.DateFormatSymbols().getMonths()[month - 1];
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(INVENTORY_WINDOW::new);
    }
}
