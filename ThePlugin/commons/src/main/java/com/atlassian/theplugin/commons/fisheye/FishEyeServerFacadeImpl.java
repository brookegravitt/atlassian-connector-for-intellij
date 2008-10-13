package com.atlassian.theplugin.commons.fisheye;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.fisheye.api.FishEyeSession;
import com.atlassian.theplugin.commons.fisheye.api.rest.FishEyeRestSession;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

/**
 * User: pmaruszak
 */
public class FishEyeServerFacadeImpl implements FishEyeServerFacade {
	private static FishEyeServerFacadeImpl instance;

	public void testServerConnection(final String url, final String userName, final String password) throws RemoteApiException {
		FishEyeSession fishEyeSession = new FishEyeRestSession(url);
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
}
