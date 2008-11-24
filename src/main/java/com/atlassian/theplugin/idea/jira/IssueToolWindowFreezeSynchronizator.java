package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.jira.model.*;

/**
 * User: pmaruszak
 */
public final class IssueToolWindowFreezeSynchronizator {
	private JIRAFilterListModel filterMode;
	private JIRAIssueListModel issueModel;
	private JIRAServerModel serverModel;

	IssueToolWindowFreezeSynchronizator(final JIRAFilterListModel filterModel, final JIRAIssueListModel issueModel,
										final JIRAServerModel serverModel) {
		this.filterMode = filterModel;
		this.issueModel = issueModel;
		this.serverModel = serverModel;

		filterModel.addFrozenModelListener(new FrozenModelListener() {

			public void modelFrozen(FrozenModel model, boolean frozen) {
				if (serverModel != null) {					
					serverModel.setModelFrozen(frozen);
				}				

			}
		});

		issueModel.addFrozenModelListener(new FrozenModelListener() {

			public void modelFrozen(FrozenModel model, boolean frozen) {
				if (filterModel != null) {
					filterModel.setModelFrozen(frozen);
				}
				if (serverModel != null) {
					serverModel.setModelFrozen(frozen);
				}
			}
		});
	}
}
