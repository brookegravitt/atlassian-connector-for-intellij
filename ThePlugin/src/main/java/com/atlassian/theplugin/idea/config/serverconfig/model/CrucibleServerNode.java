package com.atlassian.theplugin.idea.config.serverconfig.model;

import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.ServerType;

/**
 * Represents Crucible Server in servers JTree
 * User: mwent
 * Date: 2008-01-29
 * Time: 09:51:00
 */
public class CrucibleServerNode extends ServerNode {
	static final long serialVersionUID = -5578412486422465295L;
	
	public CrucibleServerNode(Server aServer) {
		super(aServer);
	}

    public ServerType getServerType() {
        return ServerType.CRUCIBLE_SERVER;
    }
}
