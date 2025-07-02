package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.border.Border;

public class admin {
    JFrame frame = new JFrame("Admin");
    BackgroundImagePanel panel = new BackgroundImagePanel("C:\\Users\\Hp\\IdeaProjects\\MANAGE_EXPENSES-main\\src\\gui\\money_bg.jpg");

    JPanel buttonPanel = new JPanel(new GridLayout(10, 1, 0, 0));

    JLabel title = new JLabel("<html><center>WELCOME TO ADMIN PANEL.<br>MANAGE EXPENSES WITH US.</center></html>");

    JButton[] buttons = {
            new JButton("Dashboard"),
            new JButton("Add Transaction"),
            new JButton("View Transactions"),
            new JButton("Manage Categories"),
            new JButton("Inventory"),
            new JButton("Settings"),
            new JButton("Add Expense"),      // <-- Added here
            new JButton("Logout"),
            new JButton("Exit"),
    };

    admin(String name, String post) {
        frame.setSize(700, 700);
        frame.setResizable(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        // Customize the label
        title.setOpaque(true);
        title.setBackground(new Color(0, 0, 0, 180)); // Semi-transparent background
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.GREEN);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setVerticalAlignment(SwingConstants.CENTER);
        title.setPreferredSize(new Dimension(600, 100));

        Border border = BorderFactory.createLineBorder(Color.RED, 3);
        title.setBorder(border);

        // Add label to panel (not to frame directly)
        panel.add(title, BorderLayout.NORTH);

        // Set background panel as the content pane
        frame.setContentPane(panel);

        buttonPanel.setOpaque(false); // Make transparent

        for (JButton button : buttons) {
            button.setContentAreaFilled(false); // No default background color
            button.setOpaque(false);
            button.setForeground(Color.BLACK);
            button.setFont(new Font("Arial", Font.BOLD, 22));
            button.setBorder(BorderFactory.createLineBorder(Color.black, 2)); // Border around button
            buttonPanel.add(button);

            // Hover effect
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(MouseEvent evt) {
                    button.setForeground(Color.YELLOW);
                    button.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 2));
                }

                public void mouseExited(MouseEvent evt) {
                    button.setForeground(Color.BLACK);
                    button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                }
            });
        }

        buttonPanel.setPreferredSize(new Dimension(270, 0));
        panel.add(buttonPanel, BorderLayout.WEST);

        // Add action listeners
        buttons[0].addActionListener(e -> new DASHBOARD_WINDOW());
        buttons[1].addActionListener(e -> new ADD_TRANSACTIONS_WINDOW(name, post));
        buttons[2].addActionListener(e -> new VIEW_TRANSACTIONS_WINDOW());
        buttons[3].addActionListener(e -> new MANAGE_CATEGORIES_WINDOW());
        buttons[4].addActionListener(e -> new INVENTORY_WINDOW());
        buttons[5].addActionListener(e -> new SETTINGS_WINDOW());
        buttons[6].addActionListener(e -> new MANAGE_EXPENSE_WINDOW()); // Added Add Expense action
        buttons[7].addActionListener(e -> {
            new homepage("Expense management portal");
            frame.dispose();
        });
        buttons[8].addActionListener(e -> System.exit(0));

        frame.setVisible(true);
    }
}
