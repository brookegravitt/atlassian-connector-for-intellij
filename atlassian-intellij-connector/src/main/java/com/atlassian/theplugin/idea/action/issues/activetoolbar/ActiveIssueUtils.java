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

import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.StringUtil;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.DeactivateIssueResultHandler;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.atlassian.theplugin.idea.jira.JiraIssueAdapter;
import com.atlassian.theplugin.commons.jira.JIRAServerFacade;
import com.atlassian.theplugin.commons.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.commons.jira.api.JIRAAction;
import com.atlassian.theplugin.commons.jira.api.rss.JIRAException;
import com.atlassian.theplugin.commons.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.cache.RecentlyOpenIssuesCache;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.atlassian.theplugin.jira.model.ActiveJiraIssueBean;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

/**
 * User: pmaruszak
 */
public final class ActiveIssueUtils {
	private ActiveIssueUtils() {

	}

	public static String getLabelText(ActiveJiraIssue issue) {
		if (issue != null && issue.getIssueKey() != null) {
			return "Active issue: " + issue.getIssueKey();
		}

		return "No active issue";
	}

	public static ActiveJiraIssue getActiveJiraIssue(final AnActionEvent event) {
		final JiraWorkspaceConfiguration conf = IdeaHelper.getProjectComponent(event, JiraWorkspaceConfiguration.class);

		if (conf != null) {
			return conf.getActiveJiraIssuee();
		}
		return null;
	}

	public static ActiveJiraIssue getActiveJiraIssue(final Project project) {
		final JiraWorkspaceConfiguration conf = IdeaHelper.getProjectComponent(project, JiraWorkspaceConfiguration.class);

		if (conf != null) {
			return conf.getActiveJiraIssuee();
		}
		return null;
	}


	public static void setActiveJiraIssue(final Project project, final ActiveJiraIssue issue, final JIRAIssue jiraIssue) {
		final JiraWorkspaceConfiguration conf = IdeaHelper.getProjectComponent(project, JiraWorkspaceConfiguration.class);
		final RecentlyOpenIssuesCache issueCache = IdeaHelper.getProjectComponent(project, RecentlyOpenIssuesCache.class);

		if (conf != null) {
			conf.setActiveJiraIssuee((ActiveJiraIssueBean) issue);

			if (jiraIssue != null && issueCache != null) {
				issueCache.addIssue(jiraIssue);
			}
		}
	}

	public static JIRAIssue getSelectedJiraIssue(final AnActionEvent event) {
		return event.getData(Constants.ISSUE_KEY);
	}

//	public static JiraServerCfg getSelectedJiraServerById(final AnActionEvent event, String serverId) {
//		final IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(event);
//		if (panel != null) {
//			return CfgUtil.getJiraServerCfgbyServerId(panel.getProjectCfgManager(), serverId);
//		}
//		return null;
//	}

	//invokeLater necessary

	public static JIRAIssue getJIRAIssue(final AnActionEvent event) throws JIRAException {
		return getJIRAIssue(IdeaHelper.getCurrentProject(event));
	}

	//invokeLater necessary
	public static JIRAIssue getJIRAIssue(final Project project) throws JIRAException {
		ServerData jiraServer = getJiraServer(project);
		if (jiraServer != null) {
			final ActiveJiraIssue issue = getActiveJiraIssue(project);
			return getJIRAIssue(jiraServer, issue);
		}
		return null;
	}

	public static JIRAIssue getJIRAIssue(final ServerData jiraServer, final ActiveJiraIssue activeIssue)
			throws JIRAException {
		if (jiraServer != null && activeIssue != null) {

			JIRAServerFacade facade = JIRAServerFacadeImpl.getInstance(PluginUtil.getLogger());
			try {
				return facade.getIssue(jiraServer, activeIssue.getIssueKey());
			} catch (JIRAException e) {
				PluginUtil.getLogger().error(e.getMessage());
				throw e;
			}
		}
		return null;
	}


	public static ServerData getJiraServer(final AnActionEvent event) {
		return getJiraServer(IdeaHelper.getCurrentProject(event));

	}

	public static ServerData getJiraServer(final Project project) {
		final ActiveJiraIssue issue = getActiveJiraIssue(project);
		return getJiraServer(project, issue);
	}

	public static ServerData getJiraServer(final Project project, final ActiveJiraIssue activeIssue) {
		final IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(project);
		ServerData jiraServer = null;

		if (panel != null && activeIssue != null) {
			jiraServer = panel.getProjectCfgManager().getJiraServerr(activeIssue.getServerId());
		}
		return jiraServer;
	}

	public static void activateIssue(final AnActionEvent event, final ActiveJiraIssue newActiveIssue,
			final ServerData jiraServerCfg) {

		final ActiveJiraIssue activeIssue = ActiveIssueUtils.getActiveJiraIssue(event);
		boolean isAlreadyActive = activeIssue != null;
		boolean isDeactivated = true;
		if (isAlreadyActive) {

			isDeactivated = Messages.showYesNoDialog(IdeaHelper.getCurrentProject(event),
					activeIssue.getIssueKey()
							+ " is active. Would you like to deactivate it first and proceed?",
					"Deactivating current issue",
					Messages.getQuestionIcon()) == DialogWrapper.OK_EXIT_CODE;
		}
		if (isDeactivated) {
			ActiveIssueUtils.deactivate(event, new DeactivateIssueResultHandler() {
				public void success() {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							ActiveIssueUtils.activate(event, newActiveIssue, jiraServerCfg);
						}
					});
				}

				public void failure(Throwable problem) {
				}
			});
		}
	}

	/**
	 * Bloking method. Refills cache if necessary.
	 *
	 * @param issue issue
	 * @return boolean
	 */
	private static boolean isInProgress(final JIRAIssue issue) {
		List<JIRAAction> actions = JiraIssueAdapter.get(issue).getCachedActions();

		if (actions == null) {

			ServerData jiraServer = issue.getServer();

			if (jiraServer != null) {
				try {
					actions = JIRAServerFacadeImpl.getInstance(PluginUtil.getLogger()).getAvailableActions(jiraServer, issue);
				} catch (JIRAException e) {
					PluginUtil.getLogger().warn("Cannot fetch issue actions: " + e.getMessage(), e);
				}

				JiraIssueAdapter.get(issue).setCachedActions(actions);
			}
		}

		if (actions != null) {
			for (JIRAAction a : actions) {
				if (a.getId() == Constants.JiraActionId.STOP_PROGRESS.getId()) {
					return true;
				}
			}

			return false;
		}

		return true;
	}

	/**
	 * Should be called from the UI thread
	 *
	 * @param project project
	 * @param issue   issue
	 */
	public static void checkIssueState(final Project project, final JIRAIssue issue) {
		ActiveJiraIssue activeIssue = getActiveJiraIssue(project);
		if (issue != null && activeIssue != null) {

			if (issue.getServer() != null && issue.getKey().equals(activeIssue.getIssueKey())
					&& issue.getServer().getServerId().equals(activeIssue.getServerId())) {

				ProgressManager.getInstance().run(new Task.Backgroundable(project, "Checking active issue state") {
					public void run(final ProgressIndicator indicator) {

						if (!issue.getServer().getUserName().equals(issue.getAssigneeId()) /*|| !isInProgress(issue)*/) {

							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									int isOk = Messages.showYesNoDialog(project,
											"Issue " + issue.getKey() + " has changed assignee (assigned to:"
													+ issue.getAssignee()
													+ ", status: " + issue.getStatus() + ").\nDo you want to deactivate?",
											"Issue " + issue.getKey(), Messages.getQuestionIcon());

									if (isOk == DialogWrapper.OK_EXIT_CODE) {
										deactivate(project, new DeactivateIssueResultHandler() {
											public void success() {
												final JiraWorkspaceConfiguration conf = IdeaHelper
														.getProjectComponent(project, JiraWorkspaceConfiguration.class);
												if (conf != null) {
													conf.setActiveJiraIssuee(null);
												}
											}

											public void failure(Throwable problem) {
											}
										});
									}
								}
							});
						}
					}
				});
			}
		}
	}

	/**
	 * this has to be run from the dispatch thread - see PL-1544
	 *
	 * @param event		  event
	 * @param newActiveIssue issue
	 * @param jiraServerCfg  server
	 */
	private static void activate(final AnActionEvent event, final ActiveJiraIssue newActiveIssue,
			final ServerData jiraServerCfg) {
		final Project project = IdeaHelper.getCurrentProject(event);

		if (project == null) {
			return;
		}

		final IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(project);

		ProgressManager.getInstance().run(new Task.Backgroundable(project, "Refreshing Issue Information", false) {
			private JIRAIssue jiraIssue = null;
			private boolean isOk = false;

			public void run(@NotNull final ProgressIndicator indicator) {
				try {
					// retrieve fresh issue instance from the server
					jiraIssue = ActiveIssueUtils.getJIRAIssue(jiraServerCfg, newActiveIssue);
					isOk = true;
				} catch (JIRAException e) {
					PluginUtil.getLogger().warn("Error starting work on issue: " + e.getMessage(), e);
					if (panel != null) {
						panel.setStatusErrorMessage("Error starting work on issue: " + e.getMessage(), e);
					}
					isOk = false;
				}
			}

			public void onSuccess() {
				if (isOk && panel != null && jiraIssue != null && jiraServerCfg != null) {
					if (!jiraServerCfg.getUserName().equals(jiraIssue.getAssigneeId())
							&& !"-1".equals(jiraIssue.getAssigneeId())) {
						isOk = Messages.showYesNoDialog(IdeaHelper.getCurrentProject(event),
								"Issue " + jiraIssue.getKey() + " is already assigned to " + jiraIssue.getAssignee()
										+ ".\nDo you want to overwrite assignee and start progress?",
								"Issue " + jiraIssue.getKey(), Messages.getQuestionIcon()) == DialogWrapper.OK_EXIT_CODE;
					}
					if (isOk) {
						//assign to me and start working
						panel.startWorkingOnIssueAndActivate(jiraIssue, newActiveIssue);
					}
				}
			}
		});
	}

	public static boolean deactivate(final AnActionEvent event, final DeactivateIssueResultHandler resultHandler) {
		return deactivate(IdeaHelper.getCurrentProject(event), resultHandler);
	}

	public static boolean deactivate(final Project project, final DeactivateIssueResultHandler resultHandler) {
		final JiraWorkspaceConfiguration conf = IdeaHelper.getProjectComponent(project, JiraWorkspaceConfiguration.class);
		if (conf != null) {
			ActiveJiraIssueBean activeIssue = conf.getActiveJiraIssuee();
			if (activeIssue != null) {
				final IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(project);
				try {
					final JIRAIssue jiraIssue = ActiveIssueUtils.getJIRAIssue(project);
					if (panel != null && jiraIssue != null) {
						boolean isOk;
						final ServerData jiraServer = ActiveIssueUtils.getJiraServer(project);

						isOk = panel.logWorkOrDeactivateIssue(jiraIssue, jiraServer,
								StringUtil.generateJiraLogTimeString(activeIssue.recalculateTimeSpent()),
								true, resultHandler);

						return isOk;
					}
				} catch (JIRAException e) {
					if (panel != null) {
						panel.setStatusErrorMessage("Error stopping work on issue: " + e.getMessage(), e);
					}
				}
			} else if (resultHandler != null) {
				resultHandler.success();
			}
		}
		return true;
	}
}
