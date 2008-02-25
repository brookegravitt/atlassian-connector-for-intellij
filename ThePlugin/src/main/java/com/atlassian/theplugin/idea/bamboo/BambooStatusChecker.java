package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.ServerType;
import com.atlassian.theplugin.bamboo.BambooBuild;
import com.atlassian.theplugin.bamboo.BambooServerFactory;
import com.atlassian.theplugin.bamboo.BambooStatusListener;
import com.atlassian.theplugin.bamboo.MissingPasswordHandler;
import com.atlassian.theplugin.configuration.ConfigurationFactory;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.idea.SchedulableComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;

import java.awt.*;
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
public final class BambooStatusChecker implements SchedulableComponent {
	private static final long BAMBOO_TIMER_TICK = 120000;

	private final List<BambooStatusListener> listenerList = new ArrayList<BambooStatusListener>();
	private static BambooStatusChecker instance;

	private BambooStatusChecker() {
		super();
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
                                    BambooServerFactory.getBambooServerFacade().getSubscribedPlansResults(server));
                        } catch (ServerPasswordNotProvidedException exception) {
                            ApplicationManager.getApplication().invokeLater(
                                    new MissingPasswordHandler(), ModalityState.defaultModalityState());
                        }
                    }

            // dispatch to the listeners
            EventQueue.invokeLater(new Runnable() {
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

	private static Collection<Server> retrieveEnabledBambooServers() {
		return ConfigurationFactory.getConfiguration().getProductServers(ServerType.BAMBOO_SERVER).getEnabledServers();
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
		return BAMBOO_TIMER_TICK;		
	}

	public static synchronized BambooStatusChecker getInstance() {
		if (instance == null) {
			instance = new BambooStatusChecker();
		}
		return instance;
	}
}
