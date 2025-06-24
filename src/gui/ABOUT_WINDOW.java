package gui;

import javax.swing.*;
import java.awt.*;

public class ABOUT_WINDOW extends JFrame {

    public ABOUT_WINDOW() {
        setTitle("About - Expense Management System");
        setSize(500, 420);
        setLocationRelativeTo(null);  // puts the screen in the center
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(new Color(240, 248, 255));
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Expense Management System", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(25, 25, 112)); // Dark blue

        JTextArea description = new JTextArea();
        description.setText("""
                Welcome to the Expense Management System!

                This application helps users efficiently manage and visualize their income,
                expenses, budgets, and financial reports.

                ✦ Features:
                • Track daily, monthly, and yearly transactions
                • Add, edit, and delete income or expense entries
                • View visual reports using bar charts and summaries
                • Set monthly budgets and monitor spending
                • Inventory and product stock management

                👤 Ideal for:
                • Individuals managing personal finance
                • Small businesses and startups
                • Managers handling employee expenditures

                📞 Contact Number: +880-1234-567890
                📧 Email: support@expensesystem.com
                """);
        description.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        description.setForeground(Color.DARK_GRAY);
        description.setBackground(new Color(240, 248, 255));
        description.setEditable(false);
        description.setWrapStyleWord(true);
        description.setLineWrap(true);
        description.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JScrollPane scrollPane = new JScrollPane(description);
        scrollPane.setBorder(null);
        scrollPane.setBackground(new Color(240, 248, 255));
        scrollPane.setPreferredSize(new Dimension(480, 250));

        JButton closeButton = new JButton("Close");
        closeButton.setFocusPainted(false);
        closeButton.setBackground(new Color(220, 20, 60)); // Crimson
        closeButton.setForeground(Color.WHITE);
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> dispose());

        JButton backButton = new JButton("Back to Homepage");
        backButton.setFocusPainted(false);
        backButton.setBackground(new Color(100, 149, 237)); // Cornflower blue
        backButton.setForeground(Color.WHITE);
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> {
            new homepage("Expense management portal");
            dispose();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(240, 248, 255));
        buttonPanel.add(backButton);
        buttonPanel.add(closeButton);

        add(title, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }
}
