package gui;

import controller.database;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.Statement;



public class adminloginpage extends JFrame {
    public adminloginpage() {
        setTitle("Admin Login Page");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 500);
        setLocationRelativeTo(null);
        setLayout(null);  // Critical for absolute positioning

        addgui();

        setVisible(true); // Call after all components are added
    }

    public void addgui() {
        // Page title
        // Background color
        getContentPane().setBackground(new Color(200, 230, 255));

// Title
        JLabel title = new JLabel("Admin Login Page");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setBounds(100, 20, 250, 35);
        add(title);

// Username label
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(30, 80, 200, 25);
        usernameLabel.setForeground(new Color(33, 33, 33));
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(usernameLabel);

// Username text field
        JTextField username = new JTextField();
        username.setBounds(30, 110, 300, 35);
        username.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        username.setForeground(Color.BLACK);
        username.setBackground(new Color(230, 230, 230));
        username.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(username);

// Password label
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(30, 160, 200, 25);
        passwordLabel.setForeground(new Color(33, 33, 33));
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(passwordLabel);

// Password field
        JPasswordField password = new JPasswordField();
        password.setBounds(30, 190, 300, 35);
        password.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        password.setForeground(Color.BLACK);
        password.setBackground(new Color(230, 230, 230));
        password.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(password);

// Admin key label
        JLabel keylbel = new JLabel("Admin Key:");
        keylbel.setBounds(30, 240, 200, 25);
        keylbel.setForeground(new Color(33, 33, 33));
        keylbel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(keylbel);

// Admin key field
        JTextField key = new JTextField();
        key.setBounds(30, 270, 300, 35);
        key.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        key.setForeground(Color.BLACK);
        key.setBackground(new Color(230, 230, 230));
        key.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(key);

// Login button
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.setBackground(new Color(52, 152, 219));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBounds(130, 350, 160, 40);
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
        backButton.setFocusPainted(false);
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> {
            new homepage("Expense Management System");
            dispose();
        });
        add(backButton);



        // button operation

        loginButton.addActionListener(e -> {
            String name;
            String pass;
            String akey;
             name = username.getText();
             pass = password.getText();
             akey = key.getText();

            try (Connection c = database.getConnection())

            {
                 if (c != null)
                 {
                     String query = "SELECT * FROM admin WHERE name = ? AND password = ? AND admin_key = ?";

                     PreparedStatement ps =c.prepareStatement(query);
                     ps.setString(1,name);
                     ps.setString(2,pass);
                     ps.setString(3,akey);

                     ResultSet rs = ps.executeQuery();
                     if (rs.next()) {
                         JOptionPane.showMessageDialog(null, "Login successful!");
                         new admin(name,"admin");
                         dispose();
                         // Navigate to admin dashboard or next page
                     } else {
                         JOptionPane.showMessageDialog(null, "Invalid credentials!");
                     }
                 } else {
                     JOptionPane.showMessageDialog(null, "Database connection failed.");
                 }
             } catch (Exception ex) {
                 ex.printStackTrace();
                 JOptionPane.showMessageDialog(null, "An error occurred while connecting to the database.");
             }


        });


    }



}
