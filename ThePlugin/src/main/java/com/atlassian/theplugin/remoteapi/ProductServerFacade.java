package com.atlassian.theplugin.remoteapi;

import com.atlassian.theplugin.ServerType;


public interface ProductServerFacade {
	void testServerConnection(String url, String userName, String password) throws RemoteApiException;

	ServerType getServerType();
}
