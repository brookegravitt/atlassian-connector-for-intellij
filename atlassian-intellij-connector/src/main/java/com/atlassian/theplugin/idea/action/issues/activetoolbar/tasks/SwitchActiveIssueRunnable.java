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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.changes.ChangeList;

import java.util.ArrayList;
import java.util.List;

/**
 * User: pmaruszak
 */
public class SwitchActiveIssueRunnable implements Runnable {
    private final Project project;
    private final LocalTask activatedLocalTask;

    public SwitchActiveIssueRunnable(Project project, LocalTask activatedLocalTask) {
        this.project = project;

        this.activatedLocalTask = activatedLocalTask;
    }

    public void run() {
        final PluginTaskManager taskManager = PluginTaskManager.getInstance(project);
        String activeTaskUrl = activatedLocalTask.getIssueUrl();
        JiraIssueAdapter jiraActiveIssueAdapater = null;


        if (activeTaskUrl == null) {
            activeTaskUrl = taskManager.getIssueUrl(activatedLocalTask.getId());
        }

        try {
            //the same issue activated == omit
             jiraActiveIssueAdapater = ActiveIssueUtils.getJIRAIssue(project);
            if (activeTaskUrl != null && jiraActiveIssueAdapater != null
                    && activeTaskUrl.equals(jiraActiveIssueAdapater.getIssueUrl())) {
                return;
            }
        } catch (Exception e) {
             PluginUtil.getLogger().error("Cannot get issue " + activeTaskUrl);
        }
        //switched to default task so silentDeactivate issue
        if (activatedLocalTask.getId() != null && activatedLocalTask.getId().equalsIgnoreCase("Default")) {
            taskManager.deactivateToDefaultTask();
            return;
        }

        final String finalActiveTaskUrl = activeTaskUrl;
        if (activeTaskUrl != null) {
            final JiraServerData server = taskManager.findJiraPluginJiraServer(activeTaskUrl);
            final ActiveJiraIssueBean issue = new ActiveJiraIssueBean();
            issue.setIssueKey(activatedLocalTask.getId());
            issue.setServerId(server != null ? (ServerIdImpl) server.getServerId() : null);

            activateTask(activatedLocalTask.getId(), server, issue, finalActiveTaskUrl, null);
        } else {


            Messages.showInfoMessage(project, "Cannot activate an issue "
                    + taskManager.getActiveTask().getId() + "."
                    + "\nIssue without linked server.", PluginUtil.PRODUCT_NAME);

            taskManager.deactivateToDefaultTask();
        }

    }

    private void activateTask(final String issueId, final JiraServerData server, final ActiveJiraIssueBean issue,
                              final String finalActiveTaskUrl, final ChangeList newDefaultList) {
        {
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

                    DialogWithDetails.showExceptionDialog(project,
                            "Cannot fetch issue " + issueId + " from server " + server.getName(), e);

                }

                if ((ActiveIssueUtils.getActiveJiraIssue(project) == null && issueId != null)
                        || (ActiveIssueUtils.getActiveJiraIssue(project) != null && issueId != null
                        && !issueId.equals(ActiveIssueUtils.getActiveJiraIssue(project).getIssueKey()))) {

                            ActiveIssueUtils.activateIssue(project, null, issue, server, newDefaultList);
                       

                }
            } else {
                

                Messages.showInfoMessage(project, "Cannot activate issue " + finalActiveTaskUrl,
                        PluginUtil.PRODUCT_NAME);

            }
        }
    }

}
