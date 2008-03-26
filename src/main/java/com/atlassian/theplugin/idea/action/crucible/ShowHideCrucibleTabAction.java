package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.idea.PluginToolWindow;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Mar 26, 2008
 * Time: 3:09:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShowHideCrucibleTabAction extends AnAction {
	public void actionPerformed(AnActionEvent e) {
		PluginToolWindow.showHidePanelIfExists(e, PluginToolWindow.ToolWindowPanels.CRUCIBLE);

	}
}
