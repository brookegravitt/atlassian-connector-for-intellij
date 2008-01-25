package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.configuration.BambooConfiguration;
import com.atlassian.theplugin.configuration.ConfigurationFactory;
import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ex.DataConstantsEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 5:08:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        BambooConfiguration bambooConfiguration = ConfigurationFactory.getConfiguration().getBambooConfiguration();

        String msg = "Name: " + bambooConfiguration.getServer().getName();
        msg += "\nURL: " + bambooConfiguration.getServer().getUrlString();
        msg += "\nUsername: " + bambooConfiguration.getServer().getUsername();
        try {
            msg += "\nPassword: " + bambooConfiguration.getServer().getPasswordString();
        } catch (ServerPasswordNotProvidedException exception) {
            msg += "\nPassword: " + exception.getMessage();
        }

        Messages.showMessageDialog(
                msg,
                "Sample",
                Messages.getInformationIcon());

        Project project = (Project) e.getDataContext().getData(DataConstantsEx.PROJECT);
        ThePluginProjectComponent tpmc = project.getComponent(ThePluginProjectComponent.class);
        tpmc.setBambooStatus("Chg", "After change action");

    }
}
