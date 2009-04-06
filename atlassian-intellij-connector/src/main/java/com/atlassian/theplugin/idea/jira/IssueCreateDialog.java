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

import com.atlassian.theplugin.commons.UiTaskAdapter;
import com.atlassian.theplugin.commons.UiTaskExecutor;
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.idea.config.GenericComboBoxItemWrapper;
import com.atlassian.theplugin.jira.api.*;
import com.atlassian.theplugin.jira.model.JIRAServerCache;
import com.atlassian.theplugin.jira.model.JIRAServerModel;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

public class IssueCreateDialog extends DialogWrapper {
	private JPanel mainPanel;
	private JTextArea description;
	private JComboBox projectComboBox;
	private JComboBox typeComboBox;
	private JTextField summary;
	private JComboBox priorityComboBox;
	private JTextField assignee;
	private JList componentsList;
	private final JiraServerCfg jiraServer;
	private final JIRAServerModel model;
	private final UiTaskExecutor uiTaskExecutor;
	private JiraWorkspaceConfiguration jiraConfiguration;
	private ActionListener projectComboListener;

	public IssueCreateDialog(JIRAServerModel model, JiraServerCfg server,
			@NotNull final JiraWorkspaceConfiguration jiraProjectCfg, @NotNull final UiTaskExecutor uiTaskExecutor) {
		super(false);
		this.model = model;
		this.jiraConfiguration = jiraProjectCfg;
		this.uiTaskExecutor = uiTaskExecutor;
		$$$setupUI$$$();
		componentsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
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

		projectComboListener = new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				JIRAProject p = (JIRAProject) projectComboBox.getSelectedItem();
				updateIssueTypes(p);
				updateComponents(p);
			}
		};
		projectComboBox.addActionListener(projectComboListener);
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
				List<JIRAProject> projects = new ArrayList<JIRAProject>();
				try {
					projects = model.getProjects(jiraServer);
				} catch (JIRAException e) {
					PluginUtil.getLogger().error("Cannot retrieve JIRA projects:" + e.getMessage());
				}
				final List<JIRAProject> finalProjects = projects;
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						addProjects(finalProjects);
					}
				});
			}
		}, "atlassian-idea-plugin jira issue priorities retrieve on issue create").start();
	}

	private void addProjects(List<JIRAProject> projects) {
		projectComboBox.removeAllItems();

		// adding elements to combo triggers selection changed action which updates components for selected project
		// we do not want to call jira several time for the same project here
		projectComboBox.removeActionListener(projectComboListener);
		for (JIRAProject project : projects) {
			if (project.getId() != JIRAServerCache.ANY_ID) {
				projectComboBox.addItem(project);
			}
		}
		projectComboBox.addActionListener(projectComboListener);


		if (projectComboBox.getModel().getSize() > 0) {

			boolean defaultSelected = false;

			// select default project
			if (jiraConfiguration != null &&
					jiraConfiguration.getView().getServerDefaults().containsKey(jiraServer.getServerId().toString())) {

				String project = jiraConfiguration.getView().getServerDefaults().
						get(jiraServer.getServerId().toString()).getProject();

				for (int i = 0; i < projectComboBox.getItemCount(); ++i) {
					if (projectComboBox.getItemAt(i) instanceof JIRAProject) {
						if (((JIRAProject) projectComboBox.getItemAt(i)).getKey().equals(project)) {
							projectComboBox.setSelectedIndex(i);
							defaultSelected = true;
							break;
						}
					}
				}
			}

			if (!defaultSelected) {
				projectComboBox.setSelectedIndex(0);
			}
		}
		projectComboBox.setEnabled(true);
	}

	private void updatePriorities() {
		priorityComboBox.setEnabled(false);
		getOKAction().setEnabled(false);
		new Thread(new Runnable() {
			public void run() {
				List<JIRAConstant> priorities = new ArrayList<JIRAConstant>();
				try {
					priorities = model.getPriorities(jiraServer);
				} catch (JIRAException e) {
					PluginUtil.getLogger().error("Cannot retrieve JIRa priorities:" + e.getMessage());
				}

				final List<JIRAConstant> finalPriorities = priorities;
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						addIssuePriorieties(finalPriorities);
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
				List<JIRAConstant> issueTypes = new ArrayList<JIRAConstant>();
				try {
					issueTypes = model.getIssueTypes(jiraServer, project, true);
				} catch (JIRAException e) {
					PluginUtil.getLogger().error("Cannto retrieve JIRA issue types:" + e.getMessage());
				}
				final List<JIRAConstant> finalIssueTypes = issueTypes;
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						addIssueTypes(finalIssueTypes);
					}
				});
			}
		}, "atlassian-idea-plugin jira issue types retrieve on issue create").start();
	}

	private void updateComponents(final JIRAProject project) {
		componentsList.setEnabled(false);
		getOKAction().setEnabled(false);
		uiTaskExecutor.execute(new UiTaskAdapter("fetching components", getContentPane()) {
			private List<JIRAComponentBean> components;

			public void run() throws Exception {
				components = model.getComponents(jiraServer, project);
			}

			@Override
			public void onSuccess() {
				addComponents(components);
			}

		});
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


	private void addComponents(Collection<JIRAComponentBean> components) {
		final DefaultListModel listModel = new DefaultListModel();
		for (JIRAComponentBean constant : components) {
			if (constant != null && constant.getId() != JIRAServerCache.ANY_ID) {
				listModel.addElement(new ComponentWrapper(constant));
			}
		}
		componentsList.setModel(listModel);

		if (projectComboBox.getSelectedItem() != null && jiraConfiguration != null && jiraConfiguration.getView() != null
				&& jiraConfiguration.getView().getServerDefaults() != null &&
				jiraConfiguration.getView().getServerDefaults().containsKey(jiraServer.getServerId().toString())) {

			String selectedProject = ((JIRAProject) projectComboBox.getSelectedItem()).getKey();

			String configProject = jiraConfiguration.getView().getServerDefaults().
					get(jiraServer.getServerId().toString()).getProject();

			Collection<Long> configComponents = jiraConfiguration.getView().getServerDefaults().
					get(jiraServer.getServerId().toString()).getComponents();

			// select default components for specified project
			if (selectedProject.equals(configProject)) {

				ArrayList<Integer> indexesToSelect = new ArrayList<Integer>(componentsList.getModel().getSize() + 1);

				for (int i = 0; i < componentsList.getModel().getSize(); ++i) {
					if (componentsList.getModel().getElementAt(i) instanceof ComponentWrapper) {
						ComponentWrapper wrapper = (ComponentWrapper) componentsList.getModel().getElementAt(i);

						if (wrapper.getWrapped() != null) {
							JIRAComponentBean component = wrapper.getWrapped();

							if (configComponents.contains(component.getId())) {
								indexesToSelect.add(i);
							}
						}
					}
				}

				if (indexesToSelect.size() > 0) {
					componentsList.setSelectedIndices(ArrayUtils.toPrimitive(indexesToSelect.toArray(new Integer[0])));
				}
			}
		}

		componentsList.setEnabled(true);
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
		List<JIRAConstant> components = MiscUtil.buildArrayList();
		Collection<Long> selectedComponents = new LinkedHashSet<Long>();
		for (Object selectedObject : componentsList.getSelectedValues()) {
			if (selectedObject instanceof ComponentWrapper) {
				ComponentWrapper componentWrapper = (ComponentWrapper) selectedObject;
				if (componentWrapper.getWrapped().getId() == JIRAServerCache.UNKNOWN_COMPONENT_ID) {
					if (componentsList.getSelectedValues().length > 1) {
						Messages.showErrorDialog(getContentPane(), "You cannot select \"Unknown\" with a specific component.");
						return;
					}
				}
				components.add(componentWrapper.getWrapped());
				selectedComponents.add(componentWrapper.getWrapped().getId());
			}
		}

		if (components.size() > 0) {
			issueProxy.setComponents(components);
		}
		String assignTo = assignee.getText();
		if (assignTo.length() > 0) {
			issueProxy.setAssignee(assignTo);
		}

		// save selected project and components to the config
		if (jiraConfiguration != null && jiraConfiguration.getView() != null) {
			JIRAProject p = (JIRAProject) projectComboBox.getSelectedItem();
			jiraConfiguration.getView().addServerDefault(jiraServer.getServerId().toString(), p.getKey(), selectedComponents);
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
		mainPanel.setLayout(new GridLayoutManager(8, 3, new Insets(5, 5, 5, 5), -1, -1));
		mainPanel.setMinimumSize(new Dimension(480, 400));
		final JScrollPane scrollPane1 = new JScrollPane();
		mainPanel.add(scrollPane1, new GridConstraints(6, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		description = new JTextArea();
		description.setLineWrap(true);
		description.setWrapStyleWord(true);
		scrollPane1.setViewportView(description);
		final JLabel label1 = new JLabel();
		label1.setText("Summary:");
		mainPanel.add(label1, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label2 = new JLabel();
		label2.setText("Project:");
		mainPanel.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		projectComboBox = new JComboBox();
		mainPanel.add(projectComboBox,
				new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
						GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, -1), null, null, 0, false));
		summary = new JTextField();
		mainPanel.add(summary, new GridConstraints(4, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
				null, new Dimension(100, -1), null, 0, false));
		final JLabel label3 = new JLabel();
		label3.setText("Description:");
		mainPanel.add(label3, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label4 = new JLabel();
		label4.setText("Assignee:");
		mainPanel.add(label4, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		assignee = new JTextField();
		mainPanel.add(assignee, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
				new Dimension(50, -1), new Dimension(150, -1), null, 0, false));
		final JLabel label5 = new JLabel();
		label5.setFont(new Font(label5.getFont().getName(), label5.getFont().getStyle(), 10));
		label5.setHorizontalTextPosition(10);
		label5.setText("Warning! This field is not validated prior to sending to JIRA");
		mainPanel.add(label5, new GridConstraints(7, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
				null, null, null, 0, false));
		final JLabel label6 = new JLabel();
		label6.setText("Type:");
		mainPanel.add(label6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		typeComboBox = new JComboBox();
		mainPanel.add(typeComboBox,
				new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
						GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label7 = new JLabel();
		label7.setText("Priority:");
		mainPanel.add(label7, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		priorityComboBox = new JComboBox();
		mainPanel.add(priorityComboBox,
				new GridConstraints(3, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
						GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, -1), null, null, 0, false));
		final JLabel label8 = new JLabel();
		label8.setText("Component:");
		mainPanel.add(label8, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JScrollPane scrollPane2 = new JScrollPane();
		mainPanel.add(scrollPane2, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
				null, null, null, 0, false));
		componentsList = new JList();
		componentsList.setToolTipText("Select Affected Components ");
		componentsList.setVisibleRowCount(5);
		scrollPane2.setViewportView(componentsList);
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

	private static class ComponentWrapper extends GenericComboBoxItemWrapper<JIRAComponentBean> {

		public ComponentWrapper(@NotNull final JIRAComponentBean wrapped) {
			super(wrapped);
		}

		@Override
		public String toString() {
			return wrapped.getName();
		}
	}
}