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

import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.util.Connector;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Listens for the click action (usually on a 'Test Connection' button), displays progress dialog with Cancel button
 * and run in a separate thread testConnection method on a ConnectionTester object passed to the constructor.
 * Displays message dialog with connection success/failure unless connection test was canceled.
 */
public class TestConnectionListener implements ActionListener {

	public interface ServerDataProvider {
		ServerData getServer();
	}

	private final Project project;
	private Connector connectionTester;
	private final ServerDataProvider serverCfgProvider;
	private final TestConnectionProcessor processor;

	/**
	 * @param project		   IDEA project
	 * @param tester			object which provide testConnection method specific to the product (Bamboo/Crucible, etc.)
	 * @param serverCfgProvider provides the data of the server to connect to
	 * @param processor
	 */
	public TestConnectionListener(Project project, Connector tester, @NotNull ServerDataProvider serverCfgProvider,
			@NotNull TestConnectionProcessor processor) {
		this.project = project;
		connectionTester = tester;
		this.serverCfgProvider = serverCfgProvider;
		this.processor = processor;
	}

	public void actionPerformed(ActionEvent e) {

		Task.Modal testConnectionTask = new TestConnectionTask(project, connectionTester, serverCfgProvider.getServer(),
				processor, "Testing Connection", true);
		testConnectionTask.setCancelText("Stop");
		ProgressManager.getInstance().run(testConnectionTask);
	}


}