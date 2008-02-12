package com.atlassian.theplugin.idea.config.serverconfig;

import com.atlassian.theplugin.idea.config.serverconfig.model.BambooServerNode;
import com.atlassian.theplugin.idea.config.serverconfig.model.CrucibleServerNode;
import com.atlassian.theplugin.idea.config.serverconfig.model.ServerTypeNode;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-02-01
 * Time: 11:20:32
 * To change this template use File | Settings | File Templates.
 */
public class ServerTreeRenderer extends DefaultTreeCellRenderer {
	private static Icon bambooServersIcon;
	private static Icon bambooServerEnabledIcon;
	private static Icon bambooServerDisabledIcon;
	private static Icon crucibleServersIcon;
	private static Icon crucibleServerEnabledIcon;
	private static Icon crucibleServerDisabledIcon;

	static {
		bambooServersIcon = IconLoader.getIcon("/icons/bamboo-blue-16.png");
		bambooServerEnabledIcon = IconLoader.getIcon("/icons/bamboo-blue-16.png");
		bambooServerDisabledIcon = IconLoader.getIcon("/icons/bamboo-grey-16.png");
		crucibleServersIcon = IconLoader.getIcon("/icons/crucible-blue-16.png");
		crucibleServerEnabledIcon = IconLoader.getIcon("/icons/crucible-blue-16.png");
		crucibleServerDisabledIcon = IconLoader.getIcon("/icons/crucible-grey-16.png");
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		if (value instanceof ServerTypeNode) {
			switch (((ServerTypeNode)value).getServerType()) {
				case BAMBOO_SERVER:
                    label.setIcon(bambooServersIcon);
					break;
				case CRUCIBLE_SERVER:
					label.setIcon(crucibleServersIcon);
					break;
			}		
		}

		if (value instanceof BambooServerNode) {
			if (((BambooServerNode)value).getServer().getEnabled()) {
				label.setIcon(bambooServerEnabledIcon);
			} else {
				label.setIcon(bambooServerDisabledIcon);
			}
		}

		if (value instanceof CrucibleServerNode) {
			if (((BambooServerNode)value).getServer().getEnabled()) {
				label.setIcon(crucibleServerEnabledIcon);
			} else {
				label.setIcon(crucibleServerDisabledIcon);
			}
		}


		return label;
	}
}
