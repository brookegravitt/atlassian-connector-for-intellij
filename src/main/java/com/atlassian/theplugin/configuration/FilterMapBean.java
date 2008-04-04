package com.atlassian.theplugin.configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: marek
 * Date: Apr 4, 2008
 * Time: 4:08:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class FilterMapBean {
	private Map<String, String> filter = new HashMap<String, String>();

	public FilterMapBean() {
	}

	public FilterMapBean(Map<String, String> filter) {
		this.filter = filter;
	}

	public Map<String, String> getFilter() {
		return filter;
	}

	public void setFilter(Map<String, String> filter) {
		this.filter = filter;
	}
}
