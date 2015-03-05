package com.atlassian.theplugin.idea.action.bamboo.onebuild;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: jgorycki
 * Date: Jan 7, 2009
 * Time: 2:23:27 PM
 */
public class CloseBuildAction extends AnAction {
	public void actionPerformed(AnActionEvent e) {
		IdeaHelper.getBuildToolWindow(e).closeToolWindow(e);
	}
}
