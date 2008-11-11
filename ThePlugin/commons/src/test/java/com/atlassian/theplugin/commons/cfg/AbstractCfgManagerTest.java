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
package com.atlassian.theplugin.commons.cfg;

import com.atlassian.theplugin.commons.util.MiscUtil;
import com.spartez.util.junit3.IAction;
import com.spartez.util.junit3.TestUtil;
import junit.framework.TestCase;
import org.easymock.EasyMock;

import java.util.ArrayList;
import java.util.Collection;

/**
 * CfgManagerImpl Tester.
 *
 * @author wseliga
 */
public abstract class AbstractCfgManagerTest extends TestCase {

    private CfgManager cfgManager;
	private static final ProjectId PROJECT_ID_1 = new ProjectId();
	private static final ProjectId PROJECT_ID_2 = new ProjectId();
	private static final ProjectId PROJECT_ID_3 = new ProjectId("emptyProject");

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
    private final BambooServerCfg bamboo2 = new BambooServerCfg("bamboo2", new ServerId());
	private final BambooServerCfg bamboo3 = new BambooServerCfg("bamboo3", new ServerId());
	private final CrucibleServerCfg crucible1 = new CrucibleServerCfg("crucible1", new ServerId());
	private final CrucibleServerCfg crucible2 = new CrucibleServerCfg("crucible2", new ServerId());
	private final JiraServerCfg jira1 = new JiraServerCfg("jira1", new ServerId());
	private final JiraServerCfg jira2 = new JiraServerCfg("jira2", new ServerId());

    public void testGetAllServers() {
		TestUtil.assertHasOnlyElements(cfgManager.getAllServers(PROJECT_ID_1), jira1, bamboo1, bamboo3, jira2);
		TestUtil.assertHasOnlyElements(cfgManager.getAllServers(PROJECT_ID_2), jira2, bamboo3, crucible1);
		TestUtil.assertHasOnlyElements(cfgManager.getAllServers(PROJECT_ID_3), jira2, bamboo3);
		TestUtil.assertThrows(IllegalArgumentException.class, new IAction() {
			public void run() {
				cfgManager.getAllServers(null);
			}
		});

		TestUtil.assertHasOnlyElements(cfgManager.getAllServers(new ProjectId()), bamboo3, jira2);
	}

	private void populateServerCfgs() {
		cfgManager.addGlobalServer(bamboo3);
		cfgManager.addGlobalServer(jira2);
		cfgManager.addProjectSpecificServer(PROJECT_ID_1, bamboo1);
		cfgManager.addProjectSpecificServer(PROJECT_ID_2, crucible1);
		cfgManager.addProjectSpecificServer(PROJECT_ID_1, jira1);
		cfgManager.updateProjectConfiguration(PROJECT_ID_3, new ProjectConfiguration());
	}

    public void testGetProjectSpecificServers() {
		TestUtil.assertHasOnlyElements(cfgManager.getProjectSpecificServers(PROJECT_ID_1), jira1, bamboo1);
		TestUtil.assertHasOnlyElements(cfgManager.getProjectSpecificServers(PROJECT_ID_2), crucible1);
		TestUtil.assertHasOnlyElements(cfgManager.getProjectSpecificServers(new ProjectId()));
		TestUtil.assertThrows(IllegalArgumentException.class, new IAction() {
			public void run() throws Exception {
				cfgManager.getProjectSpecificServers(null);
			}
		});
    }

    public void testGetGlobalServers() {
		TestUtil.assertHasOnlyElements(cfgManager.getGlobalServers(), bamboo3, jira2);
		TestUtil.assertHasOnlyElements(createCfgManager().getGlobalServers());
	}

    /**
     *
     * Method: getAllEnabledServers(final ProjectId projectId)
     *
     */
    public void testGetAllEnabledServers() {
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledServers(PROJECT_ID_1), jira1, bamboo1, bamboo3, jira2);
		jira1.setEnabled(false);
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledServers(PROJECT_ID_1), bamboo1, bamboo3, jira2);
		bamboo3.setEnabled(false);
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledServers(PROJECT_ID_1), bamboo1, jira2);

		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledServers(PROJECT_ID_2), jira2, crucible1);
		bamboo3.setEnabled(true);
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledServers(PROJECT_ID_2), jira2, bamboo3, crucible1);
		jira2.setEnabled(false);
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledServers(PROJECT_ID_2), bamboo3, crucible1);
		crucible1.setEnabled(false);
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledServers(PROJECT_ID_2), bamboo3);
		crucible1.setEnabled(true);
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledServers(PROJECT_ID_2), bamboo3, crucible1);

		bamboo3.setEnabled(false);
		crucible1.setEnabled(false);
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledServers(PROJECT_ID_2));

		final CfgManager cfgManager2 = createCfgManager();
		cfgManager2.updateProjectConfiguration(PROJECT_ID_1, new ProjectConfiguration());
		TestUtil.assertHasOnlyElements(cfgManager2.getAllEnabledServers(PROJECT_ID_1));
		TestUtil.assertThrows(IllegalArgumentException.class, new IAction() {
			public void run() {
				cfgManager.getAllEnabledServers(null);

			}
		});

	}

	protected abstract CfgManager createCfgManager();

    public void testAddProjectSpecificServer() throws Exception {
		final CfgManager myCfgManager = createCfgManager();
		TestUtil.assertHasOnlyElements(myCfgManager.getProjectSpecificServers(PROJECT_ID_1));
		TestUtil.assertHasOnlyElements(myCfgManager.getProjectSpecificServers(PROJECT_ID_2));

		myCfgManager.addProjectSpecificServer(PROJECT_ID_1, crucible1);
		TestUtil.assertHasOnlyElements(myCfgManager.getProjectSpecificServers(PROJECT_ID_1), crucible1);
		TestUtil.assertHasOnlyElements(myCfgManager.getProjectSpecificServers(PROJECT_ID_2));

		myCfgManager.addProjectSpecificServer(PROJECT_ID_2, crucible2);
		TestUtil.assertHasOnlyElements(myCfgManager.getProjectSpecificServers(PROJECT_ID_1), crucible1);
		TestUtil.assertHasOnlyElements(myCfgManager.getProjectSpecificServers(PROJECT_ID_2), crucible2);

		myCfgManager.addProjectSpecificServer(PROJECT_ID_1, jira1);
		TestUtil.assertHasOnlyElements(myCfgManager.getProjectSpecificServers(PROJECT_ID_1), jira1, crucible1);
		TestUtil.assertHasOnlyElements(myCfgManager.getProjectSpecificServers(PROJECT_ID_2), crucible2);

		myCfgManager.addProjectSpecificServer(PROJECT_ID_2, jira1);
		TestUtil.assertHasOnlyElements(myCfgManager.getProjectSpecificServers(PROJECT_ID_1), jira1, crucible1);
		TestUtil.assertHasOnlyElements(myCfgManager.getProjectSpecificServers(PROJECT_ID_2), crucible2, jira1);

		// now try to add something which already is there
		myCfgManager.addProjectSpecificServer(PROJECT_ID_2, jira1);
		TestUtil.assertHasOnlyElements(myCfgManager.getProjectSpecificServers(PROJECT_ID_2), crucible2, jira1);


		TestUtil.assertThrows(IllegalArgumentException.class, new IAction() {
			public void run() throws Exception {
				myCfgManager.addProjectSpecificServer(PROJECT_ID_1, null);

			}
		});

		TestUtil.assertThrows(IllegalArgumentException.class, new IAction() {
			public void run() throws Exception {
				myCfgManager.addProjectSpecificServer(null, crucible1);

			}
		});

		TestUtil.assertThrows(IllegalArgumentException.class, new IAction() {
			public void run() throws Exception {
				myCfgManager.addProjectSpecificServer(null, null);

			}
		});
	}


	public void testReturnedCollectionIsNotInternal() {
		final Collection<ServerCfg> servers = cfgManager.getProjectSpecificServers(PROJECT_ID_1);
		assertFalse(servers.contains(crucible1));
		servers.add(crucible1);
		assertTrue(servers.contains(crucible1));
		assertFalse(cfgManager.getProjectSpecificServers(PROJECT_ID_1).contains(crucible1));

	}

	public void testGetAllServersReturnedCollectionIsNotInternal() {
		final Collection<ServerCfg> servers = cfgManager.getAllServers(PROJECT_ID_1);
		assertFalse(servers.contains(crucible1));
		servers.add(crucible1);
		assertTrue(servers.contains(crucible1));
		assertFalse(cfgManager.getAllServers(PROJECT_ID_1).contains(crucible1));

	}


	public void testGlobalServersReturnedCollectionIsNotInternal() {
		final Collection<ServerCfg> servers = cfgManager.getGlobalServers();
		assertFalse(servers.contains(crucible1));
		servers.add(crucible1);
		assertTrue(servers.contains(crucible1));
		assertFalse(cfgManager.getGlobalServers().contains(crucible1));
	}


	public void testGetAllEnabledServersReturnedCollectionIsNotInternal() {
		crucible1.setEnabled(true);
		final Collection<ServerCfg> servers = cfgManager.getAllEnabledServers(PROJECT_ID_1);
		assertFalse(servers.contains(crucible1));
		servers.add(crucible1);
		assertTrue(servers.contains(crucible1));
		assertFalse(cfgManager.getAllEnabledServers(PROJECT_ID_1).contains(crucible1));
	}



	public void testAddGlobalServer() {
		cfgManager = createCfgManager();
		TestUtil.assertHasOnlyElements(cfgManager.getGlobalServers());
		cfgManager.addGlobalServer(bamboo1);
		cfgManager.updateProjectConfiguration(PROJECT_ID_1, new ProjectConfiguration());
		cfgManager.updateProjectConfiguration(PROJECT_ID_2, new ProjectConfiguration());
		
		TestUtil.assertHasOnlyElements(cfgManager.getGlobalServers(), bamboo1);
		TestUtil.assertHasOnlyElements(cfgManager.getAllServers(PROJECT_ID_1), bamboo1);
		TestUtil.assertHasOnlyElements(cfgManager.getProjectSpecificServers(PROJECT_ID_1));

		cfgManager.addGlobalServer(jira2);
		TestUtil.assertHasOnlyElements(cfgManager.getGlobalServers(), bamboo1, jira2);
		TestUtil.assertHasOnlyElements(cfgManager.getAllServers(PROJECT_ID_1), bamboo1, jira2);
		TestUtil.assertHasOnlyElements(cfgManager.getProjectSpecificServers(PROJECT_ID_1));

		cfgManager.addGlobalServer(crucible2);
		TestUtil.assertHasOnlyElements(cfgManager.getGlobalServers(), crucible2, bamboo1, jira2);
		TestUtil.assertHasOnlyElements(cfgManager.getAllServers(PROJECT_ID_2), crucible2, bamboo1, jira2);
		TestUtil.assertHasOnlyElements(cfgManager.getProjectSpecificServers(PROJECT_ID_1));
    }

    public void testRemoveServer() {
		TestUtil.assertHasOnlyElements(cfgManager.getGlobalServers(), bamboo3, jira2);
		assertEquals(jira2, cfgManager.removeGlobalServer(jira2.getServerId()));
		TestUtil.assertHasOnlyElements(cfgManager.getGlobalServers(), bamboo3);
		assertNull(cfgManager.removeGlobalServer(bamboo1.getServerId()));
		TestUtil.assertHasOnlyElements(cfgManager.getGlobalServers(), bamboo3);
		assertEquals(bamboo3, cfgManager.removeGlobalServer(bamboo3.getServerId()));
		assertEquals(0, cfgManager.getGlobalServers().size());

		TestUtil.assertThrows(IllegalArgumentException.class, new IAction() {
			public void run() {
				cfgManager.removeGlobalServer(null);
			}
		});
	}

    public void testRemoveProjectSpecificServer() {
		TestUtil.assertHasOnlyElements(cfgManager.getProjectSpecificServers(PROJECT_ID_1), jira1, bamboo1);
		TestUtil.assertHasOnlyElements(cfgManager.getProjectSpecificServers(PROJECT_ID_2), crucible1);

		assertNull(cfgManager.removeProjectSpecificServer(PROJECT_ID_1, crucible1.getServerId()));
		TestUtil.assertHasOnlyElements(cfgManager.getProjectSpecificServers(PROJECT_ID_1), jira1, bamboo1);
		TestUtil.assertHasOnlyElements(cfgManager.getProjectSpecificServers(PROJECT_ID_2), crucible1);

		assertEquals(jira1, cfgManager.removeProjectSpecificServer(PROJECT_ID_1, jira1.getServerId()));
		TestUtil.assertHasOnlyElements(cfgManager.getProjectSpecificServers(PROJECT_ID_1), bamboo1);
		TestUtil.assertHasOnlyElements(cfgManager.getProjectSpecificServers(PROJECT_ID_2), crucible1);

		assertEquals(crucible1, cfgManager.removeProjectSpecificServer(PROJECT_ID_2, crucible1.getServerId()));
		TestUtil.assertHasOnlyElements(cfgManager.getProjectSpecificServers(PROJECT_ID_1), bamboo1);
		TestUtil.assertHasOnlyElements(cfgManager.getProjectSpecificServers(PROJECT_ID_2));

		final Collection<ServerCfg> servers = cfgManager.getAllServers(PROJECT_ID_1);
		TestUtil.assertHasOnlyElements(servers, bamboo3, jira2, bamboo1);
		assertTrue(servers.contains(bamboo1));

		TestUtil.assertThrows(IllegalArgumentException.class, new IAction() {
			public void run() {
				cfgManager.removeProjectSpecificServer(null, null);
			}
		});

		TestUtil.assertThrows(IllegalArgumentException.class, new IAction() {
			public void run() {
				cfgManager.removeProjectSpecificServer(PROJECT_ID_1, null);
			}
		});

		TestUtil.assertThrows(IllegalArgumentException.class, new IAction() {
			public void run() {
				cfgManager.removeProjectSpecificServer(null, bamboo1.getServerId());
			}
		});
	}

	public void testUpdateGlobalServers() {
		TestUtil.assertHasOnlyElements(cfgManager.getGlobalServers(), bamboo3, jira2);
		final ServerCfg[] serverCfgs = {jira2, bamboo1, crucible2};
		final GlobalConfiguration globalConfiguration = new GlobalConfiguration();
		globalConfiguration.setGlobalServers(MiscUtil.buildArrayList(serverCfgs));
		cfgManager.updateGlobalConfiguration(globalConfiguration);

		TestUtil.assertContains(cfgManager.getAllServers(PROJECT_ID_1), serverCfgs);

		TestUtil.assertHasOnlyElements(cfgManager.getGlobalServers(), serverCfgs);
		globalConfiguration.setGlobalServers(MiscUtil.<ServerCfg>buildArrayList());
		cfgManager.updateGlobalConfiguration(globalConfiguration);
		TestUtil.assertHasOnlyElements(cfgManager.getGlobalServers());
	}

	public void testUpdateProjectSpecificServers() {
		TestUtil.assertHasOnlyElements(cfgManager.getProjectSpecificServers(PROJECT_ID_1), jira1, bamboo1);
		ArrayList<ServerCfg> servers = MiscUtil.buildArrayList(jira2, bamboo1, crucible2);
		ProjectConfiguration projectCfg = new ProjectConfiguration(servers);

		cfgManager.updateProjectConfiguration(PROJECT_ID_1, projectCfg);
		TestUtil.assertHasOnlyElements(cfgManager.getProjectSpecificServers(PROJECT_ID_1), servers);
	}


	public void testGetAllEnabledBambooServers() {
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledBambooServers(PROJECT_ID_1), bamboo1, bamboo3);
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledBambooServers(PROJECT_ID_2), bamboo3);
		TestUtil.assertThrows(IllegalArgumentException.class, new IAction() {
			public void run() {
				cfgManager.getAllEnabledBambooServers(null);
			}
		});
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledBambooServers(new ProjectId()), bamboo3);
	}

	public void testGetAllEnabledCrucibleServers() {
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledCrucibleServers(PROJECT_ID_2), crucible1);
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledCrucibleServers(PROJECT_ID_1));
		final CrucibleServerCfg crucible2 = new CrucibleServerCfg("anothercrucible", new ServerId());
		cfgManager.addGlobalServer(crucible2);
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledCrucibleServers(PROJECT_ID_2), crucible2, crucible1);
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledCrucibleServers(PROJECT_ID_1), crucible2);
		crucible2.setEnabled(false);
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledCrucibleServers(PROJECT_ID_2), crucible1);
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledCrucibleServers(PROJECT_ID_1));
		TestUtil.assertThrows(IllegalArgumentException.class, new IAction() {
			public void run() {
				cfgManager.getAllEnabledCrucibleServers(null);
			}
		});
		assertEquals(0, cfgManager.getAllEnabledCrucibleServers(new ProjectId()).size());
	}

	public void testGetAllEnabledJiraServers() {
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledJiraServers(PROJECT_ID_2), jira2);
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledJiraServers(PROJECT_ID_1), jira1, jira2);
		jira1.setEnabled(false);
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledJiraServers(PROJECT_ID_2), jira2);
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledJiraServers(PROJECT_ID_1), jira2);
		cfgManager.removeGlobalServer(jira2.getServerId());
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledJiraServers(PROJECT_ID_2));
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledJiraServers(PROJECT_ID_1));

		TestUtil.assertThrows(IllegalArgumentException.class, new IAction() {
			public void run() {
				cfgManager.getAllEnabledJiraServers(null);
			}
		});
		assertEquals(0, cfgManager.getAllEnabledJiraServers(new ProjectId()).size());
	}


	public void testRemoveProject() {
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledBambooServers(PROJECT_ID_1), bamboo1, bamboo3);
		assertNull(cfgManager.removeProject(new ProjectId()));
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledBambooServers(PROJECT_ID_1), bamboo1, bamboo3);
		assertNotNull(cfgManager.removeProject(PROJECT_ID_1));
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledBambooServers(PROJECT_ID_1), bamboo3);
		// PROJECT_ID_2 is intact
		TestUtil.assertHasOnlyElements(cfgManager.getAllEnabledBambooServers(PROJECT_ID_2), bamboo3);
	}


	public void testNotifications() {
		final ProjectConfiguration emptyCfg = ProjectConfiguration.emptyConfiguration();
		ConfigurationListener project1Listener = EasyMock.createStrictMock(ConfigurationListener.class);
		ConfigurationListener project2Listener = EasyMock.createStrictMock(ConfigurationListener.class);
		Object[] mocks = {project1Listener, project2Listener};

		project1Listener.configurationUpdated(emptyCfg);
		EasyMock.replay(mocks);

		cfgManager.addProjectConfigurationListener(PROJECT_ID_1, project1Listener);
		cfgManager.addProjectConfigurationListener(PROJECT_ID_2, project2Listener);
		cfgManager.updateGlobalConfiguration(new GlobalConfiguration());
		cfgManager.updateProjectConfiguration(PROJECT_ID_1, emptyCfg);
		EasyMock.verify(mocks);


		EasyMock.reset(mocks);
		project2Listener.configurationUpdated(emptyCfg);
		EasyMock.replay(mocks);

		cfgManager.updateGlobalConfiguration(new GlobalConfiguration());
		cfgManager.updateProjectConfiguration(PROJECT_ID_2, ProjectConfiguration.emptyConfiguration());
		EasyMock.verify(mocks);


		EasyMock.reset(mocks);
		cfgManager.removeProjectConfigurationListener(PROJECT_ID_1, project1Listener);
		// now only project2Listener will be notified
		final ProjectConfiguration nonEmptyCfg = new ProjectConfiguration(MiscUtil.<ServerCfg>buildArrayList(bamboo1));
		project2Listener.configurationUpdated(nonEmptyCfg);
		EasyMock.replay(mocks);

		cfgManager.updateGlobalConfiguration(new GlobalConfiguration());
		cfgManager.updateProjectConfiguration(PROJECT_ID_2, nonEmptyCfg);
		cfgManager.updateProjectConfiguration(PROJECT_ID_1, nonEmptyCfg);
		EasyMock.verify(mocks);
	}


	public void testAddListener() {
		TestUtil.assertThrows(IllegalArgumentException.class, new IAction() {
			public void run() throws Throwable {
				cfgManager.addProjectConfigurationListener(PROJECT_ID_1, null);
			}
		});
		TestUtil.assertThrows(IllegalArgumentException.class, new IAction() {
			public void run() throws Throwable {
				cfgManager.addProjectConfigurationListener(null, EasyMock.createNiceMock(ConfigurationListener.class));
			}
		});

	}

	public void testGerServer() {
		assertEquals(crucible1, cfgManager.getServer(PROJECT_ID_2, crucible1.getServerId()));
		assertNull(cfgManager.getServer(PROJECT_ID_2, crucible2.getServerId()));
	}

	public void testGetAllUniqueServers() {
		TestUtil.assertHasOnlyElements(cfgManager.getAllUniqueServers(), bamboo3, jira2, bamboo1, crucible1, jira1);
		cfgManager.addProjectSpecificServer(PROJECT_ID_1, crucible1);
		// must be unique - so adding new server above should have no effect
		TestUtil.assertHasOnlyElements(cfgManager.getAllUniqueServers(), bamboo3, jira2, bamboo1, crucible1, jira1);
		cfgManager = createCfgManager();
		TestUtil.assertHasOnlyElements(cfgManager.getAllUniqueServers());

	}
}

