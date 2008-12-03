package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.configuration.JiraFilterConfigurationBean;
import com.atlassian.theplugin.configuration.JiraFilterEntryBean;
import com.atlassian.theplugin.configuration.JiraProjectConfiguration;
import com.atlassian.theplugin.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.jira.api.JIRASavedFilter;
import com.atlassian.theplugin.jira.model.*;
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
	private JiraIssuesFilterPanel jiraIssuesFilterPanel;
	private Project project;
	private JiraProjectConfiguration jiraProjectCfg;
	private JIRAServerModel jiraServerModel;
	private JButton editButton;

	JIRAManualFilterDetailsPanel(JIRAFilterListModel listModel, JiraProjectConfiguration jiraProjectCfg, Project project,
						  JIRAServerModel jiraServerModel) {
		super(new BorderLayout());
		this.jiraProjectCfg = jiraProjectCfg;
		this.listModel = listModel;
		this.project = project;
		this.jiraServerModel = jiraServerModel;
		createPanelContent();

		listModel.addModelListener(new JIRAFilterListModelListener() {

			public void modelChanged(JIRAFilterListModel aListModel) {
				JIRAManualFilterDetailsPanel.this.listModel = aListModel;
			}

			public void selectedSavedFilter(JiraServerCfg jiraServer, JIRASavedFilter savedFilter, boolean isChanged) {
			}

			public void selectedManualFilter(JiraServerCfg jiraServer, java.util.List<JIRAQueryFragment> manualFilter,
											 boolean isChanged) {
			
			}

		});

		listModel.addFrozenModelListener(new FrozenModelListener() {
			public void modelFrozen(FrozenModel model, boolean frozen) {
						setEnabled(!frozen);
				editButton.setEnabled(!frozen);
			}
		});
		
	}


	private void createPanelContent() {
		editButton = new JButton("Edit");

		JScrollPane manualFilterDeatilsScrollPane = new JScrollPane(manualFilterDetailsLabel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

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
				jiraIssuesFilterPanel = new JiraIssuesFilterPanel(project, jiraServerModel,
						listModel, jiraServer);

				if (jiraServer != null && listModel.getJiraSelectedManualFilter() != null) {
					final java.util.List<JIRAQueryFragment> listClone = new ArrayList<JIRAQueryFragment>();
					for (JIRAQueryFragment fragment :  listModel.getJiraSelectedManualFilter().getQueryFragment()) {
						listClone.add(fragment.getClone());
					}
					jiraIssuesFilterPanel.setFilter(listClone);
				}
				jiraIssuesFilterPanel.show();

				if (jiraIssuesFilterPanel.getExitCode() == 0) {
					JIRAManualFilter manualFilter = listModel.getJiraSelectedManualFilter();
					listModel.clearManualFilter(jiraServer);
					manualFilter.getQueryFragment().addAll(jiraIssuesFilterPanel.getFilter());
					listModel.setManualFilter(jiraServer, manualFilter);
					listModel.selectManualFilter(jiraServer, manualFilter, true);
					// store filter in project workspace
					jiraProjectCfg.getJiraFilterConfiguaration(listModel.getJiraSelectedServer().getServerId().toString())
							.setManualFilterForName(JiraFilterConfigurationBean.MANUAL_FILTER_LABEL,
									serializeFilter(jiraIssuesFilterPanel.getFilter()));
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
