package com.atlassian.theplugin.configuration;

import com.atlassian.theplugin.ServerType;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 11, 2008
 * Time: 12:03:11 PM
 * To change this template use File | Settings | File Templates.
 */
public interface PluginConfiguration {
	boolean isPluginEnabled();
	void setPluginEnabled(boolean value);
	ProductServerConfiguration getProductServers(ServerType serverType);	
}
