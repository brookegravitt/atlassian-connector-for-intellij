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

package com.atlassian.connector.intellij.bamboo;

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.theplugin.commons.SchedulableChecker;
import com.atlassian.theplugin.commons.UIActionScheduler;
import com.atlassian.theplugin.commons.bamboo.BambooServerData;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.util.DateUtil;
import com.atlassian.theplugin.commons.util.LoggerImpl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;


/**
 * IDEA-specific class that uses  to retrieve builds info and
 * passes raw data to configured {@link BambooStatusListener}s.<p>
 * <p/>
 * Intended to be triggered by a {@link java.util.Timer} through the {@link #newTimerTask()}.<p>
 * <p/>
 * Thread safe.
 */
public final class BambooStatusChecker implements SchedulableChecker {

	private final List<BambooStatusListener> listenerList = new ArrayList<BambooStatusListener>();

	private UIActionScheduler actionScheduler;
	private static Date lastActionRun = new Date();
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("H:mm:ss:SSS");
	private static StringBuffer sb = new StringBuffer();
	private static final String NAME = "Atlassian Bamboo checker";

	//	private BambooCfgManager bambooCfgManager;
	private final IntelliJBambooServerFacade bambooServerFacade;
	private ProjectCfgManager cfgManager;
	private final PluginConfiguration pluginConfiguration;
	private final Runnable missingPasswordHandler;

	public void setActionScheduler(UIActionScheduler actionScheduler) {
		this.actionScheduler = actionScheduler;
	}

	public BambooStatusChecker(UIActionScheduler actionScheduler,
			ProjectCfgManager cfgManager, final PluginConfiguration pluginConfiguration,
			Runnable missingPasswordHandler, final IntelliJBambooServerFacade facade) {
		this.actionScheduler = actionScheduler;
		this.cfgManager = cfgManager;
		this.pluginConfiguration = pluginConfiguration;
		this.missingPasswordHandler = missingPasswordHandler;
		this.bambooServerFacade = facade;
	}

	public void registerListener(BambooStatusListener listener) {
		synchronized (listenerList) {
			listenerList.add(listener);
		}
	}

	public void unregisterListener(BambooStatusListener listener) {
		synchronized (listenerList) {
			listenerList.remove(listener);
		}
	}

	/**
	 * DO NOT use that method in 'dispatching thread' of IDEA. It can block GUI for several seconds.
	 */
	private void doRun() {
		try {

			final List<Exception> generalProblems = new ArrayList<Exception>();

			// collect build info from each server
			final Collection<BambooBuildAdapter> newServerBuildsStatus = new ArrayList<BambooBuildAdapter>();
			for (BambooServerData server : cfgManager.getAllEnabledBambooServerss()) {
				try {

					Date newRun = new Date();
					sb.delete(0, sb.length());
					sb.append(server.getName()).append(":");
					sb.append("last result time: ").append(dateFormat.format(lastActionRun));
					sb.append(" current run time : ").append(dateFormat.format(newRun));
					sb.append(" time difference: ")
                        .append(dateFormat.format((newRun.getTime() - lastActionRun.getTime())));
					LoggerImpl.getInstance().debug(sb.toString());

					newServerBuildsStatus.addAll(bambooServerFacade.getSubscribedPlansResults(server, server.getPlans(),
                        server.isUseFavourites(), server.isShowBranches(), server.isMyBranchesOnly(), server.getTimezoneOffset()));

					lastActionRun = newRun;

				} catch (ServerPasswordNotProvidedException exception) {
					actionScheduler.invokeLater(missingPasswordHandler);
					generalProblems.add(exception);
				}
			}

			// dispatch to the listeners
			actionScheduler.invokeLater(new Runnable() {
				public void run() {
					synchronized (listenerList) {
						for (BambooStatusListener listener : listenerList) {
							listener.updateBuildStatuses(newServerBuildsStatus, generalProblems);
						}
					}
				}
			});
		} catch (Throwable t) {
			LoggerImpl.getInstance().info(t);
		}
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
		return cfgManager != null && !cfgManager.getAllEnabledBambooServerss().isEmpty();
	}

	public long getInterval() {
		return pluginConfiguration.getBambooConfigurationData().getPollTime()
				* DateUtil.SECONDS_IN_MINUTE * DateUtil.MILISECONDS_IN_SECOND;
	}

	/**
	 * Resets listeners (sets them to default state)
	 * Listeners should be set to default state if the checker topic list is empty
	 */
	public void resetListenersState() {
		for (BambooStatusListener listener : listenerList) {
			listener.resetState();
		}
	}

	public String getName() {
		return NAME;
	}

	// only for unit tests
	public void updateConfiguration(ProjectCfgManager theCfgManager) {
		cfgManager = theCfgManager;
	}

}
