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
 * Time: 11:41:06 AM
 */
public class IssueResolutionComboBox extends JComboBox implements ActionFieldEditor {
	private DefaultComboBoxModel model;
	private boolean initialized;
	private JIRAActionField field;

	public IssueResolutionComboBox(final JIRAServerModel serverModel, final JIRAIssue issue, final JIRAActionField field) {
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
					final List<JIRAResolutionBean> resolutions = serverModel.getResolutions(issue.getServer(), false);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							model.removeAllElements();
							JIRAResolutionBean selected = null;
							for (JIRAResolutionBean res : resolutions) {
								model.addElement(res);
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

	public JIRAActionField getEditedFieldValue() {
		if (!initialized) {
			return null;
		}
		JIRAResolutionBean res = (JIRAResolutionBean) getSelectedItem();
		JIRAActionField ret = new JIRAActionFieldBean(field);
		ret.addValue(Long.valueOf(res.getId()).toString());
		return ret;
	}

	public Component getComponent() {
		return this;
	}
}
