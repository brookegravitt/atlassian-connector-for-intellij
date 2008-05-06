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

import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.configuration.CrucibleConfigurationBean;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.crucible.CrucibleStatusListener;
import com.atlassian.theplugin.crucible.ReviewDataInfo;
import com.atlassian.theplugin.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.*;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.remoteapi.MissingPasswordHandler;
import com.atlassian.theplugin.commons.util.DateUtil;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TimerTask;


/**
 * IDEA-specific class that uses {@link com.atlassian.theplugin.crucible.CrucibleServerFacade} to retrieve builds info and
 * passes raw data to configured {@link com.atlassian.theplugin.crucible.CrucibleStatusListener}s.<p>
 * <p/>
 * Intended to be triggered by a {@link java.util.Timer} through the {@link #newTimerTask()}.<p>
 * <p/>
 * Thread safe.
 */
public final class CrucibleStatusChecker implements SchedulableChecker {
	private final List<CrucibleStatusListener> listenerList = new ArrayList<CrucibleStatusListener>();
	private final PluginConfiguration pluginConfiguration;
	private final CrucibleServerFacade crucibleServerFacade;

	public CrucibleStatusChecker(PluginConfiguration pluginConfiguration) {
		this.pluginConfiguration = pluginConfiguration;
		this.crucibleServerFacade = CrucibleServerFacadeImpl.getInstance();
	}

	public void registerListener(CrucibleStatusListener listener) {
		synchronized (listenerList) {
			listenerList.add(listener);
		}
	}

	public void unregisterListener(CrucibleStatusListener listener) {
		synchronized (listenerList) {
			listenerList.remove(listener);
		}
	}

	/**
	 * DO NOT use that method in 'dispatching thread' of IDEA. It can block GUI for several seconds.
	 */
	private void doRun() {
		try {
			// collect build info from each server
			final Collection<ReviewDataInfo> reviews = new ArrayList<ReviewDataInfo>();
			for (Server server : retrieveEnabledCrucibleServers()) {
				try {
					PluginUtil.getLogger().debug("Crucible: updating status for server: "
							+ server.getUrlString());
					reviews.addAll(
							crucibleServerFacade.getActiveReviewsForUser(server));
				} catch (ServerPasswordNotProvidedException exception) {
					ApplicationManager.getApplication().invokeLater(
							new MissingPasswordHandler(crucibleServerFacade), ModalityState.defaultModalityState());
				} catch (RemoteApiException e) {
					PluginUtil.getLogger().info("Error getting Crucible reviews for " + server.getName()
							+ " server", e);
				}
			}

			// dispatch to the listeners
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					synchronized (listenerList) {
						for (CrucibleStatusListener listener : listenerList) {
							listener.updateReviews(reviews);
						}
					}
				}
			});
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private Collection<Server> retrieveEnabledCrucibleServers() {
		return pluginConfiguration.getProductServers(
				ServerType.CRUCIBLE_SERVER).getEnabledServers();
	}

	/**
	 * Create a new instance of {@link java.util.TimerTask} for {@link java.util.Timer} re-scheduling purposes.
	 *
	 * @return new instance of TimerTask
	 */
	public TimerTask newTimerTask() {
		return new TimerTask() {
			public void run() {
				doRun();
			}
		};
	}

	public boolean canSchedule() {
		return !retrieveEnabledCrucibleServers().isEmpty();
	}

	public long getInterval() {
		return (long) ((CrucibleConfigurationBean) pluginConfiguration
				.getProductServers(ServerType.CRUCIBLE_SERVER))
				.getPollTime() * DateUtil.SECONDS_IN_MINUTE * DateUtil.MILISECONDS_IN_SECOND;
	}

	public void resetListenersState() {
		for (StatusListener listener : listenerList) {
			listener.resetState();
		}
	}
}