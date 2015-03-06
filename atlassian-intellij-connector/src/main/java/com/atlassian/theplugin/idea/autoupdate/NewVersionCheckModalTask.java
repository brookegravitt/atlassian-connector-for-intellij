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

import com.atlassian.theplugin.ConnectionWrapper;
import com.atlassian.theplugin.commons.configuration.GeneralConfigurationBean;
import com.atlassian.theplugin.commons.exception.ThePluginException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.util.Connector;
import com.atlassian.theplugin.util.InfoServer;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * @author Jacek Jaroczynski
 */
class NewVersionCheckModalTask extends Task.Modal {
	private static final long CHECK_CANCEL_INTERVAL = 500; //milis

	private Component parentWindow;
	private InfoServer.VersionInfo newVersion;
	private GeneralConfigurationBean config;
	private boolean showConfigPath;

	public NewVersionCheckModalTask(Component parentWindow, GeneralConfigurationBean config, final boolean showConfigPath) {
		super(null, "Checking available updates", true);
		this.parentWindow = parentWindow;
		this.config = config;
		this.showConfigPath = showConfigPath;
	}

	public void run(@NotNull ProgressIndicator indicator) {
		newVersion = null;
		setCancelText("Stop");
		indicator.setText("Connecting...");
		indicator.setFraction(0);
		indicator.setIndeterminate(true);
		final ConnectionWrapper checkerThread = new ConnectionWrapper(new UpdateServerConnection(), null,
				"atlassian-idea-plugin New version checker");
		checkerThread.start();
		while (checkerThread.getConnectionState() == ConnectionWrapper.ConnectionState.NOT_FINISHED) {
			try {
				if (indicator.isCanceled()) {
					checkerThread.setInterrupted();
					//t.interrupt();
					break;
				} else {
					Thread.sleep(CHECK_CANCEL_INTERVAL);
				}
			} catch (InterruptedException e) {
				PluginUtil.getLogger().info(e.getMessage());
			}
		}

		switch (checkerThread.getConnectionState()) {
			case FAILED:
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						com.intellij.openapi.ui.Messages.showMessageDialog(parentWindow, checkerThread.getErrorMessage(),
								"Error occured when contacting update server", Messages.getErrorIcon());
					}
				});
				break;
			case INTERUPTED:
				PluginUtil.getLogger().debug("Cancel was pressed during the upgrade process");
				break;
			case NOT_FINISHED:
				break;
			case SUCCEEDED:
				if (newVersion != null) {
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							try {
								new NewVersionConfirmHandler(parentWindow, config)
										.doAction(newVersion, showConfigPath);
							} catch (ThePluginException e) {
								com.intellij.openapi.ui.Messages.showMessageDialog(parentWindow, e.getMessage(),
										"Error retrieving new version", Messages.getErrorIcon());
							}
						}
					});
				} else {
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							com.intellij.openapi.ui.Messages.showMessageDialog(parentWindow,
									"You have the latest version (" + PluginUtil.getInstance().getVersion() + ")",
									"Version checked", Messages.getInformationIcon());
						}
					});
				}
				break;
			default:
				PluginUtil.getLogger().info("Unexpected thread state: "
						+ checkerThread.getConnectionState().toString());
		}
	}

	private class UpdateServerConnection implements Connector {
		protected UpdateServerConnection() {
		}

		public void connect(ServerData serverData) throws RemoteApiException {
			try {
				NewVersionChecker.getInstance().doRun(new UpdateActionHandler() {
					public void doAction(InfoServer.VersionInfo versionInfo, boolean aShowConfigPath)
							throws ThePluginException {
						newVersion = versionInfo;
					}
				}, false, config, true);
			} catch (ThePluginException e) {
				throw new RemoteApiException(e);
			}
		}

		public void onSuccess() {
		}
	}
}
