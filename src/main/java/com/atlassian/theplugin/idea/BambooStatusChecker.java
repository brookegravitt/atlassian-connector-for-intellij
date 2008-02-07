package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.bamboo.BambooBuild;
import com.atlassian.theplugin.bamboo.BambooServerFactory;
import com.atlassian.theplugin.bamboo.BambooStatusListener;
import com.atlassian.theplugin.bamboo.MissingPasswordHandler;
import com.atlassian.theplugin.configuration.ConfigurationFactory;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.ServerType;

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
public class BambooStatusChecker {

	private final List<BambooStatusListener> listenerList = new ArrayList<BambooStatusListener>();

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
		// collect build info from each server
		final Collection<BambooBuild> newServerBuildsStatus = new ArrayList<BambooBuild>();
		for (Server server : ConfigurationFactory.getConfiguration().getProductServers(ServerType.BAMBOO_SERVER).getServers()) {
			try {
				newServerBuildsStatus.addAll(BambooServerFactory.getBambooServerFacade().getSubscribedPlansResults(server));
			} catch (ServerPasswordNotProvidedException exception) {
				EventQueue.invokeLater(new MissingPasswordHandler());
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


}
