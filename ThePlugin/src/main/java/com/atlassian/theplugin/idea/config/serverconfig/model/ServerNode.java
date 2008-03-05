package com.atlassian.theplugin.idea.config.serverconfig.model;

import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.ServerType;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Represents any Server in servers JTree
 * User: mwent
 * Date: 2008-01-29
 * Time: 09:49:47
 */
public abstract class ServerNode extends DefaultMutableTreeNode {
	private Server server;

	public ServerNode(Server aServer) {
		this.server = aServer;
	}

    public abstract ServerType getServerType();

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
