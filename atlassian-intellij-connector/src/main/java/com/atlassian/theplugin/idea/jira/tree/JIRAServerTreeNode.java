package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.idea.ui.tree.paneltree.AbstractTreeNode;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public class JIRAServerTreeNode extends AbstractTreeNode {
	private static final Icon JIRA_SERVER_ENABLED_ICON = IconLoader.getIcon("/icons/jira-blue-16.png");
	private static final Icon JIRA_SERVER_DISABLED_ICON = IconLoader.getIcon("/icons/jira-grey-16.png");
    private final ProjectCfgManager projectCfgManager;
    private ServerId serverId;

	public JIRAServerTreeNode(ProjectCfgManager projectCfgManager, JiraServerData jiraServer) {
		super(jiraServer.getName(), null, null);
        this.projectCfgManager = projectCfgManager;
        this.serverId = jiraServer != null ? jiraServer.getServerId() : null;

	}

	public String toString() {
		JiraServerData server = getJiraServerData();
        return server != null ? server.getName() : "";
	}

    @Nullable
    private JiraServerData getJiraServerData() {
        if (projectCfgManager != null) {
            return (JiraServerData) projectCfgManager.getJiraServerr(serverId);
        }

        return null;
    }
	public JComponent getRenderer(final JComponent c, final boolean selected, final boolean expanded, final boolean hasFocus) {

         return new JLabel("Invalid renderer");
//
//		Color bgColor = UIUtil.getTreeTextBackground();
//		Color fgColor = UIUtil.getTreeTextForeground();
//
//
//
//		JLabel label = new JLabel(jiraServer.getName(), JIRA_SERVER_ENABLED_ICON, SwingUtilities.LEADING);
//		label.setForeground(fgColor);
//		label.setBackground(bgColor);
//		label.setDisabledIcon(JIRA_SERVER_DISABLED_ICON);
//
//		label.setEnabled(c.isEnabled());
//		label.setOpaque(true);
//		return label;

	}

    @Nullable
	public JiraServerData getJiraServer() {
		return getJiraServerData();
	}
}
