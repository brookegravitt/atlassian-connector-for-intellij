package com.atlassian.theplugin.idea.action.issues.activetoolbar.tasks;

import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.jira.IntelliJJiraServerFacade;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.action.issues.activetoolbar.ActiveIssueUtils;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.jira.model.ActiveJiraIssueBean;
import com.atlassian.theplugin.jira.model.JIRAIssueListModelBuilder;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.changes.ChangeList;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: pmaruszak
 */
   public class SwitchActiveIssueRunnable implements Runnable {
    private final Project project;
    private final ChangeList newDefaultList;

        public SwitchActiveIssueRunnable(Project project, ChangeList newDefaultList) {
            this.project = project;
            this.newDefaultList = newDefaultList;
        }

        public void run() {
            final PluginTaskManager taskManager = PluginTaskManager.getInstance(project);
            LocalTask activeTask = taskManager.findTaskByChangeList(newDefaultList);
            String activeTaskUrl =  activeTask.getIssueUrl();

            if (activeTask == null) {
                return;
            }
            //removeChangeListListener();

            //switched to default task so silentDeactivate issue
            if (taskManager.getLocalChangeListId(newDefaultList) != null
                    && taskManager.getLocalChangeListId(taskManager.getDefaultChangeList()) != null
                    && taskManager.getLocalChangeListId(newDefaultList).equals(taskManager.getLocalChangeListId(taskManager.getDefaultChangeList()))) {
                taskManager.deactivateTask();
                return;
            }

            if (activeTaskUrl == null) {
                activeTaskUrl = taskManager.getActiveIssueUrl(activeTask.getId());
            }
            final String finalActiveTaskUrl = activeTaskUrl;


            if (activeTaskUrl != null) {
                final JiraServerData server = taskManager.findJiraPluginJiraServer(activeTaskUrl);
                final ActiveJiraIssueBean issue = new ActiveJiraIssueBean();
                issue.setIssueKey(activeTask.getId());
                issue.setServerId(server != null ? (ServerIdImpl) server.getServerId() : null);

                ApplicationManager.getApplication().invokeLater(
                        new LocalRunnable(activeTask.getId(), server, issue, finalActiveTaskUrl, newDefaultList),
                        ModalityState.defaultModalityState());
            } else {

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        Messages.showInfoMessage(project, "Cannot activate an issue " + taskManager.getActiveTask().getId() + "."
                                + "\nIssue without linked server.", PluginUtil.PRODUCT_NAME);
                    }
                });

                taskManager.deactivateTask();
            }

        }



    final class LocalRunnable implements Runnable {
        private final String issueId;
        private final JiraServerData server;
        private final ActiveJiraIssueBean issue;
        private final String finalActiveTaskUrl;
        private final ChangeList newDefaultList;

        public LocalRunnable(String issueId, JiraServerData server, ActiveJiraIssueBean issue,
                             String finalActiveTaskUrl, ChangeList newDefaultList) {
            this.issueId = issueId;
            this.server = server;
            this.issue = issue;
            this.finalActiveTaskUrl = finalActiveTaskUrl;
            this.newDefaultList = newDefaultList;
        }


        public void run() {
            JIRAIssueListModelBuilder builder = IdeaHelper.getJIRAIssueListModelBuilder(project);
            if (server != null && builder != null) {
                try {
                    if (builder.getModel().findIssue(issueId) == null) {
                        JiraIssueAdapter issueAdapter = IntelliJJiraServerFacade.getInstance().getIssue(server,
                                issueId);
                        if (issueAdapter != null) {
                            List list = new ArrayList();
                            list.add(issueAdapter);
                            builder.getModel().addIssues(list);
                        }
                    }

                } catch (final JIRAException e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            DialogWithDetails.showExceptionDialog(project,
                                    "Cannot fetch issue " + issueId + " from server " + server.getName(), e);
                        }
                    });

                }

                if ((ActiveIssueUtils.getActiveJiraIssue(project) == null && issueId != null)
                        || (ActiveIssueUtils.getActiveJiraIssue(project) != null && issueId != null
                        && !issueId.equals(ActiveIssueUtils.getActiveJiraIssue(project).getIssueKey()))) {
                    ActiveIssueUtils.activateIssue(project, null, issue, server, newDefaultList);
                }
            } else {
                PluginTaskManager.getInstance(project).addChangeListListener();
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        Messages.showInfoMessage(project, "Cannot activate issue " + finalActiveTaskUrl,
                                PluginUtil.PRODUCT_NAME);
                    }
                });

            }
        }
    }

    }
