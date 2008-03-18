package com.atlassian.theplugin.configuration;

import com.intellij.util.config.Storage;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-03-17
 * Time: 16:35:17
 * To change this template use File | Settings | File Templates.
 */
public class BambooProjectToolWindowTableConfiguration extends Storage.MapStorage {

	Map<String, String> properties = new HashMap<String, String>();

	public BambooProjectToolWindowTableConfiguration() {

	}
	
	public Map<String, String> getProperties() {

		Iterator<String> iter = getKeys();

		properties.clear();

		while (iter.hasNext()) {
			String key = iter.next();
			properties.put(key, get(key));
		}

		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;

		for (String key : properties.keySet()) {
			put(key, properties.get(key));
		}
	}
}
