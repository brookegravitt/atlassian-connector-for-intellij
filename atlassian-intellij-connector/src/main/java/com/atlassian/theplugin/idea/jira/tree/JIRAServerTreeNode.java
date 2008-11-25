package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.jira.model.JIRAFilterListModel;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;

/**
 * User: pmaruszak
 */
public class JIRAServerTreeNode extends JIRAAbstractTreeNode {
	private static final Icon JIRA_SERVER_ENABLED_ICON = IconLoader.getIcon("/icons/jira-blue-16.png");
	private static final Icon JIRA_SERVER_DISABLED_ICON = IconLoader.getIcon("/icons/jira-grey-16.png");
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

		Color bgColor = UIUtil.getTreeTextBackground();
		Color fgColor = UIUtil.getTreeTextForeground();

		JLabel label = new JLabel(jiraServer.getName(), JIRA_SERVER_ENABLED_ICON, SwingUtilities.LEADING);
		label.setForeground(fgColor);
		label.setBackground(bgColor);
		label.setDisabledIcon(JIRA_SERVER_DISABLED_ICON);		

		label.setEnabled(c.isEnabled());
		label.setOpaque(true);
		return label;

	}

	public JiraServerCfg getJiraServer() {
		return jiraServer;
	}

	public void onSelect() {
		if (listModel.getFilterTypeSlection() == JIRAFilterListModel.TypeOfFilterSelected.SAVED) {

			listModel.selectSavedFilter(listModel.getJiraSelectedServer(), listModel.getJiraSelectedSavedFilter());
		} else if (listModel.getFilterTypeSlection() == JIRAFilterListModel.TypeOfFilterSelected.MANUAL) {
			listModel.selectManualFilter(listModel.getJiraSelectedServer(), listModel.getJiraSelectedManualFilter());

		}

	}
}
