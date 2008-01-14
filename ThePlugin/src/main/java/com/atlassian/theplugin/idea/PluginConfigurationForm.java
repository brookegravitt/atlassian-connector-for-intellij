package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.configuration.BambooConnection;
import com.atlassian.theplugin.configuration.ConnectionException;
import com.atlassian.theplugin.configuration.PluginConfigurationBean;
import com.intellij.openapi.ui.Messages;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 4:20:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class PluginConfigurationForm {
    private JPanel rootComponent;
    private JTabbedPane tabbedPane1;
    private JTextField serverName;
    private JTextField serverUrl;
    private JTextField username;
    private JPasswordField password;
    private JButton testConnection;

    public PluginConfigurationForm() {
        testConnection.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                BambooConnection c = new BambooConnection();
                try {
                    c.connect(serverUrl.getText(), username.getText(), String.valueOf(password.getPassword()));
                    Messages.showMessageDialog("Connected successfully", "Connection OK", Messages.getInformationIcon());
                } catch (ConnectionException e1) {
                    Messages.showMessageDialog(e1.getMessage(), "Connection Error", Messages.getErrorIcon());
                }
            }
        });
    }

    public void setData(PluginConfigurationBean data) {
        serverName.setText(data.getBambooConfigurationData().getServerName());
        serverUrl.setText(data.getBambooConfigurationData().getServerUrl());
        username.setText(data.getBambooConfigurationData().getUsername());
        password.setText(data.getBambooConfigurationData().getPassword());
    }

    public void getData(PluginConfigurationBean data) {
        data.getBambooConfigurationData().setServerName(serverName.getText());
        data.getBambooConfigurationData().setServerUrl(serverUrl.getText());
        data.getBambooConfigurationData().setUsername(username.getText());
        data.getBambooConfigurationData().setPassword(password.getText());
    }

    public boolean isModified(PluginConfigurationBean data) {
        if (serverName.getText() != null ? !serverName.getText().equals(data.getBambooConfigurationData().getServerName()) :
                data.getBambooConfigurationData().getServerName() != null)
            return true;
        if (serverUrl.getText() != null ? !serverUrl.getText().equals(data.getBambooConfigurationData().getServerUrl()) :
                data.getBambooConfigurationData().getServerUrl() != null)
            return true;
        if (username.getText() != null ? !username.getText().equals(data.getBambooConfigurationData().getUsername()) :
                data.getBambooConfigurationData().getUsername() != null)
            return true;
        if (password.getText() != null ? !password.getText().equals(data.getBambooConfigurationData().getPassword()) :
                data.getBambooConfigurationData().getPassword() != null)
            return true;
        return false;
    }


    public JComponent getRootComponent() {
        return rootComponent;
    }
}
