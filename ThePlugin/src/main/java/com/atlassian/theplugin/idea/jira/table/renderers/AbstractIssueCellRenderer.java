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