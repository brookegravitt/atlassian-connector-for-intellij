package com.atlassian.theplugin.idea.action.jira;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.JIRAToolWindowPanel;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;

import java.awt.datatransfer.StringSelection;

public abstract class AbstractClipboardAction extends AnAction {
	
	public void actionPerformed(final AnActionEvent event) {
		JIRAIssue issue = getJIRAIssue(event);
		if (issue != null) {
			CopyPasteManager.getInstance().setContents(new StringSelection(getCliboardText(issue)));
		}
	}

	public void update(final AnActionEvent event) {
		JIRAIssue issue = getJIRAIssue(event);
		if (issue != null) {
			event.getPresentation().setText(getCliboardText(issue));
			event.getPresentation().setVisible(true);
		} else {
			event.getPresentation().setVisible(false);
		}
	}

	private JIRAIssue getJIRAIssue(final AnActionEvent event) {
		JIRAToolWindowPanel toolWindow = IdeaHelper.getJIRAToolWindowPanel(event);
		if (toolWindow != null) {
			return toolWindow.getSelectedIssue();
		}
		return null;
	}

	protected abstract String getCliboardText(JIRAIssue issue);
}