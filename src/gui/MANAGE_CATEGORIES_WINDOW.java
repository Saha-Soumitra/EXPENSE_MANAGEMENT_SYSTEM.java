package gui;

import controller.database;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class MANAGE_CATEGORIES_WINDOW extends JFrame {

    private JTextField categoryNameField;
    private JTable categoryTable;
    private DefaultTableModel categoryTableModel;

    private JTextField companyNameField;
    private JTextField companyPriceField;
    private JTable companyTable;
    private DefaultTableModel companyTableModel;

    private String selectedCategoryName = null;

    public MANAGE_CATEGORIES_WINDOW() {
        setTitle("Manage Categories and Companies");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(new Color(200, 230, 255));

        initComponents();
        loadCategories();
        setVisible(true);
    }

    private void initComponents() {
        JLabel categoryTitleLabel = new JLabel("Manage Categories");
        categoryTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        categoryTitleLabel.setBounds(30, 10, 250, 30);
        add(categoryTitleLabel);

        JLabel categoryNameLabel = new JLabel("Category Name:");
        categoryNameLabel.setBounds(30, 40, 120, 25);
        add(categoryNameLabel);

        categoryNameField = new JTextField();
        categoryNameField.setBounds(150, 40, 170, 25);
        add(categoryNameField);

        JButton addCategoryBtn = new JButton("Add Category");
        addCategoryBtn.setBounds(150, 75, 120, 30);
        addCategoryBtn.setBackground(new Color(46, 204, 113));
        addCategoryBtn.setForeground(Color.WHITE);
        add(addCategoryBtn);

        JButton deleteCategoryBtn = new JButton("Delete Category");
        deleteCategoryBtn.setBounds(280, 75, 140, 30);
        deleteCategoryBtn.setBackground(new Color(231, 76, 60));
        deleteCategoryBtn.setForeground(Color.WHITE);
        add(deleteCategoryBtn);

        categoryTableModel = new DefaultTableModel(new String[]{"Name"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        categoryTable = new JTable(categoryTableModel);
        JScrollPane categoryScrollPane = new JScrollPane(categoryTable);
        categoryScrollPane.setBounds(30, 120, 390, 300);
        add(categoryScrollPane);

        JLabel companyTitleLabel = new JLabel("Companies for Selected Category");
        companyTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        companyTitleLabel.setBounds(450, 10, 350, 30);
        add(companyTitleLabel);

        JLabel companyNameLabel = new JLabel("Company Name:");
        companyNameLabel.setBounds(450, 40, 120, 25);
        add(companyNameLabel);

        companyNameField = new JTextField();
        companyNameField.setBounds(570, 40, 170, 25);
        add(companyNameField);

        JLabel companyPriceLabel = new JLabel("Price:");
        companyPriceLabel.setBounds(450, 70, 120, 25);
        add(companyPriceLabel);

        companyPriceField = new JTextField();
        companyPriceField.setBounds(570, 70, 170, 25);
        add(companyPriceField);

        JButton addCompanyBtn = new JButton("Add Company");
        addCompanyBtn.setBounds(570, 105, 120, 30);
        addCompanyBtn.setBackground(new Color(46, 204, 113));
        addCompanyBtn.setForeground(Color.WHITE);
        add(addCompanyBtn);

        JButton deleteCompanyBtn = new JButton("Delete Company");
        deleteCompanyBtn.setBounds(700, 105, 140, 30);
        deleteCompanyBtn.setBackground(new Color(231, 76, 60));
        deleteCompanyBtn.setForeground(Color.WHITE);
        add(deleteCompanyBtn);

        companyTableModel = new DefaultTableModel(new String[]{"Company Name", "Price"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        companyTable = new JTable(companyTableModel);
        JScrollPane companyScrollPane = new JScrollPane(companyTable);
        companyScrollPane.setBounds(450, 150, 390, 270);
        add(companyScrollPane);

        addCategoryBtn.addActionListener(e -> addCategory());
        deleteCategoryBtn.addActionListener(e -> deleteSelectedCategory());
        addCompanyBtn.addActionListener(e -> addCompany());
        deleteCompanyBtn.addActionListener(e -> deleteSelectedCompany());

        categoryTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = categoryTable.getSelectedRow();
                if (selectedRow >= 0) {
                    selectedCategoryName = (String) categoryTableModel.getValueAt(selectedRow, 0);
                    loadCompanies(selectedCategoryName);
                } else {
                    selectedCategoryName = null;
                    companyTableModel.setRowCount(0);
                }
            }
        });
    }

    private void loadCategories() {
        categoryTableModel.setRowCount(0);
        try (Connection conn = database.getConnection()) {
            String sql = "SELECT name FROM categories ORDER BY name DESC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                categoryTableModel.addRow(new Object[]{rs.getString("name").toUpperCase()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading categories: " + ex.getMessage());
        }
        selectedCategoryName = null;
        companyTableModel.setRowCount(0);
    }

    private void addCategory() {
        String rawName = categoryNameField.getText().trim().toUpperCase(); // Normalize case
        if (rawName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a category name.");
            return;
        }

        Category newCategory = new Category(rawName);

        try (Connection conn = database.getConnection()) {
            String checkSql = "SELECT COUNT(*) FROM categories WHERE UPPER(name) = ?";
            PreparedStatement checkPs = conn.prepareStatement(checkSql);
            checkPs.setString(1, newCategory.getName());
            ResultSet rs = checkPs.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "This category already exists.");
                return;
            }

            String sql = "INSERT INTO categories (name) VALUES (?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, newCategory.getName());
            int rows = ps.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Category added.");
                categoryNameField.setText("");
                loadCategories();
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

        String name = (String) categoryTableModel.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Delete this category and all related companies?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = database.getConnection()) {
            PreparedStatement delCompPs = conn.prepareStatement("DELETE FROM companies WHERE category_name = ?");
            delCompPs.setString(1, name);
            delCompPs.executeUpdate();

            PreparedStatement ps = conn.prepareStatement("DELETE FROM categories WHERE name = ?");
            ps.setString(1, name);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Category and related companies deleted.");
                loadCategories();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete category.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void loadCompanies(String categoryName) {
        companyTableModel.setRowCount(0);
        if (categoryName == null) return;

        try (Connection conn = database.getConnection()) {
            String sql = "SELECT company_name, price FROM companies WHERE category_name = ? ORDER BY company_name";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, categoryName);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String name = rs.getString("company_name").toUpperCase();
                double price = rs.getDouble("price");
                companyTableModel.addRow(new Object[]{name, String.format("%.2f BDT", price)});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading companies: " + ex.getMessage());
        }
    }

    private void addCompany() {
        if (selectedCategoryName == null) {
            JOptionPane.showMessageDialog(this, "Select a category first.");
            return;
        }

        String companyName = companyNameField.getText().trim().toUpperCase();
        String priceText = companyPriceField.getText().trim();

        if (companyName.isEmpty() || priceText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both company name and price.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
            if (price < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid price.");
            return;
        }

        try (Connection conn = database.getConnection()) {
            String checkSql = "SELECT COUNT(*) FROM companies WHERE UPPER(company_name) = ? AND category_name = ?";
            PreparedStatement checkPs = conn.prepareStatement(checkSql);
            checkPs.setString(1, companyName);
            checkPs.setString(2, selectedCategoryName);
            ResultSet rs = checkPs.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "This company already exists in the selected category.");
                return;
            }

            String sql = "INSERT INTO companies (category_name, company_name, price) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, selectedCategoryName);
            ps.setString(2, companyName);
            ps.setDouble(3, price);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Company added.");
                companyNameField.setText("");
                companyPriceField.setText("");
                loadCompanies(selectedCategoryName);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add company.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void deleteSelectedCompany() {
        int row = companyTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a company to delete.");
            return;
        }

        String companyName = (String) companyTableModel.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Delete this company?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = database.getConnection()) {
            String sql = "DELETE FROM companies WHERE company_name = ? AND category_name = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, companyName);
            ps.setString(2, selectedCategoryName);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Company deleted.");
                loadCompanies(selectedCategoryName);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete company.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // ✅ Inner class that inherits PRODUCT
    private class Category extends PRODUCT {
        public Category(String name) {
            super(name, "Category");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MANAGE_CATEGORIES_WINDOW::new);
    }
}
