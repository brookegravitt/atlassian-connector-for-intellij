package com.atlassian.theplugin.idea.action.bamboo;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.bamboo.BuildToolWindow;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: jgorycki
 * Date: Jan 22, 2009
 * Time: 12:03:00 PM
 */
public class JumpToSourceAction extends AnAction {
	public void actionPerformed(AnActionEvent e) {
		BuildToolWindow window = IdeaHelper.getBuildToolWindow(e);
//		window.jumpToSource(e.getPlace());
	}

	public void update(AnActionEvent e) {
		BuildToolWindow window = IdeaHelper.getBuildToolWindow(e);
		if (window != null) {
//			e.getPresentation().setEnabled(window.canJumpToSource(e.getPlace()));
		}
		super.update(e);
	}
}
