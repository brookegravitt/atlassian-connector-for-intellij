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

public class JiraFiltersBean {
	private ArrayList<JiraFilterEntryBean> manualFilter = new ArrayList<JiraFilterEntryBean>();
	private JiraFilterEntryBean savedFilter = null;
	private boolean savedFilterUsed = false;

	public JiraFiltersBean() {
	}

	public ArrayList<JiraFilterEntryBean> getManualFilter() {
		return manualFilter;
	}

	public void setManualFilter(ArrayList<JiraFilterEntryBean> manualFilter) {
		this.manualFilter = manualFilter;
	}

	public JiraFilterEntryBean getSavedFilter() {
		return savedFilter;
	}

	public void setSavedFilter(JiraFilterEntryBean savedFilter) {
		this.savedFilter = savedFilter;
	}

	public boolean getSavedFilterUsed() {
		return savedFilterUsed;
	}

	public void setSavedFilterUsed(boolean savedFilterUsed) {
		this.savedFilterUsed = savedFilterUsed;
	}
}
