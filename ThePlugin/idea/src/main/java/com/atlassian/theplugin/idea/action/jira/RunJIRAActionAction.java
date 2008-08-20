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

package com.atlassian.theplugin.idea.action.jira;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.JIRAToolWindowPanel;
import com.atlassian.theplugin.idea.jira.JiraIssueAdapter;
import com.atlassian.theplugin.jira.JIRAServer;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.api.JIRAAction;
import com.atlassian.theplugin.jira.api.JIRAActionField;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

import java.util.List;

public class RunJIRAActionAction extends AnAction {

	private JiraIssueAdapter adapter;
	private JIRAAction action;
	private JIRAServerFacade facade;
	private JIRAToolWindowPanel window;

	public RunJIRAActionAction(JIRAToolWindowPanel toolWindow,
							   JIRAServerFacade facade, JiraIssueAdapter issueAdapter, JIRAAction jiraAction) {
		super(jiraAction.getName());
		adapter = issueAdapter;
		action = jiraAction;
		window = toolWindow;
		this.facade = facade;
	}

	@Override
	public void actionPerformed(AnActionEvent event) {
		runIssueActionOrLaunchBrowser(IdeaHelper.getCurrentProject(event));
	}

	public void runIssueActionOrLaunchBrowser(Project project) {
		new Thread(new IssueActionOrLaunchBrowserRunnable(project)).start();
	}

	public void launchBrowser() {
		adapter.clearCachedActions();
		BrowserUtil.launchBrowser(adapter.getServerUrl()
			+ "/secure/WorkflowUIDispatcher.jspa?id="
			+ adapter.getId()
			+ "&"
			+ action.getQueryStringFragment());
	}

	private class IssueActionOrLaunchBrowserRunnable implements Runnable {
		private Project project;

		IssueActionOrLaunchBrowserRunnable(Project project) {
			this.project = project;
		}
		public void run() {
			try {
				window.setStatusMessage(
						"Retrieving fields for action \""
								+ action.getName()
								+ "\" in issue "
								+ adapter.getKey()
								+ "...");
				JIRAServer jiraServer = IdeaHelper.getCurrentJIRAServer(project);
				if (jiraServer != null) {
					JiraServerCfg server = jiraServer.getServer();
					List<JIRAActionField> fields = facade.getFieldsForAction(server, adapter.getIssue(), action);
					if (fields.isEmpty()) {
						window.setStatusMessage(
								"Running action \""
										+ action.getName()
										+ "\" on issue "
										+ adapter.getKey()
										+ "...");
						facade.progressWorkflowAction(server, adapter.getIssue(), action);
						window.refreshIssuesPage();
					} else {
						window.setStatusMessage(
								"Action \""
										+ action.getName()
										+ "\" on issue "
										+ adapter.getKey()
										+ " is interactive, launching browser");
						launchBrowser();
					}
				}
			} catch (JIRAException e) {
				window.setStatusMessage(
						"Unable to run action "
								+ action.getName()
								+ " on issue "
								+ adapter.getKey()
								+ ": "
								+ e.getMessage(),
						true);
			}
		}
	}
}
