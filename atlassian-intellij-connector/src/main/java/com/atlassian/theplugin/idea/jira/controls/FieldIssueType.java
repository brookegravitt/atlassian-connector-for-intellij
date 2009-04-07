package com.atlassian.theplugin.idea.jira.controls;

import com.atlassian.theplugin.jira.api.*;
import com.atlassian.theplugin.jira.model.JIRAServerModel;
import com.atlassian.theplugin.util.PluginUtil;

import javax.swing.*;
import java.util.List;

/**
 * User: jgorycki
 * Date: Apr 6, 2009
 * Time: 12:32:54 PM
 */
public class FieldIssueType extends AbstractFieldComboBox {


	public FieldIssueType(final JIRAServerModel serverModel, final JIRAIssue issue, final JIRAActionField field) {
		super(serverModel, issue, field);
	}

	protected void fillCombo(final DefaultComboBoxModel comboModel,
							 final JIRAServerModel serverModel, final JIRAIssue issue) {

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
							comboModel.removeAllElements();
							JIRAConstant selected = null;
							for (JIRAConstant type : issueTypes) {
								comboModel.addElement(type);
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


}
