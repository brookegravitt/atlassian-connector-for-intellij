package com.atlassian.theplugin.idea.action.issues.activetoolbar;

import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.jira.cache.RecentlyOpenIssuesCache;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.atlassian.theplugin.jira.model.ActiveJiraIssueBean;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import javax.swing.*;

/**
 * User: kalamon
 * Date: Jul 16, 2009
 * Time: 12:11:02 PM
 */
public class ActiveIssueDropDownAction extends ComboBoxAction {
    public ActiveIssueDropDownAction() {
    }

    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setText("");
        event.getPresentation().setDescription("Select issue to start work on");
    }

    @NotNull
    protected DefaultActionGroup createPopupActionGroup(JComponent jComponent) {
        final Project project = IdeaHelper
                .getCurrentProject(DataManager.getInstance().getDataContext(jComponent));

        DefaultActionGroup group = new DefaultActionGroup("Issues to activate", true);
        final RecentlyOpenIssuesCache cache = IdeaHelper.getProjectComponent(project, RecentlyOpenIssuesCache.class);
        final ActiveJiraIssue activeIssue = ActiveIssueUtils.getActiveJiraIssue(project);
        if (cache != null && (cache.getLoadedRecenltyOpenIssues().size() > 1
                || cache.getLoadedRecenltyOpenIssues().size() == 1 && activeIssue == null)) {

            for (JiraIssueAdapter issue : cache.getLoadedRecenltyOpenIssues()) {
                if (activeIssue == null || !issue.getKey().equals(activeIssue.getIssueKey())) {
                    ActiveJiraIssue newActiveIsse = new ActiveJiraIssueBean(issue.getJiraServerData().getServerId(),
                            issue.getIssueUrl(), issue.getKey(), new DateTime());
                    group.add(new ActivateIssueItemAction(newActiveIsse, project));
                }
            }
        } else {
            createEmptyList(group);
        }

        return group;
    }

    private void createEmptyList(DefaultActionGroup group) {
        AnAction action = new AnAction("No  recently viewed issues found") {
            public void actionPerformed(AnActionEvent anActionEvent) {
            }
        };
        action.setDefaultIcon(true);
        group.add(action);
    }
}
