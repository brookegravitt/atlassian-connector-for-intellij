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
import junit.framework.TestCase;
import org.easymock.EasyMock;

/**
 * @author Jacek Jaroczynski
 */
public class CfgManagerNotificationSecondTest extends TestCase {

	//	private CfgManager cfgManager;
	private ConfigurationListener listener;
	private ProjectConfiguration newConf;
	private static final String SUFFIX = "SUFFIX";
	private ProjectCfgManagerImpl projectCfgManager;

	@Override
	public void setUp() throws Exception {
		super.setUp();
//		cfgManager = new CfgManagerImpl();
		projectCfgManager = new ProjectCfgManagerImpl(null, new CfgManagerImpl(), null);

		listener = EasyMock.createMock(ConfigurationListener.class);
		projectCfgManager.addProjectConfigurationListener(listener);

		populateServerCfgs();
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	private static final ProjectId PROJECT_ID = new ProjectId();

	private final BambooServerCfg bamboo1 = new BambooServerCfg("bamboo1", new ServerId());
	private final BambooServerCfg bamboo2 = new BambooServerCfg("bamboo2", new ServerId());
	private final CrucibleServerCfg crucible1 = new CrucibleServerCfg("crucible1", new ServerId());
	private final CrucibleServerCfg crucible2 = new CrucibleServerCfg("crucible2", new ServerId());
	private final JiraServerCfg jira1 = new JiraServerCfg("jira1", new ServerId());
	private final JiraServerCfg jira2 = new JiraServerCfg("jira2", new ServerId());
	private final FishEyeServerCfg fisheye1 = new FishEyeServerCfg("fisheye1", new ServerId());
	private final FishEyeServerCfg fisheye2 = new FishEyeServerCfg("fisheye2", new ServerId());

	private void populateServerCfgs() {

		projectCfgManager.addProjectSpecificServer(bamboo1);
		projectCfgManager.addProjectSpecificServer(jira1);
		projectCfgManager.addProjectSpecificServer(crucible1);
		projectCfgManager.addProjectSpecificServer(fisheye1);

		newConf = new ProjectConfiguration(projectCfgManager.getProjectConfiguration());
	}

	public void testServerAdded() {

		newConf.getServers().add(bamboo2);
		newConf.getServers().add(crucible2);
		newConf.getServers().add(jira2);
		newConf.getServers().add(fisheye2);

		// record
		listener.configurationUpdated(newConf);
		listener.serverAdded(bamboo2);
		listener.serverAdded(crucible2);
		listener.serverAdded(jira2);
		listener.serverAdded(fisheye2);
		listener.jiraServersChanged(newConf);
		listener.bambooServersChanged(newConf);
		listener.crucibleServersChanged(newConf);
		listener.fisheyeServersChanged(newConf);

		// test
		EasyMock.replay(listener);
		projectCfgManager.updateProjectConfiguration(newConf);

		EasyMock.verify(listener);
	}

	public void testServerRemoved() {

		newConf.getServers().remove(bamboo1);
		newConf.getServers().remove(crucible1);
		newConf.getServers().remove(jira1);
		newConf.getServers().remove(fisheye1);

		// record
		listener.configurationUpdated(newConf);
		listener.serverRemoved(bamboo1);
		listener.serverRemoved(crucible1);
		listener.serverRemoved(jira1);
		listener.serverRemoved(fisheye1);
		listener.jiraServersChanged(newConf);
		listener.bambooServersChanged(newConf);
		listener.crucibleServersChanged(newConf);
		listener.fisheyeServersChanged(newConf);

		// test
		EasyMock.replay(listener);
		projectCfgManager.updateProjectConfiguration(newConf);

		EasyMock.verify(listener);
	}

	public void testServerDisabledEnabled() {

		newConf.getServerCfg(bamboo1.getServerId()).setEnabled(false);
		newConf.getServerCfg(crucible1.getServerId()).setEnabled(false);
		newConf.getServerCfg(jira1.getServerId()).setEnabled(false);
		newConf.getServerCfg(fisheye1.getServerId()).setEnabled(false);

		// record
		listener.configurationUpdated(newConf);
		listener.serverDisabled(bamboo1.getServerId());
		listener.serverDisabled(crucible1.getServerId());
		listener.serverDisabled(jira1.getServerId());
		listener.serverDisabled(fisheye1.getServerId());
		listener.serverDataChanged(bamboo1.getServerId());
		listener.serverDataChanged(crucible1.getServerId());
		listener.serverDataChanged(jira1.getServerId());
		listener.serverDataChanged(fisheye1.getServerId());
		listener.jiraServersChanged(newConf);
		listener.bambooServersChanged(newConf);
		listener.crucibleServersChanged(newConf);
		listener.fisheyeServersChanged(newConf);

		// test disabled
		EasyMock.replay(listener);
		projectCfgManager.updateProjectConfiguration(newConf);

		EasyMock.verify(listener);

		// reset
		EasyMock.reset(listener);

		ProjectConfiguration conf = new ProjectConfiguration(newConf);

		conf.getServerCfg(bamboo1.getServerId()).setEnabled(true);
		conf.getServerCfg(crucible1.getServerId()).setEnabled(true);
		conf.getServerCfg(jira1.getServerId()).setEnabled(true);
		conf.getServerCfg(fisheye1.getServerId()).setEnabled(true);

		// record
		listener.configurationUpdated(conf);
		listener.serverEnabled(bamboo1.getServerId());
		listener.serverEnabled(crucible1.getServerId());
		listener.serverEnabled(jira1.getServerId());
		listener.serverEnabled(fisheye1.getServerId());
		listener.serverDataChanged(bamboo1.getServerId());
		listener.serverDataChanged(crucible1.getServerId());
		listener.serverDataChanged(jira1.getServerId());
		listener.serverDataChanged(fisheye1.getServerId());
		listener.jiraServersChanged(conf);
		listener.bambooServersChanged(conf);
		listener.crucibleServersChanged(conf);
		listener.fisheyeServersChanged(conf);

		// test enabled
		EasyMock.replay(listener);
		projectCfgManager.updateProjectConfiguration(conf);

		EasyMock.verify(listener);
	}

	public void testServerLabelChanged() {

		newConf.getServerCfg(bamboo1.getServerId()).setName(bamboo1.getName() + SUFFIX);
		newConf.getServerCfg(crucible1.getServerId()).setName(crucible1.getName() + SUFFIX);
		newConf.getServerCfg(jira1.getServerId()).setName(jira1.getName() + SUFFIX);
		newConf.getServerCfg(fisheye1.getServerId()).setName(fisheye1.getName() + SUFFIX);

		// record
		listener.configurationUpdated(newConf);
		listener.serverNameChanged(bamboo1.getServerId());
		listener.serverNameChanged(crucible1.getServerId());
		listener.serverNameChanged(jira1.getServerId());
		listener.serverNameChanged(fisheye1.getServerId());
		listener.serverDataChanged(bamboo1.getServerId());
		listener.serverDataChanged(crucible1.getServerId());
		listener.serverDataChanged(jira1.getServerId());
		listener.serverDataChanged(fisheye1.getServerId());
		listener.jiraServersChanged(newConf);
		listener.bambooServersChanged(newConf);
		listener.crucibleServersChanged(newConf);
		listener.fisheyeServersChanged(newConf);

		// test
		EasyMock.replay(listener);
		projectCfgManager.updateProjectConfiguration(newConf);

		EasyMock.verify(listener);
	}

	public void testServerConnectionDataChanged() {

		newConf.getServerCfg(bamboo1.getServerId()).setUrl(bamboo1.getUrl() + SUFFIX);
		newConf.getServerCfg(crucible1.getServerId()).setUsername(crucible1.getUserName() + SUFFIX);
		newConf.getServerCfg(jira1.getServerId()).setPassword(jira1.getPassword() + SUFFIX);
		newConf.getServerCfg(fisheye1.getServerId()).setPassword(fisheye1.getPassword() + SUFFIX);

		// record
		listener.configurationUpdated(newConf);
		listener.serverConnectionDataChanged(bamboo1.getServerId());
		listener.serverConnectionDataChanged(crucible1.getServerId());
		listener.serverConnectionDataChanged(jira1.getServerId());
		listener.serverConnectionDataChanged(fisheye1.getServerId());
		listener.serverDataChanged(bamboo1.getServerId());
		listener.serverDataChanged(crucible1.getServerId());
		listener.serverDataChanged(jira1.getServerId());
		listener.serverDataChanged(fisheye1.getServerId());
		listener.jiraServersChanged(newConf);
		listener.bambooServersChanged(newConf);
		listener.crucibleServersChanged(newConf);
		listener.fisheyeServersChanged(newConf);

		// test
		EasyMock.replay(listener);
		projectCfgManager.updateProjectConfiguration(newConf);

		EasyMock.verify(listener);
	}

	public void testConfigurationMixedUpdate() {

		newConf.getServerCfg(bamboo1.getServerId()).setUrl(bamboo1.getUrl() + SUFFIX);
		newConf.getServerCfg(bamboo1.getServerId()).setName(bamboo1.getName() + SUFFIX);
		newConf.getServerCfg(bamboo1.getServerId()).setEnabled(false);

		newConf.getServers().remove(crucible1);
		newConf.getServers().remove(jira1);
		newConf.getServers().remove(fisheye1);
		newConf.getServers().add(bamboo2);

		// record
		listener.configurationUpdated(newConf);
		listener.serverConnectionDataChanged(bamboo1.getServerId());
		listener.serverNameChanged(bamboo1.getServerId());
		listener.serverDisabled(bamboo1.getServerId());
		listener.serverDataChanged(bamboo1.getServerId());
		listener.serverRemoved(crucible1);
		listener.serverRemoved(jira1);
		listener.serverRemoved(fisheye1);
		listener.serverAdded(bamboo2);
		listener.bambooServersChanged(newConf);
		listener.crucibleServersChanged(newConf);
		listener.jiraServersChanged(newConf);
		listener.fisheyeServersChanged(newConf);

		// test
		EasyMock.replay(listener);
		projectCfgManager.updateProjectConfiguration(newConf);

		EasyMock.verify(listener);
	}

	public void testServerDataChange() {
		newConf.getServerCfg(bamboo1.getServerId()).setPasswordStored(!bamboo1.isPasswordStored());

		// record
		listener.configurationUpdated(newConf);
		listener.serverDataChanged(bamboo1.getServerId());
		listener.bambooServersChanged(newConf);

		// test
		EasyMock.replay(listener);
		projectCfgManager.updateProjectConfiguration(newConf);

		EasyMock.verify(listener);

	}
}