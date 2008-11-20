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
package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.jira.api.JIRASavedFilter;

import java.util.List;

/**
 * User: pmaruszak
 */
public interface JIRAFilterListModelListener {
	void modelChanged(JIRAFilterListModel listModel);
	void selectedSavedFilter(JiraServerCfg jiraServer, JIRASavedFilter savedFilter);
	void selectedManualFilter(JiraServerCfg jiraServer, List<JIRAQueryFragment> manualFilter);
	void modelFrozen(boolean frozen);
}
