package com.fw.edc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by mxu2 on 6/3/15.
 */
public class SettingDialog extends JDialog {

    private static final int DEFAULT_WIDTH = 400;
    private static final int DEFAULT_HEIGHT = 200;
    private JLabel hostnameLbl = new JLabel("hostname:");
    private JTextField hostnameField = new JTextField();
    private JLabel portLbl = new JLabel("port:");
    private JTextField portField = new JTextField();
    private JLabel userLbl = new JLabel("user:");
    private JTextField userField = new JTextField();
    private JLabel passwordLbl = new JLabel("password:");
    private JTextField passwordField = new JTextField();

    public SettingDialog() {
        this.setAlwaysOnTop(true);
        this.setModal(true);
        this.setBounds((Util.getScreenWidth() - DEFAULT_WIDTH) / 2, (Util.getScreenHeight() - DEFAULT_HEIGHT) / 2, DEFAULT_WIDTH, DEFAULT_HEIGHT);

        hostnameLbl.setLabelFor(hostnameField);
        portLbl.setLabelFor(portField);
        userLbl.setLabelFor(userField);
        passwordLbl.setLabelFor(passwordField);

        hostnameField.setColumns(20);
        portField.setColumns(20);
        userField.setColumns(20);
        passwordField.setColumns(20);

        Config config = Config.getInstance();
        hostnameField.setText(config.getHostname());
        portField.setText(String.valueOf(config.getPort()));
        userField.setText(config.getUser());
        passwordField.setText(config.getPassword());

        JButton okBtn = new JButton("Save Configuration");
        okBtn.setVerticalAlignment(JButton.CENTER);
        okBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Config config = Config.getInstance();
                config.setHostname(hostnameField.getText());
                config.setPort(Integer.parseInt(portField.getText()));
                config.setUser(userField.getText());
                config.setPassword(passwordField.getText());
                config.saveConfig();
                SettingDialog.this.dispose();
            }
        });

        JPanel labelPane = new JPanel(new GridLayout(0, 1));
        labelPane.add(hostnameLbl);
        labelPane.add(portLbl);
        labelPane.add(userLbl);
        labelPane.add(passwordLbl);

        JPanel fieldPane = new JPanel(new GridLayout(0, 1));
        fieldPane.add(hostnameField);
        fieldPane.add(portField);
        fieldPane.add(userField);
        fieldPane.add(passwordField);

        JPanel mainPane = new JPanel();
        mainPane.setLayout(new BorderLayout());
        mainPane.add(labelPane, BorderLayout.CENTER);
        mainPane.add(fieldPane, BorderLayout.LINE_END);
        mainPane.add(okBtn, BorderLayout.SOUTH);
        mainPane.add(new JLabel("DATABASE SETTING", JLabel.CENTER), BorderLayout.NORTH);
        add(mainPane);

    }
}
