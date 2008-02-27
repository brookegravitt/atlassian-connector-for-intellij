package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class BambooPlanReader implements Runnable {
	private final List<BambooPlanListener> listenerList = new ArrayList<BambooPlanListener>();
	private Server server;

	public BambooPlanReader(Server server) {
		this.server = server;
	}

	public void registerListener(BambooPlanListener listener) {
		synchronized (listenerList) {
			listenerList.add(listener);
		}
	}

	public void unregisterListener(BambooPlanListener listener) {
		synchronized (listenerList) {
			listenerList.remove(listener);
		}
	}

	public void run() {
		try {
			try {
				Collection<BambooPlan> plans = BambooServerFactory.getBambooServerFacade().getPlanList(server);
				synchronized (listenerList) {
					for (BambooPlanListener listener : listenerList) {
						listener.updatePlanNames(server, plans);
					}
				}
			} catch (ServerPasswordNotProvidedException e) {
				e.printStackTrace();
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}