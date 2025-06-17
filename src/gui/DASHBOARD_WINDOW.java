package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.*;
import controller.database;

public class DASHBOARD_WINDOW extends JFrame {
    private JComboBox<String> yearBox, monthBox, dayBox, viewTypeBox;
    private JLabel incomeLabel, expenseLabel;
    private JPanel chartPanel;
    private JTable employeeTable;
    private DefaultTableModel employeeModel;

    public DASHBOARD_WINDOW() {
        setTitle("📊 Dashboard");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(200, 230, 255)); // Light blue background
        setFont(new Font("Segoe UI", Font.PLAIN, 14));

        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("ComboBox.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Table.font", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 14));

        // Top panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBackground(new Color(180, 210, 255));
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        viewTypeBox = new JComboBox<>(new String[]{"Day", "Month", "Year"});
        yearBox = new JComboBox<>(getYears());
        monthBox = new JComboBox<>(getMonths());
        dayBox = new JComboBox<>(getDays());

        java.time.LocalDate now = java.time.LocalDate.now();
        yearBox.setSelectedItem(String.valueOf(now.getYear()));
        monthBox.setSelectedItem(String.format("%02d", now.getMonthValue()));
        dayBox.setSelectedItem(String.format("%02d", now.getDayOfMonth()));

        topPanel.add(label("View:"));
        topPanel.add(viewTypeBox);
        topPanel.add(label("Year:"));
        topPanel.add(yearBox);
        topPanel.add(label("Month:"));
        topPanel.add(monthBox);
        topPanel.add(label("Day:"));
        topPanel.add(dayBox);

        incomeLabel = label("Income: 0", Color.GREEN.darker());
        expenseLabel = label("Expense: 0", Color.RED);

        topPanel.add(incomeLabel);
        topPanel.add(expenseLabel);
        add(topPanel, BorderLayout.NORTH);

        // Chart panel
        chartPanel = new JPanel();
        chartPanel.setBackground(new Color(220, 240, 255));
        add(chartPanel, BorderLayout.CENTER);

        // Employee panel
        JPanel employeePanel = new JPanel(new BorderLayout(10, 10));
        employeePanel.setBackground(new Color(200, 230, 255));
        employeePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        employeePanel.setPreferredSize(new Dimension(300, 0));

        employeeModel = new DefaultTableModel(new String[]{"👤 Employee Username"}, 0);
        employeeTable = new JTable(employeeModel);
        styleTable(employeeTable);

        JLabel empTitle = new JLabel("Employees");
        empTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        empTitle.setHorizontalAlignment(SwingConstants.CENTER);

        employeePanel.add(empTitle, BorderLayout.NORTH);
        employeePanel.add(new JScrollPane(employeeTable), BorderLayout.CENTER);
        add(employeePanel, BorderLayout.EAST);

        // Event listeners
        ActionListener filterListener = e -> calculateAndDisplay();
        viewTypeBox.addActionListener(filterListener);
        yearBox.addActionListener(filterListener);
        monthBox.addActionListener(filterListener);
        dayBox.addActionListener(filterListener);

        // Initial load
        calculateAndDisplay();
        loadEmployees();

        setVisible(true);
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.DARK_GRAY);
        return label;
    }

    private JLabel label(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        return label;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(180, 220, 255));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(Object.class, center);

        table.getTableHeader().setBackground(new Color(200, 200, 200));
        table.getTableHeader().setForeground(Color.BLACK);
        table.getTableHeader().setReorderingAllowed(false);
    }

    private String[] getYears() {
        return new String[]{"2025", "2024", "2023"};
    }

    private String[] getMonths() {
        return new String[]{"01", "02", "03", "04", "05", "06",
                "07", "08", "09", "10", "11", "12"};
    }

    private String[] getDays() {
        String[] days = new String[31];
        for (int i = 1; i <= 31; i++) {
            days[i - 1] = String.format("%02d", i);
        }
        return days;
    }

    private void calculateAndDisplay() {
        String viewType = (String) viewTypeBox.getSelectedItem();
        String year = (String) yearBox.getSelectedItem();
        String month = (String) monthBox.getSelectedItem();
        String day = (String) dayBox.getSelectedItem();

        String condition = switch (viewType) {
            case "Day" -> year + "-" + month + "-" + day;
            case "Month" -> year + "-" + month;
            case "Year" -> year;
            default -> "";
        };

        double totalIncome = 0, totalExpense = 0, budget = 0;

        try (Connection con = database.getConnection()) {
            String sql = switch (viewType) {
                case "Day" -> "SELECT type, amount FROM transactions WHERE date = ?";
                default -> "SELECT type, amount FROM transactions WHERE date LIKE ?";
            };

            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, viewType.equals("Day") ? condition : condition + "%");
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String type = rs.getString("type");
                    double amount = rs.getDouble("amount");
                    if ("Income".equalsIgnoreCase(type)) totalIncome += amount;
                    else if ("Expense".equalsIgnoreCase(type)) totalExpense += amount;
                }
            }

            try (PreparedStatement budgetStmt = con.prepareStatement("SELECT budget FROM settings LIMIT 1");
                 ResultSet budgetRs = budgetStmt.executeQuery()) {
                if (budgetRs.next()) budget = budgetRs.getDouble("budget");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        incomeLabel.setText("Income: " + totalIncome);
        expenseLabel.setText("Expense: " + totalExpense);
        displayChart(totalIncome, totalExpense, budget);
    }

    private void displayChart(double income, double expense, double budget) {
        chartPanel.removeAll();

        JPanel barChart = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                int w = getWidth(), h = getHeight();

                double max = Math.max(income, Math.max(expense, budget));
                int barWidth = 80, spacing = 70;
                int baseX = 70, baseY = h - 80, chartHeight = h - 150;

                int iBar = (int) ((income / max) * chartHeight);
                int eBar = (int) ((expense / max) * chartHeight);
                int bBar = (int) ((budget / max) * chartHeight);

                drawBar(g2, "Income", baseX, iBar, baseY, barWidth, Color.BLUE, income);
                drawBar(g2, "Expense", baseX + spacing + barWidth, eBar, baseY, barWidth, Color.RED, expense);
                drawBar(g2, "Budget", baseX + 2 * (spacing + barWidth), bBar, baseY, barWidth, Color.GREEN.darker(), budget);
            }

            void drawBar(Graphics2D g2, String label, int x, int height, int baseY, int width, Color color, double value) {
                g2.setColor(color);
                g2.fillRoundRect(x, baseY - height, width, height, 10, 10);
                g2.setColor(Color.BLACK);
                g2.drawString(label, x + 10, baseY + 20);
                g2.drawString(String.format("%.0f", value), x + 10, baseY - height - 10);
            }
        };

        chartPanel.setLayout(new BorderLayout());
        chartPanel.add(barChart, BorderLayout.CENTER);
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private void loadEmployees() {
        employeeModel.setRowCount(0);
        try (Connection con = database.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT username FROM employee")) {
            while (rs.next()) {
                employeeModel.addRow(new Object[]{rs.getString("username")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DASHBOARD_WINDOW::new);
    }
}
