package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.ui.tree.paneltree.AbstractTreeNode;
import com.atlassian.theplugin.jira.model.JIRAManualFilter;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public class JIRAManualFilterTreeNode extends AbstractTreeNode {
	private JIRAManualFilter manualFilter;
	private ServerData jiraServerCfg;
	//private JIRAFilterListModel listModel;

	public JIRAManualFilterTreeNode(final JIRAManualFilter manualFilter, ServerData jiraServerCfg) {
		super(manualFilter.getName(), null, null);
		this.manualFilter = manualFilter;

		this.jiraServerCfg = jiraServerCfg;
	}

	public String toString() {
		return manualFilter.getName();
	}

	public JComponent getRenderer(final JComponent c, final boolean selected,
			final boolean expanded, final boolean hasFocus) {
		return new JLabel("Incorrect renderer");
	}

	public ServerData getJiraServerCfg() {
		return jiraServerCfg;
	}

	public JIRAManualFilter getManualFilter() {
		return manualFilter;
	}
}
