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
package com.atlassian.theplugin.idea.config.serverconfig.defaultCredentials;

import com.atlassian.connector.intellij.bamboo.IntelliJBambooServerFacade;
import com.atlassian.theplugin.ConnectionWrapper;
import com.atlassian.theplugin.commons.jira.IntelliJJiraServerFacade;
import com.atlassian.theplugin.idea.TestConnectionProcessor;
import com.atlassian.theplugin.idea.config.serverconfig.ProductConnector;
import com.atlassian.theplugin.util.PluginUtil;

/**
 * User: pmaruszak
 */
public class TestConnectionThread extends Thread {
	private static final int CHECK_CANCEL_INTERVAL = 500;
	private final TestConnectionProcessor testConnectionProcessor;
	private final ServerDataExt server;

	public TestConnectionThread(@org.jetbrains.annotations.NotNull String s, TestConnectionProcessor testConnectionProcessor,
			ServerDataExt server) {
		super(s);
		this.testConnectionProcessor = testConnectionProcessor;
		this.server = server;
	}

	@Override
	public void run() {
			ProductConnector productConnector;
			ConnectionWrapper testConnector;
			switch (server.getServerType()) {
				case JIRA_SERVER:
					productConnector = new ProductConnector(IntelliJJiraServerFacade.getInstance());
					break;

				case BAMBOO_SERVER:
					productConnector = new ProductConnector(IntelliJBambooServerFacade.getInstance(PluginUtil.getLogger()));
					break;
				default:
					return;

			}
			testConnector = new ConnectionWrapper(productConnector, server.getServerData(), "testing connection");

			testConnector.start();
			while (testConnector.getConnectionState() == ConnectionWrapper.ConnectionState.NOT_FINISHED) {
				try {
					if (false) {
						testConnector.setInterrupted();
						//t.interrupt();
						break;
					} else {
						java.lang.Thread.sleep(CHECK_CANCEL_INTERVAL);
					}
				} catch (InterruptedException e) {
					//log.info(e.getMessage());
				}
			}

			if (testConnector.getConnectionState() == ConnectionWrapper.ConnectionState.SUCCEEDED) {
				testConnectionProcessor.onSuccess();
			} else {
				testConnectionProcessor.onError(testConnector.getErrorMessage(), null, null);
			}

		}


}
