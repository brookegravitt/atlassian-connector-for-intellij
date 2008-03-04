package com.atlassian.theplugin.idea.config;

import com.atlassian.theplugin.configuration.PluginConfiguration;

public interface ContentPanel {
	boolean isEnabled();

	boolean isModified();

	String getTitle();

	void getData();

	void setData(PluginConfiguration config);
}
