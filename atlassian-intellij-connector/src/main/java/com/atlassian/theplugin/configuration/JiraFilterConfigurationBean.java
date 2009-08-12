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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JiraFilterConfigurationBean {
	public static final String MANUAL_FILTER = "manualFilter";
	public static final String RECENTLY_OPEN_FILTER = "recenltyOpen";
    public static final String SAVED_FILTER = "savedFilter";
    
    private String uid;
    private String name;

	private List<JiraFilterEntryBean> manualFilter = new ArrayList<JiraFilterEntryBean>();


    public JiraFilterConfigurationBean() {
        this.uid = UUID.randomUUID().toString();
        this.name = "Custom Filter";
	}

	public List<JiraFilterEntryBean> getManualFilter() {
		return manualFilter;
	}

	public void setManualFilter(List<JiraFilterEntryBean> manualFilter) {
		this.manualFilter = manualFilter;
	}

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}