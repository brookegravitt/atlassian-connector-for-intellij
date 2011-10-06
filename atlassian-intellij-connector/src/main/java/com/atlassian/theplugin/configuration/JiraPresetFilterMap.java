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
package com.atlassian.theplugin.configuration;

import com.atlassian.connector.commons.jira.beans.JIRAProject;
import com.atlassian.connector.commons.jira.beans.JIRAProjectBean;
import com.atlassian.theplugin.jira.model.JiraPresetFilter;
import com.intellij.util.xmlb.annotations.Transient;

import java.util.HashMap;
import java.util.Map;

/**
 * @autrhor pmaruszak
 * @date Mar 24, 2010
 */
public class JiraPresetFilterMap {
    private Map<String, JIRAProjectBean> map = new HashMap<String, JIRAProjectBean>();

    public void setPresetFilter(JiraPresetFilter presetFilter, JIRAProjectBean jiraProject) {
        if (presetFilter != null && presetFilter.getName() != null) {
            map.put(presetFilter.getName(), jiraProject);
        }
    }


    public Map<String, JIRAProjectBean> getMap() {
        return map;
    }

    public void setMap(Map<String, JIRAProjectBean> map) {
        this.map = map;
    }

    @Transient
    public JIRAProject getProject(JiraPresetFilter presetFilter) {
        return presetFilter != null
                && presetFilter.getName() != null
                && map.containsKey(presetFilter.getName()) ? map.get(presetFilter.getName()) : null;
    }

    @Transient
    public void clearPresetFilter(JiraPresetFilter presetFilter) {
        map.remove(presetFilter.getName());
    }
}
