package com.atlassian.theplugin.idea.config.serverconfig.action;

import com.atlassian.theplugin.idea.config.ConfigPanel;
import com.atlassian.theplugin.ServerType;
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
        ConfigPanel.getInstance().addServer(ServerType.BAMBOO_SERVER);		
	}
}
