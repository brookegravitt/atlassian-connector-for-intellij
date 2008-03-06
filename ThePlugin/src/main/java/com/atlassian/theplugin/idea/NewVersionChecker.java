package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.configuration.PluginConfiguration;
import com.atlassian.theplugin.exception.IncorrectVersionException;
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

	private static final Category LOG = Logger.getInstance(NewVersionChecker.class);

	private final transient PluginConfiguration pluginConfiguration;

	public NewVersionChecker(PluginConfiguration pluginConfiguration) {
		this.pluginConfiguration = pluginConfiguration;
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

	public boolean canSchedule() {
		return true; // NewVersionChecker is always enabled
	}

	public long getInterval() {
		return PLUGIN_UPDATE_ATTEMPT_DELAY;
	}

	private void doRun() {
		if (!pluginConfiguration.isAutoUpdateEnabled()) {
			return;
		}
		InfoServer server =  new InfoServer(PluginUtil.VERSION_INFO_URL,
				pluginConfiguration.getUid());
		try {
			InfoServer.VersionInfo versionInfo = server.getLatestPluginVersion();
			// simple versionInfo difference check
			InfoServer.Version newVersion = versionInfo.getVersion();
			InfoServer.Version thisVersion = new InfoServer.Version(PluginUtil.getVersion());
			if (newVersion.greater(thisVersion)) {
				ConfirmPluginUpdateHandler handler = ConfirmPluginUpdateHandler.getInstance();
				handler.setNewVersionInfo(versionInfo);
				ApplicationManager.getApplication().invokeLater(handler);
			}
		} catch (VersionServiceException e) {
			LOG.info("Error checking new version: " + e.getMessage());
		} catch (IncorrectVersionException e) {
			LOG.info("Error checking new version: " + e.getMessage());
		}
	}

}
