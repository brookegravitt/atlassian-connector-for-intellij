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

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.exception.ThePluginException;
import com.atlassian.theplugin.idea.bamboo.BambooToolWindowPanel;
import com.atlassian.theplugin.util.PluginUtil;
import com.atlassian.theplugin.util.Util;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;

/**
 * @author Jacek Jaroczynski
 */
public class PluginToolWindow {

	private ArrayList<ToolWindowPanels> panels = new ArrayList<ToolWindowPanels>(INITIAL_NUMBER_OF_TABS);

	private ToolWindow ideaToolWindow;
	private Project project;
	//private String selectedContent = null;
	// one space to have better horizontal separation between the header and the tabs
	public static final String TOOL_WINDOW_NAME = "Atlassian ";
	private static final int INITIAL_NUMBER_OF_TABS = 4;
	private static final String CONFIGURE_TAB_NAME = "Configure";
	public static final Icon ICON_CRUCIBLE = IconLoader.getIcon("/icons/crucible-16.png");
	private final CfgManager cfgManager;
	private final BambooToolWindowPanel bambooToolWindowPanel;


	public static void showHidePluginWindow(AnActionEvent event) {
		ToolWindow tw = IdeaHelper.getToolWindow(IdeaHelper.getCurrentProject(event.getDataContext()));
		if (tw != null) {
			if (tw.isVisible()) {
				tw.hide(new Runnable() {
					public void run() {
					}
				});
			} else {

				tw.show(new Runnable() {
					public void run() {
					}
				});
			}

		}

	}

	public PluginToolWindow(@NotNull Project project, @NotNull CfgManager cfgManager,
			@NotNull BambooToolWindowPanel bambooToolWindowPanel) {
		this.cfgManager = cfgManager;
		this.bambooToolWindowPanel = bambooToolWindowPanel;
		this.project = project;
	}


	public void register(@NotNull ToolWindowManager toolWindowManager) {
		this.ideaToolWindow = toolWindowManager.registerToolWindow(
				TOOL_WINDOW_NAME, false, ToolWindowAnchor.BOTTOM);
	}

	/**
	 * @return {@link ToolWindow} objects wraped up by this class
	 */
	public ToolWindow getIdeaToolWindow() {
		return ideaToolWindow;
	}


	/**
	 * Register type of panel. Register panel can be then shown/hidden using {@link #showHidePanels}
	 *
	 * @param toolWindowPanel ToolWindowPanels enum value
	 */
	public void registerPanel(ToolWindowPanels toolWindowPanel) {
		if (!panels.contains(toolWindowPanel)) {
			panels.add(toolWindowPanel);
		}
	}

//	public void registerBottomPanel(ToolWindowPanels toolWindowPanel) {
//		bottomPanels.add(toolWindowPanel);
//	}


	public void showHidePanels() {
		//stopTabChangeListener();

		final ContentManager contentManager = ideaToolWindow.getContentManager();
		if (cfgManager.getAllEnabledServers(CfgUtil.getProjectId(project)).size() == 0) {
			// no servers defined, show config panel
			if (contentManager.findContent(CONFIGURE_TAB_NAME) == null) {
				final Content content = contentManager.getFactory().createContent(
						new ToolWindowConfigPanel(project), CONFIGURE_TAB_NAME, false);
				content.setCloseable(false);
				ideaToolWindow.getContentManager().addContent(content);
			}
		} else {
			// servers defined, find config panel, hide config panel
			final Content content = contentManager.findContent(CONFIGURE_TAB_NAME);
			if (content != null) {
				ideaToolWindow.getContentManager().removeContent(content, true);
			}
		}

		//bottomIdeaToolWindow
		for (ToolWindowPanels entry : panels) {
			try {
				ServerType serverType = Util.toolWindowPanelsToServerType(entry);

				// servers are defined
				if (cfgManager.getAllEnabledServers(CfgUtil.getProjectId(project), serverType).isEmpty() == false) {
					// tab is not visible
					if (ideaToolWindow.getContentManager().findContent(entry.toString()) == null) {

						// show tab
						Content content = null;

						switch (entry) {
							case BAMBOO_OLD:
								content = project.getComponent(ThePluginProjectComponent.class).createBambooContent(
										contentManager);
								break;
							case BUILDSS:
								content = project.getComponent(ThePluginProjectComponent.class).createBuildContent(
										contentManager);
								break;
							case BUILDS_WOJTEK:
								content = createBamboo2Content();
								break;
							case CRUCIBLE:
								content = project.getComponent(ThePluginProjectComponent.class).createCrucibleContentNew(
										contentManager);
								break;
							case ISSUES:
								content = project.getComponent(ThePluginProjectComponent.class).createIssuesContent(
										contentManager);
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
	 *
	 * @param component
	 */
	public void focusPanel(ToolWindowPanels component) {
		if (ideaToolWindow != null) {
			final ContentManager contentManager = ideaToolWindow.getContentManager();
			ideaToolWindow.activate(null);
			Content content = contentManager.findContent(component.toString());

			if (content == null) {
				switch (component) {
					case BAMBOO_OLD:
						content = project.getComponent(ThePluginProjectComponent.class).createBambooContent(contentManager);
						contentManager.addContent(content);
						break;
					case BUILDSS:
						content = project.getComponent(ThePluginProjectComponent.class).createBuildContent(contentManager);
						contentManager.addContent(content);
						break;
					case BUILDS_WOJTEK:
						content = createBamboo2Content();
						break;
					case CRUCIBLE:
						content = project.getComponent(ThePluginProjectComponent.class).createCrucibleContentNew(
								contentManager);
						contentManager.addContent(content);
						break;
					case ISSUES:
						content = project.getComponent(ThePluginProjectComponent.class).createIssuesContent(contentManager);
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
	 * If component does not exists it is not created and focused
	 *
	 * @param project
	 * @param tabName
	 */
	public static void focusPanelIfExists(Project project, String tabName) {
		ToolWindow tw = IdeaHelper.getToolWindow(project);

		if (tw != null) {
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
	 *
	 * @param event
	 * @param component
	 */
	public void showHidePanelIfExists(AnActionEvent event, ToolWindowPanels component) {
		if (ideaToolWindow != null) {


			try {
				ServerType serverType = Util.toolWindowPanelsToServerType(component);
				// servers are defined
				final CfgManager myCfgManager = IdeaHelper.getCfgManager();
				if (myCfgManager.getAllEnabledServers(CfgUtil.getProjectId(project), serverType).size() > 0) {
					// tab is not visible
					final ContentManager contentManager = ideaToolWindow.getContentManager();
					Content content = contentManager.findContent(component.toString());
					if (content == null) {

						// doesn't exists so create and show tab
						switch (component) {
							case BAMBOO_OLD:
									content = project.getComponent(ThePluginProjectComponent.class).createBambooContent(
										contentManager);
								break;
							case BUILDSS:
									content = project.getComponent(ThePluginProjectComponent.class).createBuildContent(
										contentManager);
								break;
							case BUILDS_WOJTEK:
								content = createBamboo2Content();
								break;
							case CRUCIBLE:
								content = project.getComponent(ThePluginProjectComponent.class).createCrucibleContentNew(
										contentManager);
								break;
							case ISSUES:
								content = project.getComponent(ThePluginProjectComponent.class).createIssuesContent(
										contentManager);
								break;
							default:
								break;
						}

						ideaToolWindow.getContentManager().addContent(content);

					} else { //tab exists so close it, hide
						if (content.isSelected() && ideaToolWindow.isVisible()) {
							ideaToolWindow.getContentManager().removeContent(content, true);
						} else {
							ideaToolWindow.getContentManager().setSelectedContent(content);
						}
					}
					// servers are not defined
				} else {
					// tab is visible
					Content content = ideaToolWindow.getContentManager().findContent(component.toString());
					if (content != null) {
						// hide tab


						if (content.isSelected() && ideaToolWindow.isVisible()) {
							ideaToolWindow.getContentManager().removeContent(content, true);
						} else {
							ideaToolWindow.getContentManager().setSelectedContent(content);
						}
					}
				}
			} catch (ThePluginException e) {
				PluginUtil.getLogger().error(e.getMessage(), e);
			}

			ideaToolWindow.activate(null);

			focusPanelIfExists(project, component.toString());
		}
	}

	public Content createBamboo2Content() {
		final ContentManager contentManager = ideaToolWindow.getContentManager();
		final Content content = contentManager.getFactory().createContent(bambooToolWindowPanel,
				PluginToolWindow.ToolWindowPanels.BUILDS_WOJTEK.toString(), false);
		content.setIcon(IconLoader.getIcon("/icons/tab_bamboo.png"));
		content.putUserData(com.intellij.openapi.wm.ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);
		return content;
	}


	/**
	 * Methods opens the ToolWindow and focuses on a particular component.
	 * If component does not exists it is not created AND focused
	 *
	 * @param e
	 * @param component
	 */
	public static void focusPanelifExists(AnActionEvent e, ToolWindowPanels component) {
		focusPanelIfExists(IdeaHelper.getCurrentProject(e.getDataContext()), component.toString());
	}

	/**
	 * List of available panels in tool window
	 */
	public enum ToolWindowPanels {
		BAMBOO_OLD("Builds old"),
		BUILDS_WOJTEK("Builds"),
		BUILDSS("Buildss"),
		CRUCIBLE("Reviews"),
		ISSUES("Issues");

		private final String title;

		ToolWindowPanels(String title) {
			this.title = title;
		}

		@Override
		public String toString() {
			return title;
		}
	}
}
