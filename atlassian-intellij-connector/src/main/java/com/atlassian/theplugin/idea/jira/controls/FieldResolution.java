package com.atlassian.theplugin.idea.jira.controls;

import com.atlassian.theplugin.jira.api.JIRAActionField;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.api.JIRAResolutionBean;
import com.atlassian.theplugin.jira.model.JIRAServerModel;
import com.atlassian.theplugin.util.PluginUtil;

import javax.swing.*;
import java.util.List;

/**
 * User: jgorycki
 * Date: Apr 6, 2009
 * Time: 11:41:06 AM
 */
public class FieldResolution extends AbstractFieldComboBox {

	public FieldResolution(final JIRAServerModel serverModel, final JIRAIssue issue, final JIRAActionField field) {
		super(serverModel, issue, field, false);
	}

	protected void fillCombo(final DefaultComboBoxModel comboModel, final JIRAServerModel serverModel, final JIRAIssue issue) {
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					final List<JIRAResolutionBean> resolutions = serverModel.getResolutions(issue.getServer(), false);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							comboModel.removeAllElements();
							JIRAResolutionBean selected = null;
							for (JIRAResolutionBean res : resolutions) {
								comboModel.addElement(res);
								if (issue.getResolution().equals(res.getName())) {
									selected = res;
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
