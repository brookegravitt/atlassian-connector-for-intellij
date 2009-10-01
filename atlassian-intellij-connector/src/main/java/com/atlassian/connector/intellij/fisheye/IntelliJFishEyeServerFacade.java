package com.atlassian.connector.intellij.fisheye;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.api.HttpConnectionCfg;
import com.atlassian.connector.commons.fisheye.FishEyeServerFacade2;
import com.atlassian.connector.intellij.remoteapi.IntelliJHttpSessionCallback;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.fisheye.FishEyeServerFacadeImpl;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;

import java.util.Collection;


public final class IntelliJFishEyeServerFacade implements FishEyeServerFacade {
	
	private final FishEyeServerFacade2 facade = FishEyeServerFacadeImpl.getInstance(new IntelliJHttpSessionCallback());
	
	private static IntelliJFishEyeServerFacade instance;

	private IntelliJFishEyeServerFacade() {
	}

	public static synchronized FishEyeServerFacade getInstance() {
		if (instance == null) {
			instance = new IntelliJFishEyeServerFacade();
		}

		return instance;
	}


	public Collection<String> getRepositories(ServerData server) throws RemoteApiException {
		return facade.getRepositories(server.toHttpConnectionCfg());
	}

	public ServerType getServerType() {
		return facade.getServerType();
	}

	public void testServerConnection(HttpConnectionCfg httpConnectionCfg) throws RemoteApiException {
		facade.testServerConnection(httpConnectionCfg);
	}

    public void testServerConnection(ConnectionCfg connectionCfg) throws RemoteApiException {
        facade.testServerConnection(connectionCfg);
    }

}
