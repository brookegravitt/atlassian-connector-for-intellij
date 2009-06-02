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

import com.atlassian.theplugin.commons.SchedulableChecker;
import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.commons.util.StringUtil;
import com.atlassian.theplugin.configuration.IdeaPluginConfigurationBean;
import com.atlassian.theplugin.idea.autoupdate.NewVersionChecker;
import com.atlassian.theplugin.idea.config.ConfigPanel;
import com.atlassian.theplugin.util.*;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.extensions.AreaPicoContainer;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.veryquick.embweb.EmbeddedServer;
import org.veryquick.embweb.HttpRequestHandler;
import org.veryquick.embweb.Response;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Timer;

public class ThePluginApplicationComponent implements ApplicationComponent, Configurable {

	static {
		AreaPicoContainer apc = Extensions.getRootArea().getPicoContainer();
		PicoUtil.populateGlobalPicoContainer(apc);
	}

	private static final Icon PLUGIN_SETTINGS_ICON = IconLoader.getIcon("/icons/ico_plugin.png");


	private ConfigPanel configPanel;

	private final IdeaPluginConfigurationBean configuration;
	private final CfgManager cfgManager;
	private final NewVersionChecker newVersionChecker;

	private final Timer timer = new Timer("atlassian-idea-plugin background status checkers");
	private static final int TIMER_START_DELAY = 20000;

	private final Collection<TimerTask> scheduledComponents = new HashSet<TimerTask>();
	private static final int HTTP_SERVER_PORT = 51235;

	public Collection<SchedulableChecker> getSchedulableCheckers() {
		return schedulableCheckers;
	}

	private final Collection<SchedulableChecker> schedulableCheckers = new HashSet<SchedulableChecker>();


	public ThePluginApplicationComponent(IdeaPluginConfigurationBean configuration, final CfgManager cfgManager,
			final NewVersionChecker newVersionChecker) {
		this.configuration = configuration;

		this.cfgManager = cfgManager;
		this.newVersionChecker = newVersionChecker;
		this.configuration.transientSetHttpConfigurable(HttpConfigurableIdeaImpl.getInstance());

		this.schedulableCheckers.add(newVersionChecker);

		ConfigurationFactory.setConfiguration(configuration);
		PluginSSLProtocolSocketFactory.initializeSocketFactory();

		startHttpServer();

        addActionToDiffToolbar();
	}

    private void addActionToDiffToolbar() {
        AnAction addCommentAction = ActionManager.getInstance().getAction("ThePlugin.Crucible.Add.Comment.In.Editor");
        if (addCommentAction != null) {
            IdeaVersionFacade.getInstance().addActionToDiffGroup(addCommentAction);
        }
    }

    @Nls
	public String getDisplayName() {
		return "Atlassian\nConnector";
	}

	@Nullable
	public Icon getIcon() {
		return PLUGIN_SETTINGS_ICON;
	}

	@Nullable
	@NonNls
	public String getHelpTopic() {
		return null;
	}

	public IdeaPluginConfigurationBean getConfiguration() {
		return configuration;
	}

	@NotNull
	@NonNls
	public String getComponentName() {
		return "ThePluginApplicationComponent";
	}

	public void initComponent() {
		new IdeaLoggerImpl(com.intellij.openapi.diagnostic.Logger.getInstance(LoggerImpl.LOGGER_CATEGORY));
	}

	public void disposeComponent() {
		timer.cancel();
	}

	public JComponent createComponent() {
		if (configPanel == null) {
			configPanel = new ConfigPanel(configuration, cfgManager, newVersionChecker);
		}
		return configPanel;
	}

	public boolean isModified() {
		return configPanel.isModified();
	}

	private void disableTimers() {
		Iterator<TimerTask> i = scheduledComponents.iterator();
		while (i.hasNext()) {
			TimerTask timerTask = i.next();
			i.remove();
			timerTask.cancel();
		}

		timer.purge();
	}

	/**
	 * Reschedule the BambooStatusChecker with immediate execution trigger.
	 *
	 * @param rightNow set to false if the first execution should be delayed by {@link #TIMER_START_DELAY}.
	 */
	public void rescheduleStatusCheckers(boolean rightNow) {

		disableTimers();
		long delay = rightNow ? 0 : TIMER_START_DELAY;

		for (SchedulableChecker checker : schedulableCheckers) {
			if (checker.canSchedule()) {
				final TimerTask newTask = checker.newTimerTask();
				scheduledComponents.add(newTask);
				timer.schedule(newTask, delay, checker.getInterval());
			} else {
				checker.resetListenersState();
			}
		}
	}


	public void apply() throws ConfigurationException {
		if (configPanel != null) {
			// Get data from configPanel to component
			configPanel.saveData();
			//configPanel.setData();

//			for (Project project : ProjectManager.getInstance().getOpenProjects()) {
//				ThePluginProjectComponent projectComponent = project.getComponent(ThePluginProjectComponent.class);
//				// show icons if necessary
//				projectComponent.getStatusBarBambooIcon().showOrHideIcon();
//				projectComponent.getStatusBarCrucibleIcon().showOrHideIcon();
//
//				projectComponent.getToolWindow().showHidePanels();
//			}
			rescheduleStatusCheckers(true);
		}

	}

	public void reset() {
		if (configPanel != null) {
			// Reset configPanel data from component
			configPanel.setData();
		}
	}

	public void disposeUIResources() {
		configPanel = null;
	}

	private void startHttpServer() {

		// load icon
		InputStream iconStream = ThePluginProjectComponent.class.getResourceAsStream("/icons/idea_small.png");

		if (iconStream == null) {
			PluginUtil.getLogger().error("Failed to load icon for http server");
			return;
		}

		// convert icon to byte array
		byte[] iconArray;
		try {
			int size = iconStream.available();
			iconArray = new byte[size];
			if (iconStream.read(iconArray, 0, size) < size) {
				PluginUtil.getLogger().error("Failed to load icon for http server");
				return;
			}
		} catch (IOException e) {
			PluginUtil.getLogger().error("Failed to load icon for http server");
			return;
		}

		// create and start server
		try {
			EmbeddedServer.createInstance(HTTP_SERVER_PORT, new LocalHttpHandler(iconArray), true);
		} catch (Exception e) {
			PluginUtil.getLogger().error("Failed to start http server", e);
		}
	}

	private class LocalHttpHandler implements HttpRequestHandler {
		private byte[] icon;

		public LocalHttpHandler(final byte[] iconArray) {
			this.icon = iconArray;
		}

		public Response handleRequest(final Type type, final String url, final Map<String, String> parameters) {

			final String method = StringUtil.removeTrailingSlashes(url);

			Response response = new Response();

			if (method.equals("icon")) {
				writeIcon(response);
			} else if (method.equals("file")) {
				writeIcon(response);

				final String file = parameters.get("file");
				if (file != null) {
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							boolean found = false;
							// try to open received file in all open projects
							for (Project project : ProjectManager.getInstance().getOpenProjects()) {
								final PsiFile psiFile = CodeNavigationUtil.guessCorrespondingPsiFile(project, file);
								WindowManager.getInstance().getFrame(project).setVisible(true);

//										WindowManager.getInstance().getFrame(finalProject).repaint();
								String osName = System.getProperty("os.name");
								osName = osName.toLowerCase();

								if (osName.contains("windows") || osName.contains("mac os x")) {
									WindowManager.getInstance().getFrame(project).setAlwaysOnTop(true);
									WindowManager.getInstance().getFrame(project).setAlwaysOnTop(false);

								} else { //for linux
									WindowManager.getInstance().getFrame(project).toFront();

								}

								if (psiFile != null) {
									psiFile.navigate(true);
									found = true;
								}
							}

							if (!found) {
								Messages.showInfoMessage("Cannot find file " + file, PluginUtil.PRODUCT_NAME);
							}
						}
					});
				}
			} else if (method.equals("issue")) {
				writeIcon(response);

				final String issueKey = parameters.get("issue_key");
				final String serverUrl = parameters.get("server_url");
				if (issueKey != null && serverUrl != null) {
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							boolean found = false;
							// try to open received issueKey in all open projects
							for (Project project : ProjectManager.getInstance().getOpenProjects()) {
								WindowManager.getInstance().getFrame(project).setVisible(true);

								String osName = System.getProperty("os.name");
								osName = osName.toLowerCase();

								if (osName.contains("windows") || osName.contains("mac os x")) {
									WindowManager.getInstance().getFrame(project).setAlwaysOnTop(true);
									WindowManager.getInstance().getFrame(project).setAlwaysOnTop(false);
								} else { //for linux
									WindowManager.getInstance().getFrame(project).toFront();
								}

								if (IdeaHelper.getIssueListToolWindowPanel(project).openIssue(issueKey, serverUrl)) {
									found = true;
								}
							}

							if (!found) {
								Messages.showInfoMessage("Cannot find issue " + issueKey, PluginUtil.PRODUCT_NAME);
							}
						}
					});
				} else {
					PluginUtil.getLogger().warn("Cannot open issue: issue_key or server_url parameter is null");
				}
			} else {
				response.setNoContent();
				PluginUtil.getLogger().warn("Unknown command received: [" + method + "]");
			}
			return response;
		}

		private void writeIcon(final Response response) {
			response.setContentType("image/png");
			response.setBinaryContent(icon);
			response.setOk();
		}

	}
}
