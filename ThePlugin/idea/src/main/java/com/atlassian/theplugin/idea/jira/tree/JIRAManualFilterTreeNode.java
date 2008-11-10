package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.jira.model.JIRAFilterListModel;
import com.atlassian.theplugin.jira.model.JIRAManualFilter;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

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

		JLabel label = new JLabel((manualFilter != null ? manualFilter.getName() : "manual filter not defined"),
				JIRA_FILTER_ICON, SwingUtilities.HORIZONTAL);
		return label;
	}

	public void onSelect() {
		if (listModel != null && manualFilter != null) {
			listModel.selectManualFilter(((JIRAServerTreeNode) getParent()).getJiraServer(), manualFilter);
		}
	}
}
