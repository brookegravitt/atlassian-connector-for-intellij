package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.exception.VersionServiceException;
import com.atlassian.theplugin.util.InfoServer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Feb 26, 2008
 * Time: 2:42:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class PluginUpdateIcon extends StatusBarPluginIcon {
	private static final Category LOGGER = Logger.getInstance(PluginStatusBarToolTip.class);

	private static final Icon ICON_BLINK_ON = IconLoader.getIcon("/icons/icn_update_16.png");
	private static final Icon ICON_BLINK_OFF = IconLoader.getIcon("/icons/icn_update_16-empty.png");
	private transient InfoServer.VersionInfo version;
	private transient Timer timer;
	private boolean blinkOn = false;
	private static final int ICON_BLINK_TIME = 1000;

	public PluginUpdateIcon(final Project project) {
		super(project);

		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				innerHideIcon();
				String message = null;
				try {
					message = "New plugin version " + version.getVersion() + " is available. "
							+ "Your version is " + PluginInfoUtil.getVersion()
							+ ". Do you want to download and install?";
				} catch (VersionServiceException e1) {
					LOGGER.info("Error retrieving new version.");
				}
				String title = "New plugin version download";

				int answer = JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(),
						message, title, JOptionPane.YES_NO_OPTION);

				if (answer == JOptionPane.OK_OPTION) {

					// fire downloading and updating plugin in the new thread
					Thread downloader = new Thread(new PluginDownloader(version));
					downloader.start();
				}
			}
		});
	}

	public void innerHideIcon() {
		if (timer != null) {
			this.timer.cancel();
			this.timer = null;
		}
		hideIcon();	//To change body of overridden methods use File | Settings | File Templates.
	}

	protected void innerShowIcon() {
		showIcon();	//To change body of overridden methods use File | Settings | File Templates.
		this.timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				blinkIcon();
			}
		}, 0, ICON_BLINK_TIME);
	}

	/**
	 * changes status
	 * @param newVersion
	 */
	public void triggerUpdateAvailableAction(InfoServer.VersionInfo newVersion) {
		this.version = newVersion;
		innerShowIcon();
		try {
			this.setToolTipText("New version (" + newVersion.getVersion() + ") of the "
					+ PluginInfoUtil.getName() + " available");
		} catch (VersionServiceException e) {
			LOGGER.info(e.getMessage(), e);
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
