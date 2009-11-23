package com.atlassian.theplugin.idea.action.issues.activetoolbar.tasks;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ChangeListAdapter;

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
   
}

