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

package com.atlassian.theplugin.commons.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.CustomFilterBean;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;

import java.util.Arrays;
import java.util.HashMap;

public class CrucibleFiltersBean {
    private Boolean[] predefinedFilters = new Boolean[PredefinedFilter.values().length];
    private HashMap<String, CustomFilterBean> manualFilter = new HashMap<String, CustomFilterBean>();
	private Boolean readStored;

	public CrucibleFiltersBean() {
        Arrays.fill(predefinedFilters, false);
    }

    public HashMap<String, CustomFilterBean> getManualFilter() {
        return manualFilter;
    }

    public void setManualFilter(HashMap<String, CustomFilterBean> manualFilter) {
        this.manualFilter = manualFilter;
    }

    public Boolean[] getPredefinedFilters() {
        return predefinedFilters;
    }

    public void setPredefinedFilters(Boolean[] predefinedFilters) {
        this.predefinedFilters = predefinedFilters;
    }

	public Boolean getReadStored() {
		return readStored;
	}

	public void setReadStored(final Boolean readStored) {
		this.readStored = readStored;
	}
}
