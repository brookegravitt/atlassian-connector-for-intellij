package com.atlassian.theplugin.idea.jira.controls;

import com.atlassian.theplugin.commons.jira.api.JIRAActionField;
import com.atlassian.theplugin.commons.jira.api.JIRAConstant;
import com.atlassian.theplugin.commons.jira.api.JIRAProject;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.commons.jira.api.rss.JIRAException;
import com.atlassian.theplugin.commons.jira.cache.JIRAServerModel;
import com.atlassian.theplugin.util.PluginUtil;

import javax.swing.*;
import java.util.List;

/**
 * User: jgorycki
 * Date: Apr 6, 2009
 * Time: 12:32:54 PM
 */
public class FieldIssueType extends AbstractFieldComboBox {

	public FieldIssueType(final JIRAServerModel serverModel, final JiraIssueAdapter issue, final JIRAActionField field,
			final FreezeListener freezeListener) {
		super(serverModel, issue, field, true, freezeListener);
	}

	protected void fillCombo(final DefaultComboBoxModel comboModel,
			final JIRAServerModel serverModel, final JiraIssueAdapter issue) {

		freezeListener.freeze();
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					List<JIRAProject> projects = serverModel.getProjects(issue.getJiraServerData());
					JIRAProject issueProject = null;
					for (JIRAProject project : projects) {
						if (issue.getProjectKey().equals(project.getKey())) {
							issueProject = project;
							break;
						}
					}
					final List<JIRAConstant> issueTypes = issue.isSubTask()
							? serverModel.getSubtaskIssueTypes(issue.getJiraServerData(), issueProject)
							: serverModel.getIssueTypes(issue.getJiraServerData(), issueProject, false);
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

							freezeListener.unfreeze();
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
