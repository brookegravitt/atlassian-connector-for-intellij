package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.Action;
import com.atlassian.theplugin.idea.crucible.CrucibleSetReviewersWorker;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;


public class SetReviewersAction extends AbstractReviewAction {
	protected Action getRequestedAction() {
		return Action.MODIFYFILES;
	}

	public void actionPerformed(final AnActionEvent event) {
		final Project project = DataKeys.PROJECT.getData(event.getDataContext());

		new Thread(new Runnable() {
			public void run() {
				ApplicationManager.getApplication().invokeAndWait(
						new CrucibleSetReviewersWorker(project, rd),
						ModalityState.defaultModalityState());
			}
		}).start();

	}
}
