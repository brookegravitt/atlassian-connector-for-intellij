package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.configuration.PluginConfiguration;
import com.atlassian.theplugin.exception.ThePluginException;
import com.atlassian.theplugin.idea.autoupdate.QueryOnUpdateHandler;
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
	private CheckerThread checkerThread;
	private static final long CHECK_CANCEL_INTERVAL = 500; //milis
	private NewVersionChecker checker;
	private PluginConfiguration pluginConfiguration;

	public NewVersionListener(NewVersionChecker checker, PluginConfiguration pluginConfiguration) {
		this.checker = checker;
		this.pluginConfiguration = pluginConfiguration;
	}
	
	public void actionPerformed(ActionEvent event) {
		ProgressManager.getInstance().run(new Task.Modal(IdeaHelper.getCurrentProject(),
				"Checking available updates", true) {
			public void run(ProgressIndicator indicator) {
				setCancelText("Stop");
				indicator.setText("Connecting...");
				indicator.setFraction(0);
				indicator.setIndeterminate(true);
				checkerThread = new CheckerThread(checker, pluginConfiguration);
				checkerThread.start();
				while (checkerThread.getConnectionState() == CheckerThread.ConnectionState.NOT_FINISHED) {
					try {
						if (indicator.isCanceled()) {
							checkerThread.setInterrupted();
							//t.interrupt();
							break;
						} else {
							java.lang.Thread.sleep(CHECK_CANCEL_INTERVAL);
						}
					} catch (InterruptedException e) {
						PluginUtil.getLogger().info(e.getMessage());
					}
				}

				if (checkerThread.getConnectionState() == CheckerThread.ConnectionState.FAILED) {
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							showMessageDialog(checkerThread.getErrorMessage(),
									"Error occured when contacting update server", Messages.getErrorIcon());
						}
					});
				} else if (checkerThread.getConnectionState() == CheckerThread.ConnectionState.INTERUPTED) {
					PluginUtil.getLogger().debug("Cancel was pressed during the upgrade process");
				} else {
					PluginUtil.getLogger().info("Unexpected thread state: "
							+ checkerThread.getConnectionState().toString());
				}
			}
		});
	}

	private static class CheckerThread extends Thread {
		private ConnectionState connectionState = ConnectionState.NOT_FINISHED;
		private String errorMessage;

		public String getErrorMessage() {
			return errorMessage;
		}

		public enum ConnectionState {
			SUCCEEDED,
			FAILED,
			INTERUPTED,
			NOT_FINISHED
		}


		private NewVersionChecker timerTask;
		private PluginConfiguration pluginConfiguration;

		@Override
		public void run() {
		try {
			timerTask.doRun(new QueryOnUpdateHandler(pluginConfiguration));
			if (connectionState != ConnectionState.INTERUPTED) {
				connectionState = ConnectionState.SUCCEEDED;
			}
		} catch (ThePluginException e) {
			if (connectionState != ConnectionState.INTERUPTED) {
				connectionState = ConnectionState.FAILED;
				errorMessage = e.getMessage();
			}
		}
		}

		public CheckerThread(NewVersionChecker timerTask, PluginConfiguration pluginConfiguration) {
			this.timerTask = timerTask;
			this.pluginConfiguration = pluginConfiguration;
		}

		public ConnectionState getConnectionState() {
			return connectionState;
		}

		public void setInterrupted() {
			connectionState = ConnectionState.INTERUPTED;
		}

	}
}
