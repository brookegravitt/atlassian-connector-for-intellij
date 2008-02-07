package com.atlassian.theplugin.idea.config.serverconfig.model;

import com.atlassian.theplugin.configuration.ServerBean;

/**
 * Represents Bambbo server on servers JTree
 * User: mwent
 * Date: 2008-01-31
 * Time: 09:25:29
 */
public class BambooServerNode extends ServerNode {
	public BambooServerNode(ServerBean aServer) {
		super(aServer);
	}

    public ServerType getServerType() {
        return ServerType.BAMBOO_SERVER;
    }
}
