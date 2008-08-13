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
public class JiraConfigurationBean {

	private int pollTime = 1;

	private boolean displayIconDescription = false;
	private static final int HASHCODE_MAGIC = 31;

	public JiraConfigurationBean() {
    }

    public JiraConfigurationBean(JiraConfigurationBean cfg) {
        this.pollTime = cfg.getPollTime();
        this.displayIconDescription = cfg.isDisplayIconDescription();
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

	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof JiraConfigurationBean)) {
			return false;
		}

		final JiraConfigurationBean that = (JiraConfigurationBean) o;

		if (displayIconDescription != that.displayIconDescription) {
			return false;
		}
		if (pollTime != that.pollTime) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		int result;
		result = pollTime;
		result = HASHCODE_MAGIC * result + (displayIconDescription ? 1 : 0);
		return result;
	}
}