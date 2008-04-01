package com.atlassian.theplugin.idea.jira;

import javax.swing.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public final class CachedIconLoader {
	private static Map<String, Icon> icons = new HashMap<String, Icon>();

	private CachedIconLoader() {
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
