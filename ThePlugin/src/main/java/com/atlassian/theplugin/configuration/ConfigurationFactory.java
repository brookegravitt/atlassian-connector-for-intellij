package com.atlassian.theplugin.configuration;

/**
 * Provide an instance of Bamboo BambooConfigurationBean.
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 11:58:42 AM
 */
public abstract class ConfigurationFactory {

	///CLOVER:OFF
	private ConfigurationFactory() {
	}
	///CLOVER:ON

	private static PluginConfiguration pluginConfiguration;

	public static PluginConfiguration getConfiguration() {
		return pluginConfiguration;
	}

	public static void setConfiguration(PluginConfiguration newConfiguration) {
		pluginConfiguration = newConfiguration;
	}
}
