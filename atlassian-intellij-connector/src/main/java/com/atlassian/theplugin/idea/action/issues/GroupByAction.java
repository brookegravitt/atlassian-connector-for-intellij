package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.atlassian.theplugin.idea.jira.JIRAIssueGroupBy;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GroupByAction extends AnAction implements CustomComponentAction {

	private JComboBox combo;

	public void actionPerformed(AnActionEvent e) {
	}

	public JComponent createCustomComponent(Presentation presentation) {
		combo = new JComboBox(createModel());
		combo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(
						IdeaHelper.getCurrentProject(DataManager.getInstance().getDataContext()));
				if (panel != null) {
					panel.setGroupBy((JIRAIssueGroupBy) combo.getSelectedItem());
				}
			}
		});
		return combo;
	}

	private ComboBoxModel createModel() {
		return new DefaultComboBoxModel(JIRAIssueGroupBy.values());
	}

	public void update(AnActionEvent event) {
		super.update(event);
		IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(event);
		if (panel != null) {
			if (!panel.getGroupBy().equals(combo.getSelectedItem())) {
				combo.setSelectedItem(panel.getGroupBy());
			}
		}
	}
}
