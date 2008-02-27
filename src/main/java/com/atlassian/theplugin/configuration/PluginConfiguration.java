package com.atlassian.theplugin.configuration;

import com.atlassian.theplugin.ServerType;

public interface PluginConfiguration {
	ProductServerConfiguration getProductServers(ServerType serverType);

	long getUid();
	boolean isAutoUpdateEnabled();

	void setAutoUpdateEnabled(boolean autoUpdateEnabled);
}
