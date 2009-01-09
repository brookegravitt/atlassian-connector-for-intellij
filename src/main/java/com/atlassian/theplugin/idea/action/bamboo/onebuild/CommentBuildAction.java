package com.atlassian.theplugin.idea.action.bamboo.onebuild;

import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: jgorycki
 * Date: Jan 7, 2009
 * Time: 2:23:20 PM
 */
public class CommentBuildAction extends AbstractBuildDetailsAction {

	@Override
	public void actionPerformed(AnActionEvent e) {
		commentBuild(e);
	}

	@Override
	public void update(final AnActionEvent event) {
		super.update(event);
		if (getBuild(event) != null && !getBuild(event).isBamboo2()) {
			event.getPresentation().setEnabled(false);
		}
	}
}
