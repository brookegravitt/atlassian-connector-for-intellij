package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.configuration.PluginConfiguration;
import com.atlassian.theplugin.exception.VersionServiceException;
import com.atlassian.theplugin.exception.ThePluginException;
import com.atlassian.theplugin.util.InfoServer;
import com.atlassian.theplugin.util.PluginUtil;
import com.atlassian.theplugin.util.Version;
import com.atlassian.theplugin.idea.autoupdate.UpdateActionHandler;
import com.atlassian.theplugin.idea.autoupdate.ForwardToIconHandler;

import java.util.TimerTask;

/**
 * Provides functionality to check for new version and update plugin
 */
public final class NewVersionChecker implements SchedulableComponent {
	private static final long PLUGIN_UPDATE_ATTEMPT_DELAY = 120000;

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
				try {
					doRun(new ForwardToIconHandler(pluginConfiguration));
				} catch (ThePluginException e) {
					PluginUtil.getLogger().info("Error checking new version: " + e.getMessage());
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

	protected void doRun(UpdateActionHandler action) throws ThePluginException {
		if (!pluginConfiguration.isAutoUpdateEnabled()) {
			return;
		}
		if (action == null) {
			throw new IllegalArgumentException("Action handler not provided.");
		}
		InfoServer.VersionInfo versionInfo = getLatestVersion();
		Version newVersion = versionInfo.getVersion();
		Version thisVersion = new Version(PluginUtil.getVersion());
		if (newVersion.greater(thisVersion)) {
			action.doAction(versionInfo);
		}
	}

	private InfoServer.VersionInfo getLatestVersion() throws VersionServiceException {
		InfoServer server =  new InfoServer(PluginUtil.VERSION_INFO_URL,
				pluginConfiguration.getUid());
		InfoServer.VersionInfo versionInfo = server.getLatestPluginVersion();
		return versionInfo;
	}

}
