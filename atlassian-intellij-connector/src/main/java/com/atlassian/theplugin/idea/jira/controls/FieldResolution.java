package com.atlassian.theplugin.idea.jira.controls;

import com.atlassian.connector.commons.jira.JIRAActionField;
import com.atlassian.connector.commons.jira.beans.JIRAResolutionBean;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.commons.jira.cache.JIRAServerModel;
import com.atlassian.theplugin.util.PluginUtil;

import javax.swing.*;
import java.util.List;

/**
 * User: jgorycki
 * Date: Apr 6, 2009
 * Time: 11:41:06 AM
 */
public class FieldResolution extends AbstractFieldComboBox {

	public FieldResolution(final JIRAServerModel serverModel, final JiraIssueAdapter issue, final JIRAActionField field,
			final FreezeListener freezeListener) {
		super(serverModel, issue, field, false, freezeListener);
	}

	protected void fillCombo(final DefaultComboBoxModel comboModel, final JIRAServerModel serverModel,
                             final JiraIssueAdapter issue) {
		freezeListener.freeze();
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					final List<JIRAResolutionBean> resolutions = serverModel.getResolutions(issue.getJiraServerData(), false);
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
