package com.atlassian.theplugin.configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author pmaruszak
 * @date Aug 12, 2009
 */
public class JiraCustomFilterMap {
    private Map<String, JiraFilterConfigurationBean> customFilters = new HashMap<String, JiraFilterConfigurationBean>();

    public Map<String, JiraFilterConfigurationBean> getCustomFilters() {
        return customFilters;
    }

    public void setCustomFilters(Map<String, JiraFilterConfigurationBean> customFilters) {
        this.customFilters = customFilters;
    }
}
