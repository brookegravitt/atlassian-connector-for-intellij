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
public final class NewVersionChecker implements SchedulableComponent {
	private static final long PLUGIN_UPDATE_ATTEMPT_DELAY = 120000;

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

	public boolean canSchedule() {
		return true; // NewVersionChecker is always enabled
	}

	public long getInterval() {
		return PLUGIN_UPDATE_ATTEMPT_DELAY;
	}

	private void doRun() throws VersionServiceException {
		if (ConfigurationFactory.getConfiguration().isAutoUpdateEnabled() == false) {
			return;
		}
		InfoServer server = new InfoServer(PluginInfoUtil.VERSION_INFO_URL,
				ConfigurationFactory.getConfiguration().getUid());
		InfoServer.VersionInfo versionInfo = server.getLatestPluginVersion();

		// simple versionInfo difference check
		if (!versionInfo.getVersion().equals(PluginInfoUtil.getVersion())) {
			ConfirmPluginUpdateHandler handler = ConfirmPluginUpdateHandler.getInstance();
			handler.setNewVersionInfo(versionInfo);
			ApplicationManager.getApplication().invokeLater(handler);
		}
	}

}
