package com.atlassian.theplugin.idea.serverconfig.action;

import com.atlassian.theplugin.idea.serverconfig.ServerConfigPanel;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-01-28
 * Time: 12:09:24
 * To change this template use File | Settings | File Templates.
 */
public class AddBambooServerAction extends AnAction {	
	public void actionPerformed(AnActionEvent event) {
        ServerConfigPanel form = ServerConfigPanel.getInstance();
		form.addBambooServer();
	}
}
