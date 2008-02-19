/*
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 16/03/2004
 * Time: 21:00:20
 */
package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.bamboo.HtmlBambooStatusListener;
import com.atlassian.theplugin.idea.GenericHyperlinkListener;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.bamboo.BuildStatusChangedToolTip;
import com.atlassian.theplugin.jira.JIRAServer;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.JIRAServerFactory;
import com.atlassian.theplugin.jira.api.*;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.Nullable;
import thirdparty.javaworld.ClasspathHTMLEditorKit;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class IssueCreate extends DialogWrapper {
    private static final Logger LOGGER = Logger.getInstance("IssueCreate");

    private JPanel mainPanel;
    private JTextArea description;
    private JComboBox projectComboBox;
    private JComboBox typeComboBox;
    private JTextField summary;
    private JIRAServer jiraServer;

    public IssueCreate(final JIRAServer jiraServer) {
        super(false);
        init();
        this.jiraServer = jiraServer;
        setTitle("Create JIRA Issue");

        projectComboBox.setRenderer(new ColoredListCellRenderer() {
            protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
                append(((JIRAProject) value).getName(), SimpleTextAttributes.SIMPLE_CELL_ATTRIBUTES);
            }
        });

        typeComboBox.setRenderer(new ColoredListCellRenderer() {
            protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
                JIRAConstant type = (JIRAConstant) value;
                append(type.getName(), SimpleTextAttributes.SIMPLE_CELL_ATTRIBUTES);
                setIcon(new ImageIcon(type.getIconUrl()));
            }
        });

        for (JIRAProject project : jiraServer.getProjects()) {
            projectComboBox.addItem(project);
        }

        for (JIRAConstant constant : jiraServer.getIssueTypes()) {
            typeComboBox.addItem(constant);
        }

        getOKAction().putValue(Action.NAME, "Create");
    }

    protected void doOKAction() {
        JIRAServerFacade facade = JIRAServerFactory.getJIRAServerFacade();
        JIRAIssueBean issueProxy = new JIRAIssueBean();
        issueProxy.setSummary(summary.getText());
        issueProxy.setProjectKey(((JIRAProject) projectComboBox.getSelectedItem()).getKey());
        issueProxy.setType(((JIRAConstant) typeComboBox.getSelectedItem()));
        issueProxy.setDescription(description.getText());
        JEditorPane content = new JEditorPane();

        JIRAIssue newIssue = null;

        String message = null;
        try {
            newIssue = facade.createIssue(jiraServer.getServer(), issueProxy);
        } catch (JIRAException e) {
            message = "Issue created failed?<br>" + e.getMessage();
            content.setBackground(BuildStatusChangedToolTip.BACKGROUND_COLOR_FAILED);
            e.printStackTrace();
        }

        if (newIssue != null) {
            message = "<table width=100% height=100%><tr><td valign=center align=center>"
                    + "<b style=\"font-size: 24pt;\"><a href='" + newIssue.getIssueUrl() + "'>"
                    + newIssue.getKey() + "</a><br>created</b></td></tr></table>";
            content.setBackground(BuildStatusChangedToolTip.BACKGROUND_COLOR_SUCCEED);
        }

        final JIRAIssue innerIssue = newIssue;

        content.setEditable(false);
        content.setContentType("text/html");
        content.setEditorKit(new ClasspathHTMLEditorKit());
        content.setText("<html>" + HtmlBambooStatusListener.BODY_WITH_STYLE + message + "</body></html>");
        content.addHyperlinkListener(new GenericHyperlinkListener());
        content.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                BrowserUtil.launchBrowser(innerIssue.getIssueUrl());
            }
        });
        content.setCaretPosition(0); // do thi to make sure scroll pane is always at the top / header
        WindowManager.getInstance().getStatusBar(IdeaHelper.getCurrentProject()).fireNotificationPopup(
                new JScrollPane(content), null);

        super.doOKAction();
    }

    public JComponent getPreferredFocusedComponent() {
        return summary;
    }

    @Nullable
    protected JComponent createCenterPanel() {
        return mainPanel;
    }
}