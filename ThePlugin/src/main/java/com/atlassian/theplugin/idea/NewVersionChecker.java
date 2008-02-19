package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.util.InfoServer;
import com.atlassian.theplugin.configuration.ConfigurationFactory;
import com.atlassian.theplugin.exception.VersionServiceException;

import java.util.TimerTask;

/**
 * Provides functionality to check for new version and update plugin
 */
public class NewVersionChecker {

	private static NewVersionChecker instance;

	private NewVersionChecker() {
		super();
	}

	/**
	 * @return reference to the object of the class (singleton)
	 */
	public static synchronized NewVersionChecker getInstance() {
		if (instance == null) {
			instance = new NewVersionChecker();
		}
		return instance;
	}

	/**
	 * Connects to the server, checks for new version and updates if necessary
	 * @return new TimerTask to be scheduled
	 */
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
			// todo handle exception
			e.printStackTrace();
		}
	}
}
