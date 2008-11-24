package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.jira.model.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: pmaruszak
 */
public abstract class JIRAAbstractAction extends AnAction {
	public abstract void onUpdate(AnActionEvent event);

	public void onUpdate(AnActionEvent event, boolean enabled) {
	}

	@Override
	public final void update(AnActionEvent event) {
		super.update(event);
		JIRAIssueListModelBuilder issueBuilder =
				IdeaHelper.getProjectComponent(event, JIRAIssueListModelBuilderImpl.class);
		JIRAFilterListBuilder filterBuilder = IdeaHelper.getProjectComponent(event, JIRAFilterListBuilder.class);
		JIRAServerModel jiraServerModel = IdeaHelper.getProjectComponent(event, JIRAServerModelImpl.class);

		boolean enabled = (issueBuilder != null && !issueBuilder.isModelFrozen()) 
						&& (filterBuilder != null && !filterBuilder.isModelFrozen())
						&& (jiraServerModel != null && !jiraServerModel.isModelFrozen());
		event.getPresentation().setEnabled(enabled);
		if (enabled) {

			onUpdate(event);
		}

		onUpdate(event, enabled);
	}
}
