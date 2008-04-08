/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.util.PluginUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class HelpUrl {
	private static Properties properties = new Properties();

	private static String helpUrlBase;

	private HelpUrl() { }

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
