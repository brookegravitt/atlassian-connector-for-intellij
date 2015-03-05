package com.atlassian.theplugin.idea.jira;

import com.intellij.ui.HyperlinkLabel;

import javax.swing.*;
import javax.swing.event.HyperlinkListener;

/**
 * User: pmaruszak
 */
public class StatusBarIssuesPane extends StatusBarPane {

    private HyperlinkLabel getMoreIssues;


    public StatusBarIssuesPane(String initialText) {
        super(initialText);

        getMoreIssues = new HyperlinkLabel("Get More Issues...");
        getMoreIssues.setOpaque(false);

        enableGetMoreIssues(false);

        addComponent(getMoreIssues);
    }

    public void addMoreIssuesListener(HyperlinkListener listener) {
        getMoreIssues.addHyperlinkListener(listener);
    }

    public void removeMoreListener(HyperlinkListener listener) {
        getMoreIssues.removeHyperlinkListener(listener);
    }

    public void enableGetMoreIssues(final boolean enable) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (enable) {
                    getMoreIssues.setVisible(true);
                } else {
                    getMoreIssues.setVisible(false);
                }
            }
        });

    }
}
