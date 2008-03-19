package com.atlassian.theplugin.configuration;

import com.intellij.util.config.Storage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-03-17
 * Time: 16:35:17
 * To change this template use File | Settings | File Templates.
 */
public class ProjectToolWindowTableConfiguration extends Storage.MapStorage {

	private Map<String, String> properties = new HashMap<String, String>();

	public ProjectToolWindowTableConfiguration() {
	}

	/**
	 * For storage purposes.
	 * Before returning 'properties' object to store it copies inner MapStorage data into 'properties' object.
	 * @return
	 */
	public Map<String, String> getProperties() {
		Iterator<String> iter = getKeys();
		properties.clear();
		while (iter.hasNext()) {
			String key = iter.next();
			properties.put(key, get(key));
		}
		return properties;
	}

	/**
	 * For storage purposes.
	 * It copies 'properties' data into inner MapStorage object.
	 * @param properties
	 */
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
		for (Map.Entry<String, String> entry : properties.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * For deep copy purposes. Copies param data into 'properties' and into inner MapStorage
	 * @param tableConfiguration
	 */
	public void copyConfiguration(ProjectToolWindowTableConfiguration tableConfiguration) {
		Iterator<String> iter = tableConfiguration.getKeys();
		properties.clear();
		while (iter.hasNext()) {
			String key = iter.next();
			properties.put(key, tableConfiguration.get(key));
			put(key, tableConfiguration.get(key));
		}
	}
}
