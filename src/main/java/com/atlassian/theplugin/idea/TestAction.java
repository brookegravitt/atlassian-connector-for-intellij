package com.atlassian.theplugin.idea;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.atlassian.theplugin.bamboo.configuration.Configuration;
import com.atlassian.theplugin.bamboo.configuration.ConfigurationFactory;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 5:08:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        Configuration bambooConfiguration = ConfigurationFactory.getConfiguration();

        String msg = "Name: " + bambooConfiguration.getServer().getName() + "\nURL: " + bambooConfiguration.getServer().getUrlString();
        Messages.showMessageDialog(
                msg,
                "Sample",
                Messages.getInformationIcon());
    }
}
