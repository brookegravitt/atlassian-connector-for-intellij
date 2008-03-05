package com.atlassian.theplugin.idea.config;

import com.atlassian.theplugin.bamboo.BambooServerFactory;
import com.atlassian.theplugin.bamboo.api.BambooLoginException;
import com.intellij.openapi.ui.Messages;
import static com.intellij.openapi.ui.Messages.showMessageDialog;
import org.apache.log4j.Category;

import java.awt.*;


public class BambooTestConnection extends Thread {
	private String url;
	private String userName;
	private String password;

	private boolean isRunning = true;
	private static final Category LOG = Category.getInstance(BambooTestConnection.class);
	private boolean interrupted = false;

	public BambooTestConnection(String url, String userName, String password) {
		this.url = url;
		this.userName = userName;
		this.password = password;
	}

	public void run() {

		isRunning = true;

		try {
			//System.out.println("Y: Connecting...");
			BambooServerFactory.getBambooServerFacade().testServerConnection(url, userName, password);
			//System.out.println("Y: OK...");

			EventQueue.invokeLater(new Runnable() {
				public void run() {
					showMessageDialog("Connected successfully", "Connection OK", Messages.getInformationIcon());
				}
			});
		} catch (final BambooLoginException e) {
			if (!interrupted) {
				//System.out.println("Y: Failed...");
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						showMessageDialog(e.getMessage(), "Connection Error", Messages.getErrorIcon());
					}
				});
			}
		}
//		catch (IllegalThreadStateException e) {
//			System.out.println("Y: Interupted...");
//			LOG.info(e.getMessage());
//		} 
		finally {
			this.isRunning = false;
		}
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setInterrupted() {
		this.interrupted = true;
	}
}
