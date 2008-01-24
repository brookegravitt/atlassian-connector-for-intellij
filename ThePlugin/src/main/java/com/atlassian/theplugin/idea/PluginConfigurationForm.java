package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.bamboo.BambooServerFactory;
import com.atlassian.theplugin.bamboo.api.BambooLoginException;
import com.atlassian.theplugin.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.configuration.SubscribedPlanBean;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.ui.Messages;
import static com.intellij.openapi.ui.Messages.showMessageDialog;
import com.intellij.ui.HyperlinkLabel;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Plugin configuration form.
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
    private HyperlinkLabel openJiraHyperlinkLabel;
    private JCheckBox chkPasswordRemember;

    public PluginConfigurationForm() {
        
        testConnection.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){

                try {
                    BambooServerFactory.getBambooServerFacade().testServerConnection(serverUrl.getText(), username.getText(), String.valueOf(password.getPassword()));
                    showMessageDialog("Connected successfully", "Connection OK", Messages.getInformationIcon());
                } catch (BambooLoginException e1) {
                    showMessageDialog(e1.getMessage(), "Connection Error", Messages.getErrorIcon());
                    //throw e1;
                }
            }
        });
    }

    public void setData(PluginConfigurationBean data) {
        serverName.setText(data.getBambooConfigurationData().getServer().getName());
        serverUrl.setText(data.getBambooConfigurationData().getServer().getUrlString());
        username.setText(data.getBambooConfigurationData().getServer().getUsername());
        try {
            password.setText(data.getBambooConfigurationData().getServer().getPasswordString());
        } catch (ServerPasswordNotProvidedException serverPasswordNotProvidedException) {
            // swallow - password does not have to be initialized always
        }
        buildPlansTextArea.setText(subscribedPlansToString(data.getBambooConfigurationData().getServerData().getSubscribedPlansData()));
    }

    public void getData(PluginConfigurationBean data) {
        data.getBambooConfigurationData().getServerData().setName(serverName.getText());
        data.getBambooConfigurationData().getServerData().setUrlString(serverUrl.getText());
        data.getBambooConfigurationData().getServerData().setUsername(username.getText());
        data.getBambooConfigurationData().getServerData().setPasswordString(String.valueOf(password.getPassword()), chkPasswordRemember.isSelected());

        data.getBambooConfigurationData().getServerData().setSubscribedPlansData(subscribedPlansFromString(buildPlansTextArea.getText()));
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

    static List<SubscribedPlanBean> subscribedPlansFromString(String planList) {
        List<SubscribedPlanBean> plans = new ArrayList<SubscribedPlanBean>();

        for (String planId : planList.split("\\s+")) {
            if (planId.length() == 0) continue;
            SubscribedPlanBean spb = new SubscribedPlanBean();
            spb.setPlanId(planId);
            plans.add(spb);
        }

        return plans;
    }

    public boolean isModified(PluginConfigurationBean data) {
        if (chkPasswordRemember.isSelected() != data.getBambooConfigurationData().getServer().getShouldPasswordBeStored())
            return true;
        if (serverName.getText() != null ? !serverName.getText().equals(data.getBambooConfigurationData().getServer().getName()) :
                data.getBambooConfigurationData().getServer().getName() != null)
            return true;
        if (serverUrl.getText() != null ? !serverUrl.getText().equals(data.getBambooConfigurationData().getServer().getUrlString()) :
                data.getBambooConfigurationData().getServer().getUrlString() != null)
            return true;
        if (username.getText() != null ? !username.getText().equals(data.getBambooConfigurationData().getServer().getUsername()) :
                data.getBambooConfigurationData().getServer().getUsername() != null)
            return true;
        if (String.valueOf(password.getPassword()) != null) {
            for (;;) {
                try {
                    if (String.valueOf(password.getPassword()).equals(data.getBambooConfigurationData().getServer().getPasswordString()))
                        break;
                } catch (ServerPasswordNotProvidedException serverPasswordNotProvidedException) {
                    // swallow
                }
                return true;
            }
        }
        if (null != buildPlansTextArea.getText() ? !buildPlansTextArea.getText().equals(subscribedPlansToString(data.getBambooConfigurationData().getServerData().getSubscribedPlansData())) :
                data.getBambooConfigurationData().getServerData().getSubscribedPlansData() != null)
            return true;
        return false;
    }


    public JComponent getRootComponent() {
        return rootComponent;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        openJiraHyperlinkLabel = new HyperlinkLabel("Report a bug/issue/request.");
        openJiraHyperlinkLabel.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                BrowserUtil.launchBrowser("https://studio.atlassian.com/browse/PL");
            }
        });





    }
}
