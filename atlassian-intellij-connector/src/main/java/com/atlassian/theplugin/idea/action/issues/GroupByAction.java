package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.atlassian.theplugin.idea.jira.JiraIssueGroupBy;
import com.atlassian.theplugin.idea.ui.ComboWithLabel;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GroupByAction extends JIRAAbstractAction implements CustomComponentAction {
	private static final String COMBOBOX_KEY = GroupByAction.class.getName() + ".combo";


	@Override
	public void actionPerformed(AnActionEvent e) {
	}

	public JComponent createCustomComponent(Presentation presentation) {
		final JComboBox combo = new JComboBox(createModel());
		ComboWithLabel cwl = new ComboWithLabel(combo, "Group By");

		Project project = IdeaHelper.getCurrentProject(DataManager.getInstance().getDataContext());
		if (project != null) {
			IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(project);
			updateSelection(panel, combo);
		}

		presentation.putClientProperty(COMBOBOX_KEY, combo);
		combo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final Project currentProject = IdeaHelper.getCurrentProject(DataManager.getInstance().getDataContext(combo));
				if (currentProject != null) {
					IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(currentProject);
					if (panel != null) {
						panel.setGroupBy((JiraIssueGroupBy) combo.getSelectedItem());
					} else {
						LoggerImpl.getInstance().error(GroupByAction.class.getName() + ": cannot find "
								+ IssueListToolWindowPanel.class);
					}
				} else {
					System.out.println("current project is null");
					LoggerImpl.getInstance().error(GroupByAction.class.getName() + ": cannot determine current project");
				}

			}
		});
		return cwl;
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
		IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(event);
		if (myProperty instanceof JComboBox) {
			final JComboBox jComboBox = (JComboBox) myProperty;
			updateSelection(panel, jComboBox);
			if (ModelFreezeUpdater.getState(event)) {
				boolean e = panel != null && (panel.getSelectedServer() != null || panel.isRecentlyOpenFilterSelected());
				jComboBox.setEnabled(e);
			}
		}
	}

	private void updateSelection(IssueListToolWindowPanel panel, JComboBox combo) {
		if (panel != null && !panel.getGroupBy().equals(combo.getSelectedItem())) {
			combo.setSelectedItem(panel.getGroupBy());
		}
	}
}