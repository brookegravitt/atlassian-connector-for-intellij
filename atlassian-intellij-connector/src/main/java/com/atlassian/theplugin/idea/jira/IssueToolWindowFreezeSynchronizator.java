package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.jira.model.*;


/**
 * User: pmaruszak
 */
public final class IssueToolWindowFreezeSynchronizator {
	private JIRAFilterListModel filterModel;
	private JIRAIssueListModel issueModel;
	private JIRAServerModelIdea serverModel;


	public void setServerModel(JIRAServerModelIdea serverModel) {
		this.serverModel = serverModel;
		addListeners();

	}

	public void setIssueModel(JIRAIssueListModel issueModel) {
		this.issueModel = issueModel;
		addListeners();
	}

	public void setFilterModel(JIRAFilterListModel filterModel) {
		this.filterModel = filterModel;
		addListeners();
	}

	private void addListeners() {

		if (issueModel != null && serverModel != null && filterModel != null) {
			issueModel.addFrozenModelListener(new FrozenModelListener() {

				public void modelFrozen(FrozenModel model, boolean frozen) {
					if (filterModel != null) {
						filterModel.setModelFrozen(frozen);
					}

				}
			});

			serverModel.addFrozenModelListener(new FrozenModelListener() {

				public void modelFrozen(FrozenModel model, boolean frozen) {
					if (issueModel != null) {
						issueModel.setModelFrozen(frozen);
					}
				}
			});


		}
	}
}
