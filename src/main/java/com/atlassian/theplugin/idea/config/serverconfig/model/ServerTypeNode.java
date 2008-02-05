package com.atlassian.theplugin.idea.config.serverconfig.model;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Represents server type node on servers JTree
 * User: mwent
 * Date: 2008-01-29
 * Time: 09:32:04
 */
public class ServerTypeNode extends DefaultMutableTreeNode {
	ServerType serverType;

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
