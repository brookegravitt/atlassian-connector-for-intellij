package com.atlassian.theplugin.idea.config;

import com.atlassian.theplugin.exception.ThePluginException;
import com.atlassian.theplugin.idea.config.serverconfig.ConnectionTester;
import com.intellij.openapi.ui.Messages;
import static com.intellij.openapi.ui.Messages.showMessageDialog;

import java.awt.*;


public class TestConnectionThread extends Thread {
	private String url;
	private String userName;
	private String password;

	private boolean isRunning = true;
	private boolean interrupted = false;
	private ConnectionTester connectionTester;

	public TestConnectionThread(ConnectionTester tester, String url, String userName, String password) {
		this.connectionTester = tester;
		this.url = url;
		this.userName = userName;
		this.password = password;
	}

	/**
	 * Runs test connection method on a ConnectionTester and shows MessageBox with result.
	 * That method should not be used directly but using 'start' method on a thread object. 
	 */
	public void run() {

		isRunning = true;

		try {
			connectionTester.testConnection(userName, password, url);
			if (!interrupted) {
				showSuccessMessage();
			}
		} catch (ThePluginException e) {
			if (!interrupted) {
				showFailMessage(e.getMessage());
			}
		} finally {
			this.isRunning = false;
		}
	}

	private void showSuccessMessage() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				showMessageDialog("Connected successfully", "Connection OK", Messages.getInformationIcon());
			}
		});
	}

	private void showFailMessage(final String message) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				showMessageDialog(message, "Connection Error", Messages.getErrorIcon());
			}
		});
	}

	/**
	 *
	 * @return information whether the thread is still running or not
	 */
	public boolean isRunning() {
		return isRunning;
	}

	/**
	 * Send request to thread to stop
	 */
	public void setInterrupted() {
		this.interrupted = true;
	}
}
