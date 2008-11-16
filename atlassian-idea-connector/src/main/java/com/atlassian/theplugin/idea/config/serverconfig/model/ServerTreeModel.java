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
			final ServerTypeNode child = new ServerTypeNode(serverType);
			insertNodeInto(child, (DefaultMutableTreeNode) root, root.getChildCount());
			this.nodeChanged(root);
			return child;
		}
		return null;
	}
}
