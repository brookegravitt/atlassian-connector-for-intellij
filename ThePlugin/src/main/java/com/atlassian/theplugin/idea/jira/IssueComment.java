/*
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 16/03/2004
 * Time: 21:00:20
 */
package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.jira.JIRAServer;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.JIRAServerFactory;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class IssueComment extends DialogWrapper {
    private static final Logger LOGGER = Logger.getInstance("IssueComment");

    private JPanel mainPanel;
    private JTextArea comment;
    private JComboBox issueComboBox;
    private JIRAServer jiraServer;
    private static final int MAX_SUM_LENGTH = 53;
    private static final int MAX_SUM_LENGTH_MINUS_ELLIPSIS = 50;

    public IssueComment(final JIRAServer jiraServer, List<JIRAIssue> issues) {
        super(false);
        init();
        this.jiraServer = jiraServer;
        setTitle("Add Comment");
        issueComboBox.setRenderer(new ColoredListCellRenderer() {
            protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
                JIRAIssue issue = (JIRAIssue) value;
                String summary = issue.getSummary();
                String text = issue.getKey() + " : "
                        + (summary.length() > MAX_SUM_LENGTH ? summary.substring(0, MAX_SUM_LENGTH_MINUS_ELLIPSIS)
                        + "..." : summary);
                append(text, SimpleTextAttributes.SIMPLE_CELL_ATTRIBUTES);
                setIcon(new ImageIcon(issue.getTypeIconUrl()));
            }
        });

        for (JIRAIssue issue : issues) {
            issueComboBox.addItem(issue);
        }

        getOKAction().putValue(Action.NAME, "Comment");
    }

    public void setIssue(JIRAIssue issue) {
        setTitle("Add Comment: " + issue.getKey());
        issueComboBox.setSelectedItem(issue);
    }

    public JIRAIssue getIssue() {
        return (JIRAIssue) issueComboBox.getSelectedItem();
    }

    protected void doOKAction() {
        try {
            JIRAServerFacade facade = JIRAServerFactory.getJIRAServerFacade();
            facade.addComment(jiraServer.getServer(), getIssue(), comment.getText());
        } catch (JIRAException e1) {
            e1.printStackTrace();
        }

        super.doOKAction();
    }

    public JComponent getPreferredFocusedComponent() {
        return comment;
    }

    @Nullable
    protected JComponent createCenterPanel() {
        return mainPanel;
    }
}