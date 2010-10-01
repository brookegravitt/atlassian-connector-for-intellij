package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.connector.intellij.crucible.IntelliJCrucibleServerFacade;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.action.reviews.AbstractCrucibleToolbarAction;
import com.atlassian.theplugin.idea.crucible.CrucibleCreatePreCommitUploadReviewForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;

public class PreCommitReviewActionNoChangeSelected extends AbstractCrucibleToolbarAction {
	@Override
	public void actionPerformed(AnActionEvent event) {
		final Project project = event.getData(DataKeys.PROJECT);
		if (project == null) {
			return;
		}
		new CrucibleCreatePreCommitUploadReviewForm(project, IntelliJCrucibleServerFacade.getInstance(), null,
				IdeaHelper.getProjectCfgManager(event)).show();
	}
}