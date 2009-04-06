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
package com.atlassian.theplugin.idea.jira.controls;

import com.atlassian.theplugin.jira.api.JIRAActionField;
import com.atlassian.theplugin.jira.api.JIRAConstant;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.model.JIRAServerModel;
import com.atlassian.theplugin.util.PluginUtil;

import javax.swing.*;
import java.util.List;

/**
 * @author Jacek Jaroczynski
 */
public class FieldPriority extends AbstractFieldComboBox {
	public FieldPriority(final JIRAServerModel jiraServerModel, final JIRAIssue issue, final JIRAActionField field) {
		super(jiraServerModel, issue, field);
	}

	protected void fillCombo(final DefaultComboBoxModel comboModel, final JIRAServerModel serverModel, final JIRAIssue issue) {
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					final List<JIRAConstant> priorities = serverModel.getPriorities(issue.getServer());

					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							comboModel.removeAllElements();
							JIRAConstant selected = null;
							for (JIRAConstant type : priorities) {
								comboModel.addElement(type);
								if (issue.getPriority().equals(type.getName())) {
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
