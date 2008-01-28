package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.configuration.ConfigurationFactory;
import com.atlassian.theplugin.configuration.Server;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TimerTask;


/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 15, 2008
 * Time: 4:08:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class BambooStatusChecker extends TimerTask {

	private static final Category LOGGER = Logger.getInstance(BambooStatusChecker.class);

	private List<BambooStatusListenerImpl> listenerList = new ArrayList<BambooStatusListenerImpl>();


	public synchronized void registerListener(BambooStatusListenerImpl listener) {
		listenerList.add(listener);
	}

	public synchronized void unregisterListener(BambooStatusListenerImpl listener) {
		listenerList.remove(listener);
	}

	public synchronized void run() {
		// for each server
		Collection<BambooBuild> newServerBuildsStatus = new ArrayList<BambooBuild>();
		for (int maxTries = 1; maxTries > 0; maxTries--) {
			try {
				for (Server server: ConfigurationFactory.getConfiguration().getBambooConfiguration().getServers()) {
					newServerBuildsStatus.addAll(BambooServerFactory.getBambooServerFacade().getSubscribedPlansResults(server));
				}
			} catch (ServerPasswordNotProvidedException exception) {
				showBlockingDialog();
			}
		}

		for (BambooStatusListenerImpl listener : listenerList) {
			//listener.updateBuildStatuses(newServerBuildsStatus);

			listener.setBuilds(newServerBuildsStatus);
			EventQueue.invokeLater(listener);
		}

	}

	private void showBlockingDialog() {

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
