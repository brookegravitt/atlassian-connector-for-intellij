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
import com.atlassian.theplugin.commons.util.StringUtil;
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
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;

/**
 * User: pmaruszak
 */
public abstract class AbstractActiveJiraIssueAction extends AnAction {
	public abstract void onUpdate(AnActionEvent event);

	public void onUpdate(AnActionEvent event, boolean enabled) {
	}

	public final void update(final AnActionEvent event) {
		final ActiveJiraIssue activeJiraIssue = getActiveJiraIssue(event);
		boolean enabled = activeJiraIssue != null;
		if (enabled) {
			onUpdate(event);
		}
		onUpdate(event, enabled);
	}

	protected ActiveJiraIssue getActiveJiraIssue(final AnActionEvent event) {
		final JiraWorkspaceConfiguration conf = IdeaHelper.getProjectComponent(event, JiraWorkspaceConfiguration.class);

		if (conf != null) {
			return conf.getActiveJiraIssue();
		}
		return null;
	}

	protected ActiveJiraIssue getActiveJiraIssue(final Project project) {
		final JiraWorkspaceConfiguration conf = IdeaHelper.getProjectComponent(project, JiraWorkspaceConfiguration.class);

		if (conf != null) {
			return conf.getActiveJiraIssue();
		}
		return null;
	}


	protected void setActiveJiraIssue(final AnActionEvent event, final ActiveJiraIssue issue,
			final JiraServerCfg jiraServerCfg) {
		final JiraWorkspaceConfiguration conf = IdeaHelper.getProjectComponent(event, JiraWorkspaceConfiguration.class);

		if (conf != null) {
			conf.setActiveJiraIssue((ActiveJiraIssueBean) issue);
			conf.addRecentlyOpenIssue(issue.getIssue(), jiraServerCfg);
		}
	}

	protected JIRAIssue getSelectedJiraIssue(final AnActionEvent event) {
		return event.getData(Constants.ISSUE_KEY);
	}


	protected JiraServerCfg getSelectedJiraServerByUrl(final AnActionEvent event, String serverUrl) {
		final IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(event);
		if (panel != null) {
//			//return panel.getSelectedServer();

			final Project project = IdeaHelper.getCurrentProject(event);
			return CfgUtil.getJiraServerCfgByUrl(project, panel.getProjectCfgManager(), serverUrl);
		}
		return null;
	}


	protected JiraServerCfg getSelectedJiraServerById(final AnActionEvent event, String serverUrl) {
		final IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(event);
		if (panel != null) {
			final Project project = IdeaHelper.getCurrentProject(event);
			return CfgUtil.getJiraServerCfgbyServerId(project, panel.getProjectCfgManager(), serverUrl);
		}
		return null;
	}

	//invokeLater necessary
	protected JIRAIssue getJIRAIssue(final AnActionEvent event) {
		return getJIRAIssue(IdeaHelper.getCurrentProject(event));
	}

	//invokeLater necessary
	protected JIRAIssue getJIRAIssue(final Project project) {
		JiraServerCfg jiraServer = getJiraServer(project);
		if (jiraServer != null) {
			final ActiveJiraIssue issue = getActiveJiraIssue(project);
			return getJIRAIssue(jiraServer, issue);
		}
		return null;
	}

	private JIRAIssue getJIRAIssue(final JiraServerCfg jiraServer, final ActiveJiraIssue activeIssue) {
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


	protected JiraServerCfg getJiraServer(final AnActionEvent event) {
		return getJiraServer(IdeaHelper.getCurrentProject(event));

	}

	protected JiraServerCfg getJiraServer(final Project project) {
		final ActiveJiraIssue issue = getActiveJiraIssue(project);
		return getJiraServer(project, issue);
	}

	public JiraServerCfg getJiraServer(final Project project, final ActiveJiraIssue activeIssue) {
		final IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(project);
		JiraServerCfg jiraServer = null;

		if (panel != null && activeIssue != null) {
			jiraServer = CfgUtil.getJiraServerCfgbyServerId(project, panel.getProjectCfgManager(), activeIssue.getServerId());
		}
		return jiraServer;
	}

	protected boolean activate(final AnActionEvent event, final ActiveJiraIssue activeIssue) {
		final Project project = IdeaHelper.getCurrentProject(event);
		boolean isOk = true;
		final IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(project);
		final JiraServerCfg jiraServer = getJiraServer(project, activeIssue);
		final JIRAIssue jiraIssue = getJIRAIssue(jiraServer, activeIssue);

		if (panel != null && jiraIssue != null && jiraServer != null) {
			if (jiraServer != null && !jiraServer.getUsername().equals(jiraIssue.getAssigneeId())) {
				isOk = Messages.showYesNoDialog(IdeaHelper.getCurrentProject(event),
						"Is already assigned to " + jiraIssue.getAssignee()
								+ ". Do you want to overwrite assignee and start progress?",
						"Issue " + jiraIssue.getKey(),
						Messages.getQuestionIcon()) == DialogWrapper.OK_EXIT_CODE;
			}

			if (isOk) {
				//assign to me and start working
				isOk = panel.startWorkingOnIssue(jiraIssue, jiraServer);
			}
		}
		return isOk;
	}

	protected boolean deactivate(final AnActionEvent event) {
		final JiraWorkspaceConfiguration conf = IdeaHelper.getProjectComponent(event, JiraWorkspaceConfiguration.class);
		if (conf != null) {
			ActiveJiraIssueBean activeIssue = conf.getActiveJiraIssue();
			if (activeIssue != null) {
				final IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(event);
				final Project project = IdeaHelper.getCurrentProject(event);
				final JIRAIssue jiraIssue = getJIRAIssue(project);
				if (panel != null && jiraIssue != null) {
					boolean isOk = true;
					final JiraServerCfg jiraServer = getJiraServer(project);


					isOk = panel.logWorkOrDeactivateIssue(jiraIssue,
							jiraServer,
							StringUtil.generateJiraLogTimeString(activeIssue.recalculateTimeSpent()),
							true);


					return isOk;

				}

			}
		}
		return true;
	}

//	protected void refreshLabel(ActiveJiraIssue issue) {
//		ActionManager aManager = ActionManager.getInstance();
//		AnAction action = aManager.getAction(Constants.ACTIVE_JIRA_ISSUE_ACTION);
//		String label = getLabelText(issue);
//		if (issue != null) {
//			action.getTemplatePresentation().setText(label, true);
//			action.getTemplatePresentation().setEnabled(true);
//
//		} else {
//			action.getTemplatePresentation().setText(label, true);
//			action.getTemplatePresentation().setEnabled(false);
//		}
//
//		//createTooltipText("Open Issue", this);
//
//
//	}

	protected String getLabelText(ActiveJiraIssue issue) {
		if (issue != null) {
			return "Active issue: " + issue.getIssueKey();
		}

		return "No active issue";
	}
}

