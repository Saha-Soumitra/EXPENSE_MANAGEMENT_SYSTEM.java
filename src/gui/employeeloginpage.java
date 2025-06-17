package gui;
import controller.database;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.Statement;

public class employeeloginpage  extends JFrame {
    public employeeloginpage() {
        setTitle("Employee Login");
        setSize(400, 360);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(null);


        addgui2();

        setVisible(true);
    }

    public void addgui2() {

        // Page background
        getContentPane().setBackground(new Color(200, 230, 255));

// Page title
        JLabel title = new JLabel("Employee Login Page");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setBounds(50, 60, 300, 40);
        add(title);

// Username label
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(30, 100, 200, 25);
        usernameLabel.setForeground(new Color(33, 33, 33));
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(usernameLabel);

// Username text field
        JTextField username = new JTextField();
        username.setBounds(30, 130, 300, 35);
        username.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        username.setForeground(Color.BLACK);
        username.setBackground(new Color(230, 230, 230));
        username.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(username);

// Password label
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(30, 170, 200, 25);
        passwordLabel.setForeground(new Color(33, 33, 33));
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(passwordLabel);

// Password field
        JPasswordField password = new JPasswordField();
        password.setBounds(30, 200, 300, 35);
        password.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        password.setForeground(Color.BLACK);
        password.setBackground(new Color(230, 230, 230));
        password.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(password);

// Login button
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.setBackground(new Color(52, 152, 219));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBounds(100, 260, 160, 40);
        loginButton.setBorder(BorderFactory.createEmptyBorder());

        loginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(new Color(41, 128, 185));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(new Color(52, 152, 219));
            }
        });
        add(loginButton);

// Back button
        JButton backButton = new JButton("Back");
        backButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        backButton.setBounds(10, 10, 70, 25);
        backButton.setBackground(new Color(200, 200, 200));
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> {
            new homepage("Expense Management System");
            dispose();
        });
        add(backButton);

        // button operation

        loginButton.addActionListener(e -> {

            String name = username.getText();
            String pass = new String(password.getPassword());  // ✅ correct


            try (Connection c = database.getConnection()) {
                if (c != null) {
                    String query = "SELECT * FROM employee WHERE username = ? AND password = ?";
                    PreparedStatement ps = c.prepareStatement(query);
                    ps.setString(1, name);
                    ps.setString(2, pass);
                    ResultSet rs = ps.executeQuery();

                    if (rs.next()) {
                        JOptionPane.showMessageDialog(null, "Login successful!");
                        new employee(name , "employee");
                        dispose();
                        // Navigate to dashboard
                    } else {
                        JOptionPane.showMessageDialog(null, "Invalid credentials!");
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "An error occurred while connecting to the database.");
            }

        });
    }

}
