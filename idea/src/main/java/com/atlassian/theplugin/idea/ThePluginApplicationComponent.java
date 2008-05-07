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

import com.atlassian.theplugin.commons.bamboo.BambooStatusChecker;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacadeImpl;
import com.atlassian.theplugin.commons.SchedulableChecker;
import com.atlassian.theplugin.commons.UIActionScheduler;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.idea.config.ConfigPanel;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.util.*;
import com.atlassian.theplugin.remoteapi.MissingPasswordHandler;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.extensions.AreaPicoContainer;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;
import java.util.Timer;

@State(name = "ThePluginSettings", storages = { @Storage(id = "thePlugin", file = "$APP_CONFIG$/thePlugin.xml") })
public class ThePluginApplicationComponent
		implements ApplicationComponent, Configurable, PersistentStateComponent<PluginConfigurationBean> {

	static {
		AreaPicoContainer apc = Extensions.getRootArea().getPicoContainer();
		PicoUtil.populateGlobalPicoContainer(apc);
	}

	private static final Icon PLUGIN_SETTINGS_ICON = IconLoader.getIcon("/icons/ico_plugin.png");


	private final ConfigPanel configPanel;
	private final PluginConfigurationBean configuration;

	private final Timer timer = new Timer("atlassian-idea-plugin background status checkers");
	private static final int TIMER_START_DELAY = 20000;

	private final Collection<TimerTask> scheduledComponents = new HashSet<TimerTask>();

	private final BambooStatusChecker bambooStatusChecker;

	private final JIRAServerFacade jiraServerFacade;

	BambooStatusChecker getBambooStatusChecker() {
		return bambooStatusChecker;
	}



	@Nls
	public String getDisplayName() {
		return PluginUtil.getInstance().getName();
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

	@NonNls
	@NotNull
	public String getComponentName() {
		return "ThePluginApplicationComponent";
	}


	public void initComponent() {
//		LoggerImpl.setFactory(new LoggerImpl.Factory() {
//						public Logger getLoggerInstance(String category) {
//							return new IdeaLoggerImpl(com.intellij.openapi.diagnostic.Logger.getInstance(category));
//						}
//					});
//
//

	}

	public void disposeComponent() {
		timer.cancel();
	}

	public JComponent createComponent() {
		return configPanel;
	}

	public boolean isModified() {
		return configPanel.isModified();
	}

	public JIRAServerFacade getJiraServerFacade() {
		return jiraServerFacade;
	}

	public ThePluginApplicationComponent(PluginConfigurationBean configuration,
										 /*BambooStatusChecker bambooStatusChecker,*/
										 /*ConfigPanel configPanel,*/
										 SchedulableChecker[] schedulableCheckers,
										 UIActionScheduler actionScheduler) {
		this.configuration = configuration;
		this.configuration.setHttpConfigurable(HttpConfigurableIdeaImpl.getInstance());
		this.bambooStatusChecker = BambooStatusChecker.getInstance(
				actionScheduler,
				configuration,
				new MissingPasswordHandler(BambooServerFacadeImpl.getInstance(PluginUtil.getLogger())),
				PluginUtil.getLogger());
		this.schedulableCheckers = schedulableCheckers; /* get lost, findbugs! */
		this.configPanel = ConfigPanel.getInstance(configuration);
		this.jiraServerFacade = JIRAServerFacadeImpl.getInstance();
		ConfigurationFactory.setConfiguration(configuration);
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

	private final SchedulableChecker[] schedulableCheckers;

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
			configPanel.getData();
			configPanel.setData();

			for (Project project : ProjectManager.getInstance().getOpenProjects()) {
				ThePluginProjectComponent projectComponent = project.getComponent(ThePluginProjectComponent.class);
				// show icons if necessary
				projectComponent.getStatusBarBambooIcon().showOrHideIcon();
				projectComponent.getStatusBarCrucibleIcon().showOrHideIcon();

				projectComponent.getToolWindow().showHidePanels();
			}
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

	}

	public PluginConfigurationBean getState() {
		return configuration;
	}

	public void loadState(PluginConfigurationBean state) {
		state.setHttpConfigurable(HttpConfigurableIdeaImpl.getInstance());
		configuration.setConfiguration(state);
	}
}
