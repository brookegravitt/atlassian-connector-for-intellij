package com.atlassian.theplugin.idea.config.serverconfig.model;

import com.atlassian.theplugin.ServerType;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * Model for server JTree. Constructs supported server type nodes
 * User: mwent
 * Date: 2008-01-28
 * Time: 16:52:54
 */
public class ServerTreeModel extends DefaultTreeModel {
	static final long serialVersionUID = 1631701743528670523L;

	public ServerTreeModel(RootNode root) {
		super(root);
	}

	public ServerTypeNode getServerTypeNode(ServerType serverType, boolean addIfMissing) {
		for (int i = 0; i < root.getChildCount(); ++i) {
			ServerTypeNode serverTypeNode = (ServerTypeNode) root.getChildAt(i);
			if (serverTypeNode.getServerType() == serverType) {
				return serverTypeNode;
			}
		}
		if (addIfMissing) {
			insertNodeInto(new ServerTypeNode(serverType), (DefaultMutableTreeNode) root, root.getChildCount());
			for (int i = 0; i < root.getChildCount(); ++i) {
				ServerTypeNode serverTypeNode = (ServerTypeNode) root.getChildAt(i);
				if (serverTypeNode.getServerType() == serverType) {
					return serverTypeNode;
				}
			}
		}
		return null;
	}
}
