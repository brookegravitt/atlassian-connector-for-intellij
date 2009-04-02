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
package com.atlassian.theplugin.idea.action.issues.activetoolbar;

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.atlassian.theplugin.jira.model.ActiveJiraIssueBean;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

/**
 * User: pmaruszak
 */
public final class ActiveIssueHelper {

	public static String getLabelText(ActiveJiraIssue issue) {
		if (issue != null && issue.getIssueKey() != null) {
			return "Active issue: " + issue.getIssueKey();
		}

		return "No active issue";
	}

	public static ActiveJiraIssue getActiveJiraIssue(final AnActionEvent event) {
		final JiraWorkspaceConfiguration conf = IdeaHelper.getProjectComponent(event, JiraWorkspaceConfiguration.class);

		if (conf != null) {
			return conf.getActiveJiraIssue();
		}
		return null;
	}

	public static ActiveJiraIssue getActiveJiraIssue(final Project project) {
		final JiraWorkspaceConfiguration conf = IdeaHelper.getProjectComponent(project, JiraWorkspaceConfiguration.class);

		if (conf != null) {
			return conf.getActiveJiraIssue();
		}
		return null;
	}


	public static void setActiveJiraIssue(final AnActionEvent event, final ActiveJiraIssue issue,
			final JiraServerCfg jiraServerCfg) {
		final JiraWorkspaceConfiguration conf = IdeaHelper.getProjectComponent(event, JiraWorkspaceConfiguration.class);

		if (conf != null) {
			conf.setActiveJiraIssue((ActiveJiraIssueBean) issue);
			conf.addRecentlyOpenIssue(ActiveIssueHelper.getJIRAIssue(jiraServerCfg, issue), jiraServerCfg);
		}
	}

	public static JIRAIssue getSelectedJiraIssue(final AnActionEvent event) {
		return event.getData(Constants.ISSUE_KEY);
	}


	public static JiraServerCfg getSelectedJiraServerByUrl(final AnActionEvent event, String serverUrl) {
		final IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(event);
		if (panel != null) {
//			//return panel.getSelectedServer();

			final Project project = IdeaHelper.getCurrentProject(event);
			return CfgUtil.getJiraServerCfgByUrl(project, panel.getProjectCfgManager(), serverUrl);
		}
		return null;
	}


	public static JiraServerCfg getSelectedJiraServerById(final AnActionEvent event, String serverId) {
		final IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(event);
		if (panel != null) {
			final Project project = IdeaHelper.getCurrentProject(event);
			return CfgUtil.getJiraServerCfgbyServerId(project, panel.getProjectCfgManager(), serverId);
		}
		return null;
	}

	//invokeLater necessary
	public static JIRAIssue getJIRAIssue(final AnActionEvent event) {
		return getJIRAIssue(IdeaHelper.getCurrentProject(event));
	}

	//invokeLater necessary
	public static JIRAIssue getJIRAIssue(final Project project) {
		JiraServerCfg jiraServer = getJiraServer(project);
		if (jiraServer != null) {
			final ActiveJiraIssue issue = getActiveJiraIssue(project);
			return getJIRAIssue(jiraServer, issue);
		}
		return null;
	}

	public static JIRAIssue getJIRAIssue(final JiraServerCfg jiraServer, final ActiveJiraIssue activeIssue) {
		if (jiraServer != null && activeIssue != null) {

			JIRAServerFacade facade = JIRAServerFacadeImpl.getInstance();
			try {
				return facade.getIssue(jiraServer, activeIssue.getIssueKey());
			} catch (JIRAException e) {
				PluginUtil.getLogger().error(e.getMessage());
			}
		}
		return null;
	}


	public static JiraServerCfg getJiraServer(final AnActionEvent event) {
		return getJiraServer(IdeaHelper.getCurrentProject(event));

	}

	public static JiraServerCfg getJiraServer(final Project project) {
		final ActiveJiraIssue issue = getActiveJiraIssue(project);
		return getJiraServer(project, issue);
	}

	public static JiraServerCfg getJiraServer(final Project project, final ActiveJiraIssue activeIssue) {
		final IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(project);
		JiraServerCfg jiraServer = null;

		if (panel != null && activeIssue != null) {
			jiraServer = CfgUtil.getJiraServerCfgbyServerId(project, panel.getProjectCfgManager(), activeIssue.getServerId());
		}
		return jiraServer;
	}
}
