package com.atlassian.theplugin.jira.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public abstract class AbstractJIRAConstantBean implements JIRAConstant {
    protected String name;
    protected long id;
    protected URL iconUrl;

	public AbstractJIRAConstantBean() {		
	}

	protected AbstractJIRAConstantBean(long id, String name, URL iconUrl) {
		this.id = id;
		this.name = name;
		this.iconUrl = iconUrl;
	}

	public AbstractJIRAConstantBean(Map map) {
        name = (String) map.get("name");
        id = Long.valueOf((String) map.get("id"));

        if (map.containsKey("icon")) {
            try {
                iconUrl = new URL((String) map.get("icon"));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public URL getIconUrl() {
        return iconUrl;
    }
}
