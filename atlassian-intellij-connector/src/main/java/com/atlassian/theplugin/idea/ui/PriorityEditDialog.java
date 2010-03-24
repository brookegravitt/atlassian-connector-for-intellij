package com.atlassian.theplugin.idea.ui;

import com.atlassian.connector.commons.jira.beans.JIRAConstant;
import com.atlassian.connector.commons.jira.beans.JIRAPriorityBean;
import com.atlassian.connector.commons.jira.cache.CacheConstants;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.theplugin.commons.jira.IntelliJJiraServerFacade;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.jira.model.JIRAIssueListModelBuilder;
import com.atlassian.theplugin.jira.model.JIRAServerModelIdea;
import com.intellij.openapi.project.Project;

import java.util.List;

public class PriorityEditDialog extends TypeEditDialog {

	public PriorityEditDialog(Project project, JiraIssueAdapter issue, JIRAIssueListModelBuilder modelBuilder, JIRAServerModelIdea cache) {
		super(project, issue, modelBuilder, cache);
		label.setText("Priority:");
	}

	@Override
	protected void handleOKAction() {
		String selectedId = ((Long) ((JIRAPriorityBean) comboBox.getSelectedItem()).getId()).toString();
		try {
			IntelliJJiraServerFacade.getInstance().setPriority(issue.getJiraServerData(), issue, selectedId);
		} catch (JIRAException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void fillComboModel() {
		List<JIRAPriorityBean> priorities = null;
		try {
			priorities = cache.getPriorities(issue.getJiraServerData(), myPerformAction);
		} catch (JIRAException e) {
			e.printStackTrace();
		}

		if (priorities == null) {
			return;
		}

		for (JIRAConstant constant : priorities) {
			if (constant.getId() != CacheConstants.ANY_ID) {
				comboBox.addItem(constant);
			}
		}
		if (comboBox.getModel().getSize() > 0) {
			comboBox.setSelectedIndex(comboBox.getModel().getSize() / 2);
		}
	}
}