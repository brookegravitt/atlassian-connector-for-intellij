package com.atlassian.theplugin.idea.serverconfig.model;

import com.atlassian.theplugin.configuration.ServerBean;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Represents any Server in servers JTree
 * User: mwent
 * Date: 2008-01-29
 * Time: 09:49:47
 */
public class ServerNode extends DefaultMutableTreeNode {
	private ServerBean server;

	public ServerNode(ServerBean aServer) {
		this.server = aServer;
	}

	public ServerBean getServer() {
		return server;
	}

	public void setServer(ServerBean server) {
		this.server = server;
	}

	public String toString() {
		return server.getName();
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ServerNode that = (ServerNode) o;

		if (!server.equals(that.server)) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		return server.hashCode();
	}
}
