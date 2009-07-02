package com.atlassian.theplugin.idea.action.reviews;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.crucible.CrucibleSetReviewersWorker;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;

/**
 * User: jgorycki
 * Date: Dec 3, 2008
 * Time: 2:34:51 PM
 */
public class SetReviewersAction extends AbstractReviewAction {
	@Override
	protected CrucibleAction getRequestedAction() {
		return CrucibleAction.MODIFY_FILES;
	}

	@Override
	public void actionPerformed(final AnActionEvent event) {
		final Project project = DataKeys.PROJECT.getData(event.getDataContext());

		new Thread(new Runnable() {
			public void run() {
				ApplicationManager.getApplication().invokeAndWait(
						new CrucibleSetReviewersWorker(project, event.getData(Constants.REVIEW_KEY)),
						ModalityState.defaultModalityState());
			}
		}).start();

	}
}
