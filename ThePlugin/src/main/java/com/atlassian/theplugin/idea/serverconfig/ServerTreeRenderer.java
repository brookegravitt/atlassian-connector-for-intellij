package com.atlassian.theplugin.idea.serverconfig;

import com.atlassian.theplugin.idea.serverconfig.model.BambooServerNode;
import com.atlassian.theplugin.idea.serverconfig.model.CrucibleServerNode;
import com.atlassian.theplugin.idea.serverconfig.model.ServerTypeNode;
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
	private static Icon bambooServerIcon;
	private static Icon crucibleServersIcon;
	private static Icon crucibleServerIcon;


	static {
		bambooServersIcon = IconLoader.getIcon("/icons/blue-16.png");
		bambooServerIcon = IconLoader.getIcon("/icons/grey-16.png");
		crucibleServersIcon = IconLoader.getIcon("/icons/crucible-16.png");
		crucibleServerIcon = IconLoader.getIcon("/icons/crucible-16.png");		
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
			label.setIcon(bambooServerIcon);
		}

		if (value instanceof CrucibleServerNode) {
			label.setIcon(crucibleServerIcon);
		}


		return label;
	}
}
