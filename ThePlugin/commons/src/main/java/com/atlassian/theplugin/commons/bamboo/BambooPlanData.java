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

package com.atlassian.theplugin.commons.bamboo;

public class BambooPlanData implements BambooPlan {
	private String name;
	private String key;
	private boolean favourite;
	private boolean enabled;

	public BambooPlanData(String name, String key) {
		this.name = name;
		this.key = key;
	}

	public String getPlanName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPlanKey() {
		return this.key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public boolean isFavourite() {
		return favourite;
	}

	public void setFavourite(boolean favourite) {
		this.favourite = favourite;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BambooPlanData)) return false;

		BambooPlanData that = (BambooPlanData) o;

		if (key != null ? !key.equals(that.key) : that.key != null) return false;

		return true;
	}

	public int hashCode() {
		return (key != null ? key.hashCode() : 0);
	}
}
