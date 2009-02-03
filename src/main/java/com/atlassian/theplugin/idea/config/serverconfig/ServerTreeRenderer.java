/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.idea.config.serverconfig;

import com.atlassian.theplugin.idea.config.serverconfig.model.ServerInfoNode;
import com.atlassian.theplugin.idea.config.serverconfig.model.ServerNode;
import com.atlassian.theplugin.idea.config.serverconfig.model.ServerTypeNode;
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
	private static Icon fisheyeServerDisabledIcon;
	private static Icon fisheyeServerEnabledIcon;
	private static Icon fisheyeServersIcon;
	private static Icon infoIcon;

	static {
		bambooServersIcon = IconLoader.getIcon("/icons/bamboo-blue-16.png");
		bambooServerEnabledIcon = IconLoader.getIcon("/icons/bamboo-blue-16.png");
		bambooServerDisabledIcon = IconLoader.getIcon("/icons/bamboo-grey-16.png");
		crucibleServersIcon = IconLoader.getIcon("/icons/crucible-blue-16.png");
		crucibleServerEnabledIcon = IconLoader.getIcon("/icons/crucible-blue-16.png");
		crucibleServerDisabledIcon = IconLoader.getIcon("/icons/crucible-grey-16.png");
		jiraServersIcon = IconLoader.getIcon("/icons/jira-blue-16.png");
		jiraServerEnabledIcon = IconLoader.getIcon("/icons/jira-blue-16.png");
		jiraServerDisabledIcon = IconLoader.getIcon("/icons/jira-grey-16.png");
		fisheyeServersIcon = IconLoader.getIcon("/icons/fisheye-16.png");
		fisheyeServerEnabledIcon = IconLoader.getIcon("/icons/fisheye-16.png");
		fisheyeServerDisabledIcon = IconLoader.getIcon("/icons/fisheye-16-disabled.png");
		infoIcon = IconLoader.getIcon("/actions/help.png");
	}

	@Override
    public Component getTreeCellRendererComponent(
            JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        if (value instanceof ServerTypeNode) {
            final ServerTypeNode serverTypeNode = (ServerTypeNode) value;

			switch (serverTypeNode.getServerType()) {
				case BAMBOO_SERVER:
					label.setIcon(bambooServersIcon);
					break;
				case CRUCIBLE_SERVER:
					label.setIcon(crucibleServersIcon);
					break;
				case JIRA_SERVER:
					label.setIcon(jiraServersIcon);
					break;
				case FISHEYE_SERVER:
					label.setIcon(fisheyeServersIcon);
					break;
				default:
					break;

			}
        }

		if (value instanceof ServerInfoNode) {
			final ServerInfoNode serverInfoNode = (ServerInfoNode) value;
			String labelText = "";
			label = new JLabel();
			label.setIcon(infoIcon);

			switch (serverInfoNode.getServerType()) {
				case BAMBOO_SERVER:

					labelText = "Find out more about Bamboo";
					break;
				case CRUCIBLE_SERVER:
					labelText = "Find out more about Crucible";
					break;
				case JIRA_SERVER:
					labelText = "Find out more about JIRA";
					break;
				case FISHEYE_SERVER:
					labelText = "Find out more about FishEye";
					break;
				default:
					break;

			}
			label.setFont(label.getFont().deriveFont(Font.ITALIC));
			label.setText("<html><a href=\"" + serverInfoNode.getServerType().getInfoUrl() + "\">" + labelText + "</a></html>");
		}

        if (value instanceof ServerNode) {
            final ServerNode serverNode = (ServerNode) value;

			// CHECKSTYLE:OFF
			switch (serverNode.getServerType()) {
				case BAMBOO_SERVER:
					if (serverNode.getServer().isEnabled()) {
						label.setIcon(bambooServerEnabledIcon);
					} else {
						label.setIcon(bambooServerDisabledIcon);
					}
					break;
				case CRUCIBLE_SERVER:
					if (serverNode.getServer().isEnabled()) {
						label.setIcon(crucibleServerEnabledIcon);
					} else {
						label.setIcon(crucibleServerDisabledIcon);
					}
					break;
				case JIRA_SERVER:
					if (serverNode.getServer().isEnabled()) {
						label.setIcon(jiraServerEnabledIcon);
					} else {
						label.setIcon(jiraServerDisabledIcon);
					}
					break;
				case FISHEYE_SERVER:
					label.setIcon(serverNode.getServer().isEnabled()
							? fisheyeServerEnabledIcon : fisheyeServerDisabledIcon);
					break;
				default:
					assert false;

			}
        }


        return label;
	}
}
