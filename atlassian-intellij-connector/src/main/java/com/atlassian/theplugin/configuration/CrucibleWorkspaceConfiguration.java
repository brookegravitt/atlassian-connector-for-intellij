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

import com.atlassian.theplugin.commons.crucible.CrucibleFiltersBean;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilterBean;
import com.atlassian.theplugin.commons.crucible.api.model.State;

public class CrucibleWorkspaceConfiguration {

	private CrucibleViewConfigurationBean view = new CrucibleViewConfigurationBean();
	private CrucibleFiltersBean crucibleFilters = new CrucibleFiltersBean();
	//	private ProjectToolWindowTableConfiguration tableConfiguration = new ProjectToolWindowTableConfiguration();
	private boolean createReviewOnCommit;

	public CrucibleWorkspaceConfiguration() {
	}

	public CrucibleViewConfigurationBean getView() {
		return view;
	}

	public void setView(CrucibleViewConfigurationBean view) {
		this.view = view;
	}

	public CrucibleFiltersBean getCrucibleFilters() {
		return crucibleFilters;
	}

	public void setCrucibleFilters(CrucibleFiltersBean filters) {
		this.crucibleFilters = filters;
	}

//	public ProjectToolWindowTableConfiguration getTableConfiguration() {
//		return tableConfiguration;
//	}
//
//	public void setTableConfiguration(ProjectToolWindowTableConfiguration tableConfiguration) {
//		this.tableConfiguration = tableConfiguration;
//	}

	public boolean isCreateReviewOnCommit() {
		return createReviewOnCommit;
	}

	public void setCreateReviewOnCommit(boolean createReviewOnCommit) {
		this.createReviewOnCommit = createReviewOnCommit;
	}

	public void copyConfiguration(CrucibleWorkspaceConfiguration crucibleConfiguration) {
//		tableConfiguration.copyConfiguration(crucibleConfiguration.getTableConfiguration());
		crucibleFilters.setReadStored(crucibleConfiguration.getCrucibleFilters().getReadStored());
		crucibleFilters.setManualFilter(crucibleConfiguration.getCrucibleFilters().getManualFilter());
		crucibleFilters.setRecenltyOpenFilter(crucibleConfiguration.getCrucibleFilters().getRecenltyOpenFilter());
		createReviewOnCommit = crucibleConfiguration.createReviewOnCommit;

		final CustomFilterBean manualFilter = crucibleFilters.getManualFilter();
		// support just for transition perdiod, as State used to be kept as String and now its normal domain object
		if (manualFilter != null && manualFilter.getState() != null) {
			for (State state : manualFilter.getState()) {
				if (state == null) {
					manualFilter.setState(new State[0]);
				}
			}
		}
		crucibleFilters.setPredefinedFilters(crucibleConfiguration.getCrucibleFilters().getPredefinedFilters());
		view.copyConfiguration(crucibleConfiguration.getView());
	}
}