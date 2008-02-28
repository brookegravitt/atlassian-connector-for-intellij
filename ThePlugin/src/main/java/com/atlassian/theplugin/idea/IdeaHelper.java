package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.idea.jira.JIRAToolWindowPanel;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;

/**
 * Simple helper methods for the IDEA plugin
 */
public final class IdeaHelper {
    public static final String TOOLWINDOW_PANEL_JIRA = "JIRA";
    public static final String TOOLWINDOW_PANEL_BAMBOO = "Bamboo";
    public static final String TOOLWINDOW_PANEL_CRUCIBLE = "Crucible";
    public static final String TOOL_WINDOW_NAME = "Atlassian";

    private IdeaHelper() {
    }

    public static Project getCurrentProject() {
		return getCurrentProject(DataManager.getInstance().getDataContext());
    }

    public static Project getCurrentProject(DataContext dataContext) {
        return DataKeys.PROJECT.getData(dataContext);
    }

    public static ToolWindow getToolWindow() {
        return getToolWindow(getCurrentProject());
    }

    public static ToolWindow getToolWindow(Project p) {
        return ToolWindowManager.getInstance(p).getToolWindow(TOOL_WINDOW_NAME);
    }

    public static ThePluginApplicationComponent getAppComponent() {
        return ApplicationManager.getApplication().getComponent(ThePluginApplicationComponent.class);
    }

    public static JIRAToolWindowPanel getJIRAToolWindowPanel(AnActionEvent event) {
        Project p = getCurrentProject(event.getDataContext());
        ToolWindow tw = getToolWindow(p);
        Content content = tw.getContentManager().findContent(TOOLWINDOW_PANEL_JIRA);
        return (JIRAToolWindowPanel) content.getComponent();
    }

    // simple method to open the ToolWindow and focus on a particular component
    public static void focusPanel(String componentName) {
        ToolWindow tw = getToolWindow(getCurrentProject());
        if (tw != null) {
            ContentManager contentManager = tw.getContentManager();
            tw.activate(null);
            contentManager.setSelectedContent(contentManager.findContent(componentName));
        }
    }

    public static void focusPanel(AnActionEvent e, String componentName) {
        ToolWindow tw = getToolWindow(getCurrentProject(e.getDataContext()));
        if (tw != null) {
            ContentManager contentManager = tw.getContentManager();
            tw.activate(null);
            contentManager.setSelectedContent(contentManager.findContent(componentName));
        }
    }
}
