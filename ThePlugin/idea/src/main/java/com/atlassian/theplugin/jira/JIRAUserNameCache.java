package com.atlassian.theplugin.jira;

import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRAUserBean;

import java.util.HashMap;
import java.util.Map;

public final class JIRAUserNameCache {

	private Map<JIRAServer, Map<String, JIRAUserBean>> serverMap = new HashMap<JIRAServer, Map<String, JIRAUserBean>>();
	private JIRAServerFacade facade;

	private JIRAUserNameCache() {
		facade = JIRAServerFacadeImpl.getInstance();
	}

	private static JIRAUserNameCache instance = new JIRAUserNameCache();

	public static JIRAUserNameCache getInstance() {
		return instance;
	}

	public JIRAUserBean getUser(JIRAServer server, String userId) throws JIRAException {
		Map<String, JIRAUserBean> userMap = serverMap.get(server);
		if (userMap == null) {
			userMap = new HashMap<String, JIRAUserBean>();
			serverMap.put(server, userMap);
		}
		JIRAUserBean user = userMap.get(userId);
		if (user == null) {
			user = facade.getUser(server.getServer(), userId);
			userMap.put(userId, user);
		}
		return user;
	}
}
