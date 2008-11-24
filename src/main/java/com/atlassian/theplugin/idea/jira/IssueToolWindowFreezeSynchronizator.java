package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.jira.model.*;

/**
 * User: pmaruszak
 */
public final class IssueToolWindowFreezeSynchronizator {
	private JIRAFilterListModel filterModel;
	private JIRAIssueListModel issueModel;
	private JIRAServerModel serverModel;



	public void setModels(JIRAIssueListModel issueModel, JIRAServerModel serverModel, JIRAFilterListModel filterModel) {
		this.issueModel = issueModel;
		this.serverModel = serverModel;
		this.filterModel = filterModel;
		if (issueModel != null && serverModel != null && filterModel != null){
			addListeners();
		}
	}
	
	private void addListeners(){


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

		serverModel.addFrozenModelListener(new FrozenModelListener() {

			public void modelFrozen(FrozenModel model, boolean frozen) {
				if (filterModel != null) {
					filterModel.setModelFrozen(frozen);
				}
			}
		});
	}
}
