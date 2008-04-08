package com.atlassian.theplugin.idea.action.jira;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ProgressAnimationProvider;
import com.atlassian.theplugin.idea.jira.JIRAToolWindowPanel;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Apr 3, 2008
 * Time: 10:56:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class JIRAShowIssuesFilterAction extends AnAction {

	public void actionPerformed(AnActionEvent e) {

		final JIRAToolWindowPanel jiraPanel = IdeaHelper.getCurrentJIRAToolWindowPanel();

		if (jiraPanel != null) {

			final ProgressAnimationProvider animator = jiraPanel.getProgressAnimation();

			new Thread(new Runnable() {
				public void run() {
					animator.startProgressAnimation();
					jiraPanel.showJIRAIssueFilter();
					animator.stopProgressAnimation();

				}
			}, "JIRA show issues filter").start();

		}
	}

	public void update(AnActionEvent event) {
		super.update(event);

		if (IdeaHelper.getJIRAToolWindowPanel(event) != null) {
			if (IdeaHelper.getJIRAToolWindowPanel(event).getFilters().getSavedFilterUsed()) {
				event.getPresentation().setEnabled(false);
			} else {
				event.getPresentation().setEnabled(true);
			}
		} else {
			event.getPresentation().setEnabled(false);
		}

	}
}

