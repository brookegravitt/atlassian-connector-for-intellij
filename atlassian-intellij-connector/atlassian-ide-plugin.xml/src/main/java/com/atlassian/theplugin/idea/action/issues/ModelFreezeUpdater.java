package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.jira.model.JIRAFilterListBuilder;
import com.atlassian.theplugin.jira.model.JIRAIssueListModelBuilder;
import com.atlassian.theplugin.jira.model.JIRAServerModel;
import com.atlassian.theplugin.jira.model.JIRAServerModelImpl;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: jgorycki
 * Date: Nov 26, 2008
 * Time: 10:49:40 AM
 */
public final class ModelFreezeUpdater {
	private ModelFreezeUpdater() {
	}

	public static boolean getState(AnActionEvent event) {
		final JIRAIssueListModelBuilder issueBuilder = IdeaHelper.getJIRAIssueListModelBuilder(event);
		JIRAFilterListBuilder filterBuilder = IdeaHelper.getProjectComponent(event, JIRAFilterListBuilder.class);
		JIRAServerModel jiraServerModel = IdeaHelper.getProjectComponent(event, JIRAServerModelImpl.class);

		return (issueBuilder != null && !issueBuilder.isModelFrozen())
				&& (filterBuilder != null && !filterBuilder.isModelFrozen())
				&& (jiraServerModel != null && !jiraServerModel.isModelFrozen());
	}
}
