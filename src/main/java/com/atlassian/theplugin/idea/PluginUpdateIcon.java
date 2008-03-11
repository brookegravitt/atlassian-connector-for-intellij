package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.exception.IncorrectVersionException;
import com.atlassian.theplugin.exception.VersionServiceException;
import com.atlassian.theplugin.util.InfoServer;
import com.atlassian.theplugin.util.PluginUtil;
import com.atlassian.theplugin.configuration.PluginConfiguration;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;

public class PluginUpdateIcon extends StatusBarPluginIcon {

	private static final Icon ICON_BLINK_ON = IconLoader.getIcon("/icons/icn_update_16.png");
	private static final Icon ICON_BLINK_OFF = IconLoader.getIcon("/icons/icn_update_16-empty.png");
	private transient InfoServer.VersionInfo version;
	private transient Timer timer;
	private boolean blinkOn = false;
	private static final int ICON_BLINK_TIME = 1000;
	private PluginConfiguration pluginConfiguration;

	public PluginUpdateIcon(final Project project, final PluginConfiguration pluginConfiguration) {
		super(project);
		this.pluginConfiguration = pluginConfiguration;

		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				innerHideIcon();
				String message = null;
				try {
					InfoServer.Version aVersion = version.getVersion();
					message = "New plugin version " + aVersion + " is available. "
							+ "Your version is " + PluginUtil.getVersion()
							+ ". Do you want to download and install?";
					String title = "New plugin version download";

					int answer = JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(),
							message, title, JOptionPane.YES_NO_OPTION);
					if (answer == JOptionPane.OK_OPTION) {

						// fire downloading and updating plugin in the new thread
						Thread downloader = new Thread(new PluginDownloader(version));
						downloader.start();
					} else {
							pluginConfiguration.setRejectedUpgrade(version.getVersion());
					}

				} catch (VersionServiceException e1) {
					PluginUtil.getLogger().error("Error retrieving new version: " + e1.getMessage(), e1);
				} catch (IncorrectVersionException e1) {
					PluginUtil.getLogger().error("Error retrieving new version: " + e1.getMessage(), e1);
				}
			}
		});
	}

	public void innerHideIcon() {
		if (timer != null) {
			this.timer.cancel();
			this.timer = null;
		}
		hideIcon();
	}

	protected void innerShowIcon() {
		if (!isIconShown()) {
			showIcon();	//To change body of overridden methods use File | Settings | File Templates.
			this.timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
				public void run() {
					blinkIcon();
				}
			}, 0, ICON_BLINK_TIME);
		}
	}

	/**
	 * changes status
	 */
	public void triggerUpdateAvailableAction(InfoServer.VersionInfo newVersion) {
		this.version = newVersion;
		innerShowIcon();
		try {
			this.setToolTipText("New version (" + newVersion.getVersion() + ") of the "
					+ PluginUtil.getName() + " available");
		} catch (VersionServiceException e) {
			PluginUtil.getLogger().warn(e.getMessage(), e);
		} catch (IncorrectVersionException e) {
			PluginUtil.getLogger().warn(e.getMessage(), e);
		}
	}

	public void blinkIcon() {
		if (blinkOn) {
			blinkOn();
			blinkOn = false;
		} else {
			blinkOff();
			blinkOn = true;
		}
	}

	private void blinkOff() {
		this.setIcon(ICON_BLINK_OFF);
	}

	private void blinkOn() {
		this.setIcon(ICON_BLINK_ON);
	}
}
