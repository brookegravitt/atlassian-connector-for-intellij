package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.intellij.ide.BrowserUtil;
import com.intellij.ui.HyperlinkLabel;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.table.AbstractTableModel;
import java.util.Collections;
import java.util.List;

public class JIRATableModel extends AbstractTableModel {
    private List<JIRAIssue> issues = Collections.EMPTY_LIST;
    private static final int NUM_COLUMNS = 3;

    public void setIssues(List<JIRAIssue> issues) {
        this.issues = issues;
    }

    public int getRowCount() {
        return issues.size();
    }

    public int getColumnCount() {
        return NUM_COLUMNS;
    }

    public Class<?> getColumnClass(int col) {
        if (col == 0) {
            return Icon.class;
        } else if (col == 1) {
            return JLabel.class;
        }

        return String.class;
    }

    public Object getValueAt(int row, int col) {
        final JIRAIssue issue = issues.get(row);

        switch (col) {
            case 0:
                return new ImageIcon(issue.getTypeIconUrl());
            case 1:
//                JLabel label = new JLabel(issue.getKey());
//                label.setForeground(Color.BLUE);
//                return label;
                HyperlinkLabel hyperlinkLabel = new HyperlinkLabel(issue.getKey());
                hyperlinkLabel.addHyperlinkListener(new HyperlinkListener() {
                    public void hyperlinkUpdate(HyperlinkEvent event) {
                        BrowserUtil.launchBrowser(issue.getIssueUrl());
                    }
                });
                return hyperlinkLabel;
            case 2:
                return issue.getSummary();
            default:
                return "";
        }
    }

    public String getColumnName(int col) {
        switch (col) {
            case 0:
                return "Type";
            case 1:
                return "Key";
            case 2:
                return "Summary";
            default:
                return "";
        }
    }

    public JIRAIssue getIssueAtRow(int row) {
        return issues.get(row);
    }
}
