package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.table.JIRAConstantListRenderer;
import com.atlassian.theplugin.idea.jira.table.JIRAQueryFragmentListRenderer;
import com.atlassian.theplugin.jira.JIRAServer;
import com.atlassian.theplugin.jira.api.JIRAProjectBean;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


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
	private JButton viewButtonBottom;
	private JButton viewHideButtonTop;
	private JButton viewButtonTop;
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
	private JLabel affectsVersionLabel;

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

//		viewButtonBottom.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent event) {
//				IdeaHelper.getCurrentJIRAToolWindowPanel().filterAndViewJiraIssues();
//			}
//		});
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
		if (this.componentsList.getModel().getSize() <= JIRAServer.LIST_SPECIAL_VALUES_COUNT) {
			this.componentsScrollPane.setVisible(false);
			this.componentsLabel.setVisible(false);
		} else {
			this.componentsScrollPane.setVisible(true);
			this.componentsLabel.setVisible(true);
		}
		if (this.affectsVersionsList.getModel().getSize() <= JIRAServer.VERSION_SPECIAL_VALUES_COUNT) {
			this.affectVersionScrollPane.setVisible(false);
			this.affectsVersionLabel.setVisible(false);
		} else {
			this.affectVersionScrollPane.setVisible(true);
			this.affectsVersionLabel.setVisible(true);
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
		this.fixForList.setListData(jiraServer.getVersions().toArray());
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
		this.fixForList.setListData(jiraServer.getVersions().toArray());
		this.componentsList.setListData(jiraServer.getComponents().toArray());
		this.affectsVersionsList.setListData(jiraServer.getVersions().toArray());
	}

	private void createUIComponents() {
		//list1 = new JList();
		//collapsiblePanel1 = new CollapsiblePanel(list1, true);


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
		rootPanel.setLayout(new GridLayoutManager(14, 3, new Insets(0, 0, 0, 0), -1, -1));
		rootPanel.setMaximumSize(new Dimension(347, 586));
		final JLabel label1 = new JLabel();
		label1.setFont(new Font(label1.getFont().getName(), label1.getFont().getStyle(), 11));
		label1.setRequestFocusEnabled(true);
		label1.setText("Issue Type:");
		rootPanel.add(label1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		reporterComboBox = new JComboBox();
		reporterComboBox.setLightWeightPopupEnabled(false);
		reporterComboBox.setMaximumRowCount(5);
		rootPanel.add(reporterComboBox, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label2 = new JLabel();
		label2.setFont(new Font(label2.getFont().getName(), label2.getFont().getStyle(), 11));
		label2.setText("Reporter:");
		rootPanel.add(label2, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		assigneeComboBox = new JComboBox();
		assigneeComboBox.setLightWeightPopupEnabled(false);
		assigneeComboBox.setMaximumRowCount(5);
		rootPanel.add(assigneeComboBox, new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label3 = new JLabel();
		label3.setFont(new Font(label3.getFont().getName(), label3.getFont().getStyle(), 11));
		label3.setText("Assignee:");
		rootPanel.add(label3, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
		panel1.setBackground(new Color(-3355444));
		rootPanel.add(panel1, new GridConstraints(3, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label4 = new JLabel();
		label4.setText("Components/Versions");
		panel1.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer1 = new Spacer();
		panel1.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
		panel2.setBackground(new Color(-3355444));
		rootPanel.add(panel2, new GridConstraints(7, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		final JLabel label5 = new JLabel();
		label5.setText("Issue Attributes");
		panel2.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer2 = new Spacer();
		panel2.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		final JLabel label6 = new JLabel();
		label6.setFont(new Font(label6.getFont().getName(), label6.getFont().getStyle(), 11));
		label6.setRequestFocusEnabled(false);
		label6.setText("Status:");
		rootPanel.add(label6, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		issueTypeScrollPane = new JScrollPane();
		rootPanel.add(issueTypeScrollPane, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, 1, null, null, null, 0, false));
		issueTypeList = new JList();
		issueTypeList.setVisibleRowCount(5);
		issueTypeScrollPane.setViewportView(issueTypeList);
		fixForScrollPane = new JScrollPane();
		rootPanel.add(fixForScrollPane, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, 1, null, null, null, 0, false));
		fixForList = new JList();
		fixForList.setVisibleRowCount(5);
		fixForScrollPane.setViewportView(fixForList);
		fixForLabel = new JLabel();
		fixForLabel.setFont(new Font(fixForLabel.getFont().getName(), fixForLabel.getFont().getStyle(), 11));
		fixForLabel.setText("Fix For:");
		rootPanel.add(fixForLabel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		projectScrollPane = new JScrollPane();
		rootPanel.add(projectScrollPane, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, 1, null, null, null, 0, false));
		projectList = new JList();
		projectList.setInheritsPopupMenu(false);
		projectList.setLayoutOrientation(0);
		final DefaultListModel defaultListModel1 = new DefaultListModel();
		projectList.setModel(defaultListModel1);
		projectList.setVisibleRowCount(5);
		projectScrollPane.setViewportView(projectList);
		final JLabel label7 = new JLabel();
		label7.setFont(new Font(label7.getFont().getName(), label7.getFont().getStyle(), 11));
		label7.setText("Project:");
		label7.setVerticalAlignment(1);
		rootPanel.add(label7, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		statusScrollPane = new JScrollPane();
		rootPanel.add(statusScrollPane, new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, 1, null, null, null, 0, false));
		statusList = new JList();
		final DefaultListModel defaultListModel2 = new DefaultListModel();
		statusList.setModel(defaultListModel2);
		statusList.setVisibleRowCount(5);
		statusScrollPane.setViewportView(statusList);
		resolutionScrollPane = new JScrollPane();
		rootPanel.add(resolutionScrollPane, new GridConstraints(11, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, 1, null, null, null, 0, false));
		resolutionsList = new JList();
		final DefaultListModel defaultListModel3 = new DefaultListModel();
		resolutionsList.setModel(defaultListModel3);
		resolutionsList.setVisibleRowCount(5);
		resolutionScrollPane.setViewportView(resolutionsList);
		final JLabel label8 = new JLabel();
		label8.setFont(new Font(label8.getFont().getName(), label8.getFont().getStyle(), 11));
		label8.setText("Resolutions:");
		rootPanel.add(label8, new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		prioritiesScrollPane = new JScrollPane();
		rootPanel.add(prioritiesScrollPane, new GridConstraints(12, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, 1, null, null, null, 0, false));
		prioritiesList = new JList();
		prioritiesList.setVisibleRowCount(5);
		prioritiesScrollPane.setViewportView(prioritiesList);
		final JLabel label9 = new JLabel();
		label9.setFont(new Font(label9.getFont().getName(), label9.getFont().getStyle(), 11));
		label9.setText("Priorities:");
		rootPanel.add(label9, new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		affectVersionScrollPane = new JScrollPane();
		rootPanel.add(affectVersionScrollPane, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, 1, null, null, null, 0, false));
		affectsVersionsList = new JList();
		affectsVersionsList.setVisibleRowCount(5);
		affectVersionScrollPane.setViewportView(affectsVersionsList);
		affectsVersionLabel = new JLabel();
		affectsVersionLabel.setFont(new Font(affectsVersionLabel.getFont().getName(), affectsVersionLabel.getFont().getStyle(), 11));
		affectsVersionLabel.setText("<html>Affects<br>  Versions:</html>");
		rootPanel.add(affectsVersionLabel, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		componentsScrollPane = new JScrollPane();
		rootPanel.add(componentsScrollPane, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		componentsList = new JList();
		final DefaultListModel defaultListModel4 = new DefaultListModel();
		componentsList.setModel(defaultListModel4);
		componentsList.setVisible(true);
		componentsList.setVisibleRowCount(5);
		componentsScrollPane.setViewportView(componentsList);
		componentsLabel = new JLabel();
		componentsLabel.setFont(new Font(componentsLabel.getFont().getName(), componentsLabel.getFont().getStyle(), 11));
		componentsLabel.setHorizontalAlignment(4);
		componentsLabel.setText("Components:");
		rootPanel.add(componentsLabel, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer3 = new Spacer();
		rootPanel.add(spacer3, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		final JPanel panel3 = new JPanel();
		panel3.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
		rootPanel.add(panel3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		viewHideButtonTop = new JButton();
		viewHideButtonTop.setFont(new Font(viewHideButtonTop.getFont().getName(), viewHideButtonTop.getFont().getStyle(), 12));
		viewHideButtonTop.setInheritsPopupMenu(true);
		viewHideButtonTop.setLabel("<< View & Hide");
		viewHideButtonTop.setText("<< View & Hide");
		viewHideButtonTop.setMnemonic('H');
		viewHideButtonTop.setDisplayedMnemonicIndex(10);
		panel3.add(viewHideButtonTop, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, 1, null, null, null, 0, false));
		viewButtonTop = new JButton();
		viewButtonTop.setFont(new Font(viewButtonTop.getFont().getName(), viewButtonTop.getFont().getStyle(), 12));
		viewButtonTop.setLabel("View >>");
		viewButtonTop.setText("View >>");
		panel3.add(viewButtonTop, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, 1, null, new Dimension(46, 29), null, 0, false));
		final Spacer spacer4 = new Spacer();
		panel3.add(spacer4, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		final JPanel panel4 = new JPanel();
		panel4.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
		rootPanel.add(panel4, new GridConstraints(13, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		viewHideButtonBottom = new JButton();
		viewHideButtonBottom.setActionCommand("<< View & Hide");
		viewHideButtonBottom.setInheritsPopupMenu(true);
		viewHideButtonBottom.setLabel("<< View & Hide");
		viewHideButtonBottom.setText("<< View & Hide");
		viewHideButtonBottom.setMnemonic(' ');
		viewHideButtonBottom.setDisplayedMnemonicIndex(2);
		viewHideButtonBottom.putClientProperty("html.disable", Boolean.FALSE);
		viewHideButtonBottom.putClientProperty("hideActionText", Boolean.FALSE);
		panel4.add(viewHideButtonBottom, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, 1, null, null, null, 0, false));
		viewButtonBottom = new JButton();
		viewButtonBottom.setLabel("View >>");
		viewButtonBottom.setText("View >>");
		panel4.add(viewButtonBottom, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer5 = new Spacer();
		panel4.add(spacer5, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		label1.setLabelFor(issueTypeScrollPane);
		label2.setLabelFor(reporterComboBox);
		label3.setLabelFor(assigneeComboBox);
		fixForLabel.setLabelFor(fixForScrollPane);
		label7.setLabelFor(projectScrollPane);
		label8.setLabelFor(resolutionScrollPane);
		label9.setLabelFor(prioritiesScrollPane);
		affectsVersionLabel.setLabelFor(affectVersionScrollPane);
		componentsLabel.setNextFocusableComponent(componentsScrollPane);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return rootPanel;
	}
}

