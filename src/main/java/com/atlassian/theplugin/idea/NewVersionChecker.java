package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.configuration.ConfigurationFactory;
import com.atlassian.theplugin.exception.VersionServiceException;
import com.atlassian.theplugin.util.InfoServer;
import com.intellij.openapi.application.ApplicationManager;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import java.util.TimerTask;

/**
 * Provides functionality to check for new version and update plugin
 */
public final class NewVersionChecker {

	private static NewVersionChecker instance;
	private static final Category LOG = Logger.getInstance(NewVersionChecker.class);

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
				try {
					doRun();
				} catch (VersionServiceException e) {
					LOG.error("Error checking for new version", e);
				}
			}
		};
	}

	private void doRun() throws VersionServiceException {
		InfoServer server = new InfoServer(InfoServer.INFO_SERVER_URL, ConfigurationFactory.getConfiguration().getUid());
		String version = server.getLatestPluginVersion();

		// simple version difference check
		if (!version.equals(PluginInfoUtil.getVersion())) {
			ApplicationManager.getApplication().invokeLater(new ConfirmPluginUpdateHandler(version));
		}
	}
}
