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

import com.atlassian.theplugin.commons.jira.IntelliJJiraServerFacade;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.JiraServerFacade;
import com.atlassian.theplugin.commons.jira.api.JIRAAction;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.commons.jira.api.commons.rss.JIRAException;
import com.atlassian.theplugin.commons.util.StringUtil;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.*;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
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
import com.intellij.openapi.vcs.changes.ChangeList;
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


    public static void activateLocalTask(final Project project, final ActiveJiraIssue issue) {
        PluginTaskManager.getInstance(project).activateLocalTask(issue);
    }

    public static void setActiveJiraIssue(final Project project, final ActiveJiraIssue issue,
                                          final JiraIssueAdapter jiraIssue) {
        final JiraWorkspaceConfiguration conf = IdeaHelper.getProjectComponent(project, JiraWorkspaceConfiguration.class);
        final RecentlyOpenIssuesCache issueCache = IdeaHelper.getProjectComponent(project, RecentlyOpenIssuesCache.class);

        if (conf != null) {
            conf.setActiveJiraIssuee((ActiveJiraIssueBean) issue);

            if (jiraIssue != null && issueCache != null) {
                issueCache.addIssue(jiraIssue);
            }
        }
    }

    public static JiraIssueAdapter getSelectedJiraIssue(final AnActionEvent event) {
        return event.getData(Constants.ISSUE_KEY);
    }

    public static StatusBarPane getStatusBarPane(final AnActionEvent event) {
        if (event != null) {
            return event.getData(Constants.STATUS_BAR_PANE_KEY);
        }

        return null;
    }

    //invokeLater necessary

    public static JiraIssueAdapter getJIRAIssue(final AnActionEvent event) throws JIRAException {
        return getJIRAIssue(IdeaHelper.getCurrentProject(event));
    }

    //invokeLater necessary
    public static JiraIssueAdapter getJIRAIssue(final Project project) throws JIRAException {
        JiraServerData jiraServer = getJiraServer(project);
        if (jiraServer != null) {
            final ActiveJiraIssue issue = getActiveJiraIssue(project);
            return getJIRAIssue(jiraServer, issue);
        }
        return null;
    }

    public static JiraIssueAdapter getJIRAIssue(final JiraServerData jiraServer, final ActiveJiraIssue activeIssue)
            throws JIRAException {
        if (jiraServer != null && activeIssue != null) {

            JiraServerFacade facade = IntelliJJiraServerFacade.getInstance();

            try {
                return facade.getIssue(jiraServer, activeIssue.getIssueKey());
            } catch (JIRAException e) {
                PluginUtil.getLogger().error(e.getMessage());
                throw e;
            }
        }
        return null;
    }


    public static JiraServerData getJiraServer(final AnActionEvent event) {
        return getJiraServer(IdeaHelper.getCurrentProject(event));

    }

    public static JiraServerData getJiraServer(final Project project) {
        final ActiveJiraIssue issue = getActiveJiraIssue(project);
        return getJiraServer(project, issue);
    }

    public static JiraServerData getJiraServer(final Project project, final ActiveJiraIssue activeIssue) {
        final IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(project);
        JiraServerData jiraServer = null;

        if (panel != null && activeIssue != null) {
            jiraServer = panel.getProjectCfgManager().getJiraServerr(activeIssue.getServerId());
        }
        return jiraServer;
    }

    public static void activateIssue(final Project project, final AnActionEvent event, final ActiveJiraIssue newActiveIssue,
                                     final JiraServerData jiraServerCfg, final ChangeList newDefaultList) {

        final ActiveJiraIssue activeIssue = ActiveIssueUtils.getActiveJiraIssue(project);
        boolean isAlreadyActive = activeIssue != null && newActiveIssue != activeIssue;
        boolean isDeactivated = true;
        if (isAlreadyActive) {


            isDeactivated = Messages.showYesNoDialog(project,
                    activeIssue.getIssueKey()
                            + " is active. Would you like to deactivate it first and proceed?",
                    "Deactivating current issue",
                    Messages.getQuestionIcon()) == DialogWrapper.OK_EXIT_CODE;
        }
        if (isDeactivated) {


            ActiveIssueUtils.deactivate(project, new ActiveIssueResultHandler() {
                public void success() {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {

                            ActiveIssueUtils.activate(project, event, newActiveIssue, jiraServerCfg, newDefaultList,
                                    new ActiveIssueResultHandler() {

                                public void success() {
                                    ActiveIssueUtils.activateLocalTask(project, newActiveIssue);
                                    PluginTaskManager.getInstance(project).addChangeListListener();                                    
                                }

                                public void failure(Throwable problem) {
                                    PluginTaskManager.getInstance(project).addChangeListListener();
                                }

                                public void failure(String problem) {
                                    PluginTaskManager.getInstance(project).addChangeListListener();
                                }
                            });

                        }
                    });
                }

                public void failure(Throwable problem) {
                    PluginTaskManager.getInstance(project).addChangeListListener();

                }

                public void failure(String problem) {
                    PluginTaskManager.getInstance(project).addChangeListListener();
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
    private static boolean isInProgress(final JiraIssueAdapter issue) {
        List<JIRAAction> actions = JiraIssueCachedAdapter.get(issue).getCachedActions();

        if (actions == null) {

            JiraServerData jiraServer = issue.getJiraServerData();

            if (jiraServer != null) {
                try {
                    actions = IntelliJJiraServerFacade.getInstance().getAvailableActions(jiraServer, issue);
                } catch (JIRAException e) {
                    PluginUtil.getLogger().warn("Cannot fetch issue actions: " + e.getMessage(), e);
                }

                JiraIssueCachedAdapter.get(issue).setCachedActions(actions);
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
    public static void checkIssueState(final Project project, final JiraIssueAdapter issue) {
        ActiveJiraIssue activeIssue = getActiveJiraIssue(project);
        if (issue != null && activeIssue != null) {

            if (issue.getJiraServerData() != null && issue.getKey().equals(activeIssue.getIssueKey())
                    && issue.getJiraServerData().getServerId().equals(activeIssue.getServerId())) {

                ProgressManager.getInstance().run(new Task.Backgroundable(project, "Checking active issue state") {
                    public void run(final ProgressIndicator indicator) {

                        if (!issue.getJiraServerData().getUsername().equals(issue.getAssigneeId())) {

                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    int isOk = Messages.showYesNoDialog(project,
                                            "Issue " + issue.getKey() + " has changed assignee (assigned to:"
                                                    + issue.getAssignee()
                                                    + ", status: " + issue.getStatus() + ").\nDo you want to deactivate?",
                                            "Issue " + issue.getKey(), Messages.getQuestionIcon());

                                    if (isOk == DialogWrapper.OK_EXIT_CODE) {
                                        deactivate(project, new ActiveIssueResultHandler() {
                                            public void success() {
                                                final JiraWorkspaceConfiguration conf = IdeaHelper
                                                        .getProjectComponent(project, JiraWorkspaceConfiguration.class);
                                                if (conf != null) {
                                                    conf.setActiveJiraIssuee(null);
                                                }
                                                PluginTaskManager.getInstance(project).addChangeListListener();
                                            }

                                            public void failure(Throwable problem) {
                                                PluginTaskManager.getInstance(project).addChangeListListener();
                                            }

                                            public void failure(String problem) {
                                                PluginTaskManager.getInstance(project).addChangeListListener();
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
     * @param event          event
     * @param newActiveIssue issue
     * @param jiraServerCfg  server
     * @param newDefaultList
     * @param activeIssueResultHandler
     */
    private static void activate(final Project project, final AnActionEvent event, final ActiveJiraIssue newActiveIssue,
                                 final JiraServerData jiraServerCfg, ChangeList newDefaultList,
                                 ActiveIssueResultHandler activeIssueResultHandler) {

        if (project == null) {
            return;
        }

        final IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(project);
        final IssueDetailsToolWindow detailsPanel = IdeaHelper.getIssueDetailsToolWindow(project);

        ProgressManager.getInstance().run(
                new RefreshingIssueTask(project, jiraServerCfg, newActiveIssue, panel, detailsPanel, event,
                        newDefaultList, activeIssueResultHandler));
    }

    public static void deactivate(final AnActionEvent event, final ActiveIssueResultHandler resultHandler) {
        deactivate(IdeaHelper.getCurrentProject(event), resultHandler);
    }

    public static void deactivate(final Project project, final ActiveIssueResultHandler resultHandler) {
        final JiraWorkspaceConfiguration conf = IdeaHelper.getProjectComponent(project, JiraWorkspaceConfiguration.class);
        boolean isOk = true;
        PluginTaskManager.getInstance(project).removeChangeListListener();

        if (conf != null) {
            ActiveJiraIssueBean activeIssue = conf.getActiveJiraIssuee();
            if (activeIssue != null) {
                final IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(project);
                try {
                    final JiraIssueAdapter jiraIssue = ActiveIssueUtils.getJIRAIssue(project);
                    if (panel != null && jiraIssue != null) {

                        final JiraServerData jiraServer = ActiveIssueUtils.getJiraServer(project);

                        panel.logWorkOrDeactivateIssue(jiraIssue, jiraServer,
                                StringUtil.generateJiraLogTimeString(activeIssue.recalculateTimeSpent()),
                                true, resultHandler);
                        return;

                    }
                } catch (JIRAException e) {
                    if (panel != null) {
                        panel.setStatusErrorMessage(
                                "Issue stopped locally. Error stopping remotely work on issue: " + e.getMessage(), e);
                        resultHandler.failure(e);
                    }
                }
            }
            if (resultHandler != null) {
                resultHandler.success();
                return;
            }
        }

        //always allow to activate issue even if remote de-activation fails
        if (resultHandler != null) {
            resultHandler.failure("JIRA Workspace is empty");
        }
    }

    private static class RefreshingIssueTask extends Task.Backgroundable {
        private JiraIssueAdapter jiraIssue;
        private boolean isOk;
        private final Project project;
        private final JiraServerData jiraServerCfg;
        private final ActiveJiraIssue newActiveIssue;
        private final IssueListToolWindowPanel panel;
        private final IssueDetailsToolWindow detailsPanel;
        private final AnActionEvent event;
        private final ChangeList newDefaultList;
        private final ActiveIssueResultHandler activeIssueResultHandler;

        public RefreshingIssueTask(Project project, JiraServerData jiraServerCfg,
                                   ActiveJiraIssue newActiveIssue, IssueListToolWindowPanel panel,
                                   IssueDetailsToolWindow detailsPanel, AnActionEvent event, ChangeList newDefaultList,
                                   ActiveIssueResultHandler activeIssueResultHandler) {
            super(project, "Refreshing Issue Information", false);
            this.project = project;
            this.jiraServerCfg = jiraServerCfg;
            this.newActiveIssue = newActiveIssue;
            this.panel = panel;
            this.detailsPanel = detailsPanel;
            this.event = event;
            this.newDefaultList = newDefaultList;
            this.activeIssueResultHandler = activeIssueResultHandler;
            jiraIssue = null;
            isOk = false;
        }


        public void run(@NotNull final ProgressIndicator indicator) {
            try {
                // retrieve fresh issue instance from the server
                jiraIssue = ActiveIssueUtils.getJIRAIssue(jiraServerCfg, newActiveIssue);
                isOk = true;
            } catch (final JIRAException e) {
                PluginUtil.getLogger().warn("Error starting work on issue: " + e.getMessage(), e);

                if (panel != null) {
                    panel.setStatusErrorMessage("Error starting work on issue: " + e.getMessage(), e);
                }

                boolean messageDisplayed = false;
                if (detailsPanel != null) {
                    messageDisplayed = detailsPanel.setStatusErrorMessage(newActiveIssue.getIssueKey(),
                            "Error starting work on issue: " + e.getMessage(), e);
                }

                if (panel == null && detailsPanel == null || !messageDisplayed) {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            DialogWithDetails.showExceptionDialog(IdeaHelper.getCurrentProject(event),
                                    "Error starting work on issue:", e);
                            //activeIssueResultHandler.failure(e);
                        }
                    });

                }
                isOk = false;
            }
        }

        public void onSuccess() {
            if (isOk && panel != null && jiraIssue != null && jiraServerCfg != null) {
                if (!jiraServerCfg.getUsername().equals(jiraIssue.getAssigneeId())
                        && !"-1".equals(jiraIssue.getAssigneeId())) {
                    isOk = Messages.showYesNoDialog(project,
                            "Issue " + jiraIssue.getKey() + " is already assigned to " + jiraIssue.getAssignee()
                                    + ".\nDo you want to overwrite assignee and start progress?",
                            "Issue " + jiraIssue.getKey(), Messages.getQuestionIcon()) == DialogWrapper.OK_EXIT_CODE;

                }
                if (isOk) {
                    //assign to me and start working
                    try {
                        panel.startWorkingOnIssueAndActivate(jiraIssue, newActiveIssue,
                                ActiveIssueUtils.getStatusBarPane(event), newDefaultList, activeIssueResultHandler);
                        //activeIssueResultHandler.success();

                    } catch (Exception e) {
                        DialogWithDetails.showExceptionDialog(project, "Cannot start work on issue " + jiraIssue.getId(), e);
                        activeIssueResultHandler.failure(e);

                    }
                }
            }
        }
    }
}
