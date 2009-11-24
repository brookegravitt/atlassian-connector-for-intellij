package com.atlassian.theplugin.idea.action.issues.activetoolbar.tasks;

import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.action.issues.activetoolbar.ActiveIssueUtils;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.atlassian.theplugin.jira.model.JIRAIssueListModelBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ChangeListAdapter;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;

/**
 * User: pmaruszak
 */
public class TaskChangeListAdapter extends ChangeListAdapter {
    private final Project project;    

    public TaskChangeListAdapter(final Project project) {
        this.project = project;

    }

    public void changeListRemoved(ChangeList list) {
        Object localTaskObj = PluginTaskManager.getInstance(project).findTaskByChangeList(list);
        if (localTaskObj != null) {

            PluginTaskManager.getInstance(project).removeTaskFromIdea(localTaskObj);
            PluginTaskManager.getInstance(project).deactivateTask();

        }
    }

    public void defaultListChanged(final ChangeList oldDefaultList, final ChangeList newDefaultList) {
        ApplicationManager.getApplication().invokeLater(new SwitchActiveIssueRunnable(project, newDefaultList));
    }

    @Override
    public void changeListAdded(ChangeList list) {
        ActiveJiraIssue activeIssue = ActiveIssueUtils.getActiveJiraIssue(project);
        JIRAIssueListModelBuilder builder = IdeaHelper.getJIRAIssueListModelBuilder(project);
        final ChangeListManager changeListManager = ChangeListManager.getInstance(project);

        if (builder != null && builder.getModel() != null) {
            JiraIssueAdapter issue = builder.getModel().findIssue(activeIssue.getIssueKey());
            if (issue != null) {
                String changeListName = issue.getKey() + " - " + issue.getSummary() + "\n";
                if (list instanceof LocalChangeList && list.getComment().length() <= 0) {
                    ((LocalChangeList) list).setComment(changeListName);
                }

            }
        }

    }


}

