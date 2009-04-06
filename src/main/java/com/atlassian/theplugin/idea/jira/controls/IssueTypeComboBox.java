package com.atlassian.theplugin.idea.jira.controls;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.idea.jira.CachedIconLoader;
import com.atlassian.theplugin.jira.api.*;
import com.atlassian.theplugin.jira.model.JIRAServerModel;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * User: jgorycki
 * Date: Apr 6, 2009
 * Time: 12:32:54 PM
 */
public class IssueTypeComboBox extends JComboBox implements ActionFieldEditor{
	private DefaultComboBoxModel model;
	private boolean initialized;

	public IssueTypeComboBox(final JIRAServerModel serverModel, final JIRAIssue issue, final JiraServerCfg server) {
		model = new DefaultComboBoxModel();
		model.addElement("Fetching...");
		initialized = false;
		setModel(model);
		setEditable(false);
		setEnabled(false);
		setRenderer(new ListCellRenderer() {
			public Component getListCellRendererComponent(JList list, Object value, int index,
														  boolean isSelected, boolean cellHasFocus) {
				if (!initialized) {
					return new JLabel(value.toString());
				}
				JIRAConstant type = (JIRAIssueTypeBean) value;
				JLabel l = new JLabel(type.getName());
				l.setIcon(CachedIconLoader.getIcon(type.getIconUrl()));
				if (isSelected) {
					l.setForeground(UIUtil.getListSelectionForeground());
					l.setBackground(UIUtil.getListSelectionBackground());
				}
				return l;
			}
		});
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					List<JIRAProject> projects = serverModel.getProjects(server);
					JIRAProject issueProject = null;
					for (JIRAProject project : projects) {
						if (issue.getProjectKey().equals(project.getKey())) {
							issueProject = project;
							break;
						}
					}
					final List<JIRAConstant> issueTypes = serverModel.getIssueTypes(server, issueProject);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							model.removeAllElements();
							for (JIRAConstant type : issueTypes) {
								model.addElement(type);
							}
							initialized = true;
							setEnabled(true);
							setSelectedIndex(0);
						}
					});
				} catch (JIRAException e) {
					PluginUtil.getLogger().error(e.getMessage());
				}
			}
		});
		t.start();
	}

	public JIRAActionField getEditedFieldValue(JIRAActionField field) {
		if (!initialized) {
			return null;
		}
		JIRAConstant type = (JIRAConstant) getSelectedItem();
		field.addValue(Long.valueOf(type.getId()).toString());
		return field;
	}

}
