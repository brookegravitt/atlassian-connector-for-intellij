package com.atlassian.theplugin.idea.config.serverconfig.action;

import com.atlassian.theplugin.idea.config.ConfigPanel;
import com.atlassian.theplugin.ServerType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class AddJIRAServerAction extends AnAction {
	public void actionPerformed(AnActionEvent event) {
        ConfigPanel.getInstance().addServer(ServerType.JIRA_SERVER);		
	}
}