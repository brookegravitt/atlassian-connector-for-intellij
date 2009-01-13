package com.atlassian.theplugin.idea.action.bamboo;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapterIdea;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: jgorycki
 * Date: Jan 7, 2009
 * Time: 1:54:36 PM
 */
public class OpenBuildActionNew extends AbstractBuildListAction {
	public void actionPerformed(AnActionEvent event) {
		IdeaHelper.getBuildToolWindow(event).showBuild(getBuild(event));
	}

	@Override
	public void update(final AnActionEvent event) {
		super.update(event);
		if (getBuild(event) == null || !getBuild(event).isBamboo2()
				|| getBuild(event).getState() == BambooBuildAdapterIdea.BuildState.UNKNOWN) {
			event.getPresentation().setEnabled(false);
		}
	}
}
