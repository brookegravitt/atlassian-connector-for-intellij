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

package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.SchedulableChecker;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.configuration.CrucibleConfigurationBean;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.commons.util.DateUtil;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModel;
import com.atlassian.theplugin.crucible.model.UpdateReason;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;
import com.atlassian.theplugin.util.PluginUtil;

import java.util.Collection;
import java.util.TimerTask;


/**
 * IDEA-specific class that uses
 * {@link com.atlassian.theplugin.commons.crucible.CrucibleServerFacade} to retrieve builds info and
 * passes raw data to configured {@link CrucibleStatusListener}s.<p>
 * <p/>
 * Intended to be triggered by a {@link java.util.Timer} through the {@link #newTimerTask()}.<p>
 * <p/>
 * Thread safe.
 */
public final class CrucibleStatusChecker implements SchedulableChecker {
	private static final String NAME = "Atlassian Crucible checker";
	private final ProjectCfgManagerImpl cfgManager;
	private final CrucibleReviewListModel crucibleReviewListModel;
	private final CrucibleConfigurationBean crucibleConfiguration;

	public CrucibleStatusChecker(ProjectCfgManagerImpl cfgManager,
			PluginConfiguration pluginConfiguration,
			CrucibleReviewListModel crucibleReviewListModel) {
		this.cfgManager = cfgManager;
		this.crucibleReviewListModel = crucibleReviewListModel;
		this.crucibleConfiguration = pluginConfiguration.getCrucibleConfigurationData();
	}

	/**
	 * DO NOT use that method in 'dispatching thread' of IDEA. It can block GUI for several seconds.
	 */
	private void doRun() {
		try {
			crucibleReviewListModel.rebuildModel(UpdateReason.TIMER_FIRED);
		} catch (Throwable t) {
			PluginUtil.getLogger().error(t);
		}
	}

	private Collection<CrucibleServerCfg> retrieveEnabledCrucibleServers() {
		return cfgManager.getAllEnabledCrucibleServers();
	}

	/**
	 * Create a new instance of {@link java.util.TimerTask} for {@link java.util.Timer} re-scheduling purposes.
	 *
	 * @return new instance of TimerTask
	 */
	public TimerTask newTimerTask() {
		return new TimerTask() {
			@Override
			public void run() {
				doRun();
			}
		};
	}

	public boolean canSchedule() {
		return !retrieveEnabledCrucibleServers().isEmpty();
	}

	public long getInterval() {
		return (long) crucibleConfiguration.getPollTime() * DateUtil.SECONDS_IN_MINUTE * DateUtil.MILISECONDS_IN_SECOND;
	}

	public void resetListenersState() {
	}

	public String getName() {
		return NAME;
	}
}