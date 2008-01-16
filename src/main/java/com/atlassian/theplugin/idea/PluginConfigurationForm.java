package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.configuration.*;
import com.intellij.openapi.ui.Messages;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

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
    private JTextArea buildPlansTextArea;

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
        buildPlansTextArea.setText(subscribedPlansToString(data.getBambooConfigurationData().getSubscribedPlansData()));
    }

    public void getData(PluginConfigurationBean data) {
        data.getBambooConfigurationData().setServerName(serverName.getText());
        data.getBambooConfigurationData().setServerUrl(serverUrl.getText());
        data.getBambooConfigurationData().setUsername(username.getText());
        data.getBambooConfigurationData().setPassword(String.valueOf(password.getPassword()));

        data.getBambooConfigurationData().setSubscribedPlansData(subscribedPlansFromString(
                data.getBambooConfigurationData().getServer(), buildPlansTextArea.getText()));
    }

    static String subscribedPlansToString(Collection<SubscribedPlanBean> plans) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (SubscribedPlanBean plan : plans) {
            if (!first) {
                sb.append(' ');
            } else {
                first = false;
            }
            sb.append(plan.getPlanId());
        }

        return sb.toString();
    }

    static List<SubscribedPlanBean> subscribedPlansFromString(Server server, String planList) {
        List<SubscribedPlanBean> plans = new ArrayList<SubscribedPlanBean>();
        for (String planId : planList.split("\\s+")) {
            SubscribedPlanBean spb = new SubscribedPlanBean();
            spb.setServer(server);
            spb.setPlanId(planId);
            plans.add(spb);
        }

        return plans;
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
        if (String.valueOf(password.getPassword()) != null ? !String.valueOf(password.getPassword()).equals(data.getBambooConfigurationData().getPassword()) :
                data.getBambooConfigurationData().getPassword() != null)
            return true;
        return false;
    }


    public JComponent getRootComponent() {
        return rootComponent;
    }
}
