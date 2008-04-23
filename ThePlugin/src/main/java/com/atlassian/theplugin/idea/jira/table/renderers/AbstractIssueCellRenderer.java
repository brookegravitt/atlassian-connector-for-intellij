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

package com.atlassian.theplugin.idea.jira.table.renderers;

import com.atlassian.theplugin.idea.jira.JiraIcon;
import com.atlassian.theplugin.idea.jira.JiraIssueAdapter;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;


public abstract class AbstractIssueCellRenderer extends DefaultTableCellRenderer {
	protected abstract JiraIcon getJiraIcon(JiraIssueAdapter adapter);

	public Component getTableCellRendererComponent(JTable jTable,
												   Object o, boolean isSelected, boolean hasFocus, int i, int i1) {
		Component c = super.getTableCellRendererComponent(jTable, o, isSelected, hasFocus, i, i1);
		if (o instanceof JiraIssueAdapter) {
			JiraIssueAdapter issueAdapter = (JiraIssueAdapter) o;
			JiraIcon jiraIcon = getJiraIcon(issueAdapter);
			if (jiraIcon.getIcon() != null) {
				((JLabel) c).setToolTipText(jiraIcon.getText());
				if (issueAdapter.isUseIconDescription()) {
					((JLabel) c).setText(jiraIcon.getText());
				} else {
					((JLabel) c).setText("");
				}
				((JLabel) c).setIcon(jiraIcon.getIcon());
			} else {
				((JLabel) c).setToolTipText(jiraIcon.getText());
				((JLabel) c).setText(jiraIcon.getText());
				((JLabel) c).setIcon(null);
			}
		}
		return c;
	}
}