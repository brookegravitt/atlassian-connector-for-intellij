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

import com.atlassian.connector.commons.jira.JIRAActionField;
import com.atlassian.connector.commons.jira.beans.JIRAConstant;
import com.atlassian.connector.commons.jira.beans.JIRAPriorityBean;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.commons.jira.cache.JIRAServerModel;
import com.atlassian.theplugin.util.PluginUtil;

import javax.swing.*;
import java.util.List;

/**
 * @author Jacek Jaroczynski
 */
public class FieldPriority extends AbstractFieldComboBox {
	public FieldPriority(final JIRAServerModel jiraServerModel, final JiraIssueAdapter issue, final JIRAActionField field,
			final FreezeListener freezeListener) {
		super(jiraServerModel, issue, field, true, freezeListener);
	}

	protected void fillCombo(final DefaultComboBoxModel comboModel, final JIRAServerModel serverModel,
                             final JiraIssueAdapter issue) {
		freezeListener.freeze();
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					final List<JIRAPriorityBean> priorities = serverModel.getPriorities(issue.getJiraServerData(), false);

					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							comboModel.removeAllElements();
							JIRAConstant selected = null;
							for (JIRAConstant type : priorities) {
								comboModel.addElement(type);
								if (issue.getPriority() != null && issue.getPriority().equals(type.getName())) {
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
