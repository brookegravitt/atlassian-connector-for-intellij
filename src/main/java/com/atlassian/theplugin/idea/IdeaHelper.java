package com.atlassian.theplugin.idea;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.application.ApplicationManager;

/**
 * Simple helper methods for the IDEA plugin
 */
public final class IdeaHelper {
	private IdeaHelper() { }

	public static Project getCurrentProject(DataContext dataContext) {
        return DataKeys.PROJECT.getData(dataContext);
    }

    public static ToolWindow getToolWindow(Project p) {
        return ToolWindowManager.getInstance(p).getToolWindow(ThePluginProjectComponent.TOOL_WINDOW_NAME);
    }

    public static ThePluginApplicationComponent getAppComponent() {
        return ApplicationManager.getApplication().getComponent(ThePluginApplicationComponent.class);
    }
}
