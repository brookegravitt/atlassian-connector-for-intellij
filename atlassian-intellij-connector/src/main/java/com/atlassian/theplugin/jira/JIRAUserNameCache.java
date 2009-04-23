package com.atlassian.theplugin.jira;

import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRAUserBean;
import com.atlassian.theplugin.jira.api.JiraUserNotFoundException;

import java.util.HashMap;
import java.util.Map;

public final class JIRAUserNameCache {

	private Map<ServerData, Map<String, JIRAUserBean>> serverMap = new HashMap<ServerData, Map<String, JIRAUserBean>>();
	private JIRAServerFacade facade;

	private JIRAUserNameCache() {
		facade = JIRAServerFacadeImpl.getInstance();
	}

	private static JIRAUserNameCache instance = new JIRAUserNameCache();

	public static JIRAUserNameCache getInstance() {
		return instance;
	}

	public JIRAUserBean getUser(ServerData server, String userId) throws JIRAException, JiraUserNotFoundException {
		Map<String, JIRAUserBean> userMap = serverMap.get(server);
		if (userMap == null) {
			userMap = new HashMap<String, JIRAUserBean>();
			serverMap.put(server, userMap);
		}
		JIRAUserBean user = userMap.get(userId);
		if (user == null) {
			user = facade.getUser(server, userId);
			userMap.put(userId, user);
		}
		return user;
	}
}
