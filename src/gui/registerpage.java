package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import controller.database;

public class registerpage extends JFrame {

    public registerpage() {
        setTitle("Register");
        setSize(420, 520);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(new Color(245, 250, 255));

        JLabel label = new JLabel("Create New Account");
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        label.setForeground(new Color(52, 73, 94));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBounds(60, 40, 300, 40);
        add(label);

        addregistradetails();
        setVisible(true);
    }

    public void addregistradetails() {
        JLabel username = new JLabel("Username");
        username.setFont(new Font("Segoe UI", Font.BOLD, 18));
        username.setForeground(new Color(33, 33, 33));
        username.setBounds(80, 100, 250, 25);
        add(username);

        JTextField username1 = new JTextField();
        username1.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        username1.setBounds(80, 130, 250, 35);
        username1.setBackground(new Color(235, 235, 235));
        username1.setForeground(Color.BLACK);
        username1.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        add(username1);

        JLabel password = new JLabel("Password");
        password.setFont(new Font("Segoe UI", Font.BOLD, 18));
        password.setForeground(new Color(33, 33, 33));
        password.setBounds(80, 180, 250, 25);
        add(password);

        JPasswordField password1 = new JPasswordField();
        password1.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        password1.setBounds(80, 210, 250, 35);
        password1.setBackground(new Color(235, 235, 235));
        password1.setForeground(Color.BLACK);
        password1.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        add(password1);

        JButton submitbutton = new JButton("Register");
        submitbutton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        submitbutton.setBounds(80, 280, 250, 40);
        submitbutton.setBackground(new Color(52, 152, 219));
        submitbutton.setForeground(Color.WHITE);
        submitbutton.setFocusPainted(false);
        submitbutton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        submitbutton.setBorder(BorderFactory.createEmptyBorder());

        submitbutton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                submitbutton.setBackground(new Color(41, 128, 185));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                submitbutton.setBackground(new Color(52, 152, 219));
            }
        });
        add(submitbutton);

        JButton backbutton = new JButton("Back");
        backbutton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        backbutton.setBounds(80, 340, 250, 35);
        backbutton.setBackground(new Color(200, 200, 200));
        backbutton.setForeground(Color.BLACK);
        backbutton.setFocusPainted(false);
        backbutton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backbutton.setBorder(BorderFactory.createEmptyBorder());
        add(backbutton);

        backbutton.addActionListener(ex -> {
            new homepage("Expense Management System");
            dispose();
        });

        submitbutton.addActionListener(e -> {
            String uname = username1.getText();
            String pass = new String(password1.getPassword());

            if (uname.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please fill all the fields", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection c = database.getConnection()) {
                String query = "INSERT INTO employee (username, password) VALUES (?, ?)";
                PreparedStatement ps = c.prepareStatement(query);
                ps.setString(1, uname);
                ps.setString(2, pass);

                int row = ps.executeUpdate();

                if (row > 0) {
                    JOptionPane.showMessageDialog(null, "Successfully Registered!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    new employeeloginpage();
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(null, "Registration Failed!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                if (ex.getMessage().contains("Duplicate entry")) {
                    JOptionPane.showMessageDialog(null, "Username already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}
