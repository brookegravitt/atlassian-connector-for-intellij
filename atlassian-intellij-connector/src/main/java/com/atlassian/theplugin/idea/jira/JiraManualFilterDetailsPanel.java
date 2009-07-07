package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.commons.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.commons.jira.cache.JIRAServerModel;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.configuration.JiraFilterConfigurationBean;
import com.atlassian.theplugin.configuration.JiraFilterEntryBean;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.idea.ui.ScrollableTwoColumnPanel;
import com.atlassian.theplugin.jira.model.*;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * User: pmaruszak
 */
public class JiraManualFilterDetailsPanel extends JPanel {
	private JIRAFilterListModel listModel;
	private final ScrollableTwoColumnPanel panel = new ScrollableTwoColumnPanel();
	private final Project project;
	private final JiraWorkspaceConfiguration jiraProjectCfg;
	private final JIRAServerModel jiraServerModel;
	private final JButton editButton = new JButton("Edit");
	private ServerData jiraServer;
	private JIRAManualFilter jiraManualFilter;

	JiraManualFilterDetailsPanel(JIRAFilterListModel listModel,
			JiraWorkspaceConfiguration jiraProjectCfg,
			Project project,
			JIRAServerModel jiraServerModel) {
		super(new BorderLayout());
		this.jiraProjectCfg = jiraProjectCfg;
		this.listModel = listModel;
		this.project = project;
		this.jiraServerModel = jiraServerModel;
		createPanelContent();

		listModel.addModelListener(new JIRAFilterListModelListener() {
			public void modelChanged(JIRAFilterListModel aListModel) {
				JiraManualFilterDetailsPanel.this.listModel = aListModel;
			}

			public void manualFilterChanged(final JIRAManualFilter manualFilter, final ServerData server) {
				// refresh data in the view
				setFilter(manualFilter, server);
			}

			public void serverRemoved(final JIRAFilterListModel aListModel) {
				JiraManualFilterDetailsPanel.this.listModel = aListModel;
			}

			public void serverAdded(final JIRAFilterListModel jiraFilterListModel) {
				JiraManualFilterDetailsPanel.this.listModel = jiraFilterListModel;
			}

			public void serverNameChanged(final JIRAFilterListModel jiraFilterListModel) {
				JiraManualFilterDetailsPanel.this.listModel = jiraFilterListModel;
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

		final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		buttonPanel.add(editButton);
		this.add(buttonPanel, BorderLayout.SOUTH);
		this.add(panel, BorderLayout.CENTER);
		final TitledBorder border = BorderFactory.createTitledBorder("Custom Filter");

		this.setBorder(border);
		editButton.addActionListener(new ActionListener() {


			public void actionPerformed(ActionEvent event) {
				final JiraIssuesFilterPanel jiraIssuesFilterPanel
						= new JiraIssuesFilterPanel(project, jiraServerModel, listModel, jiraServer);

				if (jiraServer != null && jiraManualFilter != null) {
					final java.util.List<JIRAQueryFragment> listClone = new ArrayList<JIRAQueryFragment>();
					for (JIRAQueryFragment fragment : jiraManualFilter.getQueryFragment()) {
						if (fragment != null) {
							listClone.add(fragment.getClone());
						}
					}
					jiraIssuesFilterPanel.setFilter(listClone);
				}
				jiraIssuesFilterPanel.show();

				if (jiraIssuesFilterPanel.getExitCode() == 0) {
					JIRAManualFilter manualFilter = jiraManualFilter;
					listModel.clearManualFilter(jiraServer);
					manualFilter.getQueryFragment().addAll(jiraIssuesFilterPanel.getFilter());
					listModel.setManualFilter(jiraServer, manualFilter);
//					listModel.selectManualFilter(jiraServer, manualFilter, true);
					// store filter in project workspace
					jiraProjectCfg.getJiraFilterConfiguaration(jiraServer.getServerId())
							.setManualFilterForName(JiraFilterConfigurationBean.MANUAL_FILTER,
									serializeFilter(jiraIssuesFilterPanel.getFilter()));
					listModel.fireManualFilterChanged(manualFilter, jiraServer);
				}

			}
		});
	}

	public void setFilter(JIRAManualFilter manualFilter, final ServerData jiraServerCfg) {

		this.jiraServer = jiraServerCfg;
		this.jiraManualFilter = manualFilter;

		Collection<ScrollableTwoColumnPanel.Entry> entries = MiscUtil.buildArrayList();
		Map<JIRAManualFilter.QueryElement, ArrayList<String>> map = manualFilter.groupBy(true);
		for (JIRAManualFilter.QueryElement element : map.keySet()) {
			entries.add(new ScrollableTwoColumnPanel.Entry(element.getName(), StringUtils.join(map.get(element), ", ")));
		}


		if (entries.size() == 0) {
			// get also 'any' values
			map = manualFilter.groupBy(false);
			for (JIRAManualFilter.QueryElement element : map.keySet()) {
				entries.add(new ScrollableTwoColumnPanel.Entry(element.getName(), StringUtils.join(map.get(element), ", ")));
			}
		}

		panel.updateContent(entries);
	}

	private List<JiraFilterEntryBean> serializeFilter(List<JIRAQueryFragment> filter) {
		List<JiraFilterEntryBean> query = new ArrayList<JiraFilterEntryBean>();
		for (JIRAQueryFragment jiraQueryFragment : filter) {
			query.add(new JiraFilterEntryBean(jiraQueryFragment.getMap()));
		}
		return query;
	}
}

