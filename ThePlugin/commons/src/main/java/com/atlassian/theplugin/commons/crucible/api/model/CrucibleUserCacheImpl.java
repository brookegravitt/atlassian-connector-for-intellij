package com.atlassian.theplugin.commons.crucible.api.model;

import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

public final class CrucibleUserCacheImpl implements CrucibleUserCache {
	private Map<CrucibleServerCfg, Map<String, User>> serverMap = new HashMap<CrucibleServerCfg, Map<String, User>>();

	private static CrucibleUserCache instance;

	public static synchronized CrucibleUserCache getInstance() {
		if (instance == null) {
			instance = new CrucibleUserCacheImpl();
		}
		return instance;
	}

	public User getUser(CrucibleServerCfg server, String userId, boolean fetchIfNotExist) {
		Map<String, User> userMap = serverMap.get(server);
		if (userMap == null && fetchIfNotExist) {
			userMap = new HashMap<String, User>();
			serverMap.put(server, userMap);
			List<User> users;
			try {
				users = CrucibleServerFacadeImpl.getInstance().getUsers(server);
			} catch (RemoteApiException e) {
				return null;
			} catch (ServerPasswordNotProvidedException e) {
				return null;
			}
			for (User u : users) {
				userMap.put(u.getUserName(), u);
			}
		}
		if (userMap != null) {
			return userMap.get(userId);
		}
		return null;
	}

	public void addUser(CrucibleServerCfg server, User user) {
		Map<String, User> userMap = serverMap.get(server);
		if (userMap == null) {
			userMap = new HashMap<String, User>();
			serverMap.put(server, userMap);
		}
		userMap.put(user.getUserName(), user);
	}

}
