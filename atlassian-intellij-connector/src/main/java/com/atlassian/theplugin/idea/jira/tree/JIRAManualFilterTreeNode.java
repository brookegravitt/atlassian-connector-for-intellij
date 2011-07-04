package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.ui.tree.paneltree.AbstractTreeNode;
import com.atlassian.theplugin.jira.model.JiraCustomFilter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public class JIRAManualFilterTreeNode extends AbstractTreeNode {
    private final ProjectCfgManager projectCfgManager;
    private JiraCustomFilter manualFilter;
    private ServerId serverId;

	public JIRAManualFilterTreeNode(@NotNull ProjectCfgManager projectCfgManager, final JiraCustomFilter manualFilter,
                                    ServerData jiraServerCfg) {
		super(manualFilter.getName(), null, null);
        this.projectCfgManager = projectCfgManager;
        this.manualFilter = manualFilter;

		this.serverId = jiraServerCfg.getServerId();
	}

	public String toString() {
		return manualFilter.getName();
	}

	public JComponent getRenderer(final JComponent c, final boolean selected,
			final boolean expanded, final boolean hasFocus) {

        return new JLabel("Invalid renderer");
	}


	public ServerData getJiraServerCfg() {
		return projectCfgManager.getJiraServerr(serverId);
	}

	public JiraCustomFilter getManualFilter() {
		return manualFilter;
	}
    
}
