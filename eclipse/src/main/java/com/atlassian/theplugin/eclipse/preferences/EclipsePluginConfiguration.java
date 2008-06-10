package com.atlassian.theplugin.eclipse.preferences;

import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;

public class EclipsePluginConfiguration extends PluginConfigurationBean {
	
	BambooTabConfiguration bambooTabConfiguration = new BambooTabConfiguration();

	public BambooTabConfiguration getBambooTabConfiguration() {
		return bambooTabConfiguration;
	}

	public void setBambooTabConfiguration(BambooTabConfiguration bambooTabConfiguration) {
		this.bambooTabConfiguration = bambooTabConfiguration;
	}

}
