
package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class homepage extends JFrame {
    private JPanel bgpanel;
    private Color c = new Color(240, 248, 255);
    private Color tgc = new Color(200, 230, 255);

    public homepage(String title) {
        setTitle(title);
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);

        bgpanel = new JPanel(null);
        bgpanel.setBackground(c);
        setContentPane(bgpanel);

        addhomepageGuiComponents();

        setVisible(true);
    }

    public void addhomepageGuiComponents() {
        JLabel l1 = new JLabel("Welcome to Expense Management Portal!");
        l1.setForeground(new Color(44, 62, 80));
        l1.setFont(new Font("Segoe UI", Font.BOLD, 24));
        l1.setBounds(110, 50, 600, 50);
        bgpanel.add(l1);

        JButton adminloginbutton = new JButton("Admin Login");
        JButton employeeloginbutton = new JButton("Employee Login");
        JButton registerbutton = new JButton("Register");
        JButton aboutbutton = new JButton("About");

        styleButton(adminloginbutton, 130);
        styleButton(employeeloginbutton, 230);
        styleButton(registerbutton, 330);
        styleButton(aboutbutton, 430);

        bgpanel.add(adminloginbutton);
        bgpanel.add(employeeloginbutton);
        bgpanel.add(registerbutton);
        bgpanel.add(aboutbutton);

        adminloginbutton.addActionListener(e -> {
            new adminloginpage();
            dispose();
        });

        registerbutton.addActionListener(e -> {
            new registerpage();
            dispose();
        });

        employeeloginbutton.addActionListener(e -> {
            new employeeloginpage();
            dispose();
        });
    }

    private void styleButton(JButton button, int y) {
        button.setBounds(240, y, 200, 50);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        button.setBackground(new Color(52, 152, 219));
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(41, 128, 185));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(52, 152, 219));
            }
        });
    }
}
