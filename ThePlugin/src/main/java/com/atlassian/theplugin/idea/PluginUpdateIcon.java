package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.configuration.PluginConfiguration;
import com.atlassian.theplugin.exception.IncorrectVersionException;
import com.atlassian.theplugin.exception.VersionServiceException;
import com.atlassian.theplugin.exception.ThePluginException;
import com.atlassian.theplugin.util.InfoServer;
import com.atlassian.theplugin.util.PluginUtil;
import com.atlassian.theplugin.util.Version;
import com.atlassian.theplugin.idea.autoupdate.QueryOnUpdateHandler;
import com.atlassian.theplugin.idea.autoupdate.UpdateActionHandler;
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
	private UpdateActionHandler handler;

	public PluginUpdateIcon(final Project project, final PluginConfiguration pluginConfiguration) {
		super(project);
		handler = new QueryOnUpdateHandler(pluginConfiguration);

		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				innerHideIcon();
				try {
					handler.doAction(version);
				} catch (ThePluginException e1) {
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
