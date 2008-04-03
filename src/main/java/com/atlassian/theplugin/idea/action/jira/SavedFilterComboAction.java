package com.atlassian.theplugin.idea.action.jira;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.jira.JIRAServer;
import com.atlassian.theplugin.jira.api.JIRASavedFilter;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Iterator;

public class SavedFilterComboAction extends ComboBoxAction {
    public static final String QF_NAME = "SavedFilter";	

	@NotNull
    protected DefaultActionGroup createPopupActionGroup(JComponent jComponent) {
        final ComboBoxButton comboBox;
        if (!(jComponent instanceof ComboBoxButton)) {
            throw new UnsupportedOperationException("This action can only be used as a combobox");
        }

        comboBox = (ComboBoxButton) jComponent;
        DefaultActionGroup group = new DefaultActionGroup();

        group.add(new MyAnAction(null, comboBox, this));

        JIRAServer server = IdeaHelper.getCurrentJIRAServer();
        for (Iterator iterator = server.getSavedFilters().iterator(); iterator.hasNext();) {
            JIRASavedFilter filter = (JIRASavedFilter) iterator.next();
            group.add(new MyAnAction(filter, comboBox, this));
        }
        return group;
    }

    private String getDefaultText() {
        return "Select saved filter";
    }

    private static class MyAnAction extends AnAction {
        private final JIRASavedFilter savedFilter;
        private final ComboBoxButton comboBox;

        public MyAnAction(JIRASavedFilter savedFilter, ComboBoxButton comboBox, SavedFilterComboAction parent) {
            super((savedFilter != null ? savedFilter.getName() : parent.getDefaultText()));
            this.savedFilter = savedFilter;
            this.comboBox = comboBox;
        }

        public void actionPerformed(AnActionEvent event) {
            comboBox.setText(event.getPresentation().getText());
            IdeaHelper.getJIRAToolWindowPanel(event).addQueryFragment(QF_NAME, savedFilter);
			IdeaHelper.getJIRAToolWindowPanel(event).refreshIssues();			
		}
    }
}