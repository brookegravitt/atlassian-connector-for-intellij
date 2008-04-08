package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.ServerType;
import com.atlassian.theplugin.UIActionScheduler;
import com.atlassian.theplugin.remoteapi.MissingPasswordHandler;
import com.atlassian.theplugin.configuration.BambooConfigurationBean;
import com.atlassian.theplugin.configuration.PluginConfiguration;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.idea.SchedulableChecker;
import com.atlassian.theplugin.util.DateUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TimerTask;


/**
 * IDEA-specific class that uses {@link com.atlassian.theplugin.bamboo.BambooServerFactory} to retrieve builds info and
 * passes raw data to configured {@link com.atlassian.theplugin.bamboo.BambooStatusListener}s.<p>
 * <p/>
 * Intended to be triggered by a {@link java.util.Timer} through the {@link #newTimerTask()}.<p>
 * <p/>
 * Thread safe.
 */
public final class BambooStatusChecker implements SchedulableChecker {

	private final List<BambooStatusListener> listenerList = new ArrayList<BambooStatusListener>();

	private final UIActionScheduler actionScheduler;
	private final PluginConfiguration configuration;
	private final BambooServerFacade bambooServerFacade;


	public BambooStatusChecker(UIActionScheduler actionScheduler,
							   PluginConfiguration configuration,
							   BambooServerFacade bambooServerFacade) {
		this.actionScheduler = actionScheduler;
		this.configuration = configuration;
		this.bambooServerFacade = bambooServerFacade;
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
                            newServerBuildsStatus.addAll(
                                    bambooServerFacade.getSubscribedPlansResults(server));
                        } catch (ServerPasswordNotProvidedException exception) {
                            actionScheduler.invokeLater(new MissingPasswordHandler(bambooServerFacade));
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
		return configuration.getProductServers(ServerType.BAMBOO_SERVER).getEnabledServers();
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
	public void resetListeners() {
		for (BambooStatusListener listener : listenerList) {
			listener.reset();
		}
	}

}
