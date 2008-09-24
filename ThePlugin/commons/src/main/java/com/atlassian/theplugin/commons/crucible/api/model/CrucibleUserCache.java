package com.atlassian.theplugin.commons.crucible.api.model;

import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;

public interface CrucibleUserCache {
	User getUser(CrucibleServerCfg server, String userId, boolean fetchIfNotExist);
	void addUser(CrucibleServerCfg server, User user);
}
