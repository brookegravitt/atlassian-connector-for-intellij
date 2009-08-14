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
package com.atlassian.theplugin.idea.config;

import com.atlassian.theplugin.commons.cfg.*;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.configuration.WorkspaceConfigurationBean;
import junit.framework.TestCase;
import org.easymock.EasyMock;

/**
 * @author Jacek Jaroczynski
 */
public class CfgManagerNotificationTest extends TestCase {

	//	private CfgManager cfgManager;
	private ProjectCfgManagerImpl projectCfgManager;

	@Override
	public void setUp() throws Exception {
		super.setUp();
//		cfgManager = new CfgManagerImpl();
		projectCfgManager = new ProjectCfgManagerImpl(new WorkspaceConfigurationBean());
		populateServerCfgs();
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	private final BambooServerCfg bamboo1 = new BambooServerCfg("bamboo1", new ServerIdImpl());
	private final JiraServerCfg jira1 = new JiraServerCfg("jira1", new ServerIdImpl());

	private void populateServerCfgs() {
//		cfgManager.addGlobalServer(bamboo);
//		cfgManager.addGlobalServer(jira);

		projectCfgManager.addServer(bamboo1);
		projectCfgManager.addServer(jira1);
	}


	public void testlNotifications() {

		final ProjectConfiguration emptyCfg = ProjectConfiguration.emptyConfiguration();
		ConfigurationListener project1Listener = EasyMock.createMock(ConfigurationListener.class);

		// record
		project1Listener.configurationUpdated(emptyCfg);
		project1Listener.serverRemoved(getServerData(bamboo1));
		project1Listener.serverRemoved(getServerData(jira1));
		project1Listener.bambooServersChanged(emptyCfg);
		project1Listener.jiraServersChanged(emptyCfg);

		// test
		EasyMock.replay(project1Listener);
		projectCfgManager.addProjectConfigurationListener(project1Listener);

//		cfgManager.updateGlobalConfiguration(new GlobalConfiguration());
		projectCfgManager.updateProjectConfiguration(emptyCfg);

		EasyMock.verify(project1Listener);
		EasyMock.reset(project1Listener);

//		cfgManager.updateGlobalConfiguration(new GlobalConfiguration());

		projectCfgManager.removeProjectConfigurationListener(project1Listener);
		final ProjectConfiguration nonEmptyCfg = new ProjectConfiguration(MiscUtil.<ServerCfg>buildArrayList(bamboo1));

		// record

		// test
		EasyMock.replay(project1Listener);
//		cfgManager.updateGlobalConfiguration(new GlobalConfiguration());
		projectCfgManager.updateProjectConfiguration(nonEmptyCfg);
		EasyMock.verify(project1Listener);
	}

	private ServerData getServerData(final ServerCfg serverCfg) {
		return new ServerData(serverCfg, serverCfg.getUsername(), serverCfg.getPassword());
	}


	public void testAddRemoveListener() {
		projectCfgManager.addProjectConfigurationListener(null);
		assertFalse(projectCfgManager.removeProjectConfigurationListener(null));

		ConfigurationListener listener = new ConfigurationListenerAdapter() {
		};

		assertFalse(projectCfgManager.removeProjectConfigurationListener(listener));
		projectCfgManager.addProjectConfigurationListener(listener);
		assertTrue(projectCfgManager.removeProjectConfigurationListener(listener));
		assertFalse(projectCfgManager.removeProjectConfigurationListener(listener));

	}

}
