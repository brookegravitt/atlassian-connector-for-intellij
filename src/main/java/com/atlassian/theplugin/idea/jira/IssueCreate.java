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
import com.atlassian.theplugin.jira.model.JIRAServerCache;
import com.atlassian.theplugin.jira.api.JIRAConstant;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.api.JIRAIssueBean;
import com.atlassian.theplugin.jira.api.JIRAProject;
import com.atlassian.theplugin.jira.model.JIRAServerModel;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class IssueCreate extends DialogWrapper {
	private JPanel mainPanel;
	private JTextArea description;
	private JComboBox projectComboBox;
	private JComboBox typeComboBox;
	private JTextField summary;
	private JComboBox priorityComboBox;
	private JTextField assignee;
	private final JiraServerCfg jiraServer;
	private final JIRAServerModel model;

	public IssueCreate(JIRAServerModel model, JiraServerCfg server) {
		super(false);
		this.model = model;
		$$$setupUI$$$();
		init();
		pack();

		this.jiraServer = server;
		setTitle("Create JIRA Issue");

		projectComboBox.setRenderer(new ColoredListCellRenderer() {
			@Override
			protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
				if (value != null) {
					append(((JIRAProject) value).getName(), SimpleTextAttributes.SIMPLE_CELL_ATTRIBUTES);
				}
			}
		});

		typeComboBox.setRenderer(new ColoredListCellRenderer() {
			@Override
			protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
				if (value != null) {
					JIRAConstant type = (JIRAConstant) value;
					append(type.getName(), SimpleTextAttributes.SIMPLE_CELL_ATTRIBUTES);
					setIcon(CachedIconLoader.getIcon(type.getIconUrl()));
				}
			}
		});
		typeComboBox.setEnabled(false);

		priorityComboBox.setRenderer(new ColoredListCellRenderer() {
			@Override
			protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
				if (value != null) {
					JIRAConstant priority = (JIRAConstant) value;
					append(priority.getName(), SimpleTextAttributes.SIMPLE_CELL_ATTRIBUTES);
					setIcon(CachedIconLoader.getIcon(priority.getIconUrl()));
				}
			}
		});

		projectComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				JIRAProject p = (JIRAProject) projectComboBox.getSelectedItem();
				updateIssueTypes(p);
			}
		});
		getOKAction().setEnabled(false);
		getOKAction().putValue(Action.NAME, "Create");
	}

	public void initData() {
		updatePriorities();
		updateProject();
	}

	private void updateProject() {
		projectComboBox.setEnabled(false);
		getOKAction().setEnabled(false);

		new Thread(new Runnable() {
			public void run() {
				final List<JIRAProject> projects = model.getProjects(jiraServer);
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						addProjects(projects);
					}
				});
			}
		}, "atlassian-idea-plugin jira issue priorities retrieve on issue create").start();
	}

	private void addProjects(List<JIRAProject> projects) {
		projectComboBox.removeAllItems();
		for (JIRAProject project : projects) {
			if (project.getId() != JIRAServerCache.ANY_ID) {
				projectComboBox.addItem(project);
			}
		}

		if (projectComboBox.getModel().getSize() > 0) {
			projectComboBox.setSelectedIndex(0);
		}
		projectComboBox.setEnabled(true);
	}

	private void updatePriorities() {
		priorityComboBox.setEnabled(false);
		getOKAction().setEnabled(false);
		new Thread(new Runnable() {
			public void run() {
				final List<JIRAConstant> priorities = model.getPriorities(jiraServer);
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						addIssuePriorieties(priorities);
					}
				});
			}
		}, "atlassian-idea-plugin jira issue priorities retrieve on issue create").start();
	}

	private void addIssuePriorieties(List<JIRAConstant> priorieties) {
		priorityComboBox.removeAllItems();
		for (JIRAConstant constant : priorieties) {
			if (constant.getId() != JIRAServerCache.ANY_ID) {
				priorityComboBox.addItem(constant);
			}
		}
		if (priorityComboBox.getModel().getSize() > 0) {
			priorityComboBox.setSelectedIndex(priorityComboBox.getModel().getSize() / 2);
		}
		priorityComboBox.setEnabled(true);
	}

	private void updateIssueTypes(final JIRAProject project) {
		typeComboBox.setEnabled(false);
		getOKAction().setEnabled(false);
		new Thread(new Runnable() {
			public void run() {
				final List<JIRAConstant> issueTypes = model.getIssueTypes(jiraServer, project);
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						addIssueTypes(issueTypes);
					}
				});
			}
		}, "atlassian-idea-plugin jira issue types retrieve on issue create").start();
	}

	private void addIssueTypes(List<JIRAConstant> issueTypes) {
		typeComboBox.removeAllItems();
		for (JIRAConstant constant : issueTypes) {
			if (constant.getId() != JIRAServerCache.ANY_ID) {
				typeComboBox.addItem(constant);
			}
		}
		typeComboBox.setEnabled(true);
		getOKAction().setEnabled(true);
	}

	private JIRAIssueBean issueProxy;

	JIRAIssue getJIRAIssue() {
		return issueProxy;
	}

	@Override
	protected void doOKAction() {
		issueProxy = new JIRAIssueBean();
		issueProxy.setSummary(summary.getText());

		if (projectComboBox.getSelectedItem() == null) {
			Messages.showErrorDialog(this.getContentPane(), "Project has to be selected", "Project not defined");
			return;
		}
		issueProxy.setProjectKey(((JIRAProject) projectComboBox.getSelectedItem()).getKey());
		if (typeComboBox.getSelectedItem() == null) {
			Messages.showErrorDialog(this.getContentPane(), "Issue type has to be selected", "Issue type not defined");
			return;
		}
		issueProxy.setType(((JIRAConstant) typeComboBox.getSelectedItem()));
		issueProxy.setDescription(description.getText());
		issueProxy.setPriority(((JIRAConstant) priorityComboBox.getSelectedItem()));
		String assignTo = assignee.getText();
		if (assignTo.length() > 0) {
			issueProxy.setAssignee(assignTo);
		}
		super.doOKAction();
	}

	@Override
	public JComponent getPreferredFocusedComponent() {
		return summary;
	}

	@Override
	@Nullable
	protected JComponent createCenterPanel() {
		return mainPanel;
	}

	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayoutManager(7, 6, new Insets(5, 5, 5, 5), -1, -1));
		mainPanel.setMinimumSize(new Dimension(400, 250));
		final JScrollPane scrollPane1 = new JScrollPane();
		mainPanel.add(scrollPane1, new GridConstraints(5, 0, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		description = new JTextArea();
		description.setLineWrap(true);
		description.setWrapStyleWord(true);
		scrollPane1.setViewportView(description);
		final JLabel label1 = new JLabel();
		label1.setText("Summary:");
		mainPanel.add(label1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label2 = new JLabel();
		label2.setText("Project:");
		mainPanel.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		projectComboBox = new JComboBox();
		mainPanel.add(projectComboBox, new GridConstraints(0, 1, 1, 5, GridConstraints.ANCHOR_WEST,
				GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
				new Dimension(150, -1), null, null, 0, false));
		summary = new JTextField();
		mainPanel.add(summary, new GridConstraints(3, 1, 1, 5, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0,
				false));
		final JLabel label3 = new JLabel();
		label3.setText("Description:");
		mainPanel.add(label3, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label4 = new JLabel();
		label4.setText("Assignee:");
		mainPanel.add(label4, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		assignee = new JTextField();
		mainPanel.add(assignee, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0,
				false));
		final JLabel label5 = new JLabel();
		label5.setFont(new Font(label5.getFont().getName(), label5.getFont().getStyle(), 10));
		label5.setText("Warning! This field is not validated prior to sending to JIRA");
		mainPanel.add(label5, new GridConstraints(6, 2, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label6 = new JLabel();
		label6.setText("Type:");
		mainPanel.add(label6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		typeComboBox = new JComboBox();
		mainPanel.add(typeComboBox, new GridConstraints(1, 1, 1, 5, GridConstraints.ANCHOR_WEST,
				GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
				new Dimension(150, -1), null, null, 0, false));
		final JLabel label7 = new JLabel();
		label7.setText("Priority:");
		mainPanel.add(label7, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		priorityComboBox = new JComboBox();
		mainPanel.add(priorityComboBox, new GridConstraints(2, 1, 1, 5, GridConstraints.ANCHOR_WEST,
				GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
				new Dimension(150, -1), null, null, 0, false));
		label1.setLabelFor(summary);
		label2.setLabelFor(projectComboBox);
		label3.setLabelFor(description);
		label6.setLabelFor(typeComboBox);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return mainPanel;
	}
}