package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.ui.tree.paneltree.AbstractTreeNode;
import com.atlassian.theplugin.jira.model.JiraCustomFilter;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public class JIRAManualFilterTreeNode extends AbstractTreeNode {
	private JiraCustomFilter manualFilter;
	private ServerData jiraServerCfg;
	//private JIRAFilterListModel listModel;

	public JIRAManualFilterTreeNode(final JiraCustomFilter manualFilter, ServerData jiraServerCfg) {
		super(manualFilter.getName(), null, null);
		this.manualFilter = manualFilter;

		this.jiraServerCfg = jiraServerCfg;
	}

	public String toString() {
		return manualFilter.getName();
	}

	public JComponent getRenderer(final JComponent c, final boolean selected,
			final boolean expanded, final boolean hasFocus) {

        return new JLabel("Invalid renderer");
	}


	public ServerData getJiraServerCfg() {
		return jiraServerCfg;
	}

	public JiraCustomFilter getManualFilter() {
		return manualFilter;
	}
    
}
