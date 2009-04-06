package com.atlassian.theplugin.idea.jira.controls;

import com.atlassian.theplugin.idea.jira.renderers.JIRAQueryFragmentListRenderer;
import com.atlassian.theplugin.jira.api.*;
import com.atlassian.theplugin.jira.model.JIRAServerModel;
import com.atlassian.theplugin.util.PluginUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * User: jgorycki
 * Date: Apr 6, 2009
 * Time: 12:32:54 PM
 */
public class IssueTypeComboBox extends JComboBox implements ActionFieldEditor {
	private DefaultComboBoxModel model;
	private boolean initialized;
	private JIRAActionField field;

	public IssueTypeComboBox(final JIRAServerModel serverModel, final JIRAIssue issue, final JIRAActionField field) {
		this.field = field;
		model = new DefaultComboBoxModel();
		model.addElement("Fetching...");
		initialized = false;
		setModel(model);
		setEditable(false);
		setEnabled(false);
		setRenderer(new JIRAQueryFragmentListRenderer());
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					List<JIRAProject> projects = serverModel.getProjects(issue.getServer());
					JIRAProject issueProject = null;
					for (JIRAProject project : projects) {
						if (issue.getProjectKey().equals(project.getKey())) {
							issueProject = project;
							break;
						}
					}
					final List<JIRAConstant> issueTypes = issue.isSubTask()
							? serverModel.getSubtaskIssueTypes(issue.getServer(), issueProject) 
							: serverModel.getIssueTypes(issue.getServer(), issueProject, false);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							model.removeAllElements();
							JIRAConstant selected = null;
							for (JIRAConstant type : issueTypes) {
								model.addElement(type);
								if (issue.getType().equals(type.getName())) {
									selected = type;
								}
							}
							initialized = true;
							setEnabled(true);
							if (selected != null) {
								setSelectedItem(selected);
							} else {
								setSelectedIndex(0);
							}
						}
					});
				} catch (JIRAException e) {
					PluginUtil.getLogger().error(e.getMessage());
				}
			}
		});
		t.start();
	}

	public JIRAActionField getEditedFieldValue() {
		if (!initialized) {
			return null;
		}
		JIRAConstant type = (JIRAConstant) getSelectedItem();
		JIRAActionField ret = new JIRAActionFieldBean(field);
		ret.addValue(Long.valueOf(type.getId()).toString());
		return ret;
	}

	public Component getComponent() {
		return this;
	}

}
