package com.atlassian.connector.intellij.fisheye;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.fisheye.FishEyeServerFacade2;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.fisheye.FishEyeServerFacadeImpl;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import java.util.Collection;


public class IntelliJFishEyeServerFacade implements FishEyeServerFacade{
	
	private final FishEyeServerFacade2 facade = FishEyeServerFacadeImpl.getInstance();
	
	private static IntelliJFishEyeServerFacade instance;
	
	public static synchronized FishEyeServerFacade getInstance() {
		if (instance == null) {
			instance = new IntelliJFishEyeServerFacade();
		}

		return instance;
	}


	public Collection<String> getRepositories(ServerData server) throws RemoteApiException {
		return facade.getRepositories(server.toConnectionCfg());
	}

	public void setCallback(HttpSessionCallback callback) {
		facade.setCallback(callback);
	}

	public ServerType getServerType() {
		return facade.getServerType();
	}

	public void testServerConnection(ConnectionCfg connectionCfg) throws RemoteApiException {
		// TODO Auto-generated method stub
		
	}

}
