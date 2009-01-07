package com.atlassian.theplugin.idea.action.bamboo;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: jgorycki
 * Date: Jan 7, 2009
 * Time: 1:54:36 PM
 */
public class OpenBuildActionNew extends AbstractBamboo2BuildAction {
	public void actionPerformed(AnActionEvent event) {
		IdeaHelper.getBambooToolWindowPanel(event).openBuild();
	}
}
