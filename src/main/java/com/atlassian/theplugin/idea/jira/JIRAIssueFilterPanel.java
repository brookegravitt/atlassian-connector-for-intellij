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

import com.atlassian.theplugin.idea.ProgressAnimationProvider;
import com.atlassian.theplugin.idea.jira.table.JIRAConstantListRenderer;
import com.atlassian.theplugin.idea.jira.table.JIRAQueryFragmentListRenderer;
import com.atlassian.theplugin.idea.ui.CollapsiblePanel;
import com.atlassian.theplugin.jira.JIRAServer;
import com.atlassian.theplugin.jira.api.JIRAAssigneeBean;
import com.atlassian.theplugin.jira.api.JIRAProjectBean;
import com.atlassian.theplugin.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.jira.api.JIRAReporterBean;
import com.intellij.openapi.util.IconLoader;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class JIRAIssueFilterPanel extends JPanel {
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
	private CollapsiblePanel componentsVersionsCollapsiblePanel;
	private JPanel componentsVersionsPanel;
	private CollapsiblePanel issueAttributesCollapsiblePanel;
	private JPanel issueAttributesPanel;
	private JLabel componentsLabel;
	private JLabel fixForLabel;
	private JLabel reporterLabel;
	private JLabel assigneeLabel;
	private JLabel statusLabel;
	private JLabel resolutionsLabel;
	private JLabel prioritiesLabel;
	private JLabel affectsVersionsLabel;

	private ProgressAnimationProvider progressAnimation;
	private boolean initialFilterSet;

	private JIRAServer jiraServer;

	public JIRAIssueFilterPanel() {
		$$$setupUI$$$();

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

		projectList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
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
						clearProjectDependentLists();
						break;
				}
			}
		});
	}

	public void setProgressAnimation(ProgressAnimationProvider progressAnimation) {
		this.progressAnimation = progressAnimation;
	}

	private void enableProjectDependentLists(boolean value) {
		this.fixForList.setEnabled(value);
		this.componentsList.setEnabled(value);
		this.affectsVersionsList.setEnabled(value);

		int visibleListCount = 0;
		if (this.fixForList.getModel().getSize() <= JIRAServer.VERSION_SPECIAL_VALUES_COUNT) {
			this.fixForScrollPane.setVisible(false);
			this.fixForLabel.setVisible(false);
		} else {
			this.fixForScrollPane.setVisible(true);
			this.fixForLabel.setVisible(true);
			visibleListCount++;
		}
		if (this.componentsList.getModel().getSize() <= JIRAServer.COMPONENTS_SPECIAL_VALUES_COUNT) {
			this.componentsScrollPane.setVisible(false);
			this.componentsLabel.setVisible(false);
		} else {
			this.componentsScrollPane.setVisible(true);
			this.componentsLabel.setVisible(true);
			visibleListCount++;
		}
		if (this.affectsVersionsList.getModel().getSize() <= JIRAServer.VERSION_SPECIAL_VALUES_COUNT) {
			this.affectVersionScrollPane.setVisible(false);
			this.affectsVersionsLabel.setVisible(false);
		} else {
			this.affectVersionScrollPane.setVisible(true);
			this.affectsVersionsLabel.setVisible(true);
			visibleListCount++;
		}

		if (visibleListCount == 0) {
			componentsVersionsCollapsiblePanel.collapse();
		} else {
			componentsVersionsCollapsiblePanel.expand();
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

	private void refreshProjectDependentLists() {
		if (jiraServer.getCurrentProject() != null) {
			if (jiraServer.getCurrentProject().getId() == JIRAServer.ANY_ID) {
				clearProjectDependentLists();
			} else {
				if (initialFilterSet) {
					setProjectDependendListValues();
				} else {
					new Thread(new Runnable() {
						public void run() {
							setProjectDependendListValues();
						}
					}, "JIRA filter project values retrieve").start();
				}
			}
		} else {
			clearProjectDependentLists();
		}
	}

	private void setProjectDependendListValues() {
		progressAnimation.startProgressAnimation();
		issueTypeList.setListData(jiraServer.getIssueTypes().toArray());
		fixForList.setListData(jiraServer.getFixForVersions().toArray());
		componentsList.setListData(jiraServer.getComponents().toArray());
		affectsVersionsList.setListData(jiraServer.getVersions().toArray());
		enableProjectDependentLists(true);
		progressAnimation.stopProgressAnimation();
	}

	public void setJiraServer(final JIRAServer jServer, final List<JIRAQueryFragment> advancedQuery) {
		new Thread(new Runnable() {
			public void run() {
				initialFilterSet = true;
				progressAnimation.startProgressAnimation();
				jiraServer = jServer;
				projectList.setListData(jiraServer.getProjects().toArray());
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
				reporterComboBox.addItem(new JIRAReporterBean((long) -1, "Current User", jiraServer.getServer().getUserName()));

				assigneeComboBox.removeAllItems();
				assigneeComboBox.addItem(new JIRAAssigneeBean(JIRAServer.ANY_ID, "Any User", ""));
				assigneeComboBox.addItem(new JIRAAssigneeBean((long) -1, "Unassigned", "unassigned"));
				assigneeComboBox.addItem(new JIRAAssigneeBean((long) -1, "Current User", jiraServer.getServer().getUserName()));


				setListValues(projectList, advancedQuery);
				setListValues(statusList, advancedQuery);
				setListValues(prioritiesList, advancedQuery);
				setListValues(resolutionsList, advancedQuery);
				setListValues(issueTypeList, advancedQuery);
				setListValues(componentsList, advancedQuery);
				setListValues(fixForList, advancedQuery);
				setListValues(affectsVersionsList, advancedQuery);
				setComboValue(assigneeComboBox, advancedQuery);
				setComboValue(reporterComboBox, advancedQuery);

				progressAnimation.stopProgressAnimation();
				initialFilterSet = false;
			}
		}, "JIRA initial filter set").start();
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


	public List<JIRAQueryFragment> getFilter
			() {
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

	private void createUIComponents
			() {
		Icon collapse = IconLoader.findIcon("/icons/navigate_down_10.gif");
		Icon expand = IconLoader.findIcon("/icons/navigate_right_10.gif");
		componentsVersionsPanel = new JPanel();
		issueAttributesPanel = new JPanel();

		componentsVersionsCollapsiblePanel = new CollapsiblePanel(componentsVersionsPanel, true, false, collapse, expand, "Components/Versions");
		issueAttributesCollapsiblePanel = new CollapsiblePanel(issueAttributesPanel, true, false, collapse, expand, "Issue Attributes");
	}

	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		createUIComponents();
		rootPanel = new JPanel();
		rootPanel.setLayout(new GridLayoutManager(7, 3, new Insets(0, 0, 0, 0), -1, -1));
		rootPanel.setFocusCycleRoot(false);
		rootPanel.setMaximumSize(new Dimension(-1, -1));
		final JLabel label1 = new JLabel();
		label1.setFont(new Font(label1.getFont().getName(), label1.getFont().getStyle(), 11));
		label1.setRequestFocusEnabled(true);
		label1.setText("Issue Type:");
		rootPanel.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(70, 14), null, 0, false));
		issueTypeScrollPane = new JScrollPane();
		rootPanel.add(issueTypeScrollPane, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, 1, null, new Dimension(273, 80), null, 0, false));
		issueTypeList = new JList();
		issueTypeList.setVisibleRowCount(5);
		issueTypeScrollPane.setViewportView(issueTypeList);
		projectScrollPane = new JScrollPane();
		rootPanel.add(projectScrollPane, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, 1, null, new Dimension(273, 80), null, 0, false));
		projectList = new JList();
		projectList.setInheritsPopupMenu(false);
		projectList.setLayoutOrientation(0);
		final DefaultListModel defaultListModel1 = new DefaultListModel();
		projectList.setModel(defaultListModel1);
		projectList.setVisibleRowCount(5);
		projectScrollPane.setViewportView(projectList);
		final JLabel label2 = new JLabel();
		label2.setFont(new Font(label2.getFont().getName(), label2.getFont().getStyle(), 11));
		label2.setText("Project:");
		label2.setVerticalAlignment(1);
		rootPanel.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(70, 14), null, 0, false));
		final Spacer spacer1 = new Spacer();
		rootPanel.add(spacer1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		componentsVersionsPanel.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
		componentsVersionsPanel.setEnabled(false);
		rootPanel.add(componentsVersionsPanel, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, 1, GridConstraints.SIZEPOLICY_FIXED, new Dimension(350, 100), null, null, 0, false));
		fixForScrollPane = new JScrollPane();
		componentsVersionsPanel.add(fixForScrollPane, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, 1, null, null, null, 0, false));
		fixForList = new JList();
		fixForList.setVisibleRowCount(5);
		fixForScrollPane.setViewportView(fixForList);
		componentsScrollPane = new JScrollPane();
		componentsVersionsPanel.add(componentsScrollPane, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		componentsList = new JList();
		final DefaultListModel defaultListModel2 = new DefaultListModel();
		componentsList.setModel(defaultListModel2);
		componentsList.setVisible(true);
		componentsList.setVisibleRowCount(5);
		componentsScrollPane.setViewportView(componentsList);
		componentsLabel = new JLabel();
		componentsLabel.setFont(new Font(componentsLabel.getFont().getName(), componentsLabel.getFont().getStyle(), 11));
		componentsLabel.setHorizontalAlignment(4);
		componentsLabel.setText("Components:");
		componentsVersionsPanel.add(componentsLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		fixForLabel = new JLabel();
		fixForLabel.setFont(new Font(fixForLabel.getFont().getName(), fixForLabel.getFont().getStyle(), 11));
		fixForLabel.setText("Fix For:");
		componentsVersionsPanel.add(fixForLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		affectVersionScrollPane = new JScrollPane();
		componentsVersionsPanel.add(affectVersionScrollPane, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, 1, null, null, null, 0, false));
		affectsVersionsList = new JList();
		final DefaultListModel defaultListModel3 = new DefaultListModel();
		affectsVersionsList.setModel(defaultListModel3);
		affectsVersionsList.setVisibleRowCount(5);
		affectVersionScrollPane.setViewportView(affectsVersionsList);
		affectsVersionsLabel = new JLabel();
		affectsVersionsLabel.setFont(new Font(affectsVersionsLabel.getFont().getName(), affectsVersionsLabel.getFont().getStyle(), 11));
		affectsVersionsLabel.setText("<html>Affects<br>  Versions:</html>");
		componentsVersionsPanel.add(affectsVersionsLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		issueAttributesCollapsiblePanel.putClientProperty("html.disable", Boolean.TRUE);
		rootPanel.add(issueAttributesCollapsiblePanel, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, 1, 1, new Dimension(354, -1), null, new Dimension(354, -1), 0, false));
		issueAttributesPanel.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
		issueAttributesPanel.setEnabled(false);
		rootPanel.add(issueAttributesPanel, new GridConstraints(5, 0, 1, 2, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, 1, GridConstraints.SIZEPOLICY_FIXED, new Dimension(350, 100), null, null, 0, false));
		reporterComboBox = new JComboBox();
		reporterComboBox.setLightWeightPopupEnabled(false);
		reporterComboBox.setMaximumRowCount(5);
		issueAttributesPanel.add(reporterComboBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		reporterLabel = new JLabel();
		reporterLabel.setFont(new Font(reporterLabel.getFont().getName(), reporterLabel.getFont().getStyle(), 11));
		reporterLabel.setText("Reporter:");
		issueAttributesPanel.add(reporterLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		assigneeComboBox = new JComboBox();
		assigneeComboBox.setLightWeightPopupEnabled(false);
		assigneeComboBox.setMaximumRowCount(5);
		issueAttributesPanel.add(assigneeComboBox, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		assigneeLabel = new JLabel();
		assigneeLabel.setFont(new Font(assigneeLabel.getFont().getName(), assigneeLabel.getFont().getStyle(), 11));
		assigneeLabel.setText("Assignee:");
		issueAttributesPanel.add(assigneeLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		statusScrollPane = new JScrollPane();
		issueAttributesPanel.add(statusScrollPane, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, 1, null, null, null, 0, false));
		statusList = new JList();
		final DefaultListModel defaultListModel4 = new DefaultListModel();
		statusList.setModel(defaultListModel4);
		statusList.setVisibleRowCount(5);
		statusScrollPane.setViewportView(statusList);
		statusLabel = new JLabel();
		statusLabel.setFont(new Font(statusLabel.getFont().getName(), statusLabel.getFont().getStyle(), 11));
		statusLabel.setRequestFocusEnabled(false);
		statusLabel.setText("Status:");
		issueAttributesPanel.add(statusLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		resolutionScrollPane = new JScrollPane();
		issueAttributesPanel.add(resolutionScrollPane, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, 1, null, null, null, 0, false));
		resolutionsList = new JList();
		final DefaultListModel defaultListModel5 = new DefaultListModel();
		resolutionsList.setModel(defaultListModel5);
		resolutionsList.setVisibleRowCount(5);
		resolutionScrollPane.setViewportView(resolutionsList);
		resolutionsLabel = new JLabel();
		resolutionsLabel.setFont(new Font(resolutionsLabel.getFont().getName(), resolutionsLabel.getFont().getStyle(), 11));
		resolutionsLabel.setText("Resolutions:");
		issueAttributesPanel.add(resolutionsLabel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		prioritiesScrollPane = new JScrollPane();
		issueAttributesPanel.add(prioritiesScrollPane, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, 1, null, null, null, 0, false));
		prioritiesList = new JList();
		prioritiesList.setVisibleRowCount(5);
		prioritiesScrollPane.setViewportView(prioritiesList);
		prioritiesLabel = new JLabel();
		prioritiesLabel.setFont(new Font(prioritiesLabel.getFont().getName(), prioritiesLabel.getFont().getStyle(), 11));
		prioritiesLabel.setText("Priorities:");
		issueAttributesPanel.add(prioritiesLabel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		componentsVersionsCollapsiblePanel.setBackground(new Color(-3368704));
		componentsVersionsCollapsiblePanel.setFont(new Font(componentsVersionsCollapsiblePanel.getFont().getName(), componentsVersionsCollapsiblePanel.getFont().getStyle(), 14));
		rootPanel.add(componentsVersionsCollapsiblePanel, new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(354, 20), null, new Dimension(354, -1), 0, false));
		final Spacer spacer2 = new Spacer();
		rootPanel.add(spacer2, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		label1.setLabelFor(issueTypeScrollPane);
		label2.setLabelFor(projectScrollPane);
		componentsLabel.setNextFocusableComponent(componentsScrollPane);
		fixForLabel.setLabelFor(fixForScrollPane);
		affectsVersionsLabel.setLabelFor(affectVersionScrollPane);
		reporterLabel.setLabelFor(reporterComboBox);
		assigneeLabel.setLabelFor(assigneeComboBox);
		resolutionsLabel.setLabelFor(resolutionScrollPane);
		prioritiesLabel.setLabelFor(prioritiesScrollPane);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return rootPanel;
	}
}

