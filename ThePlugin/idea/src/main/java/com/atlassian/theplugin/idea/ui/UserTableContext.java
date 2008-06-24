package com.atlassian.theplugin.idea.ui;

import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
* User: lguminski
* Date: Jun 23, 2008
* Time: 9:43:11 PM
* To change this template use File | Settings | File Templates.
*/
public class UserTableContext {
	private Map<String, Object> properties = new HashMap<String, Object>();
	public Object getProperty(String property) {
		return properties.get(property);
	}

	public void setProperty(String property, Object value) {
		properties.put(property, value);
	}
}
