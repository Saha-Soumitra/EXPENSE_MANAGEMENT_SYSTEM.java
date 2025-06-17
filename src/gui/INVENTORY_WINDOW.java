package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class INVENTORY_WINDOW extends JFrame {

    private JTable inventoryTable;
    private DefaultTableModel tableModel;

    public INVENTORY_WINDOW() {
        setTitle("Inventory Manager");
        setSize(850, 500);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Top panel
        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        topPanel.setBackground(new Color(240, 248, 255));

        JLabel title = new JLabel("Inventory Management System", SwingConstants.CENTER);
        title.setFont(new Font("Verdana", Font.BOLD, 24));
        title.setForeground(new Color(52, 152, 219));
        topPanel.add(title);

        // Button panel
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.setBackground(new Color(245, 255, 250));

        JButton btnChangeQty = new JButton("Change Product Quantity");
        styleButton(btnChangeQty, new Color(52, 152, 219));
        inputPanel.add(btnChangeQty);

        topPanel.add(inputPanel);
        add(topPanel, BorderLayout.NORTH);

        // Table setup
        tableModel = new DefaultTableModel(new String[]{"Product Name", "Quantity"}, 0);
        inventoryTable = new JTable(tableModel);
        inventoryTable.setRowHeight(25);
        inventoryTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        add(scrollPane, BorderLayout.CENTER);

        // Listener
        btnChangeQty.addActionListener(e -> changeProductQuantity());

        loadInventory();
        setVisible(true);
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setFocusPainted(false);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
    }

    private void changeProductQuantity() {
        String nameInput = JOptionPane.showInputDialog(this, "Enter Product Name:");
        if (nameInput == null || nameInput.trim().isEmpty()) return;

        String name = nameInput.trim().toUpperCase();

        try (Connection conn = controller.database.getConnection()) {
            String selectQuery = "SELECT quantity FROM inventory WHERE UPPER(product_name) = ?";
            PreparedStatement ps = conn.prepareStatement(selectQuery);
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int currentQty = rs.getInt("quantity");

                String qtyStr = JOptionPane.showInputDialog(this, "Enter Quantity to Add:");
                if (qtyStr == null || qtyStr.trim().isEmpty()) return;

                try {
                    int qtyToAdd = Integer.parseInt(qtyStr.trim());
                    int newQty = currentQty + qtyToAdd;

                    String updateQuery = "UPDATE inventory SET quantity = ? WHERE UPPER(product_name) = ?";
                    PreparedStatement updatePs = conn.prepareStatement(updateQuery);
                    updatePs.setInt(1, newQty);
                    updatePs.setString(2, name);
                    updatePs.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Quantity updated.");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Quantity must be a number.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Product not found.");
            }

            loadInventory();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void loadInventory() {
        tableModel.setRowCount(0);
        try (Connection conn = controller.database.getConnection()) {
            String query = "SELECT product_name, quantity FROM inventory";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("product_name"));
                row.add(rs.getInt("quantity"));
                tableModel.addRow(row);
            }

            inventoryTable.getColumnModel().getColumn(1).setCellRenderer(new TableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                               boolean hasFocus, int row, int column) {
                    JLabel label = new JLabel(value.toString());
                    label.setOpaque(true);
                    label.setHorizontalAlignment(JLabel.CENTER);

                    int quantity = (int) value;
                    if (quantity < 100) {
                        label.setForeground(Color.RED);
                    } else {
                        label.setForeground(Color.BLACK);
                    }

                    if (isSelected) {
                        label.setBackground(table.getSelectionBackground());
                    } else {
                        label.setBackground(Color.WHITE);
                    }

                    return label;
                }
            });

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading inventory: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(INVENTORY_WINDOW::new);
    }
}
