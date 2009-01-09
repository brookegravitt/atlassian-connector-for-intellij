package com.atlassian.theplugin.idea.action.bamboo.onebuild;

import com.atlassian.theplugin.idea.action.bamboo.AbstractBuildListAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: jgorycki
 * Date: Jan 7, 2009
 * Time: 2:23:11 PM
 */
public class ViewBuildAction extends AbstractBuildListAction {
	public void actionPerformed(AnActionEvent e) {
		openBuildInBrowser(e);
	}
}
