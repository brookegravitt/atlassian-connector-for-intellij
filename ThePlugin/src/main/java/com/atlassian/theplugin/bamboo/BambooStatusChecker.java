package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.ServerType;
import com.atlassian.theplugin.UIActionScheduler;
import com.atlassian.theplugin.configuration.*;
import com.atlassian.theplugin.idea.SchedulableComponent;

import java.util.*;


/**
 * IDEA-specific class that uses {@link com.atlassian.theplugin.bamboo.BambooServerFactory} to retrieve builds info and
 * passes raw data to configured {@link com.atlassian.theplugin.bamboo.BambooStatusListener}s.<p>
 * <p/>
 * Intended to be triggered by a {@link java.util.Timer} through the {@link #newTimerTask()}.<p>
 * <p/>
 * Thread safe.
 */
public final class BambooStatusChecker implements SchedulableComponent {
	private static final int SECONDS_IN_MINUTE = 60;
	private static final int MILISECONDS_IN_SECOND = 1000;

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
                            actionScheduler.invokeLater(new MissingPasswordHandler());
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
				.getPollTime() * SECONDS_IN_MINUTE * MILISECONDS_IN_SECOND;
	}

}
