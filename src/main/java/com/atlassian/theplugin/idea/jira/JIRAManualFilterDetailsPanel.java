package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.configuration.JiraFilterConfigurationBean;
import com.atlassian.theplugin.configuration.JiraFilterEntryBean;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.jira.api.JIRASavedFilter;
import com.atlassian.theplugin.jira.model.JIRAFilterListModel;
import com.atlassian.theplugin.jira.model.JIRAFilterListModelListener;
import com.atlassian.theplugin.jira.model.JIRAManualFilter;
import com.atlassian.theplugin.jira.model.JIRAServerModel;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * User: pmaruszak
 */
public class JIRAManualFilterDetailsPanel extends JPanel {
	private JIRAFilterListModel listModel;
	private JLabel manualFilterDetailsLabel = new JLabel();
	private JIRAIssueFilterPanel jiraIssueFilterPanel;
	private Project project;
	private ProjectConfigurationBean projectConfigBean;
	private JIRAServerModel jiraServerModel;
	private JButton editButton;

	JIRAManualFilterDetailsPanel(JIRAFilterListModel listModel, ProjectConfigurationBean projectConfigBean, Project project,
						  JIRAServerModel jiraServerModel) {
		super(new BorderLayout());
		this.projectConfigBean = projectConfigBean;
		this.listModel = listModel;
		this.project = project;
		this.jiraServerModel = jiraServerModel;
		createPanelContent();

		listModel.addModelListener(new JIRAFilterListModelListener() {

			public void modelChanged(JIRAFilterListModel listModel) {
				JIRAManualFilterDetailsPanel.this.listModel = listModel;
			}

			public void selectedSavedFilter(JiraServerCfg jiraServer, JIRASavedFilter savedFilter) {
			}

			public void selectedManualFilter(JiraServerCfg jiraServer, java.util.List<JIRAQueryFragment> manualFilter) {
			}

			public void modelFrozen(boolean frozen) {
				setEnabled(!frozen);
				editButton.setEnabled(!frozen);				
			}
		});
		
	}


	private void createPanelContent() {
		editButton = new JButton("Edit");

		JScrollPane manualFilterDeatilsScrollPane = new JScrollPane(manualFilterDetailsLabel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		manualFilterDeatilsScrollPane.getViewport().setBackground(manualFilterDetailsLabel.getBackground());
		manualFilterDeatilsScrollPane.setWheelScrollingEnabled(true);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		buttonPanel.add(editButton);

		this.add(buttonPanel, BorderLayout.SOUTH);
		this.add(manualFilterDeatilsScrollPane, BorderLayout.CENTER);
		TitledBorder border = BorderFactory.createTitledBorder("Custom Filter");

		this.setBorder(border);
		editButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				JiraServerCfg jiraServer = listModel.getJiraSelectedServer();
				jiraIssueFilterPanel = new JIRAIssueFilterPanel(project, jiraServerModel,
						listModel, jiraServer);

				if (jiraServer != null && listModel.getJiraSelectedManualFilter() != null) {
					jiraIssueFilterPanel.setFilter(listModel.getJiraSelectedManualFilter().getQueryFragment());
				}
				jiraIssueFilterPanel.show();

				if (jiraIssueFilterPanel.getExitCode() == 0) {
					JIRAManualFilter manualFilter = listModel.getJiraSelectedManualFilter();
					listModel.clearManualFilter(jiraServer);
					manualFilter.getQueryFragment().addAll(jiraIssueFilterPanel.getFilter());
					listModel.setManualFilter(jiraServer, manualFilter);
					listModel.selectManualFilter(jiraServer, manualFilter);
					// store filter in project workspace
					projectConfigBean.getJiraConfiguration()
							.getJiraFilterConfiguaration(
									listModel.getJiraSelectedServer().getServerId().toString())
							.setManualFilterForName(
									JiraFilterConfigurationBean.MANUAL_FILTER_LABEL,
									serializeFilter(jiraIssueFilterPanel.getFilter()));
				}

			}
		});
	}

	public void setText(String text) {
		manualFilterDetailsLabel.setText(text);
	}

	private java.util.List<JiraFilterEntryBean> serializeFilter(java.util.List<JIRAQueryFragment> filter) {
		java.util.List<JiraFilterEntryBean> query = new ArrayList<JiraFilterEntryBean>();
		for (JIRAQueryFragment jiraQueryFragment : filter) {
			query.add(new JiraFilterEntryBean(jiraQueryFragment.getMap()));
		}
		return query;
	}
}
