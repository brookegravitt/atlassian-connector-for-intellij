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

import java.io.File;
import java.util.Collection;

/**
 * ServerCfgFactoryImpl Tester.
 *
 * @author wseliga
 */
public class GlobalConfigurationFactoryImplTest extends TestCase {

	public static final BambooServerCfg BAMBOO_1 = new BambooServerCfg("mybamboo1", new ServerId());
	public static final BambooServerCfg BAMBOO_2 = new BambooServerCfg("mybamboo2", new ServerId());
	public static final CrucibleServerCfg CRUCIBLE_1 = new CrucibleServerCfg("mycrucible1", new ServerId());
	public static final JiraServerCfg JIRA_1 = new JiraServerCfg("myjira1", new ServerId());

	private GlobalConfigurationFactoryImpl cfgFactory;
	private static final String TMP_DIR = System.getProperty("java.io.tmpdir");
	private File globalFile;
	private File projectFile;


	protected GlobalConfigurationFactoryImpl createServerCfgFactory() {
		return new GlobalConfigurationFactoryImpl(globalFile.getAbsolutePath(), projectFile.getAbsolutePath());
	}

	public GlobalConfigurationFactoryImplTest(String name) {
        super(name);
    }

    @Override
	public void setUp() throws Exception {
        super.setUp();
		BAMBOO_1.setEnabled(false);
		BAMBOO_1.setUsername("wseliga");
		globalFile = File.createTempFile("intellij-connector-global", "tmp");
		projectFile = File.createTempFile("intellij-connector-project", "tmp");
		cfgFactory = createServerCfgFactory();
	}

	@Override
	protected void tearDown() throws Exception {
		globalFile.delete();
		projectFile.delete();
		super.tearDown();
	}

	public void testInvalidSetup() {
		TestUtil.assertThrows(NullPointerException.class, new IAction() {
			public void run() throws Throwable {
				cfgFactory = new GlobalConfigurationFactoryImpl(null, TMP_DIR);
			}
		});
		TestUtil.assertThrows(NullPointerException.class, new IAction() {
			public void run() throws Throwable {
				cfgFactory = new GlobalConfigurationFactoryImpl(TMP_DIR, null);
			}
		});

		cfgFactory = new GlobalConfigurationFactoryImpl("/somenonexistingdir/anotherdir/xyznonexisting", "/somenonexistingdir/anotherdir/xyznonexisting");
		TestUtil.assertThrows(ServerCfgFactoryException.class, new IAction() {
			public void run() throws Throwable {
				cfgFactory.load();
			}
		});
		TestUtil.assertThrows(ServerCfgFactoryException.class, new IAction() {
			public void run() throws Throwable {
				cfgFactory.save(new GlobalConfiguration());
			}
		});

	}


	public void testSaveLoadAll() throws Exception {

		TestUtil.assertThrows(NullPointerException.class, new IAction() {
			public void run() throws Throwable {
				cfgFactory.save(null);
			}
		});

		GlobalConfiguration cfg = new GlobalConfiguration();
		cfg.setGlobalServers(MiscUtil.buildArrayList(CRUCIBLE_1, BAMBOO_2));
// FIXME wseliga cfg.setProjectConfiguration(new ProjectConfiguration(MiscUtil.<ServerCfg>buildArrayList(BAMBOO_1, JIRA_1)));
		cfgFactory.save(cfg);

		GlobalConfiguration loadedCfg = cfgFactory.load();
		assertEquals(cfg, loadedCfg);
    }

    public void testSaveLoadGlobalServers() throws ServerCfgFactoryException {
		final Collection<ServerCfg> serversToSave = MiscUtil.buildArrayList(BAMBOO_1, CRUCIBLE_1, BAMBOO_2);
		cfgFactory.saveGlobalServers(serversToSave);
		final Collection<ServerCfg> loadedServers =  cfgFactory.loadGlobalServers();
		TestUtil.assertEquals(serversToSave, loadedServers);

		TestUtil.assertThrows(NullPointerException.class, new IAction() {
			public void run() throws Throwable {
				cfgFactory.saveGlobalServers(null);
			}
		});
	}



}
