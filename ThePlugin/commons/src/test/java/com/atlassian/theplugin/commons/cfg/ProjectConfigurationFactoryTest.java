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

import junit.framework.TestCase;

import java.util.Collection;

import com.atlassian.theplugin.commons.util.MiscUtil;

public abstract class ProjectConfigurationFactoryTest extends TestCase {
	private ProjectConfigurationFactory factory;

	public static final BambooServerCfg BAMBOO_1 = new BambooServerCfg("mybamboo1", new ServerId());
	public static final BambooServerCfg BAMBOO_2 = new BambooServerCfg("mybamboo2", new ServerId());
	public static final CrucibleServerCfg CRUCIBLE_1 = new CrucibleServerCfg("mycrucible1", new ServerId());
	public static final JiraServerCfg JIRA_1 = new JiraServerCfg("myjira1", new ServerId());

	{
		BAMBOO_1.setUrl("url1");
		BAMBOO_2.setUrl("url2");
		BAMBOO_2.setUsername("myuser");
		JIRA_1.setUrl("url3");
	}

	protected abstract ProjectConfigurationFactory getProjectConfigurationFactory();

	@Override
	protected void setUp() throws Exception {
		factory = getProjectConfigurationFactory();
	}

	public void testSaveLoad() throws ServerCfgFactoryException {
		final Collection<ServerCfg> serversToSave = MiscUtil.buildArrayList(BAMBOO_1, CRUCIBLE_1, BAMBOO_2);
		final ProjectConfiguration projectCfg = new ProjectConfiguration(serversToSave);
		factory.save(projectCfg);
		final ProjectConfiguration newCfg = factory.load();
		assertEquals(projectCfg, newCfg);
	}

}
