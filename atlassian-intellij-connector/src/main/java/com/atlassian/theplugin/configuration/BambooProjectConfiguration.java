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

/**
 * @author Jacek Jaroczynski
 */
public class BambooProjectConfiguration {
	private ProjectToolWindowTableConfiguration tableConfiguration =
			new ProjectToolWindowTableConfiguration();
	private BambooViewConfigurationBean view = new BambooViewConfigurationBean();

	public BambooProjectConfiguration() {

	}

	public ProjectToolWindowTableConfiguration getTableConfiguration() {
		return tableConfiguration;
	}

	public void setTableConfiguration(ProjectToolWindowTableConfiguration tableConfiguration) {
		this.tableConfiguration = tableConfiguration;
	}

	public void copyConfiguration(BambooProjectConfiguration bambooConfiguration) {
		tableConfiguration.copyConfiguration(bambooConfiguration.getTableConfiguration());
		view.copyConfiguration(bambooConfiguration.getView());
	}

	public BambooViewConfigurationBean getView() {
		return view;
	}

	public void setView(BambooViewConfigurationBean view) {
		this.view = view;
	}
}
