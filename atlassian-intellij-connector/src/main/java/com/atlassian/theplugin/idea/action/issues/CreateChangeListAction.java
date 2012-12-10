package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vcs.changes.ChangeListManager;

import javax.swing.*;

public class CreateChangeListAction extends JIRAAbstractAction {
    @Override
	public void actionPerformed(AnActionEvent anActionEvent) {
		final IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(anActionEvent);
		final JiraIssueAdapter issue = anActionEvent.getData(Constants.ISSUE_KEY);
		if (panel != null && issue != null) {
			panel.createChangeListAction(issue);
		}
	}

	public void onUpdate(AnActionEvent event) {
        setActionIcon(event);
	}

    private void setActionIcon(AnActionEvent event) {
        try {
            Icon icon = IconLoader.getIcon("/general/toolWindowChanges.png");
            if (icon == null || icon.getIconHeight() == 0) {
                icon = IconLoader.getIcon("/toolwindows/toolWindowChanges.png");
            }
            if (icon != null && icon.getIconHeight() > 0) {
    //                Icon disabledIcon = IconLoader.getDisabledIcon(icon);
                    event.getPresentation().setIcon(icon);
            }
        } catch (Throwable t) {
        }
    }

    public void onUpdate(AnActionEvent event, boolean enabled) {

        setActionIcon(event);

//		if (enabled) {
		final JiraIssueAdapter issue = event.getData(Constants.ISSUE_KEY);
		event.getPresentation().setEnabled(issue != null);

		if (issue != null) {
			String changeListName = issue.getKey() + " - " + issue.getSummary();
			final Project project = event.getData(PlatformDataKeys.PROJECT);
			if (project != null) {
				if (ChangeListManager.getInstance(project).findChangeList(changeListName) == null) {
					event.getPresentation().setText("Create ChangeList");
				} else {
					event.getPresentation().setText("Activate ChangeList");
				}
			}
		}
	}
//	}
}
