package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.commons.jira.JIRAIssueProgressTimestampCache;
import com.atlassian.theplugin.commons.jira.JiraActionFieldType;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.JiraServerFacade;
import com.atlassian.theplugin.commons.jira.api.JIRAAction;
import com.atlassian.theplugin.commons.jira.api.JIRAActionField;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.commons.jira.api.commons.rss.JIRAException;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.ActiveIssueResultHandler;
import com.atlassian.theplugin.idea.jira.IssueActionProvider;
import com.atlassian.theplugin.idea.jira.JiraIssueCachedAdapter;
import com.atlassian.theplugin.idea.jira.PerformIssueActionForm;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.jira.model.JIRAIssueListModelBuilder;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

import java.awt.*;
import java.util.List;

public class RunIssueActionAction extends AnAction {
	private final JiraIssueAdapter issue;
	private JIRAAction action;
	private JiraServerFacade facade;
	private IssueActionProvider window;
	private JIRAIssueListModelBuilder jiraIssueListModelBuilder;

    public RunIssueActionAction(IssueActionProvider toolWindow, JiraServerFacade facade,
			JiraIssueAdapter issue, JIRAAction jiraAction, final JIRAIssueListModelBuilder jiraIssueListModelBuilder) {
		super(jiraAction.getName());
		this.issue = issue;
		action = jiraAction;
		window = toolWindow;
		this.facade = facade;
		this.jiraIssueListModelBuilder = jiraIssueListModelBuilder;
	}

	@Override
	public void actionPerformed(AnActionEvent event) {
		runIssueAction(IdeaHelper.getCurrentProject(event), null);
	}

	public void runIssueAction(Project project, ActiveIssueResultHandler resultHandler) {
		ProgressManager.getInstance().run(new IssueActionRunnable(project, resultHandler));
	}

	private class IssueActionRunnable extends Task.Modal {
		private Project project;
        private ActiveIssueResultHandler resultHandler;

        IssueActionRunnable(Project project, ActiveIssueResultHandler resultHandler) {
			super(project, "Running Issue Action", true);
			this.project = project;
            this.resultHandler = resultHandler;
        }

		public void run(final ProgressIndicator indicator) {

			if (indicator != null) {
				indicator.setFraction(0.0);
				indicator.setIndeterminate(true);
			}

			showInfo("Retrieving fields for action \"" + action.getName() + "\" in issue " + issue.getKey() + "...");

			final JiraServerData server = issue.getJiraServerData();

			if (server != null) {
				final List<JIRAActionField> fields;
				try {
					fields = facade.getFieldsForAction(server, issue, action);
				} catch (JIRAException e) {
					showError(
							"Cannot retrieve fields for action [" + action.getName() + "] on issue [" + issue.getKey() + "]"
									+ e.getMessage(), e);
					showDialogDetailedInfo(project, e);
                    notifyResultHandler(resultHandler, e);
					return;
				}

				showInfo("Retrieving issue details");
				final JiraIssueAdapter detailedIssue;
				try {
					JiraIssueAdapter issueWithTime = facade.getIssue(issue.getJiraServerData(),
							issue.getKey());
					detailedIssue = facade.getIssueDetails(issue.getJiraServerData(), issueWithTime);
                    IdeaHelper.getProjectCfgManager(project).
                        addProjectConfigurationListener(detailedIssue.getLocalConfigurationListener());
                    
				} catch (JIRAException e) {
					showError("Cannot retrieve issue details for [" + issue.getKey() + "]: " + e.getMessage(), e);
					showDialogDetailedInfo(project, e);
                    notifyResultHandler(resultHandler, e);
					return;
				}

				showInfo("Running action [" + action.getName() + "] on issue [" + issue.getKey() + "]...");

				showInfo("Retrieving values for action fields");
				final List<JIRAActionField> preFilleddfields = JiraActionFieldType.fillFieldValues(detailedIssue, fields);

				if (preFilleddfields.isEmpty()) {
					try {
						facade.progressWorkflowAction(server, issue, action);
						performPostActionActivity(server);
                        if (resultHandler != null) {
                            resultHandler.success();
                        }
					} catch (JIRAException e) {
						showError("Unable to run action [" + action.getName() + "] on issue [" + issue.getKey() + "]: "
								+ e.getMessage(), e);
						showDialogDetailedInfo(project, e);
                        notifyResultHandler(resultHandler, e);
					}
				} else {
					EventQueue.invokeLater(new LocalDisplayActionDialogRunnable(project, detailedIssue,
                            preFilleddfields, server, resultHandler));
				}
			}
		}
	}

	private void showInfo(final String s) {
		window.setStatusInfoMessage(s);
	}

	private void showError(final String error, final Throwable exception) {
		window.setStatusErrorMessage(error, exception);
	}

	private void showDialogDetailedInfo(final Project project, final Throwable e) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				DialogWithDetails.showExceptionDialog(project, e.getMessage(), e);
			}
		});
	}

	/**
	 * Should be called in the background thread
	 *
	 * @param server
	 * @throws com.atlassian.theplugin.commons.jira.api.commons.rss.JIRAException
	 */
	private void performPostActionActivity(final JiraServerData server) throws JIRAException {
		if (action.getId() == Constants.JiraActionId.START_PROGRESS.getId()) {
			JIRAIssueProgressTimestampCache.getInstance().setTimestamp(server, issue);
		} else if (action.getId() == Constants.JiraActionId.STOP_PROGRESS.getId()) {
			JIRAIssueProgressTimestampCache.getInstance().removeTimestamp(server, issue);
		}

		JiraIssueCachedAdapter.get(issue).clearCachedActions();

		if (jiraIssueListModelBuilder != null) {
			jiraIssueListModelBuilder.reloadIssue(issue.getKey(), server);
		}
		showInfo("Action [" + action.getName() + "] on issue " + issue.getKey() + " run succesfully");
	}

	private class LocalDisplayActionDialogRunnable implements Runnable {
		private Project project;
		private JiraIssueAdapter detailedIssue;
		private List<JIRAActionField> preFilleddfields;
		private JiraServerData server;
        private ActiveIssueResultHandler resultHandler;

        public LocalDisplayActionDialogRunnable(final Project project,
                                                final JiraIssueAdapter detailedIssue,
                                                final List<JIRAActionField> preFilleddfields,
                                                final JiraServerData server,
                                                ActiveIssueResultHandler resultHandler) {
			this.project = project;
			this.detailedIssue = detailedIssue;
			this.preFilleddfields = preFilleddfields;
			this.server = server;
            this.resultHandler = resultHandler;
        }

		public void run() {
			// show action fields dialog
			final PerformIssueActionForm dialog =
					new PerformIssueActionForm(project, detailedIssue, preFilleddfields,
							action.getName() + " " + detailedIssue.getKey());
			dialog.show();
			if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
				// perform workflow action in the background thread
				ProgressManager.getInstance().run(
						new Task.Backgroundable(project, "Running Workflow Action", false) {
							public void run(final ProgressIndicator indicator) {
								if (indicator != null) {
									indicator.setFraction(0.0);
									indicator.setIndeterminate(true);
								}
								try {
									facade.progressWorkflowAction(server, issue, action, dialog.getFields());
								} catch (JIRAException e) {
									showError("Unable to run action [" + action.getName() + "] on issue ["
											+ issue.getKey() + "]: " + e.getMessage(), e);
									showDialogDetailedInfo(project, e);
                                    notifyResultHandler(resultHandler, e);
                                    return;
								}
								try {
									if (dialog.getComment() != null && dialog.getComment().length() > 0) {
										facade.addComment(server, issue.getKey(), dialog.getComment());
									}
								} catch (JIRAException e) {
									showError("Unable to add comment to action [" + action.getName()
											+ "] on issue [" + issue.getKey() + "]: " + e.getMessage(), e);
									showDialogDetailedInfo(project, e);
                                    notifyResultHandler(resultHandler, e);
                                }
								try {
									performPostActionActivity(server);
                                    if (resultHandler != null) {
                                        resultHandler.success();
                                    }
								} catch (JIRAException e) {
									showError(e.getMessage(), e);
									showDialogDetailedInfo(project, e);
                                    notifyResultHandler(resultHandler, e);
                                }
							}
						});
			} else {
				showInfo("Running workflow action [" + action.getName() + "] cancelled");
                if (resultHandler != null) {
                    resultHandler.failure("Running workflow action [" + action.getName() + "] cancelled");
                }
			}
		}
    }

    private void notifyResultHandler(ActiveIssueResultHandler resultHandler, JIRAException e) {
        if (resultHandler != null) {
            resultHandler.failure(e);
        }
    }
}
