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

/**
 * Represents server type node on servers JTree
 * User: mwent
 * Date: 2008-01-29
 * Time: 09:32:04
 */
public class ServerTypeNode extends DefaultMutableTreeNode {
	static final long serialVersionUID = -1192703287399203299L;

	private ServerType serverType;

	public ServerTypeNode(ServerType aServerType) {
		this.serverType = aServerType;
	}

	public ServerType getServerType() {
		return serverType;
	}

	public void setServerType(ServerType serverType) {
		this.serverType = serverType;
	}

	public String toString() {
		return serverType.toString();
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ServerTypeNode that = (ServerTypeNode) o;

		if (serverType != that.serverType) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		return serverType.hashCode();
	}
}
