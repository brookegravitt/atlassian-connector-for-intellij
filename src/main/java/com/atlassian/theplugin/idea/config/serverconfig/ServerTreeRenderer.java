package com.atlassian.theplugin.idea.config.serverconfig;

import com.atlassian.theplugin.idea.config.serverconfig.model.BambooServerNode;
import com.atlassian.theplugin.idea.config.serverconfig.model.CrucibleServerNode;
import com.atlassian.theplugin.idea.config.serverconfig.model.ServerTypeNode;
import com.atlassian.theplugin.idea.config.serverconfig.model.JIRAServerNode;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class ServerTreeRenderer extends DefaultTreeCellRenderer {
	private static Icon bambooServersIcon;
	private static Icon bambooServerEnabledIcon;
	private static Icon bambooServerDisabledIcon;
	private static Icon crucibleServersIcon;
	private static Icon crucibleServerEnabledIcon;
	private static Icon crucibleServerDisabledIcon;
	private static Icon jiraServersIcon;
	private static Icon jiraServerEnabledIcon;
	private static Icon jiraServerDisabledIcon;

	static {
		bambooServersIcon = IconLoader.getIcon("/icons/bamboo-blue-16.png");
		bambooServerEnabledIcon = IconLoader.getIcon("/icons/bamboo-blue-16.png");
		bambooServerDisabledIcon = IconLoader.getIcon("/icons/bamboo-grey-16.png");
		crucibleServersIcon = IconLoader.getIcon("/icons/crucible-blue-16.png");
		crucibleServerEnabledIcon = IconLoader.getIcon("/icons/crucible-blue-16.png");
		crucibleServerDisabledIcon = IconLoader.getIcon("/icons/crucible-grey-16.png");
		jiraServersIcon = IconLoader.getIcon("/icons/jira-blue-16.png");
		jiraServerEnabledIcon = IconLoader.getIcon("/icons/jira-blue-16.png");
		jiraServerDisabledIcon = IconLoader.getIcon("/icons/jira-blue-16.png");
	}

	public Component getTreeCellRendererComponent(
            JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		if (value instanceof ServerTypeNode) {
			switch (((ServerTypeNode) value).getServerType()) {
				case BAMBOO_SERVER:
                    label.setIcon(bambooServersIcon);
					break;
				case CRUCIBLE_SERVER:
					label.setIcon(crucibleServersIcon);
					break;
				case JIRA_SERVER:
					label.setIcon(jiraServersIcon);
					break;
                default:
                    break;
            }
		}

		if (value instanceof BambooServerNode) {
			if (((BambooServerNode) value).getServer().getEnabled()) {
				label.setIcon(bambooServerEnabledIcon);
			} else {
				label.setIcon(bambooServerDisabledIcon);
			}
		}
		if (value instanceof JIRAServerNode) {
			if (((JIRAServerNode) value).getServer().getEnabled()) {
				label.setIcon(jiraServerEnabledIcon);
			} else {
				label.setIcon(jiraServerDisabledIcon);
			}
		}

		if (value instanceof CrucibleServerNode) {
			if (((CrucibleServerNode) value).getServer().getEnabled()) {
				label.setIcon(crucibleServerEnabledIcon);
			} else {
				label.setIcon(crucibleServerDisabledIcon);
			}
		}


		return label;
	}
}
