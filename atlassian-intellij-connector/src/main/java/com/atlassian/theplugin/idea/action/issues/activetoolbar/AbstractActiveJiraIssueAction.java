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
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

/**
 * User: pmaruszak
 */
public abstract class AbstractActiveJiraIssueAction extends AnAction {
	private JIRAIssue jiraIssue = null;

	public abstract void onUpdate(AnActionEvent event);

	public void onUpdate(AnActionEvent event, boolean enabled) {
	}

	public final void update(final AnActionEvent event) {
		boolean enabled = getActiveJiraIssue(event) != null ? true : false;

		if (enabled) {
			onUpdate(event);
		}
		onUpdate(event, enabled);
	}

	protected ActiveJiraIssueBean getActiveJiraIssue(final AnActionEvent event) {
		final JiraWorkspaceConfiguration conf = IdeaHelper.getProjectComponent(event, JiraWorkspaceConfiguration.class);

		if (conf != null) {
			return conf.getActiveJiraIssue();
		}

		return null;
	}

	protected void setActiveJiraIssue(final AnActionEvent event, final ActiveJiraIssue issue) {
		final JiraWorkspaceConfiguration conf = IdeaHelper.getProjectComponent(event, JiraWorkspaceConfiguration.class);

		if (conf != null) {
			conf.setActiveJiraIssue((ActiveJiraIssueBean) issue);
		}
	}

	protected JIRAIssue getSelectedJiraIssue(final AnActionEvent event) {
		return event.getData(Constants.ISSUE_KEY);
	}


	protected JiraServerCfg getSelectedJiraServer(final AnActionEvent event) {
		final IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(event);
		if (panel != null) {
			return panel.getSelectedServer();
		}
		return null;
	}

	//invokeLater necessary
	protected JIRAIssue getJIRAIssue(final AnActionEvent event) {
		JiraServerCfg jiraServer = getJiraServer(event);

		if (jiraServer != null) {

			final ActiveJiraIssueBean issue = getActiveJiraIssue(event);
			JIRAServerFacade facade = JIRAServerFacadeImpl.getInstance();
			jiraIssue = null;

			try {
				jiraIssue = facade.getIssue(jiraServer, issue.getIssueKey());
			} catch (JIRAException e) {
				PluginUtil.getLogger().error(e.getMessage());
			}
		}
		return jiraIssue;
	}

	protected JiraServerCfg getJiraServer(final AnActionEvent event) {
		final IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(event);
		final Project project = IdeaHelper.getCurrentProject(event);
		final ActiveJiraIssueBean issue = getActiveJiraIssue(event);

		JiraServerCfg jiraServer = CfgUtil.getJiraServerCfg(project, panel.getProjectCfgManager(), issue.getServerId());
		return jiraServer;
	}
}
