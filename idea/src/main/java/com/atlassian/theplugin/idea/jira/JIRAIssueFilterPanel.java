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
import com.intellij.uiDesigner.core.Spacer;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

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
	private JPanel generalPanel;

	private ProgressAnimationProvider progressAnimation;
	private boolean initialFilterSet;

	private JIRAServer jiraServer;

	public JIRAIssueFilterPanel(ProgressAnimationProvider progressAnimation) {
		$$$setupUI$$$();

		this.progressAnimation = progressAnimation;
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
						jiraServer.setCurrentProject(null);
						clearProjectDependentLists();
						refreshGlobalIssueTypeList();
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
		if (this.fixForList.getModel().getSize() == 0) {
			this.fixForScrollPane.setEnabled(false);
		} else {
			this.fixForScrollPane.setEnabled(true);
			visibleListCount++;
		}
		if (this.componentsList.getModel().getSize() == 0) {
			this.componentsScrollPane.setEnabled(false);
		} else {
			this.componentsScrollPane.setEnabled(true);
			visibleListCount++;
		}
		if (this.affectsVersionsList.getModel().getSize() == 0) {
			this.affectVersionScrollPane.setEnabled(false);
		} else {
			this.affectVersionScrollPane.setEnabled(true);
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

	private void refreshGlobalIssueTypeList() {
		if (initialFilterSet) {
			issueTypeList.setListData(jiraServer.getIssueTypes().toArray());
		} else {
			new Thread(new Runnable() {
				public void run() {
					progressAnimation.startProgressAnimation();
					issueTypeList.setListData(jiraServer.getIssueTypes().toArray());
					progressAnimation.stopProgressAnimation();
				}
			}, "JIRA filter project values retrieve").start();
		}

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

	private void createUIComponents
			() {
		Icon collapse = IconLoader.findIcon("/icons/navigate_down_10.gif");
		Icon expand = IconLoader.findIcon("/icons/navigate_right_10.gif");
		componentsVersionsPanel = new JPanel();
		issueAttributesPanel = new JPanel();

		componentsVersionsCollapsiblePanel = new CollapsiblePanel(componentsVersionsPanel, true, false, collapse, expand,
				"Components/Versions");
		issueAttributesCollapsiblePanel = new CollapsiblePanel(issueAttributesPanel, true, false, collapse, expand,
				"Issue Attributes");
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
		rootPanel.setLayout(new FormLayout("fill:max(d;4px):noGrow",
				"center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:d:grow"));
		rootPanel.setFocusCycleRoot(false);
		rootPanel.setMaximumSize(new Dimension(-1, -1));
		componentsVersionsPanel.setLayout(new FormLayout("fill:72px:noGrow,left:4dlu:noGrow,fill:d:grow",
				"center:d:grow,top:3dlu:noGrow,center:d:grow,top:3dlu:noGrow,center:d:grow"));
		componentsVersionsPanel.setEnabled(false);
		componentsVersionsPanel.setMinimumSize(new Dimension(335, -1));
		componentsVersionsPanel.setPreferredSize(new Dimension(335, 250));
		CellConstraints cc = new CellConstraints();
		rootPanel.add(componentsVersionsPanel, cc.xy(1, 5, CellConstraints.DEFAULT, CellConstraints.TOP));
		fixForScrollPane = new JScrollPane();
		componentsVersionsPanel.add(fixForScrollPane, cc.xy(3, 1));
		fixForList = new JList();
		fixForList.setVisibleRowCount(5);
		fixForScrollPane.setViewportView(fixForList);
		componentsScrollPane = new JScrollPane();
		componentsVersionsPanel.add(componentsScrollPane, cc.xy(3, 3));
		componentsList = new JList();
		final DefaultListModel defaultListModel1 = new DefaultListModel();
		componentsList.setModel(defaultListModel1);
		componentsList.setVisible(true);
		componentsList.setVisibleRowCount(5);
		componentsScrollPane.setViewportView(componentsList);
		affectVersionScrollPane = new JScrollPane();
		componentsVersionsPanel.add(affectVersionScrollPane, cc.xy(3, 5));
		affectsVersionsList = new JList();
		final DefaultListModel defaultListModel2 = new DefaultListModel();
		affectsVersionsList.setModel(defaultListModel2);
		affectsVersionsList.setVisibleRowCount(5);
		affectVersionScrollPane.setViewportView(affectsVersionsList);
		fixForLabel = new JLabel();
		fixForLabel.setFont(new Font(fixForLabel.getFont().getName(), fixForLabel.getFont().getStyle(), 11));
		fixForLabel.setText("Fix For:");
		componentsVersionsPanel.add(fixForLabel, cc.xy(1, 1, CellConstraints.RIGHT, CellConstraints.TOP));
		componentsLabel = new JLabel();
		componentsLabel.setFont(new Font(componentsLabel.getFont().getName(), componentsLabel.getFont().getStyle(), 11));
		componentsLabel.setHorizontalAlignment(4);
		componentsLabel.setText("Components:");
		componentsVersionsPanel.add(componentsLabel, cc.xy(1, 3, CellConstraints.RIGHT, CellConstraints.TOP));
		affectsVersionsLabel = new JLabel();
		affectsVersionsLabel
				.setFont(new Font(affectsVersionsLabel.getFont().getName(), affectsVersionsLabel.getFont().getStyle(), 11));
		affectsVersionsLabel.setText("<html>Affects<br>  Versions:</html>");
		componentsVersionsPanel.add(affectsVersionsLabel, cc.xy(1, 5, CellConstraints.RIGHT, CellConstraints.TOP));
		issueAttributesCollapsiblePanel.setBackground(new Color(-6711040));
		issueAttributesCollapsiblePanel.putClientProperty("html.disable", Boolean.TRUE);
		rootPanel.add(issueAttributesCollapsiblePanel, cc.xy(1, 7));
		issueAttributesPanel.setLayout(new FormLayout("fill:72px:noGrow,left:4dlu:noGrow,fill:d:grow",
				"center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:d:grow,top:3dlu:noGrow,center:d:grow,top:3dlu:noGrow,center:d:grow"));
		issueAttributesPanel.setEnabled(false);
		issueAttributesPanel.setMinimumSize(new Dimension(335, -1));
		issueAttributesPanel.setPreferredSize(new Dimension(335, 314));
		rootPanel.add(issueAttributesPanel,
				new CellConstraints(1, 9, 1, 1, CellConstraints.DEFAULT, CellConstraints.TOP, new Insets(0, 0, 5, 0)));
		reporterComboBox = new JComboBox();
		reporterComboBox.setLightWeightPopupEnabled(false);
		reporterComboBox.setMaximumRowCount(5);
		issueAttributesPanel.add(reporterComboBox, cc.xy(3, 1));
		assigneeComboBox = new JComboBox();
		assigneeComboBox.setLightWeightPopupEnabled(false);
		assigneeComboBox.setMaximumRowCount(5);
		issueAttributesPanel.add(assigneeComboBox, cc.xy(3, 3));
		statusScrollPane = new JScrollPane();
		issueAttributesPanel.add(statusScrollPane, cc.xy(3, 5));
		statusList = new JList();
		final DefaultListModel defaultListModel3 = new DefaultListModel();
		statusList.setModel(defaultListModel3);
		statusList.setVisibleRowCount(5);
		statusScrollPane.setViewportView(statusList);
		resolutionScrollPane = new JScrollPane();
		issueAttributesPanel.add(resolutionScrollPane, cc.xy(3, 7));
		resolutionsList = new JList();
		final DefaultListModel defaultListModel4 = new DefaultListModel();
		resolutionsList.setModel(defaultListModel4);
		resolutionsList.setVisibleRowCount(5);
		resolutionScrollPane.setViewportView(resolutionsList);
		prioritiesScrollPane = new JScrollPane();
		issueAttributesPanel.add(prioritiesScrollPane, cc.xy(3, 9));
		prioritiesList = new JList();
		prioritiesList.setVisibleRowCount(5);
		prioritiesScrollPane.setViewportView(prioritiesList);
		reporterLabel = new JLabel();
		reporterLabel.setFont(new Font(reporterLabel.getFont().getName(), reporterLabel.getFont().getStyle(), 11));
		reporterLabel.setHorizontalAlignment(4);
		reporterLabel.setText("Reporter:");
		issueAttributesPanel.add(reporterLabel, cc.xy(1, 1, CellConstraints.RIGHT, CellConstraints.TOP));
		assigneeLabel = new JLabel();
		assigneeLabel.setFont(new Font(assigneeLabel.getFont().getName(), assigneeLabel.getFont().getStyle(), 11));
		assigneeLabel.setHorizontalAlignment(4);
		assigneeLabel.setText("Assignee:");
		issueAttributesPanel.add(assigneeLabel, cc.xy(1, 3, CellConstraints.RIGHT, CellConstraints.TOP));
		statusLabel = new JLabel();
		statusLabel.setFont(new Font(statusLabel.getFont().getName(), statusLabel.getFont().getStyle(), 11));
		statusLabel.setHorizontalAlignment(4);
		statusLabel.setRequestFocusEnabled(false);
		statusLabel.setText("Status:");
		issueAttributesPanel.add(statusLabel, cc.xy(1, 5, CellConstraints.RIGHT, CellConstraints.TOP));
		resolutionsLabel = new JLabel();
		resolutionsLabel.setFont(new Font(resolutionsLabel.getFont().getName(), resolutionsLabel.getFont().getStyle(), 11));
		resolutionsLabel.setHorizontalAlignment(4);
		resolutionsLabel.setText("Resolutions:");
		issueAttributesPanel.add(resolutionsLabel, cc.xy(1, 7, CellConstraints.RIGHT, CellConstraints.TOP));
		prioritiesLabel = new JLabel();
		prioritiesLabel.setFont(new Font(prioritiesLabel.getFont().getName(), prioritiesLabel.getFont().getStyle(), 11));
		prioritiesLabel.setHorizontalAlignment(4);
		prioritiesLabel.setText("Priorities:");
		prioritiesLabel.setVerticalTextPosition(0);
		issueAttributesPanel.add(prioritiesLabel, cc.xy(1, 9, CellConstraints.RIGHT, CellConstraints.TOP));
		componentsVersionsCollapsiblePanel.setBackground(new Color(-3368704));
		componentsVersionsCollapsiblePanel.setFont(new Font(componentsVersionsCollapsiblePanel.getFont().getName(),
				componentsVersionsCollapsiblePanel.getFont().getStyle(),
				componentsVersionsCollapsiblePanel.getFont().getSize()));
		rootPanel.add(componentsVersionsCollapsiblePanel, cc.xy(1, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
		generalPanel = new JPanel();
		generalPanel.setLayout(new FormLayout("fill:72px:noGrow,left:4dlu:noGrow,fill:d:grow",
				"fill:d:grow,top:3dlu:noGrow,fill:max(d;4px):noGrow"));
		generalPanel.setDoubleBuffered(true);
		generalPanel.setEnabled(false);
		generalPanel.setMinimumSize(new Dimension(335, -1));
		rootPanel.add(generalPanel,
				new CellConstraints(1, 1, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(5, 0, 0, 0)));
		final JLabel label1 = new JLabel();
		label1.setFont(new Font(label1.getFont().getName(), label1.getFont().getStyle(), 11));
		label1.setText("Project:");
		label1.setVerticalAlignment(1);
		generalPanel.add(label1, cc.xy(1, 1, CellConstraints.RIGHT, CellConstraints.TOP));
		issueTypeScrollPane = new JScrollPane();
		issueTypeScrollPane.setDoubleBuffered(false);
		generalPanel.add(issueTypeScrollPane, cc.xy(3, 3));
		issueTypeList = new JList();
		issueTypeList.setVisibleRowCount(5);
		issueTypeScrollPane.setViewportView(issueTypeList);
		projectScrollPane = new JScrollPane();
		generalPanel.add(projectScrollPane, cc.xy(3, 1));
		projectList = new JList();
		projectList.setInheritsPopupMenu(false);
		projectList.setLayoutOrientation(0);
		final DefaultListModel defaultListModel5 = new DefaultListModel();
		projectList.setModel(defaultListModel5);
		projectList.setVisibleRowCount(5);
		projectScrollPane.setViewportView(projectList);
		final JLabel label2 = new JLabel();
		label2.setFont(new Font(label2.getFont().getName(), label2.getFont().getStyle(), 11));
		label2.setRequestFocusEnabled(true);
		label2.setText("Issue Type:");
		generalPanel.add(label2, cc.xy(1, 3, CellConstraints.RIGHT, CellConstraints.TOP));
		final Spacer spacer1 = new Spacer();
		rootPanel.add(spacer1, cc.xy(1, 11, CellConstraints.DEFAULT, CellConstraints.FILL));
		fixForLabel.setLabelFor(fixForScrollPane);
		componentsLabel.setLabelFor(componentsScrollPane);
		componentsLabel.setNextFocusableComponent(componentsScrollPane);
		affectsVersionsLabel.setLabelFor(affectVersionScrollPane);
		reporterLabel.setLabelFor(reporterComboBox);
		assigneeLabel.setLabelFor(assigneeComboBox);
		statusLabel.setLabelFor(statusScrollPane);
		resolutionsLabel.setLabelFor(resolutionScrollPane);
		prioritiesLabel.setLabelFor(prioritiesScrollPane);
		label1.setLabelFor(projectScrollPane);
		label2.setLabelFor(issueTypeScrollPane);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return rootPanel;
	}
}

