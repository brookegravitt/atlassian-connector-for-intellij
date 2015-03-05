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

package com.atlassian.theplugin.idea.config.serverconfig.model;

import com.atlassian.theplugin.commons.ServerType;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

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
		for (ServerType serverType : ServerType.values()) {
			if (serverType.isPseudoServer() || serverType.equals(ServerType.FISHEYE_SERVER)) {
				continue;
			}
			ServerTypeNode serverTypeNode = new ServerTypeNode(serverType);
			serverTypeNode.add(new ServerInfoNode(serverType));
			root.add(serverTypeNode);
		}
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


	public void insertNodeInto(final MutableTreeNode newChild, final MutableTreeNode parent, final int index) {
		int newIndex = index;
		for (int i = 0; i < parent.getChildCount(); i++) {
			if (parent instanceof ServerTypeNode && parent.getChildAt(i) instanceof ServerInfoNode) {
				parent.remove(i);
				super.nodeStructureChanged(parent);
				newIndex = index > 0 ? index - 1 : 0;
				break;
			}
		}
		super.insertNodeInto(newChild, parent, newIndex);
	}

	public void nodeStructureChanged(final TreeNode node) {
		if (node != null) {
			for (int i = 0; i < node.getChildCount(); i++) {
				if (node instanceof ServerTypeNode && node.getChildAt(i) instanceof ServerInfoNode) {
					((ServerTypeNode) node).remove(i);
					break;
				}
			}
			if (node.getChildCount() == 0) {
				if (node instanceof ServerTypeNode) {
					ServerTypeNode serverTypeNode = (ServerTypeNode) node;
					serverTypeNode.add(new ServerInfoNode(serverTypeNode.getServerType()));
				}
			}
			super.nodeStructureChanged(node);
		}
	}
}
