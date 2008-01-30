package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.bamboo.BambooBuild;
import com.atlassian.theplugin.bamboo.BambooServerFactory;
import com.atlassian.theplugin.bamboo.BambooStatusListener;
import com.atlassian.theplugin.bamboo.MissingPasswordHandler;
import com.atlassian.theplugin.configuration.ConfigurationFactory;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TimerTask;


/**
 * IDEA-specific class that uses {@link com.atlassian.theplugin.bamboo.BambooServerFactory} to retrieve builds info and
 * passes raw data to configured {@link com.atlassian.theplugin.bamboo.BambooStatusListener}s.
 */
public class BambooStatusChecker extends TimerTask implements Cloneable {

	private List<BambooStatusListener> listenerList = new ArrayList<BambooStatusListener>();

	@Override
	public Object clone() throws CloneNotSupportedException {
		super.clone();
		BambooStatusChecker result = new BambooStatusChecker();
		result.listenerList = listenerList;
		return result;
	}


	public void registerListener(BambooStatusListener listener) {
		synchronized(listenerList) {
			listenerList.add(listener);
		}
	}

	public void unregisterListener(BambooStatusListener listener) {
		synchronized(listenerList) {
			listenerList.remove(listener);
		}
	}

	/**
	 * DO NOT use that method in 'dispatching thread' of IDEA. It can block GUI for several seconds.
	 * The method should be call in a separate thread (currently called by timer set in ApplicationComponent) 
	 */
	public void run() {
		// for each server
		final Collection<BambooBuild> newServerBuildsStatus = new ArrayList<BambooBuild>();
		for (int maxTries = 1; maxTries > 0; maxTries--) {
			try {
				for (Server server : ConfigurationFactory.getConfiguration().getBambooConfiguration().getServers()) {
					newServerBuildsStatus.addAll(BambooServerFactory.getBambooServerFacade().getSubscribedPlansResults(server));
				}
			} catch (ServerPasswordNotProvidedException exception) {
				showBlockingDialog();
			}
		}

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				synchronized(listenerList) {
					for (BambooStatusListener listener : listenerList) {
						listener.updateBuildStatuses(newServerBuildsStatus);
					}
				}
			}
		});

	}

	private void showBlockingDialog() {

		//TODO: this is IDEA specific, it must not be in general package. Move to idea package
		MissingPasswordHandler handler = new MissingPasswordHandler();

		EventQueue.invokeLater(handler);

//		try {
//			EventQueue.invokeAndWait(handler);
//		} catch (InterruptedException e) {
//			LOGGER.warn("Missing password dialog problem", e);
//		} catch (InvocationTargetException e) {
//			LOGGER.warn("Missing password dialog problem", e);
//		}
	}

}
