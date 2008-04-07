package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ui.CollapsiblePanel;
import com.atlassian.theplugin.idea.jira.table.JIRAConstantListRenderer;
import com.atlassian.theplugin.idea.jira.table.JIRAQueryFragmentListRenderer;
import com.atlassian.theplugin.jira.JIRAServer;
import com.atlassian.theplugin.jira.api.JIRAProjectBean;
import com.atlassian.theplugin.jira.api.JIRAQueryFragment;
import com.intellij.openapi.util.IconLoader;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Apr 1, 2008
 * Time: 9:54:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class JIRAIssueFilterPanel extends JPanel {
	private JList projectList;
	private JList issueTypeList;
	private JList fixForList;
	private JList affectsVersionsList;
	private JComboBox reporterComboBox;
	private JComboBox assigneeComboBox;
	private JList resolutionsList;
	private JList prioritiesList;
	private JButton viewHideButtonBottom;
	private JButton viewHideButtonTop;
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


	private JIRAServer jiraServer;


	public JIRAIssueFilterPanel() {
		$$$setupUI$$$();

		viewHideButtonBottom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				IdeaHelper.getCurrentJIRAToolWindowPanel().filterAndViewJiraIssues();
			}
		});

		viewHideButtonTop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				IdeaHelper.getCurrentJIRAToolWindowPanel().filterAndViewJiraIssues();
			}
		});

		this.projectList.setCellRenderer(new JIRAQueryFragmentListRenderer());
		this.issueTypeList.setCellRenderer(new JIRAConstantListRenderer());
		this.statusList.setCellRenderer(new JIRAConstantListRenderer());
		this.prioritiesList.setCellRenderer(new JIRAConstantListRenderer());
		this.resolutionsList.setCellRenderer(new JIRAQueryFragmentListRenderer());
		this.fixForList.setCellRenderer(new JIRAQueryFragmentListRenderer());
		this.componentsList.setCellRenderer(new JIRAQueryFragmentListRenderer());
		this.affectsVersionsList.setCellRenderer(new JIRAQueryFragmentListRenderer());

		projectList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				if (projectList.getSelectedValues().length == 1) {
					JIRAProjectBean project = (JIRAProjectBean) projectList.getSelectedValues()[0];
					jiraServer.setCurrentProject(project);
					refreshProjectDependentLists();
				} else {
					clearProjectDependentLists();
				}
			}


		});
	}

	private void enableProjectDependentLists(boolean value) {
		this.issueTypeList.setEnabled(value);
		this.fixForList.setEnabled(value);
		this.componentsList.setEnabled(value);
		this.affectsVersionsList.setEnabled(value);

		if (this.fixForList.getModel().getSize() <= JIRAServer.VERSION_SPECIAL_VALUES_COUNT) {
			this.fixForScrollPane.setVisible(false);
			this.fixForLabel.setVisible(false);
		} else {
			this.fixForScrollPane.setVisible(true);
			this.fixForLabel.setVisible(true);
		}
		if (this.componentsList.getModel().getSize() <= JIRAServer.COMPONENTS_SPECIAL_VALUES_COUNT) {
			this.componentsScrollPane.setVisible(false);
			this.componentsLabel.setVisible(false);
		} else {
			this.componentsScrollPane.setVisible(true);
			this.componentsLabel.setVisible(true);
		}
		if (this.affectsVersionsList.getModel().getSize() <= JIRAServer.VERSION_SPECIAL_VALUES_COUNT) {
			this.affectVersionScrollPane.setVisible(false);
			this.affectsVersionsLabel.setVisible(false);
		} else {
			this.affectVersionScrollPane.setVisible(true);
			this.affectsVersionsLabel.setVisible(true);
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
		this.issueTypeList.setListData(jiraServer.getIssueTypes().toArray());
		this.fixForList.setListData(jiraServer.getFixForVersions().toArray());
		this.componentsList.setListData(jiraServer.getComponents().toArray());
		this.affectsVersionsList.setListData(jiraServer.getVersions().toArray());
		enableProjectDependentLists(true);
	}

	public void setJiraServer(JIRAServer jiraServer) {
		this.jiraServer = jiraServer;
		this.projectList.setListData(jiraServer.getProjects().toArray());
		this.issueTypeList.setListData(jiraServer.getIssueTypes().toArray());
		this.statusList.setListData(jiraServer.getStatuses().toArray());
		this.prioritiesList.setListData(jiraServer.getPriorieties().toArray());
		this.resolutionsList.setListData(jiraServer.getResolutions().toArray());
		this.fixForList.setListData(jiraServer.getFixForVersions().toArray());
		this.componentsList.setListData(jiraServer.getComponents().toArray());
		this.affectsVersionsList.setListData(jiraServer.getVersions().toArray());
	}

	public void setInitialFilter(List<JIRAQueryFragment> advancedQuery) {
		setComboValues(projectList, advancedQuery);
		setComboValues(statusList, advancedQuery);
		setComboValues(prioritiesList, advancedQuery);
		setComboValues(resolutionsList, advancedQuery);
		setComboValues(issueTypeList, advancedQuery);
		setComboValues(componentsList, advancedQuery);
		setComboValues(fixForList, advancedQuery);
		setComboValues(affectsVersionsList, advancedQuery);
	}

	public void setComboValues(JList list,
							   List<JIRAQueryFragment> advancedQuery) {
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
		return query;
	}

	private void createUIComponents() {
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
		rootPanel.setLayout(new GridLayoutManager(8, 3, new Insets(0, 0, 0, 0), -1, -1));
		rootPanel.setMaximumSize(new Dimension(347, 586));
		final JLabel label1 = new JLabel();
		label1.setFont(new Font(label1.getFont().getName(), label1.getFont().getStyle(), 11));
		label1.setRequestFocusEnabled(true);
		label1.setText("Issue Type:");
		rootPanel.add(label1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(70, 14), null, 0, false));
		issueTypeScrollPane = new JScrollPane();
		rootPanel.add(issueTypeScrollPane, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, 1, null, new Dimension(273, 80), null, 0, false));
		issueTypeList = new JList();
		issueTypeList.setVisibleRowCount(5);
		issueTypeScrollPane.setViewportView(issueTypeList);
		projectScrollPane = new JScrollPane();
		rootPanel.add(projectScrollPane, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, 1, null, new Dimension(273, 80), null, 0, false));
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
		rootPanel.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(70, 14), null, 0, false));
		final Spacer spacer1 = new Spacer();
		rootPanel.add(spacer1, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
		rootPanel.add(panel1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, 1, 1, null, new Dimension(273, 33), null, 0, false));
		viewHideButtonTop = new JButton();
		viewHideButtonTop.setFont(new Font(viewHideButtonTop.getFont().getName(), viewHideButtonTop.getFont().getStyle(), 12));
		viewHideButtonTop.setInheritsPopupMenu(true);
		viewHideButtonTop.setLabel("<< View & Hide");
		viewHideButtonTop.setText("<< View & Hide");
		viewHideButtonTop.setMnemonic('H');
		viewHideButtonTop.setDisplayedMnemonicIndex(10);
		panel1.add(viewHideButtonTop, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, 1, null, null, null, 0, false));
		final Spacer spacer2 = new Spacer();
		panel1.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
		rootPanel.add(panel2, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(273, 33), null, 0, false));
		viewHideButtonBottom = new JButton();
		viewHideButtonBottom.setActionCommand("<< View & Hide");
		viewHideButtonBottom.setInheritsPopupMenu(true);
		viewHideButtonBottom.setLabel("<< View & Hide");
		viewHideButtonBottom.setText("<< View & Hide");
		viewHideButtonBottom.setMnemonic(' ');
		viewHideButtonBottom.setDisplayedMnemonicIndex(2);
		viewHideButtonBottom.putClientProperty("hideActionText", Boolean.FALSE);
		viewHideButtonBottom.putClientProperty("html.disable", Boolean.FALSE);
		panel2.add(viewHideButtonBottom, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, 1, null, null, null, 0, false));
		final Spacer spacer3 = new Spacer();
		panel2.add(spacer3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		componentsVersionsPanel.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
		componentsVersionsPanel.setEnabled(false);
		rootPanel.add(componentsVersionsPanel, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, 1, GridConstraints.SIZEPOLICY_FIXED, new Dimension(350, 100), null, null, 0, false));
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
		affectVersionScrollPane.setViewportView(affectsVersionsList);
		affectsVersionsLabel = new JLabel();
		affectsVersionsLabel.setFont(new Font(affectsVersionsLabel.getFont().getName(), affectsVersionsLabel.getFont().getStyle(), 11));
		affectsVersionsLabel.setText("<html>Affects<br>  Versions:</html>");
		componentsVersionsPanel.add(affectsVersionsLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		rootPanel.add(issueAttributesCollapsiblePanel, new GridConstraints(5, 0, 1, 2, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, 1, 1, new Dimension(350, -1), null, null, 0, false));
		issueAttributesPanel.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
		issueAttributesPanel.setEnabled(false);
		rootPanel.add(issueAttributesPanel, new GridConstraints(6, 0, 1, 2, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, 1, GridConstraints.SIZEPOLICY_FIXED, new Dimension(350, 100), null, null, 0, false));
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
		rootPanel.add(componentsVersionsCollapsiblePanel, new GridConstraints(3, 0, 1, 3, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(350, 20), null, new Dimension(350, -1), 0, false));
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

