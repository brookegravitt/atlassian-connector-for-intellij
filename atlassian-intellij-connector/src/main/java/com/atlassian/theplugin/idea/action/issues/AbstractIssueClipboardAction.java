package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.idea.Constants;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;

import java.awt.datatransfer.StringSelection;

public abstract class AbstractIssueClipboardAction extends JIRAAbstractAction {
	public void actionPerformed(final AnActionEvent event) {
		JiraIssueAdapter issue = getJIRAIssue(event);
		if (issue != null) {
			CopyPasteManager.getInstance().setContents(new StringSelection(getCliboardText(issue)));
		}
	}

    public void onUpdate(AnActionEvent event) {
        onUpdate(event, true);
	}

	public void onUpdate(AnActionEvent event, boolean enabled) {
		JiraIssueAdapter issue = getJIRAIssue(event);
		if (issue != null) {
			event.getPresentation().setText(getCliboardText(issue));
			event.getPresentation().setVisible(true);
			event.getPresentation().setEnabled(true);
		}
	}

	private JiraIssueAdapter getJIRAIssue(final AnActionEvent event) {
		final JiraIssueAdapter issue = event.getData(Constants.ISSUE_KEY);
		if (issue != null) {
			return issue;
		}
		return null;
	}

	protected abstract String getCliboardText(JiraIssueAdapter issue);

}
