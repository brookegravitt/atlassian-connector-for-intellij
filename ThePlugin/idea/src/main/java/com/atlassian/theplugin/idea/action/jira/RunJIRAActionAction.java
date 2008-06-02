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

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.JIRAToolWindowPanel;
import com.atlassian.theplugin.idea.jira.JiraIssueAdapter;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.api.JIRAAction;
import com.atlassian.theplugin.jira.api.JIRAActionField;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.commons.Server;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

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

	public void actionPerformed(AnActionEvent event) {
		runIssueActionOrLaunchBrowser();
	}

	public void runIssueActionOrLaunchBrowser() {
		new Thread(new IssueActionOrLaunchBrowserRunnable())
		.start();
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
		public void run() {
			try {
				window.setStatusMessage(
						"Retrieving fields for action \""
								+ action.getName()
								+ "\" in issue "
								+ adapter.getKey()
								+ "...");
				Server server = IdeaHelper.getCurrentJIRAServer().getServer();
				List<JIRAActionField> fields =
						facade.getFieldsForAction(
								server, adapter.getIssue(), action);
				if (fields.isEmpty()) {
					window.setStatusMessage(
							"Running action \""
									+ action.getName()
									+ "\" on issue "
									+ adapter.getKey()
									+ "...");
					facade.progressWorkflowAction(
							server, adapter.getIssue(), action);
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
