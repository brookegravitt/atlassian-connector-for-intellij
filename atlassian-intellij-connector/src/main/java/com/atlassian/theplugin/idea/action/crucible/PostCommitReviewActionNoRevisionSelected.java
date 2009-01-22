package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.commons.UiTaskExecutor;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.action.reviews.AbstractCrucibleToolbarAction;
import com.atlassian.theplugin.idea.crucible.CrucibleReviewCreateForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;

/**
 * TODO: Document this class / interface here
 *
 * @since v3.13
 */
public class PostCommitReviewActionNoRevisionSelected extends AbstractCrucibleToolbarAction {
	@Override
	public void actionPerformed(AnActionEvent event) {
		final Project project = event.getData(DataKeys.PROJECT);
		if (project == null) {
			return;
		}

		final CrucibleReviewCreateForm reviewCreateForm = new CrucibleReviewCreateForm(project,
				CrucibleServerFacadeImpl.getInstance(), null, IdeaHelper.getCfgManager(),
				IdeaHelper.getProjectComponent(project,
						UiTaskExecutor.class));
		reviewCreateForm.show();
	}
}
