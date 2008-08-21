/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.idea.autoupdate;

import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.commons.exception.ThePluginException;
import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.idea.StatusBarPluginIcon;
import com.atlassian.theplugin.util.InfoServer;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;

public class PluginUpdateIcon extends StatusBarPluginIcon {

	private static final Icon ICON_BLINK_ON = IconLoader.getIcon("/icons/status_plugin.png");
	private static final Icon ICON_BLINK_OFF = IconLoader.getIcon("/icons/icn_update_16-empty.png");
	private transient InfoServer.VersionInfo version;
	private transient Timer timer;
	private boolean blinkOn = false;
	private static final int ICON_BLINK_TIME = 1000;
	private transient UpdateActionHandler handler = null;

	public PluginUpdateIcon(final Project project, final PluginConfiguration pluginConfiguration, final CfgManager cfgManager) {
		super(project, cfgManager);
		handler = new NewVersionConfirmHandler(this, project, pluginConfiguration.getGeneralConfigurationData());

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				stopBlinking();
				try {
					handler.doAction(version, true);
				} catch (ThePluginException e1) {
					PluginUtil.getLogger().info("Error retrieving new version: " + e1.getMessage(), e1);
				}
			}
		});
	}

	public void stopBlinking() {
		if (timer != null) {
			this.timer.cancel();
			this.timer = null;
		}
		hideIcon();
	}

	protected void startBlinking() {
		if (this.timer == null) {
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
		startBlinking();
		this.setToolTipText("New version (" + newVersion.getVersion() + ") of the "
				+ PluginUtil.getInstance().getName() + " available");

	}

	public void blinkIcon() {
		if (!isIconShown()) {
			showIcon();
		}
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
