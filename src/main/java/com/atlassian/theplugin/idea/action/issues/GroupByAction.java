package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.atlassian.theplugin.idea.jira.JiraIssueGroupBy;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GroupByAction extends JIRAAbstractAction implements CustomComponentAction {
	private final String COMBOBOX_KEY = GroupByAction.class.getName() + ".combo";

	@Override
	public void actionPerformed(AnActionEvent e) {
	}

	public JComponent createCustomComponent(Presentation presentation) {
		final JComboBox combo = new JComboBox(createModel());
		presentation.putClientProperty(COMBOBOX_KEY, combo);
		combo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final Project currentProject = IdeaHelper.getCurrentProject(DataManager.getInstance().getDataContext(combo));
				if (currentProject != null) {
					IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(currentProject);
					if (panel != null) {
						panel.setGroupBy((JiraIssueGroupBy) combo.getSelectedItem());
					} else {
						LoggerImpl.getInstance().error(GroupByAction.class.getName() + ": cannot find "
								+ IssuesToolWindowPanel.class);
					}
				} else {
					LoggerImpl.getInstance().error(GroupByAction.class.getName() + ": cannot determine current project");
				}
				
			}
		});		
		return combo;
	}


	private ComboBoxModel createModel() {
		return new DefaultComboBoxModel(JiraIssueGroupBy.values());
	}

	@Override
	public void onUpdate(AnActionEvent event) {
	}

	@Override
	public void onUpdate(AnActionEvent event, boolean enabled) {
		Object myProperty = event.getPresentation().getClientProperty(COMBOBOX_KEY);
		if (myProperty instanceof JComboBox) {
			final JComboBox jComboBox = (JComboBox) myProperty;
			IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(event);
			if (panel != null && !panel.getGroupBy().equals(jComboBox.getSelectedItem())) {
				jComboBox.setSelectedItem(panel.getGroupBy());
			}
		}
	}
 }