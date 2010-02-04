package com.atlassian.theplugin.idea.action.issues.activetoolbar.tasks;

import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.action.issues.activetoolbar.ActiveIssueUtils;
import com.atlassian.theplugin.idea.jira.ActiveIssueResultHandler;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.intellij.openapi.project.Project;

import javax.swing.*;

/**
 * @author pmaruszak
 */
public class DeactivateIssueRunnable  implements Runnable {
        private final Project project;
        private final JiraWorkspaceConfiguration conf;

        public DeactivateIssueRunnable(final Project project) {

            this.project = project;
            conf = IdeaHelper.getProjectComponent(project, JiraWorkspaceConfiguration.class);
        }


        public void run() {

            ActiveIssueUtils.deactivate(project, new ActiveIssueResultHandler() {
                public void success() {
                    if (conf != null) {
                        conf.setActiveJiraIssuee(null);

                    }
                }

                public void failure(final Throwable problem) {
                    if (conf != null) {
                        conf.setActiveJiraIssuee(null);

                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if (project != null && !project.isDisposed()) {
                                DialogWithDetails.showExceptionDialog(
                                        project,
                                        "Issue Deactivated Locally but Failed to Deactivate Issue remotely.", problem);
                            }
                        }
                    });
                }

                public void cancel(String problem) {
                    PluginTaskManagerFacade.silentActivateIssue(project, conf.getActiveJiraIssuee());
                }
            });

        }
    }
