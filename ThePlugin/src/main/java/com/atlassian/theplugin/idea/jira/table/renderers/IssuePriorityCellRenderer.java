package com.atlassian.theplugin.idea.jira.table.renderers;

import com.atlassian.theplugin.idea.jira.JiraIssueAdapter;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;


public class IssuePriorityCellRenderer extends DefaultTableCellRenderer {
	public Component getTableCellRendererComponent(JTable jTable,
												   Object o, boolean isSelected, boolean hasFocus, int i, int i1) {
		Component c = super.getTableCellRendererComponent(jTable, o, isSelected, hasFocus, i, i1);
		if (o instanceof JiraIssueAdapter) {
			JiraIssueAdapter issueAdapter = (JiraIssueAdapter) o;
			if (issueAdapter.getPriorityIcon() != null) {
				((JLabel) c).setToolTipText(issueAdapter.getPriority());
				if (issueAdapter.isUseIconDescription()) {
					((JLabel) c).setText(issueAdapter.getPriority());
				} else {
					((JLabel) c).setText("");
				}
				((JLabel) c).setIcon(issueAdapter.getPriorityIcon());
			} else {
				((JLabel) c).setToolTipText(issueAdapter.getPriority());
				((JLabel) c).setText(issueAdapter.getPriority());
				((JLabel) c).setIcon(null);
			}
		}
		return c;
	}
}