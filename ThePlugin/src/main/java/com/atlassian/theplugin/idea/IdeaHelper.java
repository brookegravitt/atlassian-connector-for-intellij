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
import org.jetbrains.annotations.Nullable;

/**
 * Simple helper methods for the IDEA plugin
 */
public final class IdeaHelper {

	/**
	 * List of available panels in tool window
	 */
	public enum ToolWindowPanels {
		BAMBOO {
			public String toString() {
				return "Bamboo";
			}
		},
		CRUCIBLE {
			public String toString() {
				return "Crucible";
			}
		},
		JIRA {
			public String toString() {
				return "JIRA";
			}
		}
	}

    public static final String TOOL_WINDOW_NAME = "Atlassian";

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

    public static com.intellij.openapi.wm.ToolWindow getToolWindow(Project p) {
        return ToolWindowManager.getInstance(p).getToolWindow(TOOL_WINDOW_NAME);
    }

    public static ThePluginApplicationComponent getAppComponent() {
        return ApplicationManager.getApplication().getComponent(ThePluginApplicationComponent.class);
    }

    public static JIRAToolWindowPanel getJIRAToolWindowPanel(AnActionEvent event) {
        Project p = getCurrentProject(event.getDataContext());
        com.intellij.openapi.wm.ToolWindow tw = getToolWindow(p);
        Content content = tw.getContentManager().findContent(ToolWindowPanels.JIRA.toString());
        return (JIRAToolWindowPanel) content.getComponent();
    }

    // simple method to open the ToolWindow and focus on a particular component
    public static void focusPanel(Project project, ToolWindowPanels component) {
        ToolWindow tw = getToolWindow(project);
        if (tw != null) {
            ContentManager contentManager = tw.getContentManager();
            tw.activate(null);
			Content content = contentManager.findContent(component.toString());

			if (content == null) {
				switch (component) {
					case BAMBOO:
						content = project.getComponent(ThePluginProjectComponent.class).createBambooContent();
						contentManager.addContent(content);
						break;
					case CRUCIBLE:
						break;
					case JIRA:
						break;
					default:
						break;
				}
			}
			
			contentManager.setSelectedContent(content);
        }
    }

    public static void focusPanel(AnActionEvent e, ToolWindowPanels component) {
		Project project = getCurrentProject(e.getDataContext());
		focusPanel(project, component);
    }
}
