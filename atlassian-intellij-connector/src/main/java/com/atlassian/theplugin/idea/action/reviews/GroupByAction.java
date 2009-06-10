package com.atlassian.theplugin.idea.action.reviews;

import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.CrucibleReviewGroupBy;
import com.atlassian.theplugin.idea.crucible.ReviewListToolWindowPanel;
import com.atlassian.theplugin.idea.ui.ComboWithLabel;
import com.intellij.ide.DataManager;
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
public class GroupByAction extends AbstractCrucibleToolbarAction implements CustomComponentAction {
	private static final String COMBOBOX_KEY = GroupByAction.class.getName() + ".combo";

	@Override
	public void actionPerformed(AnActionEvent e) {
	}

	public JComponent createCustomComponent(Presentation presentation) {
		final JComboBox combo = new JComboBox(createModel());
		ComboWithLabel cwl = new ComboWithLabel(combo, "Group By");

		Project project = IdeaHelper.getCurrentProject(DataManager.getInstance().getDataContext());
		if (project != null) {
			ReviewListToolWindowPanel panel = IdeaHelper.getReviewListToolWindowPanel(project);
			updateSelection(panel, combo);
		}

		presentation.putClientProperty(COMBOBOX_KEY, combo);
		combo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final Project currentProject = IdeaHelper.getCurrentProject(DataManager.getInstance().getDataContext(combo));
				if (currentProject != null) {
					ReviewListToolWindowPanel panel = IdeaHelper.getReviewListToolWindowPanel(currentProject);
					if (panel != null) {
						panel.setGroupBy((CrucibleReviewGroupBy) combo.getSelectedItem());
					} else {
						LoggerImpl.getInstance().error(GroupByAction.class.getName() + ": cannot find "
								+ ReviewListToolWindowPanel.class);
					}
				} else {
					LoggerImpl.getInstance().error(GroupByAction.class.getName() + ": cannot determine current project");
				}

			}
		});
		return cwl;
	}


	private ComboBoxModel createModel() {
		return new DefaultComboBoxModel(CrucibleReviewGroupBy.values());
	}

	@Override
	protected void onUpdateFinished(AnActionEvent e, boolean enabled) {
		Object myProperty = e.getPresentation().getClientProperty(COMBOBOX_KEY);
		if (myProperty instanceof JComboBox) {
			final JComboBox jComboBox = (JComboBox) myProperty;
			jComboBox.setEnabled(enabled);
			ReviewListToolWindowPanel panel = IdeaHelper.getReviewListToolWindowPanel(e);
			updateSelection(panel, jComboBox);
		}
	}

	private void updateSelection(ReviewListToolWindowPanel panel, JComboBox combo) {
		if (panel != null && !panel.getGroupBy().equals(combo.getSelectedItem())) {
			combo.setSelectedItem(panel.getGroupBy());
		}
	}
}
