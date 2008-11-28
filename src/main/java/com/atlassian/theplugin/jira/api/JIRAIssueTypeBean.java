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

import java.net.URL;
import java.util.Map;

public class JIRAIssueTypeBean extends AbstractJIRAConstantBean {
    private boolean subTask = false;
    public JIRAIssueTypeBean(Map<String, String> map) {
        super(map);
        subTask = Boolean.valueOf(map.get("subTask"));
    }

	public JIRAIssueTypeBean(long id, String name, URL iconUrl) {
		super(id, name, iconUrl);
	}

	public JIRAIssueTypeBean(JIRAIssueTypeBean parent) {
		this(parent.getMap());
	}

	public String getQueryStringFragment() {
        return "type=" + getId();
    }

	public JIRAQueryFragment getClone() {
		return new JIRAIssueTypeBean(this);
	}

	public boolean isSubTask() {
        return subTask;
    }
}
