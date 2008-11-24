package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.jira.model.*;

/**
 * User: pmaruszak
 */
public final class IssueToolWindowFreezeSynchronizator {
	private JIRAFilterListModel filterMode;
	private JIRAIssueListModel issueModel;
	private JIRAServerModel serverModel;
	
	private IssueToolWindowFreezeSynchronizator(){
	}
	IssueToolWindowFreezeSynchronizator(final JIRAFilterListModel filterModel, final JIRAIssueListModel issueModel,
										final JIRAServerModel serverModel) {
		this.filterMode = filterModel;
		this.issueModel = issueModel;
		this.serverModel = serverModel;


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

		serverModel.addFrozenModelListener(new FrozenModelListener(){

			public void modelFrozen(FrozenModel model, boolean frozen) {
				if (filterModel != null) {
					filterModel.setModelFrozen(frozen);
				}
			}
		});
	}
}
