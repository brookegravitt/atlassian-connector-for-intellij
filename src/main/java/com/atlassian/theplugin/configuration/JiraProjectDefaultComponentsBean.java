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

import java.util.LinkedHashSet;

/**
 * @author Jacek Jaroczynski
 */
public class JiraProjectDefaultComponentsBean {

	private String project = "";
	private LinkedHashSet<Long> components = new LinkedHashSet<Long>();

	private static final int HASH_INT = 31;

	public JiraProjectDefaultComponentsBean() {
	}

	public JiraProjectDefaultComponentsBean(final String project, final LinkedHashSet<Long> components) {
		this.project = project;
		this.components = components;
	}

	public String getProject() {
		return project;
	}

	public LinkedHashSet<Long> getComponents() {
		return components;
	}

	public void setProject(final String project) {
		this.project = project;
	}

	public void setComponents(final LinkedHashSet<Long> components) {
		this.components = components;
	}

	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final JiraProjectDefaultComponentsBean defaults = (JiraProjectDefaultComponentsBean) o;

		if (project != null ? !project.equals(defaults.project) : defaults.project != null) {
			return false;
		}
		if (components != null ? !components.equals(defaults.components) : defaults.components != null) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		int result;
		result = (project != null ? project.hashCode() : 0);
		result = HASH_INT * result + (components != null ? components.hashCode() : 0);
		return result;
	}
}
