package com.atlassian.theplugin.configuration;

import com.atlassian.theplugin.ServerType;

public interface PluginConfiguration {
	boolean isPluginEnabled();
	void setPluginEnabled(boolean value);
	ProductServerConfiguration getProductServers(ServerType serverType);

	long getUid();
	boolean isAutoUpdateEnabled();

	void setAutoUpdateEnabled(boolean autoUpdateEnabled);
}
