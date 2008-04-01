package com.atlassian.theplugin.idea.config;

import com.atlassian.theplugin.configuration.PluginConfiguration;

public interface ContentPanel {
	boolean isEnabled();

	boolean isModified();

	String getTitle();


	/**
	 * Copies configuration to global config object serialized by IDEA
	 */
	void getData();

	/**
	 * Assigns config parameter to local configuration
	 * @param config
	 */
	void setData(PluginConfiguration config);
}
