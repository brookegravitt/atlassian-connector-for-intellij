package com.atlassian.theplugin.idea;

import javax.swing.*;

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

    public void setData(PluginConfiguration data) {
        serverName.setText(data.getBambooConfiguration().getServerName());
        serverUrl.setText(data.getBambooConfiguration().getServerUrl());
        username.setText(data.getBambooConfiguration().getUsername());
        password.setText(data.getBambooConfiguration().getPassword());
    }

    public void getData(PluginConfiguration data) {
        data.getBambooConfiguration().setServerName(serverName.getText());
        data.getBambooConfiguration().setServerUrl(serverUrl.getText());
        data.getBambooConfiguration().setUsername(username.getText());
        data.getBambooConfiguration().setPassword(password.getText());
    }

    public boolean isModified(PluginConfiguration data) {
        if (serverName.getText() != null ? !serverName.getText().equals(data.getBambooConfiguration().getServerName()) :
                data.getBambooConfiguration().getServerName() != null)
            return true;
        if (serverUrl.getText() != null ? !serverUrl.getText().equals(data.getBambooConfiguration().getServerUrl()) :
                data.getBambooConfiguration().getServerUrl() != null)
            return true;
        if (username.getText() != null ? !username.getText().equals(data.getBambooConfiguration().getUsername()) :
                data.getBambooConfiguration().getUsername() != null)
            return true;
        if (password.getText() != null ? !password.getText().equals(data.getBambooConfiguration().getPassword()) :
                data.getBambooConfiguration().getPassword() != null)
            return true;
        return false;
    }


    public JComponent getRootComponent() {
        return rootComponent;
    }
}
