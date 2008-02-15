package com.atlassian.theplugin.idea.config.serverconfig.model;

import com.atlassian.theplugin.configuration.ServerBean;
import com.atlassian.theplugin.ServerType;

/**
 * Represents JIRA Server in servers JTree
 */
public class JIRAServerNode extends ServerNode {
	public JIRAServerNode(ServerBean aServer) {
		super(aServer);
	}

    public ServerType getServerType() {
        return ServerType.JIRA_SERVER;
    }
}