package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.model.JIRAIssueListModelBuilder;
import com.atlassian.theplugin.jira.model.JIRAIssueListModelBuilderImpl;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;

import java.awt.datatransfer.StringSelection;

public abstract class AbstractIssueClipboardAction extends JIRAAbstractAction {
	public void actionPerformed(final AnActionEvent event) {
		JIRAIssue issue = getJIRAIssue(event);
		if (issue != null) {
			CopyPasteManager.getInstance().setContents(new StringSelection(getCliboardText(issue)));
		}
	}

	public void onUpdate(AnActionEvent event) {
		JIRAIssue issue = getJIRAIssue(event);
		if (issue != null) {
			event.getPresentation().setText(getCliboardText(issue));
			event.getPresentation().setVisible(true);
		} else {
			event.getPresentation().setVisible(false);
		}
	}

	private JIRAIssue getJIRAIssue(final AnActionEvent event) {
		JIRAIssueListModelBuilder builder =
				IdeaHelper.getProjectComponent(IdeaHelper.getCurrentProject(event), JIRAIssueListModelBuilderImpl.class);
		if (builder == null || builder.getModel() == null) {
			return null;
		}
		return builder.getModel().getSelectedIssue();
	}

	protected abstract String getCliboardText(JIRAIssue issue);

}
