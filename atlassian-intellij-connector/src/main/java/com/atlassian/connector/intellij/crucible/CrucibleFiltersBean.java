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

package com.atlassian.connector.intellij.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.CustomFilterBean;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;

import java.util.Arrays;

public class CrucibleFiltersBean {
	private Boolean[] predefinedFilters = new Boolean[PredefinedFilter.values().length];
	//    private HashMap<String, CustomFilterBean> manualFilter = new HashMap<String, CustomFilterBean>();
	private CustomFilterBean manualFilter = new CustomFilterBean();
	private RecentlyOpenReviewsFilter recenltyOpenFilter = new RecentlyOpenReviewsFilter();

	private Boolean readStored;

	public CrucibleFiltersBean() {
		Arrays.fill(predefinedFilters, false);
	}

	public CustomFilterBean getManualFilter() {
		return manualFilter;
	}

	public void setManualFilter(CustomFilterBean manualFilter) {
		this.manualFilter = manualFilter;
	}

	public Boolean[] getPredefinedFilters() {
		return predefinedFilters;
	}

	public void setPredefinedFilters(Boolean[] predefinedFilters) {
		if (predefinedFilters.length < PredefinedFilter.values().length) {
			this.predefinedFilters = new Boolean[PredefinedFilter.values().length];
			Arrays.fill(this.predefinedFilters, Boolean.valueOf(false));
			for (int i = 0; i < predefinedFilters.length; ++i) {
				this.predefinedFilters[i] = predefinedFilters[i];
			}
		} else {
			this.predefinedFilters = predefinedFilters;
		}
	}

	public Boolean getReadStored() {
		return readStored;
	}

	public void setReadStored(final Boolean readStored) {
		this.readStored = readStored;
	}

	public RecentlyOpenReviewsFilter getRecenltyOpenFilter() {
		return recenltyOpenFilter;
	}

	public void setRecenltyOpenFilter(final RecentlyOpenReviewsFilter recenltyOpenFilter) {
		this.recenltyOpenFilter = recenltyOpenFilter;
	}
}
