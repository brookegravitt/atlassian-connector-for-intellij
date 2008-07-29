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

package com.atlassian.theplugin.commons.configuration;

/**
 * Bean storing information about Bamboo servers etc.<p>
 * The class serves both as a configuration provider for plugin logic and Bean for persistence.
 */
public class JiraConfigurationBean extends AbstractServerConfigurationBean {

	private int pollTime = 1;

	private boolean displayIconDescription = false;

	public JiraConfigurationBean() {
        super();
    }

    public JiraConfigurationBean(JiraConfigurationBean cfg) {
		super(cfg);
        this.pollTime = ((JiraConfigurationBean) cfg).getPollTime();
        this.displayIconDescription = ((JiraConfigurationBean) cfg).isDisplayIconDescription();
    }

	public int getPollTime() {
		return pollTime;
	}

	public void setPollTime(int pollTime) {
		this.pollTime = pollTime;
	}

	public boolean isDisplayIconDescription() {
		return displayIconDescription;
	}

	public void setDisplayIconDescription(boolean displayIconDescription) {
		this.displayIconDescription = displayIconDescription;
	}
}