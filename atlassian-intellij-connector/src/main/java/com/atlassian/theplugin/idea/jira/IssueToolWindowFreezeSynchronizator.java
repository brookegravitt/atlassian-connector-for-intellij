package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.jira.model.*;
import com.atlassian.theplugin.commons.jira.cache.JIRAServerModel;


/**
 * User: pmaruszak
 */
public final class IssueToolWindowFreezeSynchronizator {
	private JIRAFilterListModel filterModel;
	private JIRAIssueListModel issueModel;
	private JIRAServerModel serverModel;


	public void setServerModel(JIRAServerModel serverModel) {
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

					}

				}
			});

			((JIRAServerModelIdea) serverModel).addFrozenModelListener(new FrozenModelListener() {

				public void modelFrozen(FrozenModel model, boolean frozen) {
					if (issueModel != null) {
						issueModel.setModelFrozen(frozen);
					}
				}
			});


		}
	}
}
