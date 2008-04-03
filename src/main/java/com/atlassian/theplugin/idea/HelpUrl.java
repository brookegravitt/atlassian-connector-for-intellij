package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.util.PluginUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class HelpUrl {
	private static Properties properties = new Properties();

	private static String helpUrlBase;

	static {
		InputStream is = HelpUrl.class.getResourceAsStream("/properties/help-paths.properties");
		try {
			properties.load(is);
			helpUrlBase = properties.getProperty(Constants.HELP_URL_BASE);
		} catch (IOException e) {
			PluginUtil.getLogger().info(e);
		}
	}

	public static String getHelpUrl(String topic) {
		String s = properties.getProperty(topic);
		return s != null ? helpUrlBase + s : null;
	}
}
