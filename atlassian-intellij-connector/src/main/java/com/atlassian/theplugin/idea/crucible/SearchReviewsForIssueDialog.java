package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class SearchReviewsForIssueDialog extends DialogWrapper {
    private JPanel contentPane;
    private JTextField tfIssueKey;

    public SearchReviewsForIssueDialog(Project project) {
        super(project);        
		init();
		pack();
        final ServerData defaultServer = IdeaHelper.getProjectCfgManager(project).getDefaultCrucibleServer();
		setTitle("Search Crucible Reviews" + defaultServer != null ? " (" + defaultServer.getName() + ")" : "");
		getOKAction().putValue(Action.NAME, "Search");
		getOKAction().setEnabled(false);
        tfIssueKey.addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent keyEvent) {

            }

            public void keyPressed(KeyEvent keyEvent) {

            }

            public void keyReleased(KeyEvent keyEvent) {
                getOKAction().setEnabled(tfIssueKey.getText().length() > 0 && defaultServer != null);
            }
        });

//        contentPane.setRequestFocusEnabled(true);

        setOKActionEnabled(false);
		setButtonsAlignment(SwingConstants.CENTER);
        tfIssueKey.requestFocus();

    }

    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    public String getIssueKey() {
        return tfIssueKey.getText();
    }

}
