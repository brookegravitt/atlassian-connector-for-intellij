package com.atlassian.theplugin.idea.action.crucible;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ThePluginProjectComponent;
import com.atlassian.theplugin.idea.crucible.comments.ReviewCommentsPanel;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 19, 2008
 * Time: 1:33:06 AM
 * To change this template use File | Settings | File Templates.
 */
public class BackToGeneralCommentsAction extends AnAction {
	public void actionPerformed(AnActionEvent event) {
		ThePluginProjectComponent pluginProjectComponent = IdeaHelper.getCurrentProject(event.getDataContext())
				.getComponent(ThePluginProjectComponent.class);
		ReviewCommentsPanel panel = pluginProjectComponent.getCrucibleBottomToolWindowPanel().getReviewComentsPanel();
		panel.switchToComments();
	}
}
