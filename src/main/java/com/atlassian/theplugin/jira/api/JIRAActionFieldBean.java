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

package com.atlassian.theplugin.jira.api;

public class JIRAActionFieldBean extends AbstractJIRAConstantBean implements JIRAActionField {
	private String fieldId;

	public JIRAActionFieldBean(String fieldId, String name) {
		super(fieldId.hashCode(), name, null);
		this.fieldId = fieldId;
	}

	public JIRAActionFieldBean(JIRAActionFieldBean parent) {
		this(parent.fieldId, parent.name);
	}

	public String getQueryStringFragment() {
		// todo: I am almost absolutely sure this is wrong. Once we get
		// to actually handling action fields, this will have to be fixed
		return fieldId + "=";
	}

	public JIRAQueryFragment getClone() {
		return new JIRAActionFieldBean(this);
	}
}
