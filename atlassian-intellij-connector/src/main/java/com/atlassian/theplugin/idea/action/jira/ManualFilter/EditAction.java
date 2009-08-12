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
package com.atlassian.theplugin.idea.action.jira.ManualFilter;

import com.atlassian.theplugin.commons.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.commons.jira.cache.JIRAServerModel;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.configuration.JiraCustomFilterMap;
import com.atlassian.theplugin.configuration.JiraFilterConfigurationBean;
import com.atlassian.theplugin.configuration.JiraFilterEntryBean;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.atlassian.theplugin.idea.jira.JiraIssuesFilterPanel;
import com.atlassian.theplugin.jira.model.JIRAFilterListModel;
import com.atlassian.theplugin.jira.model.JiraCustomFilter;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import javax.swing.tree.DefaultTreeModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Jul 16, 2009
 */
public class EditAction extends AnAction {
    public void actionPerformed(AnActionEvent event) {
        final IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(event);

        if (panel == null) {
            return;
        }

        JiraCustomFilter manualFilter = panel.getSelectedManualFilter();
        ServerData jiraServer = panel.getSelectedServer();
        JIRAServerModel jiraServerModel = panel.getJiraServerModel();
        JIRAFilterListModel jiraFilterListModel = panel.getJIRAFilterListModel();


        if (manualFilter != null && jiraServer != null && jiraServerModel != null && jiraFilterListModel != null) {
            	final JiraIssuesFilterPanel jiraIssuesFilterPanel
						= new JiraIssuesFilterPanel(IdeaHelper.getCurrentProject(event), jiraServerModel,
                                    jiraFilterListModel, jiraServer);

					final java.util.List<JIRAQueryFragment> listClone = new ArrayList<JIRAQueryFragment>();
					for (JIRAQueryFragment fragment : manualFilter.getQueryFragment()) {
						if (fragment != null) {
							listClone.add(fragment.getClone());
						}
					}
					jiraIssuesFilterPanel.setFilter(manualFilter.getName(), listClone);
				jiraIssuesFilterPanel.show();

				if (jiraIssuesFilterPanel.getExitCode() == 0) {
                    jiraFilterListModel.clearManualFilter(jiraServer, manualFilter);
					manualFilter.getQueryFragment().addAll(jiraIssuesFilterPanel.getFilter());
					jiraFilterListModel.addManualFilter(jiraServer, manualFilter);
//					listModel.selectManualFilter(jiraServer, manualFilter, true);
					// store filter in project workspace
					JiraWorkspaceConfiguration jiraProjectCfg = IdeaHelper.getJiraWorkspaceConfiguration(event);
                    if (jiraProjectCfg != null) {
                        JiraCustomFilterMap filterMap =
                                jiraProjectCfg.getJiraFilterConfiguaration(jiraServer.getServerId());

                        for (JiraFilterConfigurationBean bean : filterMap.getCustomFilters().values()) {
                            if (bean.getUid().equals(manualFilter.getUid())) {
                                bean.setManualFilter(serializeFilter(jiraIssuesFilterPanel.getFilter()));    
                            }

                        }
                    }
					jiraFilterListModel.fireManualFilterChanged(manualFilter, jiraServer);
                    ((DefaultTreeModel) panel.getJiraFilterTree().getModel())
                            .nodeStructureChanged(panel.getJiraFilterTree().getSelectedNode());

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
}
