package com.atlassian.theplugin.idea.jira;

import javax.swing.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

public class CachedIconLoader {
	private static Map<String, Icon> icons = new HashMap<String, Icon>();

	public static Icon getIcon(URL url) {
		if (url != null) {
			String key = url.toString();
			if (!icons.containsKey(key)) {
				if (url != null) {
					icons.put(key, new ImageIcon(url));
				}
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
