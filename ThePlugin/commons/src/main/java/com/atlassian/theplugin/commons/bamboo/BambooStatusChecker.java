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

import com.atlassian.theplugin.commons.ConfigurationListener;
import com.atlassian.theplugin.commons.SchedulableChecker;
import com.atlassian.theplugin.commons.UIActionScheduler;
import com.atlassian.theplugin.commons.cfg.ProjectId;
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import com.atlassian.theplugin.commons.cfg.BambooCfgManager;
import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.util.DateUtil;
import com.atlassian.theplugin.commons.util.Logger;
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
public final class BambooStatusChecker implements SchedulableChecker, ConfigurationListener {

	private final List<BambooStatusListener> listenerList = new ArrayList<BambooStatusListener>();

	private UIActionScheduler actionScheduler;
	private static Date lastActionRun = new Date();
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("H:mm:ss:SSS");
	private static StringBuffer sb = new StringBuffer();
	private static final String NAME = "Atlassian Bamboo checker";

	private BambooCfgManager cfgManager;
	private final BambooServerFacade bambooServerFacade;
	private Runnable missingPasswordHandler;
	private static BambooStatusChecker instance;
	private final ProjectId projectId;

	public void setActionScheduler(UIActionScheduler actionScheduler) {
		this.actionScheduler = actionScheduler;
	}



	public BambooStatusChecker(final ProjectId projectId, UIActionScheduler actionScheduler,
			BambooCfgManager cfgManager,
			Runnable missingPasswordHandler,
			Logger logger) {
		this.projectId = projectId;
		this.actionScheduler = actionScheduler;
		this.cfgManager = cfgManager;
		this.missingPasswordHandler = missingPasswordHandler;

		this.bambooServerFacade = BambooServerFacadeImpl.getInstance(logger);
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
            for (BambooServerCfg server : retrieveEnabledBambooServers()) {
                        try {

							Date newRun = new Date();
							sb.delete(0, sb.length());
							sb.append(server.getName()).append(":");
							sb.append("last result time: ").append(dateFormat.format(lastActionRun));
							sb.append(" current run time : ").append(dateFormat.format(newRun));
							sb.append(" time difference: ")
                                    .append(dateFormat.format((newRun.getTime() - lastActionRun.getTime())));
							LoggerImpl.getInstance().debug(sb.toString());

							newServerBuildsStatus.addAll(bambooServerFacade.getSubscribedPlansResults(server));
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
			LoggerImpl.getInstance().info(t);
        }
    }

	private Collection<BambooServerCfg> retrieveEnabledBambooServers() {
		return cfgManager.getAllEnabledBambooServers(projectId);
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
		return !retrieveEnabledBambooServers().isEmpty();
	}

	public long getInterval() {
		return cfgManager.getGlobalBambooCfg().getPollTime() * DateUtil.SECONDS_IN_MINUTE * DateUtil.MILISECONDS_IN_SECOND;
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

	public void updateConfiguration(final ProjectId project, final CfgManager aCfgManager) {
		this.cfgManager = aCfgManager;
	}


	public void updateConfiguration(BambooCfgManager theCfgManager) {
		cfgManager = theCfgManager;
	}

}
