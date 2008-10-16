package com.atlassian.theplugin.commons.fisheye;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.FishEyeServerCfg;
import com.atlassian.theplugin.commons.fisheye.api.FishEyeSession;
import com.atlassian.theplugin.commons.fisheye.api.rest.FishEyeRestSession;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;

import java.util.List;

/**
 * User: pmaruszak
 */
public class FishEyeServerFacadeImpl implements FishEyeServerFacade {
	private static FishEyeServerFacadeImpl instance;

	protected FishEyeServerFacadeImpl() {
	}
	
	public void testServerConnection(final String url, final String userName, final String password) throws RemoteApiException {
		FishEyeSession fishEyeSession = getSession(url);
		fishEyeSession.login(userName, password.toCharArray());
		fishEyeSession.logout();		
	}

	public ServerType getServerType() {
		return ServerType.FISHEYE_SERVER;
	}

	public static synchronized FishEyeServerFacadeImpl getInstance() {
		if (instance == null) {
			instance = new FishEyeServerFacadeImpl();
		}

		return instance;
	}


	public FishEyeSession getSession(final String url) throws RemoteApiMalformedUrlException {
		return new FishEyeRestSession(url);

	}
	
	public List<String> getRepositories(final FishEyeServerCfg server) throws RemoteApiException {
		FishEyeSession fishEyeSession = getSession(server.getUrl());
		List<String> repositories;
		
		fishEyeSession.login(server.getUsername(), server.getPassword().toCharArray());
		repositories = fishEyeSession.getRepositories();
		fishEyeSession.logout();
		return repositories;
	}
}
