package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.jira.api.JIRASavedFilter;
import com.atlassian.theplugin.jira.model.*;

import java.util.List;

/**
 * User: pmaruszak
 */
public class IssueToolWindowFreezeSynchronizator {
	private JIRAFilterListModel filterMode;
	private JIRAIssueListModel issueModel;
	private JIRAServerModel serverModel;

	IssueToolWindowFreezeSynchronizator(final JIRAFilterListModel filterModel, final JIRAIssueListModel issueModel,
										final JIRAServerModel serverModel){
		this.filterMode = filterModel;
		this.issueModel = issueModel;
		this.serverModel = serverModel;

		filterModel.addModelListener(new JIRAFilterListModelListener(){

			public void modelChanged(JIRAFilterListModel listModel) {
			}

			public void selectedSavedFilter(JiraServerCfg jiraServer, JIRASavedFilter savedFilter) {
			}

			public void selectedManualFilter(JiraServerCfg jiraServer, List<JIRAQueryFragment> manualFilter) {
			}

			public void modelFrozen(JIRAFilterListModel jiraFilterListModel, boolean frozen) {
				if (issueModel != null) {
					issueModel.setModelFrozen(frozen);
				}

				if (serverModel != null){
					serverModel.setModelFrozen(frozen);
				}
			}
		});

		issueModel.addModelListener(new JIRAIssueListModelListener(){

			public void modelChanged(JIRAIssueListModel model) {
			}

			public void issuesLoaded(JIRAIssueListModel model, int loadedIssues) {
			}

			public void modelFrozen(JIRAIssueListModel model, boolean frozen) {
				if (filterModel != null){
					filterModel.setModelFrozen(frozen);
				}
				if (serverModel != null){
						serverModel.setModelFrozen(frozen);
				}
			}
		});
	}
}
