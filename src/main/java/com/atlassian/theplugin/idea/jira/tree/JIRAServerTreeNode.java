package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.jira.model.JIRAFilterListModel;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public class JIRAServerTreeNode extends JIRAAbstractTreeNode {
	private static final Icon JIRA_SERVER_ICON = IconLoader.getIcon("/icons/jira-blue-16.png");
	private JiraServerCfg jiraServer;
	private JIRAFilterListModel listModel;

	public JIRAServerTreeNode(final JIRAFilterListModel listModel, JiraServerCfg jiraServer) {
		this.listModel = listModel;
		this.jiraServer = jiraServer;
	}

	public String toString() {
		return jiraServer.getName();
	}

	public JComponent getRenderer(final JComponent c, final boolean selected, final boolean expanded, final boolean hasFocus) {
		final JLabel label = new JLabel(jiraServer.getName(), JIRA_SERVER_ICON, SwingUtilities.HORIZONTAL);		
		return label;
	}

	public JiraServerCfg getJiraServer() {
		return jiraServer;
	}

	public void onSelect() {
	}
}
