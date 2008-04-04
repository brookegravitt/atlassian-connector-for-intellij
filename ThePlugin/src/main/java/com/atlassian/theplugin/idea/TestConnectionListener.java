package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.ConnectionWrapper;
import com.atlassian.theplugin.LoginDataProvided;
import com.atlassian.theplugin.util.Connector;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import static com.intellij.openapi.ui.Messages.showMessageDialog;
import org.apache.log4j.Category;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Listens for the click action (usually on a 'Test Connection' button), displays progress dialog with Cancel button
 * and run in a separate thread testConnection method on a ConnectionTester object passed to the constructor.
 * Displays message dialog with connection success/failure unless connection test was canceled.
*/
public class TestConnectionListener implements ActionListener {

	private Connector connectionTester = null;
	private LoginDataProvided loginDataProvided = null;

	/**
	 * @param tester object which provide testConnection method specific to the product (Bamboo/Crucible, etc.)
	 * @param loginDataProvided object with methods which provide userName, password and url for connection
	 */
	public TestConnectionListener(Connector tester, LoginDataProvided loginDataProvided) {
		connectionTester = tester;
		this.loginDataProvided = loginDataProvided;
	}

	public void actionPerformed(ActionEvent e) {

		Task.Modal testConnectionTask =	new TestConnectionTask(
				IdeaHelper.getCurrentProject(), "Testing Connection", true, connectionTester);
		testConnectionTask.setCancelText("Stop");

		ProgressManager.getInstance().run(testConnectionTask);
	}

	private class TestConnectionTask extends Task.Modal {

		private ConnectionWrapper testConnector = null;
		private static final int CHECK_CANCEL_INTERVAL = 500;	// miliseconds
		private final Category log = Category.getInstance(TestConnectionTask.class);

		public TestConnectionTask(Project currentProject, String title, boolean canBeCanceled,
								  Connector tester) {
			super(currentProject, title, canBeCanceled);
			tester.setUrl(loginDataProvided.getServerUrl());
			tester.setUserName(loginDataProvided.getUserName());
			tester.setPassword(loginDataProvided.getPassword());
			testConnector = new ConnectionWrapper(tester, "test thread");
		}

		public void run(ProgressIndicator indicator) {

			indicator.setText("Connecting...");
			indicator.setFraction(0);
			indicator.setIndeterminate(true);

			testConnector.start();

			while (testConnector.getConnectionState() == ConnectionWrapper.ConnectionState.NOT_FINISHED) {
				try {
					if (indicator.isCanceled()) {
						testConnector.setInterrupted();
						//t.interrupt();
						break;
					} else {
						java.lang.Thread.sleep(CHECK_CANCEL_INTERVAL);
					}
				} catch (InterruptedException e) {
					log.info(e.getMessage());
				}
			}

			switch (testConnector.getConnectionState()) {
				case FAILED:
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							showMessageDialog(testConnector.getErrorMessage(),
									"Connection Error", Messages.getErrorIcon());
						}
					});
					break;
				case INTERUPTED:
					log.debug("Cancel was pressed during 'Test Connection' operation");
					break;
				case SUCCEEDED:
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							showMessageDialog("Connected successfully", "Connection OK", Messages.getInformationIcon());
						}
					});
					break;
				default: //NOT_FINISHED:
					log.warn("Unexpected 'Test Connection' thread state: "
							+ testConnector.getConnectionState().toString());
			}
		}
	}
}