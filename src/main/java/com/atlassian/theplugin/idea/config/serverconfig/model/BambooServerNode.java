package com.atlassian.theplugin.idea.config.serverconfig.model;

import com.atlassian.theplugin.configuration.ServerBean;
import com.atlassian.theplugin.ServerType;

/**
 * Represents Bambbo server on servers JTree
 * User: mwent
 * Date: 2008-01-31
 * Time: 09:25:29
 */
public class BambooServerNode extends ServerNode {
	static final long serialVersionUID = -6944317541000292469L;
	
	public BambooServerNode(ServerBean aServer) {
		super(aServer);
	}

    public ServerType getServerType() {
        return ServerType.BAMBOO_SERVER;
    }
}
