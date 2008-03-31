package com.atlassian.theplugin.idea.jira.table.renderers;

import com.atlassian.theplugin.idea.jira.JiraIssueAdapter;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;


public class IssueTypeCellRenderer extends DefaultTableCellRenderer {
	public Component getTableCellRendererComponent(JTable jTable,
												   Object o, boolean isSelected, boolean hasFocus, int i, int i1) {
		Component c = super.getTableCellRendererComponent(jTable, o, isSelected, hasFocus, i, i1);
		if (o instanceof JiraIssueAdapter) {
			JiraIssueAdapter issueAdapter = (JiraIssueAdapter) o;
			if (issueAdapter.getTypeIcon() != null) {
				((JLabel) c).setToolTipText(issueAdapter.getType());
				if (issueAdapter.isUseIconDescription()) {
					((JLabel) c).setText(issueAdapter.getType());
				} else {
					((JLabel) c).setText("");					
				}
				((JLabel) c).setIcon(issueAdapter.getTypeIcon());
			} else {
				((JLabel) c).setToolTipText(issueAdapter.getType());
				((JLabel) c).setText(issueAdapter.getType());
			}
		}
		return c;
	}
}