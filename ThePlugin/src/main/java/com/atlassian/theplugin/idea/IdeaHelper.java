package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.idea.bamboo.BambooTableToolWindowPanel;
import com.atlassian.theplugin.idea.jira.JIRAToolWindowPanel;
import com.atlassian.theplugin.jira.JIRAServer;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.Nullable;

/**
 * Simple helper methods for the IDEA plugin
 */
public final class IdeaHelper {

	private IdeaHelper() {
    }

    @Nullable
	public static Project getCurrentProject() {
		return getCurrentProject(DataManager.getInstance().getDataContext());
    }

	@Nullable
	public static Project getCurrentProject(DataContext dataContext) {
        return DataKeys.PROJECT.getData(dataContext);
    }

    @Nullable
	public static JIRAServer getCurrentJIRAServer() {
		Project p = getCurrentProject(DataManager.getInstance().getDataContext());
		if (p == null) {
			return null;
		}
		return p.getComponent(ThePluginProjectComponent.class).getCurrentJiraServer();
    }

    @Nullable
	public static void setCurrentJIRAServer(JIRAServer jiraServer) {
		Project p = getCurrentProject(DataManager.getInstance().getDataContext());
		if (p == null) {
			return;
		}
		p.getComponent(ThePluginProjectComponent.class).setCurrentJiraServer(jiraServer);
    }

	public static com.intellij.openapi.wm.ToolWindow getToolWindow(Project p) {
        return ToolWindowManager.getInstance(p).getToolWindow(PluginToolWindow.TOOL_WINDOW_NAME);
    }

    public static ThePluginApplicationComponent getAppComponent() {
        return ApplicationManager.getApplication().getComponent(ThePluginApplicationComponent.class);
    }

	public static JIRAToolWindowPanel getCurrentJIRAToolWindowPanel(){
		Project p = getCurrentProject();
		if (p == null) {
			return null;
		}
		com.intellij.openapi.wm.ToolWindow tw = getToolWindow(p);
        Content content = tw.getContentManager().findContent(PluginToolWindow.ToolWindowPanels.JIRA.toString());
        return (JIRAToolWindowPanel) content.getComponent();
	}

	public static JIRAToolWindowPanel getJIRAToolWindowPanel(AnActionEvent event) {
        Project p = getCurrentProject(event.getDataContext());
		if (p == null) {
			return null;
		}
		com.intellij.openapi.wm.ToolWindow tw = getToolWindow(p);
        Content content = tw.getContentManager().findContent(PluginToolWindow.ToolWindowPanels.JIRA.toString());
        return (JIRAToolWindowPanel) content.getComponent();
    }

	public static BambooTableToolWindowPanel getBambooToolWindowPanel(AnActionEvent event) {
		Project p = getCurrentProject(event.getDataContext());
		if (p == null) {
			return null;
		}
		com.intellij.openapi.wm.ToolWindow tw = getToolWindow(p);
		Content content = tw.getContentManager().findContent(PluginToolWindow.ToolWindowPanels.BAMBOO.toString());
		return (BambooTableToolWindowPanel) content.getComponent();
	}


}
