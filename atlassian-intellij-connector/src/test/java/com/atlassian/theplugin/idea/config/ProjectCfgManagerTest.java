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

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.*;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.spartez.util.junit3.TestUtil;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;

/**
 * CfgManagerImpl Tester.
 *
 * @author wseliga
 */
public class ProjectCfgManagerTest extends TestCase {

	private ProjectCfgManagerImpl cfgManager;
	private static final ProjectId PROJECT_ID_1 = new ProjectId();

	@Override
	public void setUp() throws Exception {
		super.setUp();
		cfgManager = createCfgManager();
		populateServerCfgs();
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	// these fields are not static as they must be reinitialized (refreshed) in every instance (to keep them clean)

	private final BambooServerCfg bamboo1 = new BambooServerCfg("bamboo1", new ServerId());
	private final BambooServerCfg bamboo3 = new BambooServerCfg("bamboo3", new ServerId());
	private final CrucibleServerCfg crucible1 = new CrucibleServerCfg("crucible1", new ServerId());
	private final CrucibleServerCfg crucible2 = new CrucibleServerCfg("crucible2", new ServerId());
	private final JiraServerCfg jira1 = new JiraServerCfg("jira1", new ServerId());
	private final JiraServerCfg jira2 = new JiraServerCfg("jira2", new ServerId());

	public void testGetAllServers() {
		TestUtil.assertHasOnlyElements(cfgManager.getAllServers(), crucible1, jira1, bamboo1);
		assertEquals(0, cfgManager.getAllServers(null).size());

		TestUtil.assertHasOnlyElements(cfgManager.getAllServers(ServerType.BAMBOO_SERVER), bamboo1);
		TestUtil.assertHasOnlyElements(cfgManager.getAllServers(ServerType.JIRA_SERVER), jira1);
		TestUtil.assertHasOnlyElements(cfgManager.getAllServers(ServerType.CRUCIBLE_SERVER), crucible1);
	}

	private void populateServerCfgs() {
//		cfgManager.addGlobalServer(bamboo3);
//		cfgManager.addGlobalServer(jira2);
		cfgManager.addServer(bamboo1);
		cfgManager.addServer(crucible1);
		cfgManager.addServer(jira1);
//		cfgManager.updateProjectConfiguration(PROJECT_ID_3, new ProjectConfiguration());
	}

//	public void testGetProjectSpecificServers() {
//		TestUtil.assertHasOnlyElements(cfgManager.getProjectSpecificServers(PROJECT_ID_1), jira1, bamboo1);
//		TestUtil.assertHasOnlyElements(cfgManager.getProjectSpecificServers(PROJECT_ID_2), crucible1);
//		TestUtil.assertHasOnlyElements(cfgManager.getProjectSpecificServers(new ProjectId()));
//		TestUtil.assertThrows(IllegalArgumentException.class, new IAction() {
//			public void run() throws Exception {
//				cfgManager.getProjectSpecificServers(null);
//			}
//		});
//	}

	/**
	 * Method: getAllEnabledServers(final ProjectId projectId)
	 */
	public void testGetAllEnabledServers() {
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledServers(), crucible1, jira1, bamboo1);
		jira1.setEnabled(false);
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledServers(), crucible1, bamboo1);
		bamboo3.setEnabled(false);
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledServers(), crucible1, bamboo1);
	}

	protected ProjectCfgManagerImpl createCfgManager() {
		return new ProjectCfgManagerImpl(null);
	}

	public void testAddServer() throws Exception {
		final ProjectCfgManagerImpl myCfgManager = createCfgManager();
		TestUtil.assertHasOnlyElements(myCfgManager.getAllServers());

		myCfgManager.addServer(crucible1);
		TestUtil.assertHasOnlyElements(myCfgManager.getAllServers(), crucible1);

		myCfgManager.addServer(jira1);
		TestUtil.assertHasOnlyElements(myCfgManager.getAllServers(), jira1, crucible1);

		// now try to add something which already is there
		myCfgManager.addServer(jira1);
		TestUtil.assertHasOnlyElements(myCfgManager.getAllServers(), crucible1, jira1);

	}


	public void testReturnedCollectionIsNotInternal() {
		final Collection<ServerCfg> servers = cfgManager.getAllServers();
		assertTrue(servers.contains(crucible1));
		servers.remove(crucible1);
		assertFalse(servers.contains(crucible1));
		assertTrue(cfgManager.getAllServers().contains(crucible1));

	}

//	public void testGlobalServersReturnedCollectionIsNotInternal() {
//		final Collection<ServerCfg> servers = cfgManager.getGlobalServers();
//		assertFalse(servers.contains(crucible1));
//		servers.add(crucible1);
//		assertTrue(servers.contains(crucible1));
//		assertFalse(cfgManager.getGlobalServers().contains(crucible1));
//	}


	public void testGetAllEnabledServersReturnedCollectionIsNotInternal() {
		crucible1.setEnabled(true);
		final Collection<ServerCfg> servers = cfgManager.getAllEnabledServers();
		assertTrue(servers.contains(crucible1));
		servers.remove(crucible1);
		assertFalse(servers.contains(crucible1));
		assertTrue(cfgManager.getAllEnabledServers().contains(crucible1));
	}

//	public void testAddGlobalServer() {
//		cfgManager = createCfgManager();
//		TestUtil.assertHasOnlyElements(cfgManager.getGlobalServers());
//		cfgManager.addGlobalServer(bamboo1);
//		cfgManager.updateProjectConfiguration(PROJECT_ID_1, new ProjectConfiguration());
//		cfgManager.updateProjectConfiguration(PROJECT_ID_2, new ProjectConfiguration());
//
//		TestUtil.assertHasOnlyElements(cfgManager.getGlobalServers(), bamboo1);
//		TestUtil.assertHasOnlyElements(cfgManager.getAllServers(PROJECT_ID_1), bamboo1);
//		TestUtil.assertHasOnlyElements(cfgManager.getProjectSpecificServers(PROJECT_ID_1));
//
//		cfgManager.addGlobalServer(jira2);
//		TestUtil.assertHasOnlyElements(cfgManager.getGlobalServers(), bamboo1, jira2);
//		TestUtil.assertHasOnlyElements(cfgManager.getAllServers(PROJECT_ID_1), bamboo1, jira2);
//		TestUtil.assertHasOnlyElements(cfgManager.getProjectSpecificServers(PROJECT_ID_1));
//
//		cfgManager.addGlobalServer(crucible2);
//		TestUtil.assertHasOnlyElements(cfgManager.getGlobalServers(), crucible2, bamboo1, jira2);
//		TestUtil.assertHasOnlyElements(cfgManager.getAllServers(PROJECT_ID_2), crucible2, bamboo1, jira2);
//		TestUtil.assertHasOnlyElements(cfgManager.getProjectSpecificServers(PROJECT_ID_1));
//	}

//	public void testRemoveServer() {
//		TestUtil.assertHasOnlyElements(cfgManager.getGlobalServers(), bamboo3, jira2);
//		assertEquals(jira2, cfgManager.removeGlobalServer(jira2.getServerId()));
//		TestUtil.assertHasOnlyElements(cfgManager.getGlobalServers(), bamboo3);
//		assertNull(cfgManager.removeGlobalServer(bamboo1.getServerId()));
//		TestUtil.assertHasOnlyElements(cfgManager.getGlobalServers(), bamboo3);
//		assertEquals(bamboo3, cfgManager.removeGlobalServer(bamboo3.getServerId()));
//		assertEquals(0, cfgManager.getGlobalServers().size());
//
//		TestUtil.assertThrows(IllegalArgumentException.class, new IAction() {
//			public void run() {
//				cfgManager.removeGlobalServer(null);
//			}
//		});
//	}

	public void testRemoveServer() {
		TestUtil.assertHasOnlyElements(cfgManager.getAllServers(), crucible1, jira1, bamboo1);

		assertNull(cfgManager.removeServer(bamboo3.getServerId()));
		TestUtil.assertHasOnlyElements(cfgManager.getAllServers(), crucible1, jira1, bamboo1);

		assertEquals(jira1, cfgManager.removeServer(jira1.getServerId()));
		assertNull(cfgManager.removeServer(jira1.getServerId()));
		TestUtil.assertHasOnlyElements(cfgManager.getAllServers(), crucible1, bamboo1);

		final Collection<ServerCfg> servers = cfgManager.getAllServers();
		TestUtil.assertHasOnlyElements(servers, crucible1, bamboo1);

		assertNull(cfgManager.removeServer(null));
	}

//	public void testUpdateGlobalServers() {
//		TestUtil.assertHasOnlyElements(cfgManager.getGlobalServers(), bamboo3, jira2);
//		final ServerCfg[] serverCfgs = {jira2, bamboo1, crucible2};
//		final GlobalConfiguration globalConfiguration = new GlobalConfiguration();
//		globalConfiguration.setGlobalServers(MiscUtil.buildArrayList(serverCfgs));
//		cfgManager.updateGlobalConfiguration(globalConfiguration);
//
//		TestUtil.assertContains(cfgManager.getAllServers(PROJECT_ID_1), serverCfgs);
//
//		TestUtil.assertHasOnlyElements(cfgManager.getGlobalServers(), serverCfgs);
//		globalConfiguration.setGlobalServers(MiscUtil.<ServerCfg>buildArrayList());
//		cfgManager.updateGlobalConfiguration(globalConfiguration);
//		TestUtil.assertHasOnlyElements(cfgManager.getGlobalServers());
//	}

	public void testUpdateServers() {
		TestUtil.assertHasOnlyElements(cfgManager.getAllServers(), crucible1, jira1, bamboo1);
		ArrayList<ServerCfg> servers = MiscUtil.buildArrayList(jira2, bamboo1, crucible2);
		ProjectConfiguration projectCfg = new ProjectConfiguration(servers);

		cfgManager.updateProjectConfiguration(projectCfg);
		TestUtil.assertHasOnlyElements(cfgManager.getAllServers(), servers);
	}

	public void testGetAllBmbooServers() {
		TestUtil.assertHasOnlyElements(cfgManager.getAllBambooServers(), bamboo1);		
		cfgManager.removeServer(bamboo1.getServerId());
		TestUtil.assertHasOnlyElements(cfgManager.getAllBambooServers());
	}

	public void testGetAllEnabledBambooServers() {
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledBambooServers(), bamboo1);
		bamboo1.setEnabled(false);
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledBambooServers());
	}

	public void testGetAllCrucibleServers() {
		TestUtil.assertHasOnlyElements(cfgManager.getAllCrucibleServers(), crucible1);
		crucible1.setEnabled(false);
		TestUtil.assertHasOnlyElements(cfgManager.getAllCrucibleServers(), crucible1);
		cfgManager.removeServer(crucible1.getServerId());
		TestUtil.assertHasOnlyElements(cfgManager.getAllCrucibleServers());
	}

	public void testGetAllEnabledCrucibleServers() {
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledCrucibleServers(), crucible1);
		crucible1.setEnabled(false);
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledCrucibleServers());
	}

	public void testGetAllJiraServers() {
		TestUtil.assertHasOnlyElements(cfgManager.getAllJiraServers(), jira1);
		jira1.setEnabled(false);
		TestUtil.assertHasOnlyElements(cfgManager.getAllJiraServers(), jira1);
		cfgManager.removeServer(jira1.getServerId());
		TestUtil.assertHasOnlyElements(cfgManager.getAllJiraServers());
	}

	public void testGetAllEnabledJiraServers() {
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledJiraServers(), jira1);
		jira1.setEnabled(false);
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledJiraServers());

	}

//	public void testRemoveProject() {
//		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledBambooServers(PROJECT_ID_1), bamboo1);
//		assertNull(cfgManager.removeProject(new ProjectId()));
//		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledBambooServers(PROJECT_ID_1), bamboo1, bamboo3);
//		assertNotNull(cfgManager.removeProject(PROJECT_ID_1));
//		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledBambooServers(PROJECT_ID_1), bamboo3);
//		// PROJECT_ID_2 is intact
//		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledBambooServers(PROJECT_ID_2), bamboo3);
//	}

	public void testGerServer() {
		assertEquals(crucible1, cfgManager.getServer(crucible1.getServerId()));
		assertNull(cfgManager.getServer(new ServerId()));
	}

	public void testGetAllUniqueServers() {
		TestUtil.assertHasOnlyElements(cfgManager.getAllServers(), bamboo1, crucible1, jira1);
		cfgManager.addServer(crucible1);
		// must be unique - so adding new server above should have no effect
		TestUtil.assertHasOnlyElements(cfgManager.getAllServers(), bamboo1, crucible1, jira1);
		cfgManager = createCfgManager();
		TestUtil.assertHasOnlyElements(cfgManager.getAllServers());

	}
}

