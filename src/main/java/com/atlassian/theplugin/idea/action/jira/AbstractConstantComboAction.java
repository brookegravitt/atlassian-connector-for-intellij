package com.atlassian.theplugin.idea.action.jira;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ThePluginApplicationComponent;
import com.atlassian.theplugin.jira.JIRAServer;
import com.atlassian.theplugin.jira.api.JIRAConstant;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractConstantComboAction extends ComboBoxAction {
    @NotNull
    protected DefaultActionGroup createPopupActionGroup(JComponent jComponent) {
        final ComboBoxButton comboBox;
        if (!(jComponent instanceof ComboBoxButton)) {
            throw new UnsupportedOperationException("This action can only be used as a combobox");
        }

        comboBox = (ComboBoxButton) jComponent;
        DefaultActionGroup group = new DefaultActionGroup();

        group.add(makeAction(comboBox, null));
        ThePluginApplicationComponent app = IdeaHelper.getAppComponent();
        JIRAServer server = app.getCurrentJIRAServer();
        for (Iterator iterator = getValues(server).iterator(); iterator.hasNext();) {
            JIRAConstant constant = (JIRAConstant) iterator.next();
            group.add(makeAction(comboBox, constant));
        }
        return group;
    }

    protected abstract List getValues(JIRAServer server);

    protected abstract String getDefaultText();

    private AnAction makeAction(final ComboBoxButton comboBox, final JIRAConstant constant) {
        return new AnAction(
                (constant != null ? constant.getName() : getDefaultText()),
                null,
                (constant != null && constant.getIconUrl() != null ? new ImageIcon(constant.getIconUrl()) : null)
        ) {
            public void actionPerformed(AnActionEvent event) {
                comboBox.setText(event.getPresentation().getText());
                if (constant.getIconUrl() != null) {
                    comboBox.setIcon(new ImageIcon(constant.getIconUrl()));
                }
                IdeaHelper.getJIRAToolWindowPanel(event).addQueryFragment(getDefaultText(), constant);
                IdeaHelper.getJIRAToolWindowPanel(event).refreshIssues();
            }
        };
    }
}
