package com.atlassian.theplugin.idea.action.bamboo.onebuild;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.idea.bamboo.BambooTableToolWindowPanel;
import com.atlassian.theplugin.idea.bamboo.BuildToolWindow;
import com.atlassian.theplugin.idea.IdeaHelper;

/**
 * User: jgorycki
 * Date: Jan 7, 2009
 * Time: 2:23:20 PM
 */
public class CommentBuildAction extends AnAction {
	public void actionPerformed(AnActionEvent e) {
		BambooTableToolWindowPanel panel = IdeaHelper.getBambooToolWindowPanel(e);
		if (panel != null) {
			BuildToolWindow btw = IdeaHelper.getBuildToolWindow(e);
			if (btw != null) {
				panel.openCommentDialog(btw.getBuild(e.getPlace()));
			}
		}
	}
}
