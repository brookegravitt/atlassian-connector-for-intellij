package com.atlassian.theplugin.idea.serverconfig.model;

import com.atlassian.theplugin.configuration.Server;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Represents any Server in servers JTree
 * User: mwent
 * Date: 2008-01-29
 * Time: 09:49:47
 */
public class ServerNode extends DefaultMutableTreeNode {
	private Server server;

	public ServerNode(Server aServer) {
		this.server = aServer;
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
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
