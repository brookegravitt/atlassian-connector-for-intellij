package com.atlassian.theplugin.idea.jira;

import com.atlassian.connector.commons.jira.JIRAAction;
import com.intellij.openapi.ui.DialogWrapper;

import javax.swing.*;
import java.awt.event.*;
import java.util.List;

public class IssueActionSelectorDialog extends DialogWrapper {
    private JPanel mainPanel;
    private JComboBox comboActions;

    private class JIRAActionAdapter {
        private JIRAAction action;

        private JIRAActionAdapter(JIRAAction action) {
            this.action = action;
        }

        public JIRAAction getAction() {
            return action;
        }

        @Override
        public String toString() {
            return action.getName() + " (" + action.getId() + ")";
        }
    }

    public IssueActionSelectorDialog(String issueKey, List<JIRAAction> actions) {
        super(false);
        init();
        setTitle("Select action to start work on " + issueKey);

        getOKAction().putValue(Action.NAME, "Select");
        for (JIRAAction action : actions) {
            comboActions.addItem(new JIRAActionAdapter(action));
        }
    }

    @Override
    protected JComponent createCenterPanel() {
        return mainPanel;
    }

    public JIRAAction getSelectedAction() {
        return ((JIRAActionAdapter) comboActions.getSelectedItem()).getAction();
    }
}
