package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.connector.intellij.crucible.IntelliJCrucibleServerFacade;
import com.atlassian.theplugin.commons.UiTaskExecutor;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.action.reviews.AbstractCrucibleToolbarAction;
import com.atlassian.theplugin.idea.crucible.CrucibleCreatePostCommitReviewForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

public class PostCommitReviewActionNoRevisionSelected extends AbstractCrucibleToolbarAction {
	@Override
	public void actionPerformed(AnActionEvent event) {
		final Project project = event.getData(DataKeys.PROJECT);
		if (project == null) {
			return;
		}

		final UiTaskExecutor uiTaskExecutor = IdeaHelper.getProjectComponent(project, UiTaskExecutor.class);
		if (uiTaskExecutor == null) {
			Messages.showErrorDialog(project, "Cannot fetch UI Task Executor", "Internal error");
			return;
		}
		new CrucibleCreatePostCommitReviewForm(project, IntelliJCrucibleServerFacade.getInstance(), IdeaHelper
				.getProjectCfgManager(event), uiTaskExecutor).show();
	}
}
