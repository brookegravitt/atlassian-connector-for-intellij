package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.jira.model.JIRAFilterListModel;
import com.atlassian.theplugin.jira.model.JIRAManualFilter;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;

/**
 * User: pmaruszak
 */
public class JIRAManualFilterTreeNode extends JIRAAbstractTreeNode {
	private static final Icon JIRA_FILTER_ICON = IconLoader.getIcon("/actions/showViewer.png");
	private JIRAManualFilter manualFilter;
	private JIRAFilterListModel listModel;

	public JIRAManualFilterTreeNode(final JIRAFilterListModel listModel, final JIRAManualFilter manualFilter) {
		this.listModel = listModel;
		this.manualFilter = manualFilter;

	}

	public String toString() {
		return manualFilter.getName();
	}

	public JComponent getRenderer(final JComponent c, final boolean selected,
	                              final boolean expanded, final boolean hasFocus) {


		Color bgColor = selected ? UIUtil.getTreeSelectionBackground() : UIUtil.getTreeTextBackground();
		Color fgColor = selected ? UIUtil.getTreeSelectionForeground() : UIUtil.getTreeTextForeground();

		fgColor = c.isEnabled() ? fgColor : UIUtil.getInactiveTextColor();

		JLabel label = new JLabel("Custom filter", JIRA_FILTER_ICON, SwingUtilities.LEADING);
		label.setForeground(fgColor);
		label.setDisabledIcon(JIRA_FILTER_ICON);
		label.setBackground(bgColor);		

		label.setEnabled(c.isEnabled());
		label.setOpaque(true);
		return label;
	}

	public void onSelect() {
		if (listModel != null) {
			listModel.selectManualFilter(((JIRAServerTreeNode) getParent()).getJiraServer(), manualFilter);

		}
	}

	public JIRAManualFilter getManualFilter() {
		return manualFilter;
	}
}
