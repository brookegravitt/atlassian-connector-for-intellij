package com.atlassian.theplugin.idea.config.serverconfig.model;

import com.atlassian.theplugin.configuration.ServerBean;
import com.atlassian.theplugin.ServerType;

/**
 * Represents JIRA Server in servers JTree
 */
public class JIRAServerNode extends ServerNode {
    static final long serialVersionUID = -6944317541000292469L;

    public JIRAServerNode(ServerBean aServer) {
		super(aServer);
	}

    public ServerType getServerType() {
        return ServerType.JIRA_SERVER;
    }
}