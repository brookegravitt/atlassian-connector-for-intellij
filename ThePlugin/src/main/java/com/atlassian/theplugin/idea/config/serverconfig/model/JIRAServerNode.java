package com.atlassian.theplugin.idea.config.serverconfig.model;

import com.atlassian.theplugin.ServerType;
import com.atlassian.theplugin.configuration.Server;

/**
 * Represents JIRA Server in servers JTree
 */
public class JIRAServerNode extends ServerNode {
    static final long serialVersionUID = -6944317541000292469L;

    public JIRAServerNode(Server aServer) {
		super(aServer);
	}

    public ServerType getServerType() {
        return ServerType.JIRA_SERVER;
    }
}