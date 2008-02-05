package com.atlassian.theplugin.idea.config.serverconfig.model;

import javax.swing.tree.DefaultTreeModel;

/**
 * Model for server JTree. Constructs supported server type nodes
 * User: mwent
 * Date: 2008-01-28
 * Time: 16:52:54
 */
public class ServerTreeModel extends DefaultTreeModel {

	public ServerTreeModel(RootNode root) {
		super(root);

		insertNodeInto(new ServerTypeNode(ServerType.BAMBOO_SERVER), root, root.getChildCount());
		insertNodeInto(new ServerTypeNode(ServerType.CRUCIBLE_SERVER), root, root.getChildCount());
	}

	public ServerTypeNode getServerTypeNode(ServerType serverType) {
		for (int i = 0; i < root.getChildCount(); ++i) {
			ServerTypeNode serverTypeNode = (ServerTypeNode) root.getChildAt(i);
			if (serverTypeNode.getServerType() == serverType) {
				return serverTypeNode;
			}
		}
		return null;
	}
}
