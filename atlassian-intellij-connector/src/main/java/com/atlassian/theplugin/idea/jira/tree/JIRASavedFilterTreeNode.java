package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.commons.jira.api.JIRASavedFilter;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.ui.tree.paneltree.AbstractTreeNode;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public class JIRASavedFilterTreeNode extends AbstractTreeNode {
	private JIRASavedFilter savedFilter;
	private ServerData serverData;


	public JIRASavedFilterTreeNode(final JIRASavedFilter savedFilter, final ServerData jiraServerCfg) {
		super(savedFilter.getName(), null, null);
		this.savedFilter = savedFilter;
		this.serverData = jiraServerCfg;
	}

	public String toString() {
		return savedFilter.getName();
	}

	public JComponent getRenderer(final JComponent c, final boolean selected,
			final boolean expanded, final boolean hasFocus) {


		return new JLabel("Incorrect renderer");
	}

	public ServerData getServerData() {
		return serverData;
	}

	public JIRASavedFilter getSavedFilter() {
		return savedFilter;
	}


}
