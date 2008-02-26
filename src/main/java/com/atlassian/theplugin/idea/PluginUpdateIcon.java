package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.exception.VersionServiceException;
import com.atlassian.theplugin.util.InfoServer;
import com.atlassian.theplugin.ServerType;
import com.atlassian.theplugin.StatusBarPluginIcon;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.project.Project;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Feb 26, 2008
 * Time: 2:42:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class PluginUpdateIcon extends StatusBarPluginIcon {
	private static final Category LOGGER = Logger.getInstance(PluginStatusBarToolTip.class);

	private static final Icon ICON_NEW = IconLoader.getIcon("/icons/icn_update_16.png");
	private InfoServer.VersionInfo version;
	private Project project;

	public PluginUpdateIcon(final Project project) {
		super(project);
		resetIcon();

		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				String message = null;
				try {
					message = "New plugin version " + version.getVersion() + " is available. "
							+ "Your version is " + PluginInfoUtil.getVersion()
							+ ". Do you want to download and install?";
				} catch (VersionServiceException e1) {
					LOGGER.error("Error retrieving new version.");
				}
				String title = "New plugin version download";

				int answer = JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(),
						message, title, JOptionPane.YES_NO_OPTION);

				if (answer == JOptionPane.OK_OPTION) {

					// fire downloading and updating plugin in the new thread
					Thread downloader = new Thread(new PluginDownloader(version));

					downloader.start();
				}
				resetIcon();
			}
		});
	}

	/**
	 * changes status
	 * @param newVersion
	 */
	public void triggerUpdateAvailableAction(InfoServer.VersionInfo newVersion) {
		this.version = newVersion;
		this.setIcon(ICON_NEW);
		try {
			this.setToolTipText("New version (" + newVersion.getVersion() + ") of the "
					+ PluginInfoUtil.getName() + " available");
		} catch (VersionServiceException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	/**
	 * Sets the icon to standard state (sets grey icon, removes text label, change tooltip)
	 */
	public void resetIcon() {
		this.setIcon(null);
		this.setToolTipText(null);
		this.setText(null);
	}

	public void showOrHideIcon() {
		super.showOrHideIcon(ServerType.CRUCIBLE_SERVER);
	}
}
