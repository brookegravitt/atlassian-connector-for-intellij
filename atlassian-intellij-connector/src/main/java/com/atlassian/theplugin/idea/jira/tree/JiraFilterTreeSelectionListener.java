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
package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.commons.jira.api.JIRASavedFilter;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.jira.model.JiraCustomFilter;

/**
 * @author Jacek Jaroczynski
 */
public interface JiraFilterTreeSelectionListener {

	void selectedSavedFilterNode(final JIRASavedFilter savedFilter, final JiraServerData jiraServerCfg);

	void selectedManualFilterNode(final JiraCustomFilter manualFilter, final JiraServerData jiraServerCfg);

	void selectionCleared();

	void selectedRecentlyOpenNode();
}
