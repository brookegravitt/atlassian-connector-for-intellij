package com.atlassian.theplugin.commons;

import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;

public interface ConfigurationListener {
	public void updateConfiguration(PluginConfigurationBean configuration);
}
