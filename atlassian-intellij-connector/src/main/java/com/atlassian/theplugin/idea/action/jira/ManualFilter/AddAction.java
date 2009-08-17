package com.atlassian.theplugin.idea.action.jira.ManualFilter;

import com.atlassian.theplugin.commons.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.commons.jira.cache.JIRAServerModel;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.configuration.JiraFilterConfigurationBean;
import com.atlassian.theplugin.configuration.JiraFilterEntryBean;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.atlassian.theplugin.idea.jira.JiraIssuesFilterPanel;
import com.atlassian.theplugin.jira.model.JIRAFilterListModel;
import com.atlassian.theplugin.jira.model.JiraCustomFilter;
import com.intellij.openapi.actionSystem.AnActionEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: pmaruszak
 * @date Aug 7, 2009
 */
public class AddAction extends AbstractFilterAction {

    public void actionPerformed(AnActionEvent event) {
        final IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(event);

        if (panel == null) {
            return;
        }

        JiraCustomFilter newCustomFilter = new JiraCustomFilter();
        ServerData jiraServer = panel.getSelectedServer();
        JIRAServerModel jiraServerModel = panel.getJiraServerModel();
        JIRAFilterListModel jiraFilterListModel = panel.getJIRAFilterListModel();


        if (jiraServer != null && jiraServerModel != null && jiraFilterListModel != null) {
            final JiraIssuesFilterPanel jiraIssuesFilterPanel
                    = new JiraIssuesFilterPanel(IdeaHelper.getCurrentProject(event), jiraServerModel,
                    jiraFilterListModel, jiraServer);

            jiraIssuesFilterPanel.setFilter(newCustomFilter.getName(), new ArrayList<JIRAQueryFragment>());
            jiraIssuesFilterPanel.show();

            if (jiraIssuesFilterPanel.getExitCode() == 0) {
                jiraFilterListModel.clearManualFilter(jiraServer, newCustomFilter);
                newCustomFilter.getQueryFragment().addAll(jiraIssuesFilterPanel.getFilter());
                newCustomFilter.setName(jiraIssuesFilterPanel.getFilterName());
                jiraFilterListModel.addManualFilter(jiraServer, newCustomFilter);

                JiraWorkspaceConfiguration jiraProjectCfg = IdeaHelper.getJiraWorkspaceConfiguration(event);
                if (jiraProjectCfg != null) {
                    JiraFilterConfigurationBean bean = new JiraFilterConfigurationBean();
                    bean.setManualFilter(serializeFilter(newCustomFilter.getQueryFragment()));
                    bean.setName(newCustomFilter.getName());
                    bean.setUid(newCustomFilter.getUid());

                    jiraProjectCfg.getJiraFilterConfiguaration(jiraServer.getServerId())
                            .getCustomFilters().put(bean.getUid(), bean);
                }

                jiraFilterListModel.fireManualFilterAdded(newCustomFilter, jiraServer);
            }

        }

    }


    private List<JiraFilterEntryBean> serializeFilter(List<JIRAQueryFragment> filter) {
        List<JiraFilterEntryBean> query = new ArrayList<JiraFilterEntryBean>();
        for (JIRAQueryFragment jiraQueryFragment : filter) {
            query.add(new JiraFilterEntryBean(jiraQueryFragment.getMap()));
        }
        return query;
    }

    public boolean isEnabled(AnActionEvent event) {
        final IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(event);
        return panel != null && panel.getSelectedServer() != null;
    }
}
