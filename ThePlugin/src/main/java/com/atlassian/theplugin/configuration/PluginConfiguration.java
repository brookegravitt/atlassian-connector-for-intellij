package com.atlassian.theplugin.configuration;

import com.atlassian.theplugin.ServerType;
import com.atlassian.theplugin.util.InfoServer;

public interface PluginConfiguration {
	ProductServerConfiguration getProductServers(ServerType serverType);

	long getUid();
	boolean isAutoUpdateEnabled();

	void setAutoUpdateEnabled(boolean autoUpdateEnabled);

	InfoServer.Version getRejectedUpgrade();

	void setRejectedUpgrade(InfoServer.Version version);
}
