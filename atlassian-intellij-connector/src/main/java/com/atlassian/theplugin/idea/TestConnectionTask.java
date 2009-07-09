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
package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.ConnectionWrapper;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.util.Connector;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import static com.intellij.openapi.ui.Messages.showMessageDialog;
import org.apache.log4j.Category;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * User: pmaruszak
 */
public class TestConnectionTask extends Task.Modal {

	private static final int CHECK_CANCEL_INTERVAL = 500;	// miliseconds
	private final Category log = Category.getInstance(TestConnectionTask.class);
	private final ConnectionWrapper testConnector;
	private final TestConnectionProcessor processor;
	private final ServerData serverData;
	private boolean showOkMessage = true;
	private boolean showErrorMessage = true;



	public TestConnectionTask(Project currentProject, final Connector connectionTester,
			ServerData serverData, final TestConnectionProcessor processor,
			String title, boolean canBeCanceled, boolean showOkMessage, boolean showErrorMessage) {
		this(currentProject, connectionTester, serverData, processor, title, canBeCanceled);
		this.showOkMessage = showOkMessage;
		this.showErrorMessage = showErrorMessage;		
	}

	public TestConnectionTask(Project currentProject, final Connector connectionTester,
			ServerData serverData, final TestConnectionProcessor processor,
			String title, boolean canBeCanceled) {
		super(currentProject, title, canBeCanceled);
		this.processor = processor;
		this.serverData = serverData;
		testConnector = new ConnectionWrapper(connectionTester, serverData, "test thread");
		
	}

	@Override
	public void run(@NotNull ProgressIndicator indicator) {

		if (indicator == null) {
			PluginUtil.getLogger().error("Progress Indicator is null in TestConnectionTask!!!");
			System.out.println("Progress Indicator is null in TestConnectionTask!!!");
		} else {
			indicator.setText("Connecting...");
			indicator.setFraction(0);
			indicator.setIndeterminate(true);
		}
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

		ConnectionWrapper.ConnectionState state = testConnector.getConnectionState();
		processor.setConnectionResult(state);
		switch (testConnector.getConnectionState()) {
			case FAILED:
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						if (showErrorMessage) {
							DialogWithDetails.showExceptionDialog(getProject(),
									serverData.getName() + " : " + testConnector.getErrorMessage(),
									testConnector.getException(), HelpUrl.getHelpUrl(Constants.HELP_TEST_CONNECTION));
						}
						processor.onError(testConnector.getErrorMessage(), testConnector.getException(), 
                                HelpUrl.getHelpUrl(Constants.HELP_TEST_CONNECTION));
					}
				});
				break;
			case INTERUPTED:
				log.debug("Cancel was pressed during 'Test Connection' operation");
				break;
			case SUCCEEDED:
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						if (showOkMessage) {
							showMessageDialog(getProject(), "Connected successfully", "Connection OK",
									Messages.getInformationIcon());
						}
						processor.onSuccess();
					}
				});
				break;
			default: //NOT_FINISHED:
				log.warn("Unexpected 'Test Connection' thread state: "
						+ testConnector.getConnectionState().toString());
		}
	}
}

