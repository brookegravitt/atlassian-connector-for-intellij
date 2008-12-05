package com.atlassian.theplugin.idea.action.reviews;

import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.CrucibleReviewGroupBy;
import com.atlassian.theplugin.idea.crucible.ReviewsToolWindowPanel;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * User: jgorycki
 * Date: Dec 5, 2008
 * Time: 2:26:25 PM
 */
public class GroupByAction extends AnAction implements CustomComponentAction {
	private static final String COMBOBOX_KEY = GroupByAction.class.getName() + ".combo";

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
					ReviewsToolWindowPanel panel = IdeaHelper.getReviewsToolWindowPanel(currentProject);
					if (panel != null) {
						panel.setGroupBy((CrucibleReviewGroupBy) combo.getSelectedItem());
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
		return new DefaultComboBoxModel(CrucibleReviewGroupBy.values());
	}

	@Override
	public final void update(AnActionEvent event) {
		Object myProperty = event.getPresentation().getClientProperty(COMBOBOX_KEY);
		if (myProperty instanceof JComboBox) {
			final JComboBox jComboBox = (JComboBox) myProperty;
			ReviewsToolWindowPanel panel = IdeaHelper.getReviewsToolWindowPanel(event);
			if (panel != null && !panel.getGroupBy().equals(jComboBox.getSelectedItem())) {
				jComboBox.setSelectedItem(panel.getGroupBy());
			}
		}
	}

}
