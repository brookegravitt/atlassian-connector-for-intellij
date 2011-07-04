package com.atlassian.theplugin.idea.action.bamboo;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.connector.intellij.bamboo.BambooBuildAdapter;
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
		final BambooBuildAdapter build = getBuild(event);

        event.getPresentation().setEnabled(
                        build != null
                        && isBamboo2(event, build.getServer())
                        && build.getPlanKey() != null
                        && build.isValid()
                        && build.areActionsAllowed(true));
	}
}
