package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.ServerType;
import com.atlassian.theplugin.configuration.ConfigurationFactory;
import com.atlassian.theplugin.exception.ThePluginException;
import com.atlassian.theplugin.util.PluginUtil;
import com.atlassian.theplugin.util.UrlUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.content.ContentManagerAdapter;
import com.intellij.ui.content.ContentManagerEvent;
import com.intellij.util.containers.HashSet;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-03-20
 * Time: 11:22:10
 * To change this template use File | Settings | File Templates.
 */
public class PluginToolWindow extends ContentManagerAdapter {

	private Set<ToolWindowPanels> panels = new HashSet<ToolWindowPanels>(INITIAL_NUMBER_OF_TABS);

	private ToolWindow ideaToolWindow;
	private Project project;
	//private String selectedContent = null;
	public static final String TOOL_WINDOW_NAME = "Atlassian";
	private static final int INITIAL_NUMBER_OF_TABS = 3;

	/**
	 *
	 * @param toolWindowManager ToolWindowManager object
	 * @param project reference to the project
	 */
	public PluginToolWindow(ToolWindowManager toolWindowManager, Project project) {
		this.ideaToolWindow = toolWindowManager.registerToolWindow(
				TOOL_WINDOW_NAME, true, ToolWindowAnchor.RIGHT);
		this.project = project;
	}

	/**
	 * Starts listening to the tab selection changes.
	 * It will fire {@link #selectionChanged} which store new selection in project configuration
	 */
	public void startTabChangeListener() {
		this.ideaToolWindow.getContentManager().addContentManagerListener(this);
	}

	/**
	 * Stops listening to the tab selection changes.
	 */
	public void stopTabChangeListener() {
		this.ideaToolWindow.getContentManager().removeContentManagerListener(this);
	}

	/**
	 * @return {@link ToolWindow} objects wraped up by this class
	 */
	public ToolWindow getIdeaToolWindow() {
		return ideaToolWindow;
	}

	/**
	 * Register type of panel. Register panel can be then shown/hidden using {@link #showHidePanels}
	 * @param toolWindowPanel ToolWindowPanels enum value
	 */
	public void registerPanel(ToolWindowPanels toolWindowPanel) {
		panels.add(toolWindowPanel);
	}

	/**
	 * Show registered panels if servers are defined for the type of panel.
	 * Hides registered panels if servers are not define for the type of panel.
	 */
	public void showHidePanels() {

		//stopTabChangeListener();

		for (ToolWindowPanels entry : panels) {
			try {
				ServerType serverType = UrlUtil.toolWindowPanelsToServerType(entry);

				// servers are defined
				if (ConfigurationFactory.getConfiguration().getProductServers(serverType).getServers().size() > 0) {
					// tab is not visible
					if (ideaToolWindow.getContentManager().findContent(entry.toString()) == null) {

						// show tab
						Content content = null;

						switch (entry) {
							case BAMBOO:
								content = project.getComponent(ThePluginProjectComponent.class).createBambooContent();
								break;
							case CRUCIBLE:
								content = project.getComponent(ThePluginProjectComponent.class).createCrusibleContent();
								break;
							case JIRA:
								content = project.getComponent(ThePluginProjectComponent.class).createJiraContent();
								break;
							default:
								break;
						}

						ideaToolWindow.getContentManager().addContent(content);
					}
				// servers are not defined
				} else {
					// tab is visible
					Content content = ideaToolWindow.getContentManager().findContent(entry.toString());
					if (content != null) {
						// hide tab
						ideaToolWindow.getContentManager().removeContent(content, true);
					}
				}
			} catch (ThePluginException e) {
				PluginUtil.getLogger().error(e.getMessage(), e);
			}
		}

		//startTabChangeListener();
	}

	// simple method to open the ToolWindow and focus on a particular component
    public static void focusPanel(Project project, ToolWindowPanels component) {
        ToolWindow tw = IdeaHelper.getToolWindow(project);
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
						content = project.getComponent(ThePluginProjectComponent.class).createCrusibleContent();
						contentManager.addContent(content);
						break;
					case JIRA:
						content = project.getComponent(ThePluginProjectComponent.class).createJiraContent();
						contentManager.addContent(content);
						break;
					default:
						break;
				}
			}

			contentManager.setSelectedContent(content);
        }
    }

	public static void focusPanel(Project project, String activeToolWindowTab) {
		if (activeToolWindowTab.equals(ToolWindowPanels.BAMBOO.toString())) {
			focusPanel(project, ToolWindowPanels.BAMBOO);
		} else if (activeToolWindowTab.equals(ToolWindowPanels.CRUCIBLE.toString())) {
			focusPanel(project, ToolWindowPanels.CRUCIBLE);
		} else if (activeToolWindowTab.equals(ToolWindowPanels.JIRA.toString())) {
			focusPanel(project, ToolWindowPanels.JIRA);
		}
	}

	public static void focusPanel(AnActionEvent e, ToolWindowPanels component) {
		Project project = IdeaHelper.getCurrentProject(e.getDataContext());
		focusPanel(project, component);
    }


	public void selectionChanged(ContentManagerEvent event) {
		//this.selectedContent = event.getContent().getDisplayName();

		project.getComponent(ThePluginProjectComponent.class).getProjectConfigurationBean().
				setActiveToolWindowTab(event.getContent().getDisplayName());
	}

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
}
