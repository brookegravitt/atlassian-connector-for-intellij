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

public class CrucibleConfigurationBean extends AbstractConfigurationBean {
	private int pollTime = 1;

	public CrucibleConfigurationBean() {
		super();
	}

	public CrucibleConfigurationBean(ProductServerConfiguration cfg) {

		super(cfg);
		if (cfg instanceof CrucibleConfigurationBean) {
			this.pollTime = ((CrucibleConfigurationBean) cfg).getPollTime();
		}
	}

	public int getPollTime() {
		return pollTime;
	}

	public void setPollTime(int pollTime) {
		this.pollTime = pollTime;
	}
}