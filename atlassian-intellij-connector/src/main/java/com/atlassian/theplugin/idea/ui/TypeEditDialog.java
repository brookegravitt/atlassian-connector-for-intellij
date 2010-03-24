package com.atlassian.theplugin.idea.ui;

import com.atlassian.connector.commons.jira.beans.JIRAConstant;

import com.atlassian.connector.commons.jira.beans.JIRAProject;
import com.atlassian.connector.commons.jira.cache.CacheConstants;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.theplugin.commons.jira.IntelliJJiraServerFacade;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.jira.model.JIRAIssueListModelBuilder;
import com.atlassian.theplugin.jira.model.JIRAServerModelIdea;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.List;

public class TypeEditDialog extends DialogWrapper {
	protected final Project project;
	protected final JiraIssueAdapter issue;
	protected final JIRAIssueListModelBuilder modelBuilder;
	protected final JIRAServerModelIdea cache;
	protected JComboBox comboBox = new JComboBox();
	protected JLabel label = new JLabel("Type:");
	private JPanel rootPanel = new JPanel(new BorderLayout());

	public TypeEditDialog(Project project, JiraIssueAdapter issue, JIRAIssueListModelBuilder modelBuilder, JIRAServerModelIdea cache) {
		super(project, false);
		this.project = project;
		this.issue = issue;
		this.modelBuilder = modelBuilder;
		this.cache = cache;
		fillComboModel();
		setOKButtonText("Change");
		setModal(true);
		comboBox.setRenderer(new JiraConstantCellRenderer());
		rootPanel.add(label, BorderLayout.WEST);
		rootPanel.add(comboBox, BorderLayout.CENTER);
		rootPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		init();
	}

	@Override
	protected JComponent createCenterPanel() {
		return rootPanel;
	}

	protected void handleOKAction() {
		String selectedType = ((Long) ((JIRAConstant) comboBox.getSelectedItem()).getId()).toString();
		try {
			IntelliJJiraServerFacade.getInstance().setType(issue.getJiraServerData(), issue, selectedType);
		} catch (JIRAException e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void doOKAction() {
		ProgressManager.getInstance().run(new Task.Backgroundable(project,
				"Updating issue " + issue.getKey(), false) {
			@Override
			public void run(@NotNull ProgressIndicator progressIndicator) {
				handleOKAction();
				EventQueue.invokeLater(new Runnable(){
				    public void run() {
						try {
							modelBuilder.reloadIssue(issue.getKey(), issue.getJiraServerData());
						} catch (JIRAException e) {
							e.printStackTrace();
						}
					}
				});
			}
		});
		super.doOKAction();
	}

	protected void fillComboModel() {
		JIRAProject jiraProject = null;
		JiraServerData jiraServerData = issue.getJiraServerData();
		try {
			List<JIRAProject> projects = cache.getProjects(jiraServerData);
			for (JIRAProject candidate : projects) {
				if (issue.getProjectKey().equals(candidate.getKey())) {
					jiraProject = candidate;
					break;
				}
			}
		} catch (JIRAException e) {
			e.printStackTrace();
		}
		List<JIRAConstant> issueTypes = null;
		try {
			issueTypes = cache.getIssueTypes(jiraServerData, jiraProject, true);
		} catch (JIRAException e) {
			e.printStackTrace();
		}
		if (issueTypes == null) {
			return;
		}
		for (JIRAConstant constant : issueTypes) {
			if (constant.getId() != CacheConstants.ANY_ID) {
				comboBox.addItem(constant);
			}
		}
	}
}
