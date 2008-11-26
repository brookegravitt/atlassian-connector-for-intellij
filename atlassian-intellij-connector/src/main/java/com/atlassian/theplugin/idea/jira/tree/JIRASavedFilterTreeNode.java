package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.jira.api.JIRASavedFilter;
import com.atlassian.theplugin.jira.model.JIRAFilterListModel;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public class JIRASavedFilterTreeNode extends JIRAAbstractTreeNode {	
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


		return new JLabel("Incorrect renderer");
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
