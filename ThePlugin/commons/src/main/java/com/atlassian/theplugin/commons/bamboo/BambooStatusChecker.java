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

package com.atlassian.theplugin.commons.bamboo;

import com.atlassian.theplugin.commons.*;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.commons.configuration.BambooConfigurationBean;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.commons.util.DateUtil;
import com.atlassian.theplugin.commons.util.Logger;
import com.atlassian.theplugin.commons.util.LoggerImpl;

import java.util.*;
import java.text.SimpleDateFormat;

import org.joda.time.DateTime;


/**
 * IDEA-specific class that uses  to retrieve builds info and
 * passes raw data to configured {@link BambooStatusListener}s.<p>
 * <p/>
 * Intended to be triggered by a {@link java.util.Timer} through the {@link #newTimerTask()}.<p>
 * <p/>
 * Thread safe.
 */
public final class BambooStatusChecker implements SchedulableChecker, ConfigurationListener {

	private final List<BambooStatusListener> listenerList = new ArrayList<BambooStatusListener>();

	private UIActionScheduler actionScheduler;
	private static Date lastActionRun = new Date();
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("H:mm:ss:SSS");
	private static StringBuffer sb = new StringBuffer();
	private static final String NAME = "Atlassian Bamboo checker";

	public void setActionScheduler(UIActionScheduler actionScheduler) {
		this.actionScheduler = actionScheduler;
	}

	public void setConfiguration(PluginConfiguration configuration) {
		this.configuration = configuration;
	}

	private PluginConfiguration configuration;
	private final BambooServerFacade bambooServerFacade;
	private Runnable missingPasswordHandler;
	private static BambooStatusChecker instance;


	private BambooStatusChecker(UIActionScheduler actionScheduler,
							   PluginConfiguration configuration,
							   Runnable missingPasswordHandler,
							   Logger logger) {
		this.actionScheduler = actionScheduler;
		this.configuration = configuration;
		this.missingPasswordHandler = missingPasswordHandler;

		this.bambooServerFacade = BambooServerFacadeImpl.getInstance(logger);
	}

	public static BambooStatusChecker getInstance(UIActionScheduler actionScheduler,
							   PluginConfiguration configuration,
							   Runnable missingPasswordHandler,
							   Logger logger) {
		if (instance == null) {
			instance = new BambooStatusChecker(actionScheduler, configuration, missingPasswordHandler, logger);
		}
		return instance;
	}

	/**
	 * Call this method to get reference to existing BambooStatusChecker.
	 * If BambooStatusChecker has not been created before then null will be returned.
	 * In such case call {@link #getInstance(UIActionScheduler, PluginConfiguration, Runnable, Logger)}
	 * @return reference to existing BambooStatusChecker or null if object does not exist
	 */
	public static BambooStatusChecker getInstance() {
		return instance;
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

			// collect build info from each server
            final Collection<BambooBuild> newServerBuildsStatus = new ArrayList<BambooBuild>();
            for (Server server : retrieveEnabledBambooServers()) {
                        try {

							Date newRun = new Date();
							sb.delete(0, sb.length());
							sb.append(server.getName()).append(":");
							sb.append("last result time: ").append(dateFormat.format(lastActionRun));
							sb.append(" current run time : ").append(dateFormat.format(newRun));
							sb.append(" time difference: ").append(dateFormat.format((newRun.getTime()-lastActionRun.getTime())));
							LoggerImpl.getInstance().debug(sb.toString());

							newServerBuildsStatus.addAll(
                                    bambooServerFacade.getSubscribedPlansResults(server));
							lastActionRun = newRun;

						} catch (ServerPasswordNotProvidedException exception) {
                            actionScheduler.invokeLater(missingPasswordHandler);
                        }
                    }

            // dispatch to the listeners
            actionScheduler.invokeLater(new Runnable() {
                public void run() {
                    synchronized (listenerList) {
                        for (BambooStatusListener listener : listenerList) {
                            listener.updateBuildStatuses(newServerBuildsStatus);
                        }
                    }
                }
            });
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

	private Collection<Server> retrieveEnabledBambooServers() {
		return configuration.getProductServers(ServerType.BAMBOO_SERVER).transientgetEnabledServers();
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
		return !retrieveEnabledBambooServers().isEmpty();
	}

	public long getInterval() {
		return (long) ((BambooConfigurationBean) configuration
				.getProductServers(ServerType.BAMBOO_SERVER))
				.getPollTime() * DateUtil.SECONDS_IN_MINUTE * DateUtil.MILISECONDS_IN_SECOND;
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

	public void updateConfiguration(PluginConfigurationBean configuration) {
		this.configuration = configuration;
	}

}
