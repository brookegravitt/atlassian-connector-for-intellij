package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.theplugin.commons.jira.JiraServerData;

import javax.swing.*;

/**
 * User: kalamon
 * Date: 2009-12-15
 * Time: 16:04:40
 */
public abstract class AbstractJiraFilterGroupTreeNode extends JIRAServerTreeNode {

    public AbstractJiraFilterGroupTreeNode(ProjectCfgManager projectCfgManager, JiraServerData jiraServer) {
        super(projectCfgManager, jiraServer);
    }

    public abstract Icon getIcon();
}
