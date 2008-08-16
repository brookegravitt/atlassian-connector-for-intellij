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
package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.commons.cfg.BambooCfg;
import com.atlassian.theplugin.commons.cfg.BambooCfgManager;
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import com.atlassian.theplugin.commons.cfg.ProjectId;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.util.MiscUtil;

import java.util.Collection;

public class MockBambooCfgManager implements  BambooCfgManager {

	private final BambooCfg bambooCfg = new BambooCfg();

	private final Collection<BambooServerCfg> bambooServers;

	public MockBambooCfgManager(final BambooServerCfg... serverCfgs) {
		bambooServers = MiscUtil.buildArrayList(serverCfgs);
	}

	static MockBambooCfgManager createBambooTestConfiguration() {
		return new MockBambooCfgManager(new BambooServerCfg("mybamboo1", new ServerId()));
	}

	static MockBambooCfgManager createEmptyBambooTestConfiguration() {
		return new MockBambooCfgManager();
	}

	public BambooCfg getGlobalBambooCfg() {
		return bambooCfg;
	}

	public Collection<BambooServerCfg> getAllEnabledBambooServers(final ProjectId projectId) {
		return bambooServers;
	}

	void addServer(BambooServerCfg bambooServerCfg) {
		bambooServers.add(bambooServerCfg);
	}
}
