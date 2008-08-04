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

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.exception.ThePluginException;
import com.atlassian.theplugin.util.PluginUtil;
import com.atlassian.theplugin.util.Util;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.peer.PeerFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.content.ContentManagerAdapter;
import com.intellij.ui.content.ContentManagerEvent;
import com.intellij.util.containers.HashSet;

import javax.swing.*;
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
	private Set<ToolWindowPanels> bottomPanels = new HashSet<ToolWindowPanels>(INITIAL_NUMBER_OF_BOTTOM_TABS);

	private ToolWindow ideaToolWindow;
	private Project project;
	//private String selectedContent = null;
	public static final String TOOL_WINDOW_NAME = "Atlassian";	
	private static final int INITIAL_NUMBER_OF_TABS = 3;
	private static final int INITIAL_NUMBER_OF_BOTTOM_TABS = 1;
	private static final String CONFIGURE_TAB_NAME = "Configure";
	public static final Icon ICON_CRUCIBLE = IconLoader.getIcon("/icons/crucible-16.png");


	public static void showHidePluginWindow(AnActionEvent event) {
		ToolWindow tw = IdeaHelper.getToolWindow(IdeaHelper.getCurrentProject(event.getDataContext()));
		if (tw != null) {
			if (tw.isVisible()) {
				tw.hide(new Runnable() {
					public void run() {
						//To change body of implemented methods use File | Settings | File Templates.
					}
				});
			} else {

 				tw.show(new Runnable() {
					public void run() {
						//To change body of implemented methods use File | Settings | File Templates.
					}
				});
			}

		}

	}
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

	public void registerBottomPanel(ToolWindowPanels toolWindowPanel) {
		bottomPanels.add(toolWindowPanel);
	}


	public void showHidePanels() {
		//stopTabChangeListener();

		if (!ConfigurationFactory.getConfiguration().isAnyServerDefined()) {
			// no servers defined, show config panel
			if (ideaToolWindow.getContentManager().findContent(CONFIGURE_TAB_NAME) == null) {
				Content content = PeerFactory.getInstance().getContentFactory().
						createContent(new ToolWindowConfigPanel(), CONFIGURE_TAB_NAME, false);
				content.setCloseable(false);
				ideaToolWindow.getContentManager().addContent(content);
			}
		} else {
			// servers defined, find config panel, hide config panel
			Content content = ideaToolWindow.getContentManager().findContent(CONFIGURE_TAB_NAME);
			if (content != null) {
				ideaToolWindow.getContentManager().removeContent(content, true);
			}
		}

		 //bottomIdeaToolWindow
		for (ToolWindowPanels entry : panels) {
			try {
				ServerType serverType = Util.toolWindowPanelsToServerType(entry);

				// servers are defined
				if (ConfigurationFactory.getConfiguration().getProductServers(serverType).transientGetServers().size() > 0) {
					// tab is not visible
					if (ideaToolWindow.getContentManager().findContent(entry.toString()) == null) {

						// show tab
						Content content = null;

						switch (entry) {
							case BAMBOO:
								content = project.getComponent(ThePluginProjectComponent.class).createBambooContent();
								break;
							case CRUCIBLE:
								content = project.getComponent(ThePluginProjectComponent.class).createCrucibleContent();
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

	/**
	 * Methods opens the ToolWindow and focuses on a particular component.
	 * If component does not exists it is created
	 * @param project
	 * @param component
	 */
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
						content = project.getComponent(ThePluginProjectComponent.class).createCrucibleContent();
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

	/**
	 * Methods opens the ToolWindow and focuses on a particular component.
	 * If component does not exists it is created
	 * @param project
	 * @param tabName
	 */
	public static void focusPanel(Project project, String tabName) {
		if (tabName.equals(ToolWindowPanels.BAMBOO.toString())) {
			focusPanel(project, ToolWindowPanels.BAMBOO);
		} else if (tabName.equals(ToolWindowPanels.CRUCIBLE.toString())) {
			focusPanel(project, ToolWindowPanels.CRUCIBLE);
		} else if (tabName.equals(ToolWindowPanels.JIRA.toString())) {
			focusPanel(project, ToolWindowPanels.JIRA);
		}
	}

	/**
	 * Methods opens the ToolWindow and focuses on a particular component.
	 * If component does not exists it is created
	 * @param e
	 * @param component
	 */
	public static void focusPanel(AnActionEvent e, ToolWindowPanels component) {
		Project project = IdeaHelper.getCurrentProject(e.getDataContext());
		focusPanel(project, component);
    }

	/**
	 * Methods opens the ToolWindow and focuses on a particular component.
	 * If component does not exists it is not created and focused
	 * @param project
	 * @param tabName
	 */
	public static void focusPanelIfExists(Project project, String tabName) {
		ToolWindow tw = IdeaHelper.getToolWindow(project);

        if (tw != null) {
			//tw.activate(null);
			ContentManager contentManager = tw.getContentManager();
			Content content = contentManager.findContent(tabName);

			if (content != null) {
				contentManager.setSelectedContent(content);
			}
        }
	}

	/**
	 * Shows/hides panel if exists at least one server configured for this panel.
	 * If component does not exists it is not created and focused.
	 * If component is not focused than is focused.
	 * If component is focused then is closed/hidden
	 * @param event
	 * @param component
	 */
	public static void showHidePanelIfExists(AnActionEvent event, ToolWindowPanels component) {
		Project project = IdeaHelper.getCurrentProject(event.getDataContext());
		ToolWindow tw = IdeaHelper.getToolWindow(project);

		if (tw != null) {


			try {
				ServerType serverType = Util.toolWindowPanelsToServerType(component);
				// servers are defined
				if (ConfigurationFactory.getConfiguration().getProductServers(serverType).transientGetServers().size() > 0) {
					// tab is not visible
					Content content =  tw.getContentManager().findContent(component.toString());
					if (content == null) {

						// doesn't exists so create and show tab
						switch (component) {
							case BAMBOO:
								content = project.getComponent(ThePluginProjectComponent.class).createBambooContent();
								break;
							case CRUCIBLE:
								content = project.getComponent(ThePluginProjectComponent.class).createCrucibleContent();
								break;
							case JIRA:
								content = project.getComponent(ThePluginProjectComponent.class).createJiraContent();
								break;
							default:
								break;
						}

						tw.getContentManager().addContent(content);
					} else { //tab exists so close it, hide


						if (content.isSelected() && tw.isVisible()) {
							tw.getContentManager().removeContent(content, true);
						} else {
							tw.getContentManager().setSelectedContent(content);
						}
					}
				// servers are not defined
				} else {
					// tab is visible
					Content content = tw.getContentManager().findContent(component.toString());
					if (content != null) {
						// hide tab


						if (content.isSelected() && tw.isVisible()) {
							tw.getContentManager().removeContent(content, true);
						} else {
							tw.getContentManager().setSelectedContent(content);
						}
					}
				}
			} catch (ThePluginException e) {
				PluginUtil.getLogger().error(e.getMessage(), e);
			}
			
			tw.activate(null);

			focusPanelIfExists(project, component.toString());
		}
	}

	/**
	 * Methods opens the ToolWindow and focuses on a particular component.
	 * If component does not exists it is not created AND focused
	 * @param e
	 * @param component
	 */
	public static void focusPanelifExists(AnActionEvent e, ToolWindowPanels component) {
		focusPanelIfExists(IdeaHelper.getCurrentProject(e.getDataContext()), component.toString());
	}

	public void contentAdded(ContentManagerEvent event) {
		super.contentAdded(event);	//To change body of overridden methods use File | Settings | File Templates.
	}

	public void contentRemoved(ContentManagerEvent event) {
		super.contentRemoved(event);	//To change body of overridden methods use File | Settings | File Templates.
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
        BAMBOO("Bamboo"),
		CRUCIBLE("Crucible"),
		JIRA("JIRA");

        private final String title;

        ToolWindowPanels(String title) {
            this.title = title;
        }

        public String toString() {
            return title;
        }
    }
}
