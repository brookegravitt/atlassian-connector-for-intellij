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

package com.atlassian.theplugin.idea.jira;

import javax.swing.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public final class CachedIconLoader {
	private static Map<String, Icon> icons = new HashMap<String, Icon>();
	private static Map<String, Icon> disabledIcons = new HashMap<String, Icon>();

	private CachedIconLoader() {
	}

	public static Icon getDisabledIcon(String urlString) {
		return disabledIcons.get(urlString);
	}

	public static void addDisabledIcon(String urlString, Icon icon) {
		disabledIcons.put(urlString, icon);		
	}

	public static Icon getIcon(URL url) {
		if (url != null) {
			String key = url.toString();
			if (!icons.containsKey(key)) {
				icons.put(key, new ImageIcon(url));
			}
			return icons.get(key);
		} else {
			return null;
		}
	}

	public static Icon getIcon(String urlString) {
		if (urlString != null) {
			if (!icons.containsKey(urlString)) {
				try {
					URL url = new URL(urlString);
					icons.put(urlString, new ImageIcon(url));
				} catch (MalformedURLException e1) {
					return null;
				}
			}
			return icons.get(urlString);
		} else {
			return null;
		}
	}

}
