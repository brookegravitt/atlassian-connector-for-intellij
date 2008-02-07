package com.atlassian.theplugin.idea.config.serverconfig.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.idea.config.ConfigPanel;
import com.atlassian.theplugin.ServerType;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-01-28
 * Time: 12:09:49
 * To change this template use File | Settings | File Templates.
 */
public class AddCrucibleServerAction extends AnAction {
    public void actionPerformed(AnActionEvent event) {
		ConfigPanel.getInstance().addServer(ServerType.CRUCIBLE_SERVER);
    }
}
