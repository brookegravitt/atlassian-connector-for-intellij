package com.atlassian.theplugin.idea.action.jira;

import com.atlassian.theplugin.idea.PluginToolWindow;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Mar 26, 2008
 * Time: 3:06:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShowHideJiraTabAction extends AnAction {
	public void actionPerformed(AnActionEvent e) {
		PluginToolWindow.showHidePanelIfExists(e, PluginToolWindow.ToolWindowPanels.JIRA);

	}
}

