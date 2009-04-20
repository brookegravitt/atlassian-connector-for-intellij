package com.atlassian.theplugin.idea.action.bamboo.onebuild;

import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: jgorycki
 * Date: Jan 7, 2009
 * Time: 2:23:11 PM
 */
public class ViewBuildAction extends AbstractBuildDetailsAction {
	public void actionPerformed(AnActionEvent e) {
		openBuildInBrowser(e);
	}
}
