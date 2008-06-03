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

import com.atlassian.theplugin.commons.crucible.api.rest.CrucibleFiltersBean;
import com.atlassian.theplugin.commons.crucible.api.CustomFilterData;
import com.intellij.util.xmlb.annotations.Transient;

import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-03-17
 * Time: 16:33:11
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleProjectConfiguration {
	private ProjectToolWindowTableConfiguration tableConfiguration =
			new ProjectToolWindowTableConfiguration();

	private CrucibleFiltersBean crucibleFilters = new CrucibleFiltersBean();

	public CrucibleFiltersBean getCrucibleFilters() {
		return crucibleFilters;
	}

	public void setCrucibleFilters(CrucibleFiltersBean filters) {
		this.crucibleFilters = filters;
	}
	public CrucibleProjectConfiguration() {

	}

	public ProjectToolWindowTableConfiguration getTableConfiguration() {
		return tableConfiguration;
	}

	public void setTableConfiguration(ProjectToolWindowTableConfiguration tableConfiguration) {
		this.tableConfiguration = tableConfiguration;
	}

	public void copyConfiguration(CrucibleProjectConfiguration crucibleConfiguration) {
		tableConfiguration.copyConfiguration(crucibleConfiguration.getTableConfiguration());
	}

	public void safeAddCrucibleFilter(CustomFilterData filter){
		crucibleFilters.safeAddCrucibleFilter(filter);
		
	}
}