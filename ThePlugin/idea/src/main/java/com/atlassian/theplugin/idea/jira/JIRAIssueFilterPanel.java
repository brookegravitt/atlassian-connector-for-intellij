/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.idea.jira.table.JIRAConstantListRenderer;
import com.atlassian.theplugin.idea.jira.table.JIRAQueryFragmentListRenderer;
import com.atlassian.theplugin.jira.JIRAServer;
import com.atlassian.theplugin.jira.api.JIRAAssigneeBean;
import com.atlassian.theplugin.jira.api.JIRAProjectBean;
import com.atlassian.theplugin.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.jira.api.JIRAReporterBean;
import com.atlassian.theplugin.jira.model.JIRAFilterListModel;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;


public class JIRAIssueFilterPanel extends DialogWrapper {
	private JList projectList;
	private JList issueTypeList;
	private JList fixForList;
	private JList affectsVersionsList;
	private JComboBox reporterComboBox;
	private JComboBox assigneeComboBox;
	private JList resolutionsList;
	private JList prioritiesList;
	private JList statusList;
	private JPanel rootPanel;
	private JScrollPane prioritiesScrollPane;
	private JScrollPane resolutionScrollPane;
	private JScrollPane statusScrollPane;
	private JScrollPane affectVersionScrollPane;
	private JScrollPane fixForScrollPane;
	private JScrollPane projectScrollPane;
	private JScrollPane issueTypeScrollPane;
	private JScrollPane componentsScrollPane;
	private JList componentsList;
	private JLabel componentsLabel;
	private JLabel fixForLabel;
	private JLabel reporterLabel;
	private JLabel assigneeLabel;
	private JLabel statusLabel;
	private JLabel resolutionsLabel;
	private JLabel prioritiesLabel;
	private JLabel affectsVersionsLabel;

	private boolean initialFilterSet;

	private JIRAServer jiraServer;
	private Project project;

	private final JIRAFilterListModel filterListModel;
	private JiraServerCfg jiraServerCfg;

	public JIRAIssueFilterPanel(
			final Project project,
			final JIRAFilterListModel filterListModel,
			final JiraServerCfg jiraServerCfg) {

		super(project, false);

		this.filterListModel = filterListModel;
		this.jiraServerCfg = jiraServerCfg;
		$$$setupUI$$$();
		setModal(true);
		setTitle("Configure Custom JIRA Filter");
		getOKAction().putValue(Action.NAME, "Apply");
		init();
		pack();


		this.project = project;
		this.projectList.setCellRenderer(new JIRAQueryFragmentListRenderer());
		this.issueTypeList.setCellRenderer(new JIRAConstantListRenderer());
		this.statusList.setCellRenderer(new JIRAConstantListRenderer());
		this.prioritiesList.setCellRenderer(new JIRAConstantListRenderer());
		this.resolutionsList.setCellRenderer(new JIRAQueryFragmentListRenderer());
		this.fixForList.setCellRenderer(new JIRAQueryFragmentListRenderer());
		this.componentsList.setCellRenderer(new JIRAQueryFragmentListRenderer());
		this.affectsVersionsList.setCellRenderer(new JIRAQueryFragmentListRenderer());
		this.reporterComboBox.setRenderer(new JIRAQueryFragmentListRenderer());
		this.assigneeComboBox.setRenderer(new JIRAQueryFragmentListRenderer());

		//addProjectActionListener();
	}

	private void addProjectActionListener() {
		projectList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				if (!event.getValueIsAdjusting()) {
					int size = projectList.getSelectedValues().length;
					switch (size) {
						case 0:
							jiraServer.setCurrentProject(null);
							refreshProjectDependentLists();
							break;
						case 1:
							JIRAProjectBean project = (JIRAProjectBean) projectList.getSelectedValues()[0];
							jiraServer.setCurrentProject(project);
							refreshProjectDependentLists();
							break;
						default:
							jiraServer.setCurrentProject(null);
							clearProjectDependentLists();
							refreshGlobalIssueTypeList();
							break;
					}

				}
			}
		});
	}

//	public void setProgressAnimation(ProgressAnimationProvider progressAnimation) {
//		this.panel = progressAnimation;
//	}

	@Override
	protected Action[] createActions() {
		return new Action[]{getOKAction(), new ClearFilterAction(), getCancelAction()};
	}

	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		rootPanel = new JPanel();
		rootPanel.setLayout(new GridLayoutManager(2, 3, new Insets(10, 10, 10, 10), -1, -1));
		rootPanel.setFocusCycleRoot(false);
		rootPanel.setMaximumSize(new Dimension(-1, -1));
		final JLabel label1 = new JLabel();
		label1.setText("Components / Versions");
		rootPanel.add(label1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
		rootPanel.add(panel1, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
		fixForLabel = new JLabel();
		fixForLabel.setFont(new Font(fixForLabel.getFont().getName(), fixForLabel.getFont().getStyle(), 11));
		fixForLabel.setText("Fix For:");
		panel1.add(fixForLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(66, 14), null, 0,
				false));
		componentsLabel = new JLabel();
		componentsLabel.setFont(new Font(componentsLabel.getFont().getName(), componentsLabel.getFont().getStyle(), 11));
		componentsLabel.setHorizontalAlignment(4);
		componentsLabel.setText("Components:");
		panel1.add(componentsLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(66, 14), null, 0,
				false));
		affectsVersionsLabel = new JLabel();
		affectsVersionsLabel
				.setFont(new Font(affectsVersionsLabel.getFont().getName(), affectsVersionsLabel.getFont().getStyle(), 11));
		affectsVersionsLabel.setText("<html>Affects<br>  Versions:</html>");
		panel1.add(affectsVersionsLabel,
				new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE,
						GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(66, 28),
						null, 0, false));
		fixForScrollPane = new JScrollPane();
		panel1.add(fixForScrollPane, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
				null, null, null, 0, false));
		fixForList = new JList();
		fixForList.setVisibleRowCount(5);
		fixForScrollPane.setViewportView(fixForList);
		componentsScrollPane = new JScrollPane();
		panel1.add(componentsScrollPane,
				new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
						GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		componentsList = new JList();
		componentsList.setVisible(true);
		componentsList.setVisibleRowCount(5);
		componentsScrollPane.setViewportView(componentsList);
		affectVersionScrollPane = new JScrollPane();
		panel1.add(affectVersionScrollPane,
				new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
						GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		affectsVersionsList = new JList();
		affectsVersionsList.setVisibleRowCount(5);
		affectVersionScrollPane.setViewportView(affectsVersionsList);
		final JLabel label2 = new JLabel();
		label2.setText("Issue Attributes");
		rootPanel.add(label2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
		rootPanel.add(panel2, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
		reporterLabel = new JLabel();
		reporterLabel.setFont(new Font(reporterLabel.getFont().getName(), reporterLabel.getFont().getStyle(), 11));
		reporterLabel.setHorizontalAlignment(2);
		reporterLabel.setText("Reporter:");
		panel2.add(reporterLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(73, 14), null, 0,
				false));
		reporterComboBox = new JComboBox();
		reporterComboBox.setLightWeightPopupEnabled(false);
		reporterComboBox.setMaximumRowCount(5);
		panel2.add(reporterComboBox,
				new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
						GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(160, 27),
						null, 0, false));
		assigneeLabel = new JLabel();
		assigneeLabel.setFont(new Font(assigneeLabel.getFont().getName(), assigneeLabel.getFont().getStyle(), 11));
		assigneeLabel.setHorizontalAlignment(2);
		assigneeLabel.setText("Assignee:");
		panel2.add(assigneeLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(73, 14), null, 0,
				false));
		assigneeComboBox = new JComboBox();
		assigneeComboBox.setLightWeightPopupEnabled(false);
		assigneeComboBox.setMaximumRowCount(5);
		panel2.add(assigneeComboBox,
				new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
						GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(160, 27),
						null, 0, false));
		statusLabel = new JLabel();
		statusLabel.setFont(new Font(statusLabel.getFont().getName(), statusLabel.getFont().getStyle(), 11));
		statusLabel.setHorizontalAlignment(2);
		statusLabel.setRequestFocusEnabled(false);
		statusLabel.setText("Status:");
		panel2.add(statusLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(73, 14), null, 0,
				false));
		statusScrollPane = new JScrollPane();
		panel2.add(statusScrollPane, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		statusList = new JList();
		statusList.setVisibleRowCount(5);
		statusScrollPane.setViewportView(statusList);
		resolutionsLabel = new JLabel();
		resolutionsLabel.setFont(new Font(resolutionsLabel.getFont().getName(), resolutionsLabel.getFont().getStyle(), 11));
		resolutionsLabel.setHorizontalAlignment(2);
		resolutionsLabel.setText("Resolutions:");
		panel2.add(resolutionsLabel,
				new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE,
						GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(73, 14),
						null, 0, false));
		resolutionScrollPane = new JScrollPane();
		panel2.add(resolutionScrollPane,
				new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0,
						false));
		resolutionsList = new JList();
		resolutionsList.setVisibleRowCount(5);
		resolutionScrollPane.setViewportView(resolutionsList);
		prioritiesLabel = new JLabel();
		prioritiesLabel.setFont(new Font(prioritiesLabel.getFont().getName(), prioritiesLabel.getFont().getStyle(), 11));
		prioritiesLabel.setHorizontalAlignment(2);
		prioritiesLabel.setText("Priorities:");
		prioritiesLabel.setVerticalTextPosition(0);
		panel2.add(prioritiesLabel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(73, 14), null, 0,
				false));
		prioritiesScrollPane = new JScrollPane();
		panel2.add(prioritiesScrollPane,
				new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0,
						false));
		prioritiesList = new JList();
		prioritiesList.setVisibleRowCount(5);
		prioritiesScrollPane.setViewportView(prioritiesList);
		final JPanel panel3 = new JPanel();
		panel3.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
		rootPanel.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
		projectScrollPane = new JScrollPane();
		panel3.add(projectScrollPane, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 80), null,
				0, false));
		projectList = new JList();
		projectList.setInheritsPopupMenu(false);
		projectList.setLayoutOrientation(0);
		projectList.setVisibleRowCount(5);
		projectScrollPane.setViewportView(projectList);
		final JLabel label3 = new JLabel();
		label3.setFont(new Font(label3.getFont().getName(), label3.getFont().getStyle(), 11));
		label3.setRequestFocusEnabled(true);
		label3.setText("Issue Type:");
		panel3.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(70, 14), null, 0,
				false));
		issueTypeScrollPane = new JScrollPane();
		issueTypeScrollPane.setDoubleBuffered(false);
		panel3.add(issueTypeScrollPane,
				new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null,
						new Dimension(200, 80), null, 0, false));
		issueTypeList = new JList();
		issueTypeList.setVisibleRowCount(5);
		issueTypeScrollPane.setViewportView(issueTypeList);
		final JLabel label4 = new JLabel();
		label4.setFont(new Font(label4.getFont().getName(), label4.getFont().getStyle(), 11));
		label4.setText("Project:");
		label4.setVerticalAlignment(1);
		panel3.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(70, 14), null, 0,
				false));
		final JLabel label5 = new JLabel();
		label5.setText("Project/ Issue");
		rootPanel.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		fixForLabel.setLabelFor(fixForScrollPane);
		componentsLabel.setNextFocusableComponent(componentsScrollPane);
		componentsLabel.setLabelFor(componentsScrollPane);
		affectsVersionsLabel.setLabelFor(affectVersionScrollPane);
		reporterLabel.setLabelFor(reporterComboBox);
		assigneeLabel.setLabelFor(assigneeComboBox);
		statusLabel.setLabelFor(statusScrollPane);
		resolutionsLabel.setLabelFor(resolutionScrollPane);
		prioritiesLabel.setLabelFor(prioritiesScrollPane);
		label3.setLabelFor(issueTypeScrollPane);
		label4.setLabelFor(projectScrollPane);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return rootPanel;
	}


	private class ClearFilterAction extends AbstractAction {
		private static final String CLEAR_FILTER = "Clear filter";

		private ClearFilterAction() {
			putValue(Action.NAME, CLEAR_FILTER);
		}

		public void actionPerformed(ActionEvent event) {
			if (filterListModel != null) {
				filterListModel.clearManualFilter(jiraServerCfg);
			}
		}
	}

	private void enableProjectDependentLists(boolean value) {
		this.fixForList.setEnabled(value);
		this.componentsList.setEnabled(value);
		this.affectsVersionsList.setEnabled(value);

		if (this.fixForList.getModel().getSize() == 0) {
			this.fixForScrollPane.setEnabled(false);
		} else {
			this.fixForScrollPane.setEnabled(true);
		}
		if (this.componentsList.getModel().getSize() == 0) {
			this.componentsScrollPane.setEnabled(false);
		} else {
			this.componentsScrollPane.setEnabled(true);
		}
		if (this.affectsVersionsList.getModel().getSize() == 0) {
			this.affectVersionScrollPane.setEnabled(false);
		} else {
			this.affectVersionScrollPane.setEnabled(true);
		}

		rootPanel.validate();
	}

	private void clearProjectDependentLists() {
		this.issueTypeList.setListData(new Object[0]);
		this.fixForList.setListData(new Object[0]);
		this.componentsList.setListData(new Object[0]);
		this.affectsVersionsList.setListData(new Object[0]);

		enableProjectDependentLists(false);
	}

	private void refreshGlobalIssueTypeList() {
		if (initialFilterSet) {
			issueTypeList.setListData(jiraServer.getIssueTypes().toArray());
		} else {
			Task.Backgroundable refresh = new Task.Backgroundable(project, "Retrieving JIRA Issue Type List", false) {
				@Override
				public void run(final ProgressIndicator indicator) {
					enableFields(false);
					issueTypeList.setListData(jiraServer.getIssueTypes().toArray());
					enableFields(true);
				}


			};

			ProgressManager.getInstance().run(refresh);
		}
	}

	private void enableFields(boolean enable) {
		projectList.setEnabled(enable);
		issueTypeList.setEnabled(enable);
		fixForList.setEnabled(enable);
		affectsVersionsList.setEnabled(enable);
		reporterComboBox.setEnabled(enable);
		assigneeComboBox.setEnabled(enable);
		componentsList.setEnabled(enable);
		resolutionsList.setEnabled(enable);
		statusList.setEnabled(enable);
		prioritiesList.setEnabled(enable);
	}

	private void refreshProjectDependentLists() {
		if (jiraServer.getCurrentProject() != null) {
			if (jiraServer.getCurrentProject().getId() == JIRAServer.ANY_ID) {
				clearProjectDependentLists();
			} else {
				if (initialFilterSet) {
					setProjectDependendListValues();
				} else {
					Task.Backgroundable refresh =
							new Task.Backgroundable(project, "Retrieving JIRA Project Dependent List", false) {
								@Override
								public void run(final ProgressIndicator indicator) {
									setProjectDependendListValues();
								}
							};

					ProgressManager.getInstance().run(refresh);
				}
			}
		} else {
			clearProjectDependentLists();
		}
	}

	private void setProjectDependendListValues() {
		enableFields(false);
		issueTypeList.setListData(jiraServer.getIssueTypes().toArray());
		fixForList.setListData(jiraServer.getFixForVersions().toArray());
		componentsList.setListData(jiraServer.getComponents().toArray());
		affectsVersionsList.setListData(jiraServer.getVersions().toArray());
		enableProjectDependentLists(true);
		enableFields(true);
	}

	public void setJiraServer(final JIRAServer jServer, final List<JIRAQueryFragment> advancedQuery) {

//		Task.Backgroundable setServer = new Task.Backgroundable(project, "Setting JIRA Server", false) {
//			@Override
//			public void run(final ProgressIndicator indicator) {
		projectList.addListSelectionListener(null);
		initialFilterSet = true;
		//progressAnimation.startProgressAnimation();
		enableFields(false);
		jiraServer = jServer;

		projectList.setListData(jiraServer.getProjects().toArray());
		setListValues(projectList, advancedQuery);
		if ((projectList.getSelectedValues().length > 0) && projectList.getSelectedValues()[0] != null) {
			jiraServer.setCurrentProject((JIRAProjectBean) projectList.getSelectedValues()[0]);
		}
		issueTypeList.setListData(jiraServer.getIssueTypes().toArray());
		statusList.setListData(jiraServer.getStatuses().toArray());
		prioritiesList.setListData(jiraServer.getPriorieties().toArray());
		resolutionsList.setListData(jiraServer.getResolutions().toArray());
		fixForList.setListData(jiraServer.getFixForVersions().toArray());
		componentsList.setListData(jiraServer.getComponents().toArray());
		affectsVersionsList.setListData(jiraServer.getVersions().toArray());

		reporterComboBox.removeAllItems();
		reporterComboBox.addItem(new JIRAReporterBean(JIRAServer.ANY_ID, "Any User", null));
//reporterComboBox.addItem(new JIRAReporterBean((long) -1, "No reporter", "issue_no_reporter"));
		reporterComboBox.addItem(new JIRAReporterBean((long) -1, "Current User", jiraServer.getServer().getUsername()));

		assigneeComboBox.removeAllItems();
		assigneeComboBox.addItem(new JIRAAssigneeBean(JIRAServer.ANY_ID, "Any User", ""));
		assigneeComboBox.addItem(new JIRAAssigneeBean((long) -1, "Unassigned", "unassigned"));
		assigneeComboBox.addItem(new JIRAAssigneeBean((long) -1, "Current User", jiraServer.getServer().getUsername()));


		setListValues(statusList, advancedQuery);
		setListValues(prioritiesList, advancedQuery);
		setListValues(resolutionsList, advancedQuery);
		setListValues(issueTypeList, advancedQuery);
		setListValues(componentsList, advancedQuery);
		setListValues(fixForList, advancedQuery);
		setListValues(affectsVersionsList, advancedQuery);
		setComboValue(assigneeComboBox, advancedQuery);
		setComboValue(reporterComboBox, advancedQuery);

		//progressAnimation.stopProgressAnimation();

		addProjectActionListener();
		enableFields(true);
		//initialFilterSet = false;

//			}
//		};
//
//		ProgressManager.getInstance().run(setServer);

	}

	public void setListValues(JList list, List<JIRAQueryFragment> advancedQuery) {
		List<Integer> selection = new ArrayList<Integer>();
		for (int i = 0, size = list.getModel().getSize(); i < size; ++i) {
			for (JIRAQueryFragment jiraQueryFragment : advancedQuery) {
				JIRAQueryFragment fragment = (JIRAQueryFragment) list.getModel().getElementAt(i);
				if (jiraQueryFragment.getQueryStringFragment().equals(fragment.getQueryStringFragment())) {
					selection.add(i);
				}
			}
		}
		if (selection.size() > 0) {
			int[] sel = new int[selection.size()];
			int j = 0;
			for (Integer integer : selection) {
				sel[j++] = integer;
			}
			list.setSelectedIndices(sel);
			list.ensureIndexIsVisible(sel[0]);
		}
	}

	public void setComboValue
			(JComboBox
					combo,
					List<JIRAQueryFragment> advancedQuery) {
		for (int i = 0, size = combo.getModel().getSize(); i < size; ++i) {
			for (JIRAQueryFragment jiraQueryFragment : advancedQuery) {
				JIRAQueryFragment fragment = (JIRAQueryFragment) combo.getModel().getElementAt(i);
				if (jiraQueryFragment.getQueryStringFragment().equals(fragment.getQueryStringFragment())) {
					combo.setSelectedIndex(i);
					break;
				}
			}
		}
	}


	public List<JIRAQueryFragment> getFilter() {
		List<JIRAQueryFragment> query = new ArrayList<JIRAQueryFragment>();
		for (Object o : projectList.getSelectedValues()) {
			query.add((JIRAQueryFragment) o);
		}
		for (Object o : issueTypeList.getSelectedValues()) {
			query.add((JIRAQueryFragment) o);
		}
		for (Object o : statusList.getSelectedValues()) {
			query.add((JIRAQueryFragment) o);
		}
		for (Object o : prioritiesList.getSelectedValues()) {
			query.add((JIRAQueryFragment) o);
		}
		for (Object o : resolutionsList.getSelectedValues()) {
			query.add((JIRAQueryFragment) o);
		}
		for (Object o : fixForList.getSelectedValues()) {
			query.add((JIRAQueryFragment) o);
		}
		for (Object o : componentsList.getSelectedValues()) {
			query.add((JIRAQueryFragment) o);
		}
		for (Object o : affectsVersionsList.getSelectedValues()) {
			query.add((JIRAQueryFragment) o);
		}
		query.add((JIRAQueryFragment) assigneeComboBox.getSelectedItem());
		query.add((JIRAQueryFragment) reporterComboBox.getSelectedItem());
		return query;
	}

	@Override
	@Nullable
	protected JComponent createCenterPanel() {
		return this.$$$getRootComponent$$$();
	}

}

