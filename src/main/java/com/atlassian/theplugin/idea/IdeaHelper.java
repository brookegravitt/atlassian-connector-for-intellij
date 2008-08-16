/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.idea.bamboo.BambooTableToolWindowPanel;
import com.atlassian.theplugin.idea.crucible.CrucibleTableToolWindowPanel;
import com.atlassian.theplugin.idea.crucible.comments.ReviewActionEventBroker;
import com.atlassian.theplugin.idea.jira.JIRAToolWindowPanel;
import com.atlassian.theplugin.jira.JIRAServer;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Simple helper methods for the IDEA plugin
 */
public final class IdeaHelper {

	private IdeaHelper() {
	}

	@Nullable
	public static Project getCurrentProject(DataContext dataContext) {
		return DataKeys.PROJECT.getData(dataContext);
	}

	@Nullable
	public static Project getCurrentProject(AnActionEvent e) {
		return getCurrentProject(e.getDataContext());
	}

	@Nullable
	public static JIRAServer getCurrentJIRAServer() {
		Project p = getCurrentProject(DataManager.getInstance().getDataContext());
		if (p == null) {
			return null;
		}
		return p.getComponent(ThePluginProjectComponent.class).getCurrentJiraServer();
	}

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

	public static CfgManager getCfgManager() {
		return ApplicationManager.getApplication().getComponent(CfgManager.class);
	}

	public static PluginConfigurationBean getPluginConfiguration() {
		return getAppComponent().getState();
	}
  
	public static JIRAToolWindowPanel getJIRAToolWindowPanel(Project p) {
		if (p == null) {
			return null;
		}

		com.intellij.openapi.wm.ToolWindow tw = getToolWindow(p);
		Content content = tw.getContentManager().findContent(PluginToolWindow.ToolWindowPanels.JIRA.toString());
        if (content == null) {
            return null;
        }
        return (JIRAToolWindowPanel) content.getComponent();
	}
	public static JIRAToolWindowPanel getJIRAToolWindowPanel(AnActionEvent event) {
		Project p = getCurrentProject(event.getDataContext());
		if (p == null) {
			return null;
		}
		com.intellij.openapi.wm.ToolWindow tw = getToolWindow(p);
		Content content = tw.getContentManager().findContent(PluginToolWindow.ToolWindowPanels.JIRA.toString());
        if (content == null) {
            return null;
        }
        return (JIRAToolWindowPanel) content.getComponent();

	}


	public static BambooTableToolWindowPanel getBambooToolWindowPanel(AnActionEvent event) {
		Project p = getCurrentProject(event.getDataContext());
		if (p == null) {
			return null;
		}

		ToolWindow tw = getToolWindow(p);
		Content content = tw.getContentManager().findContent(PluginToolWindow.ToolWindowPanels.BAMBOO.toString());
        if (content == null) {
            return null;
        }
        return (BambooTableToolWindowPanel) content.getComponent();
	}

	public static ThePluginProjectComponent getCurrentProjectComponent(AnActionEvent e) {
		Project project = getCurrentProject(e.getDataContext());

		if (project == null) {
			return null;
		} else {
			return project.getComponent(ThePluginProjectComponent.class);
		}
	}

	

	public static CrucibleTableToolWindowPanel getCrucibleToolWindowPanel(AnActionEvent e) {
		Project p = getCurrentProject(e.getDataContext());
		if (p == null) {
			return null;
		}

		ToolWindow tw = getToolWindow(p);
		Content content = tw.getContentManager().findContent(PluginToolWindow.ToolWindowPanels.CRUCIBLE.toString());
        if (content == null)  {
            return null;
        }
        return (CrucibleTableToolWindowPanel) content.getComponent();
	}

	public static ReviewActionEventBroker getReviewActionEventBroker(Project project) {
		return project.getUserData(ThePluginProjectComponent.BROKER_KEY);
	}

    public static void handleRemoteApiException(final Project project, final RemoteApiException e) {
		ApplicationManager.getApplication().invokeLater(new Runnable() {
			public void run() {
				Messages.showErrorDialog(project, "The following error has occurred while using remote service:\n"
						+ e.getMessage(), "Error while using remote service");
			}
		});
    }

    /**
     * Placeholder for handling missing password. Dummy at the moment
     * s 
     * @param e exception to handle
     * @return true if called should retry the action which caused this excepction, false if it does not make sense
     *              for example, the user has not provided a new password
     */
    public static boolean handleMissingPassword(ServerPasswordNotProvidedException e) {
        return false;
    }

	/**
	 * Returns current project for given jComponent (the only known way to find out current project
	 * project in ComboBoxAction)
	 * @param jComponent component as passed to
	 *      {@link com.intellij.openapi.actionSystem.ex.ComboBoxAction#createPopupActionGroup(javax.swing.JComponent)}
	 *
	 * @return current project or null
	 */
	@Nullable
	public static Project getCurrentProject(JComponent jComponent) {
		return DataKeys.PROJECT.getData(DataManager.getInstance().getDataContext(jComponent));
	}

	public static <T> T getProjectComponent(final AnActionEvent event, final Class<T> clazz) {
		final Project project = getCurrentProject(event);
		if (project == null) {
			return null;
		}
		return getProjectComponent(project, clazz);
	}

	public static <T> T getProjectComponent(final Project project, final Class<T> clazz) {
		return clazz.cast(project.getPicoContainer().getComponentInstanceOfType(clazz));
	}
}
