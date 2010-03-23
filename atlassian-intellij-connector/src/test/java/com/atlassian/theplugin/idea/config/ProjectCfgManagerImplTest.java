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

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.bamboo.BambooServerData;
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.configuration.WorkspaceConfigurationBean;
import com.spartez.util.junit3.TestUtil;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;

/**
 * CfgManagerImpl Tester.
 *
 * @author wseliga
 */
public class ProjectCfgManagerImplTest extends TestCase {

	private ProjectCfgManagerImpl cfgManager;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		cfgManager = (ProjectCfgManagerImpl)createCfgManager();
		populateServerCfgs();
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	// these fields are not static as they must be reinitialized (refreshed) in every instance (to keep them clean)

	private final LocalBambooServerData bamboo1 = new LocalBambooServerData(new BambooServerCfg("bamboo1", new ServerIdImpl()),
			new UserCfg("", ""));
	private final LocalBambooServerData bamboo3 = new LocalBambooServerData(new BambooServerCfg("bamboo3", new ServerIdImpl()),
			new UserCfg("", ""));
	private final LocalServerData crucible1 = new LocalServerData(new CrucibleServerCfg("crucible1", new ServerIdImpl()),
			new UserCfg("", ""));
	private final LocalServerData crucible2 = new LocalServerData(new CrucibleServerCfg("crucible2", new ServerIdImpl()),
			new UserCfg("", ""));
	private final LocalJiraServerData jira1 = new LocalJiraServerData(new JiraServerCfg("jira1", new ServerIdImpl(), true),
			new UserCfg("", ""));
	private final LocalJiraServerData jira2 = new LocalJiraServerData(new JiraServerCfg("jira2", new ServerIdImpl(), true),
			new UserCfg("", ""));

	public void testGetAllServers() {
		assertHasOnlyElements(cfgManager.getAllServerss(), crucible1, jira1, bamboo1);
		assertEquals(0, cfgManager.getAllServerss(null).size());

		assertHasOnlyElements(cfgManager.getAllServerss(ServerType.BAMBOO_SERVER), bamboo1);
		assertHasOnlyElements(cfgManager.getAllServerss(ServerType.JIRA_SERVER), jira1);
		assertHasOnlyElements(cfgManager.getAllServerss(ServerType.CRUCIBLE_SERVER), crucible1);
	}

	private <E> void assertHasOnlyElements(Collection<E> collection, E... elements) {

		for (E e : elements) {
			assertTrue(collection.contains(e));
		}

		assertEquals(elements.length, collection.size());
	}

	private void populateServerCfgs() {
		cfgManager.addServer(bamboo1.getServerr());
		cfgManager.addServer(crucible1.getServerr());
		cfgManager.addServer(jira1.getServerr());
	}


	/**
	 * Method: getAllEnabledServers(final ProjectId projectId)
	 */
	public void testGetAllEnabledServers() {
		assertHasOnlyElements(cfgManager.getAllEnabledServerss(), crucible1, jira1, bamboo1);
		jira1.getServerr().setEnabled(false);
		assertHasOnlyElements(cfgManager.getAllEnabledServerss(), crucible1, bamboo1);
		bamboo3.getServerr().setEnabled(false);
		assertHasOnlyElements(cfgManager.getAllEnabledServerss(), crucible1, bamboo1);
	}

	protected ProjectCfgManager createCfgManager() {
		return new ProjectCfgManagerImpl(new WorkspaceConfigurationBean());
	}

	public void testAddServer() throws Exception {
		final ProjectCfgManagerImpl myCfgManager = (ProjectCfgManagerImpl)createCfgManager();
		assertHasOnlyElements(myCfgManager.getAllServerss());

		myCfgManager.addServer(crucible1.getServerr());
		assertHasOnlyElements(myCfgManager.getAllServerss(), crucible1);

		myCfgManager.addServer(jira1.getServerr());
		assertHasOnlyElements(myCfgManager.getAllServerss(), jira1, crucible1);

		// now try to add something which already is there
		myCfgManager.addServer(jira1.getServerr());
		assertHasOnlyElements(myCfgManager.getAllServerss(), crucible1, jira1);

	}


	public void testReturnedCollectionIsNotInternal() {
		final Collection<ServerData> servers = cfgManager.getAllServerss();
		assertTrue(servers.contains(crucible1));
		servers.remove(crucible1);
		assertFalse(servers.contains(crucible1));
		assertTrue(cfgManager.getAllServerss().contains(crucible1));

	}

	public void testGetAllEnabledServersReturnedCollectionIsNotInternal() {
		crucible1.getServerr().setEnabled(true);
		final Collection<ServerData> servers = cfgManager.getAllEnabledServerss();
		assertTrue(servers.contains(crucible1));
		servers.remove(crucible1);
		assertFalse(servers.contains(crucible1));
		assertTrue(cfgManager.getAllEnabledServerss().contains(crucible1));
	}

	public void testRemoveServer() {
		assertHasOnlyElements(cfgManager.getAllServerss(), crucible1, jira1, bamboo1);

		assertNull(cfgManager.removeServer(bamboo3.getServerId()));
		assertHasOnlyElements(cfgManager.getAllServerss(), crucible1, jira1, bamboo1);

		assertEquals(jira1.getServerr(), cfgManager.removeServer(jira1.getServerId()));
		assertNull(cfgManager.removeServer(jira1.getServerId()));
		assertHasOnlyElements(cfgManager.getAllServerss(), crucible1, bamboo1);

		final Collection<ServerData> servers = cfgManager.getAllServerss();
		assertHasOnlyElements(servers, crucible1, bamboo1);

		assertNull(cfgManager.removeServer(null));
	}

	public void testUpdateServers() {
		assertHasOnlyElements(cfgManager.getAllServerss(), crucible1, jira1, bamboo1);
		ArrayList<ServerCfg> serversc = MiscUtil
				.buildArrayList(jira2.getServerr(), bamboo1.getServerr(), crucible2.getServerr());
		ArrayList<ServerData> servers = MiscUtil.buildArrayList(jira2, bamboo1, crucible2);
		ProjectConfiguration projectCfg = new ProjectConfiguration(serversc);

		cfgManager.updateProjectConfiguration(projectCfg);
		TestUtil.assertHasOnlyElements(cfgManager.getAllServerss(), servers);
	}

	public void testGetAllBmbooServers() {
		assertHasOnlyElements(cfgManager.getAllBambooServerss(), bamboo1);
		cfgManager.removeServer(bamboo1.getServerId());
		assertHasOnlyElements(cfgManager.getAllBambooServerss());
	}

	public void testGetAllEnabledBambooServers() {
		assertHasOnlyElements(cfgManager.getAllEnabledBambooServerss(), bamboo1);
		bamboo1.getServerr().setEnabled(false);
		assertHasOnlyElements(cfgManager.getAllEnabledBambooServerss());
	}

	public void testGetAllCrucibleServers() {
		assertHasOnlyElements(cfgManager.getAllCrucibleServerss(), crucible1);
		crucible1.getServerr().setEnabled(false);
		assertHasOnlyElements(cfgManager.getAllCrucibleServerss(), crucible1);
		cfgManager.removeServer(crucible1.getServerId());
		assertHasOnlyElements(cfgManager.getAllCrucibleServerss());
	}

	public void testGetAllEnabledCrucibleServers() {
		assertHasOnlyElements(cfgManager.getAllEnabledCrucibleServerss(), crucible1);
		crucible1.getServerr().setEnabled(false);
		assertHasOnlyElements(cfgManager.getAllEnabledCrucibleServerss());
	}

	public void testGetAllJiraServers() {
		assertHasOnlyElements(cfgManager.getAllJiraServerss(), jira1);
		jira1.getServerr().setEnabled(false);
		assertHasOnlyElements(cfgManager.getAllJiraServerss(), jira1);
		cfgManager.removeServer(jira1.getServerId());
		assertHasOnlyElements(cfgManager.getAllJiraServerss());
	}

	public void testGetAllEnabledJiraServers() {
		assertHasOnlyElements(cfgManager.getAllEnabledJiraServerss(), jira1);
		jira1.getServerr().setEnabled(false);
		assertHasOnlyElements(cfgManager.getAllEnabledJiraServerss());

	}

	public void testGerServer() {
		assertEquals(crucible1, cfgManager.getServerr(crucible1.getServerId()));
		assertNull(cfgManager.getServerr(new ServerIdImpl()));
	}

	public void testGetAllUniqueServers() {
		assertHasOnlyElements(cfgManager.getAllServerss(), bamboo1, crucible1, jira1);
		cfgManager.addServer(crucible1.getServerr());
		// must be unique - so adding new server above should have no effect
		assertHasOnlyElements(cfgManager.getAllServerss(), bamboo1, crucible1, jira1);
		cfgManager = (ProjectCfgManagerImpl)createCfgManager();
		assertHasOnlyElements(cfgManager.getAllServerss());

	}

	public void testGetJiraServerr() {
		assertEquals(cfgManager.getJiraServerr(jira1.getServerId()), jira1);
	}

	private class LocalServerData extends ServerData {
		public LocalServerData(final ServerCfg serverCfg, final UserCfg userCfg) {
			super(serverCfg, userCfg);
		}

		public ServerCfg getServerr() {
			return (ServerCfg) getServer();
		}
	}

    private class LocalJiraServerData extends JiraServerData {
        public LocalJiraServerData(final ServerCfg serverCfg, final UserCfg userCfg) {
            super(serverCfg, userCfg);
        }

        public JiraServerCfg getServerr() {
            return (JiraServerCfg) getServer();
        }
    }

	private class LocalBambooServerData extends BambooServerData {
		public LocalBambooServerData(final ServerCfg serverCfg, final UserCfg userCfg) {
			super(serverCfg, userCfg);
		}

		public ServerCfg getServerr() {
			return getServer();
		}
	}
}

