package com.atlassian.theplugin.configuration;

import com.atlassian.theplugin.ServerType;
import com.atlassian.theplugin.util.Version;

public interface PluginConfiguration {
	ProductServerConfiguration getProductServers(ServerType serverType);

	long getUid();
	boolean isAutoUpdateEnabled();

	void setAutoUpdateEnabled(boolean autoUpdateEnabled);

	Version getRejectedUpgrade();

	void setRejectedUpgrade(Version version);

	void setCheckUnstableVersionsEnabled(boolean checkUnstableVersions);

	boolean getCheckUnstableVersionsEnabled();

	boolean getIsAnonymousFeedbackEnabled();

	void setIsAnonymousFeedbackEnabled(boolean isAnonymousFeedbackEnabled);
}
