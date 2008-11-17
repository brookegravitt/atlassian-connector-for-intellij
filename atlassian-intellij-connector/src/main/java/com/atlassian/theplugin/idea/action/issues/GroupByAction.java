package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.JIRAIssueGroupBy;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class GroupByAction extends ComboBoxAction {

	@NotNull
	protected DefaultActionGroup createPopupActionGroup(JComponent jComponent) {
		final DefaultActionGroup g = new DefaultActionGroup();

		final ComboBoxButton button = (ComboBoxButton) jComponent;
		for (final JIRAIssueGroupBy groupBy : JIRAIssueGroupBy.values()) {
			g.add(new AnAction(groupBy.toString()) {
				public void actionPerformed(AnActionEvent e) {
					button.setText(e.getPresentation().getText());
					IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(e);
					if (panel != null) {
						panel.setGroupBy(groupBy);
					}
				}
			});
		}

		return g;
	}

	public void update(AnActionEvent event) {
		super.update(event);
		IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(event);
		if (panel != null) {
			event.getPresentation().setText(panel.getGroupBy().toString());
		}
	}
}
