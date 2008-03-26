package com.atlassian.theplugin.idea.action.bamboo;

import com.atlassian.theplugin.idea.PluginToolWindow;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Mar 26, 2008
 * Time: 2:53:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShowHideBambooTabAction extends AnAction {
	public void actionPerformed(AnActionEvent e) {
		PluginToolWindow.showHidePanelIfExists(e, PluginToolWindow.ToolWindowPanels.BAMBOO);

	}
}
