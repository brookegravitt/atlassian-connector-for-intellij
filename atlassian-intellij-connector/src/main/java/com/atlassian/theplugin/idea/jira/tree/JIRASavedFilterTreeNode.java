package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.connector.commons.jira.beans.JIRASavedFilter;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.ui.tree.paneltree.AbstractTreeNode;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;



/**
 * User: pmaruszak
 */
public class JIRASavedFilterTreeNode extends AbstractTreeNode {
    private final ProjectCfgManager projectCfgManager;
    private JIRASavedFilter savedFilter;
    private ServerId serverId;

	public JIRASavedFilterTreeNode(@NotNull ProjectCfgManager projectCfgManager, final JIRASavedFilter savedFilter,
                                final ServerData jiraServerCfg) {
		super(savedFilter.getName(), null, null);
        this.projectCfgManager = projectCfgManager;
        this.savedFilter = savedFilter;
        this.serverId = jiraServerCfg.getServerId();

	}

	public String toString() {
		return savedFilter.getName();
	}

	public JComponent getRenderer(final JComponent c, final boolean selected,
			final boolean expanded, final boolean hasFocus) {


		return new JLabel("Incorrect renderer");
	}

	public ServerData getServerData() {
		return getJiraServerData();
	}

	public JIRASavedFilter getSavedFilter() {
		return savedFilter;
	}

    private ServerData getJiraServerData() {
        return projectCfgManager.getJiraServerr(serverId);

    }

}
