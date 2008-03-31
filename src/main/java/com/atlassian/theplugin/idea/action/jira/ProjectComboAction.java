package com.atlassian.theplugin.idea.action.jira;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.jira.JIRAServer;
import com.atlassian.theplugin.jira.api.JIRAProject;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Iterator;

public class ProjectComboAction extends ComboBoxAction {
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
        for (Iterator iterator = server.getProjects().iterator(); iterator.hasNext();) {
            JIRAProject project = (JIRAProject) iterator.next();
            group.add(new MyAnAction(project, comboBox, this));
        }
        return group;
    }

    private String getDefaultText() {
        return "  Any Project  ";
    }

    private static class MyAnAction extends AnAction {
        private final JIRAProject project;
        private final ComboBoxButton comboBox;

        public MyAnAction(JIRAProject project, ComboBoxButton comboBox, ProjectComboAction parent) {
            super((project != null ? project.getName() : parent.getDefaultText()));
            this.project = project;
            this.comboBox = comboBox;
        }

        public void actionPerformed(AnActionEvent event) {
            comboBox.setText(event.getPresentation().getText());
            IdeaHelper.getJIRAToolWindowPanel(event).addQueryFragment("project", project);
            IdeaHelper.getJIRAToolWindowPanel(event).refreshIssues();
        }
    }
}
