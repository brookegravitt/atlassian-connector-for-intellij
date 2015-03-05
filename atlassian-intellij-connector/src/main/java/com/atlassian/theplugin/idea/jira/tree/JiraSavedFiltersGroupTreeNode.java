package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public class JiraSavedFiltersGroupTreeNode extends AbstractJiraFilterGroupTreeNode {

    private static final Icon JIRA_SAVED_FILTER_ICON = IconLoader.getIcon("/icons/jira/nodes/ico_jira_saved_filter.png");

	public JiraSavedFiltersGroupTreeNode(ProjectCfgManager projectCfgManager, JiraServerData jiraServer) {
		super(projectCfgManager, jiraServer);
	}

    @Override
    public Icon getIcon() {
        return JIRA_SAVED_FILTER_ICON;
    }

    @Override
    public String toString() {
        return "Saved Filters";
    }
}