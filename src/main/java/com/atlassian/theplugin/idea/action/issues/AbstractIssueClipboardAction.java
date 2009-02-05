package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.jira.api.JIRAIssue;
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
		final JIRAIssue issue = event.getData(Constants.ISSUE_KEY);
		if (issue != null) {
			return issue;
		}
		return null;
	}

	protected abstract String getCliboardText(JIRAIssue issue);

}
