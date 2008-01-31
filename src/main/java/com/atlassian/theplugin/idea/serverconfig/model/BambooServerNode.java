package com.atlassian.theplugin.idea.serverconfig.model;

import com.atlassian.theplugin.configuration.Server;

/**
 * Represents Bambbo server on servers JTree
 * User: mwent
 * Date: 2008-01-31
 * Time: 09:25:29
 */
public class BambooServerNode extends ServerNode {
	public BambooServerNode(Server aServer) {
		super(aServer);
	}
}
