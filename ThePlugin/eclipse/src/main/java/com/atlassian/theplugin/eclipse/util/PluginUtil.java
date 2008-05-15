package com.atlassian.theplugin.eclipse.util;

import com.atlassian.theplugin.commons.util.Logger;
import com.atlassian.theplugin.commons.util.LoggerImpl;

public class PluginUtil {
	private static final String PLUGIN_NAME = "Attlasian IDE Eclipse plug-in";

	public static String getPluginName() {
		return PLUGIN_NAME;
	}
	
	public static Logger getLogger() {
		return LoggerImpl.getInstance();
	}
	
}
