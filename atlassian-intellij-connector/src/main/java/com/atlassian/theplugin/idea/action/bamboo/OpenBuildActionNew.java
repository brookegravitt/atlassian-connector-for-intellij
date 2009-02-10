package com.atlassian.theplugin.idea.action.bamboo;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapterIdea;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * @author jgorycki
 */
public class OpenBuildActionNew extends AbstractBuildListAction {
	@Override
	public void actionPerformed(AnActionEvent event) {
		IdeaHelper.getBuildToolWindow(event).showBuild(getBuild(event));
	}

	@Override
	public void update(final AnActionEvent event) {
		super.update(event);
		final BambooBuildAdapterIdea build = getBuild(event);
		if (build == null || !build.isBamboo2() || !build.areActionsAllowed()) {
			event.getPresentation().setEnabled(false);
		}
	}
}
