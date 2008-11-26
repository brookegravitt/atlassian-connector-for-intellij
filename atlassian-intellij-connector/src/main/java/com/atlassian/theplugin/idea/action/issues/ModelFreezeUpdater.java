package com.atlassian.theplugin.idea.action.issues;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.jira.model.*;
import com.atlassian.theplugin.idea.IdeaHelper;

/**
 * User: jgorycki
 * Date: Nov 26, 2008
 * Time: 10:49:40 AM
 */
public final class ModelFreezeUpdater {
	private ModelFreezeUpdater() { }

	public static boolean getStateAndSetPresentationEnabled(AnActionEvent event) {
		JIRAIssueListModelBuilder issueBuilder =
				IdeaHelper.getProjectComponent(event, JIRAIssueListModelBuilderImpl.class);
		JIRAFilterListBuilder filterBuilder = IdeaHelper.getProjectComponent(event, JIRAFilterListBuilder.class);
		JIRAServerModel jiraServerModel = IdeaHelper.getProjectComponent(event, JIRAServerModelImpl.class);

		boolean enabled = (issueBuilder != null && !issueBuilder.isModelFrozen())
						&& (filterBuilder != null && !filterBuilder.isModelFrozen())
						&& (jiraServerModel != null && !jiraServerModel.isModelFrozen());
		
		event.getPresentation().setEnabled(enabled);

		return enabled;
	}
}
