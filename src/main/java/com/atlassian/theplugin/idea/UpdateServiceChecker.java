package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.idea.crucible.CrucibleStatusChecker;
import com.atlassian.theplugin.util.InfoServer;
import com.atlassian.theplugin.configuration.ConfigurationFactory;
import com.atlassian.theplugin.exception.VersionServiceException;

import java.util.TimerTask;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Feb 19, 2008
 * Time: 3:06:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateServiceChecker {

	private static UpdateServiceChecker instance;

	private UpdateServiceChecker() {
		super();
	}

	public static synchronized UpdateServiceChecker getInstance() {
		if(instance == null) {
			instance = new UpdateServiceChecker();
		}
		return instance;
	}

	public TimerTask newTimerTask() {
		return new TimerTask() {
			public void run() {
				doRun();
			}
		};
	}

	private void doRun() {
		InfoServer server = new InfoServer(InfoServer.INFO_SERVER_URL, ConfigurationFactory.getConfiguration().getUid());
		try {
			String version = server.getLatestPluginVersion();
			// todo lguminski display dialog for update if newer version exists
		} catch (VersionServiceException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}
}
