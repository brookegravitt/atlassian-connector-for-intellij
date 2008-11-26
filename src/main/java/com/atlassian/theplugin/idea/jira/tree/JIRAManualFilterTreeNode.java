package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.jira.model.JIRAFilterListModel;
import com.atlassian.theplugin.jira.model.JIRAManualFilter;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public class JIRAManualFilterTreeNode extends JIRAAbstractTreeNode {	
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
		return new JLabel("Incorrect renderer");
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
