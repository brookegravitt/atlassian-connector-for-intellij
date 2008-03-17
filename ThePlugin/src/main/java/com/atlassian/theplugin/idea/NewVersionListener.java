package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.ConnectionWrapper;
import com.atlassian.theplugin.configuration.PluginConfiguration;
import com.atlassian.theplugin.exception.ThePluginException;
import com.atlassian.theplugin.idea.autoupdate.UpdateActionHandler;
import com.atlassian.theplugin.idea.autoupdate.QueryOnUpdateHandler;
import com.atlassian.theplugin.util.Connector;
import com.atlassian.theplugin.util.InfoServer;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.Messages;
import static com.intellij.openapi.ui.Messages.showMessageDialog;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Mar 12, 2008
 * Time: 5:04:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class NewVersionListener implements ActionListener {
	private ConnectionWrapper checkerThread;
	private static final long CHECK_CANCEL_INTERVAL = 500; //milis
	private NewVersionChecker checker;
	private PluginConfiguration pluginConfiguration;
	private InfoServer.VersionInfo newVersion;

	public NewVersionListener(NewVersionChecker checker, PluginConfiguration pluginConfiguration) {
		this.checker = checker;
		this.pluginConfiguration = pluginConfiguration;
	}
	
	public void actionPerformed(ActionEvent event) {
		ProgressManager.getInstance().run(new UpdateModalTask());
	}

	private class UpdateServerConnection extends Connector {
		public void connect() throws ThePluginException {
			checker.doRun(new UpdateActionHandler() {
				public void doAction(InfoServer.VersionInfo versionInfo) throws ThePluginException {
					newVersion = versionInfo;
				}
			});
		}
	}

	private class UpdateModalTask extends Task.Modal {
		public UpdateModalTask() {
			super(IdeaHelper.getCurrentProject(), "Checking available updates", true);
		}

		public void run(ProgressIndicator indicator) {
			newVersion = null;
			setCancelText("Stop");
			indicator.setText("Connecting...");
			indicator.setFraction(0);
			indicator.setIndeterminate(true);
			checkerThread = new ConnectionWrapper(new UpdateServerConnection());
			checkerThread.start();
			while (checkerThread.getConnectionState() == ConnectionWrapper.ConnectionState.NOT_FINISHED)

			{
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
							showMessageDialog(checkerThread.getErrorMessage(),
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
						try {
							new QueryOnUpdateHandler(pluginConfiguration).doAction(newVersion);
						} catch (final ThePluginException e) {
							EventQueue.invokeLater(new Runnable() {
								public void run() {
									showMessageDialog(e.getMessage(),
											"Error retrieving new version", Messages.getErrorIcon());
								}
							});
						}
					} else {
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								showMessageDialog("You have the latest version (\"" + PluginUtil.getVersion() + "\")",
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
	}
}
