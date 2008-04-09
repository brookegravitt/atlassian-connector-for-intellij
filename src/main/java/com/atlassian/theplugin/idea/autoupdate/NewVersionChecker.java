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

package com.atlassian.theplugin.idea.autoupdate;

import com.atlassian.theplugin.configuration.PluginConfiguration;
import com.atlassian.theplugin.exception.VersionServiceException;
import com.atlassian.theplugin.exception.ThePluginException;
import com.atlassian.theplugin.exception.IncorrectVersionException;
import com.atlassian.theplugin.util.InfoServer;
import com.atlassian.theplugin.util.PluginUtil;
import com.atlassian.theplugin.util.Version;
import com.atlassian.theplugin.idea.SchedulableChecker;

import java.util.TimerTask;

/**
 * Provides functionality to check for new version and update plugin
 */
public final class NewVersionChecker implements SchedulableChecker {
	private static final long PLUGIN_UPDATE_ATTEMPT_DELAY = 60 * 60 * 1000; // every hour

	private final transient PluginConfiguration pluginConfiguration;

	public NewVersionChecker(PluginConfiguration pluginConfiguration) {
		this.pluginConfiguration = pluginConfiguration;
	}

	/**
	 * Connects to the server, checks for new version and updates if necessary
	 * @return new TimerTask to be scheduled
	 */
	public TimerTask newTimerTask() {
		return new TimerTask() {
			public void run() {
				try {
					doRun(new ForwardToIconHandler(pluginConfiguration));
				} catch (ThePluginException e) {
					PluginUtil.getLogger().info("Error checking new version: " + e.getMessage());
				}
			}
		};
	}

	public boolean canSchedule() {
		return true; // NewVersionChecker is always enabled
	}

	public long getInterval() {
		return PLUGIN_UPDATE_ATTEMPT_DELAY;
	}

	public void resetListenersState() {
		// do nothing
	}

	protected void doRun(UpdateActionHandler action) throws ThePluginException {
		if (!pluginConfiguration.isAutoUpdateEnabled()) {
			return;
		}
		if (action == null) {
			throw new IllegalArgumentException("Action handler not provided.");
		}
		InfoServer.VersionInfo versionInfo = getLatestVersion();
		Version newVersion = versionInfo.getVersion();
		Version thisVersion = new Version(PluginUtil.getVersion());
		if (newVersion.greater(thisVersion)) {
			action.doAction(versionInfo);
		}
	}

	private InfoServer.VersionInfo getLatestVersion() throws VersionServiceException, IncorrectVersionException {
//		InfoServer server =  new InfoServer(PluginUtil.STABLE_VERSION_INFO_URL,
//				pluginConfiguration.getUid());
		return InfoServer.getLatestPluginVersion(
				pluginConfiguration.getUid(),
				pluginConfiguration.getCheckUnstableVersionsEnabled());
	}

}
