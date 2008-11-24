package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.jira.api.JIRASavedFilter;
import com.atlassian.theplugin.jira.model.JIRAFilterListModel;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;

/**
 * User: pmaruszak
 */
public class JIRASavedFilterTreeNode extends JIRAAbstractTreeNode {
	private static final Icon JIRA_FILTER_ICON = IconLoader.getIcon("/actions/showSource.png");

	private JIRASavedFilter savedFilter;

	private JIRAFilterListModel listModel;

	public JIRASavedFilterTreeNode(final JIRAFilterListModel listModel, final JIRASavedFilter savedFilter) {
		this.listModel = listModel;
		this.savedFilter = savedFilter;
	}

	public String toString() {
		return savedFilter.getName();
	}

	public JComponent getRenderer(final JComponent c, final boolean selected,
	                              final boolean expanded, final boolean hasFocus) {

		Color bgColor = selected ? UIUtil.getTreeSelectionBackground() : UIUtil.getTreeTextBackground();
		Color fgColor = selected ? UIUtil.getTreeSelectionForeground() : UIUtil.getTreeTextForeground();

		fgColor = c.isEnabled() ? fgColor : UIUtil.getInactiveTextColor();

		JLabel label = new JLabel(savedFilter.getName(), JIRA_FILTER_ICON, SwingUtilities.LEADING);
		label.setDisabledIcon(JIRA_FILTER_ICON);
		label.setForeground(fgColor);
		label.setBackground(bgColor);			

		label.setEnabled(c.isEnabled());
		label.setOpaque(true);
		return label;
	}

	public void onSelect() {
		if (listModel != null && savedFilter != null) {
			listModel.selectSavedFilter(((JIRAServerTreeNode) getParent()).getJiraServer(), savedFilter);
		}
	}

	public JIRASavedFilter getSavedFilter() {
		return savedFilter;
	}


}
