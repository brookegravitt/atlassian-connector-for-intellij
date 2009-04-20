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
package com.atlassian.theplugin.util;

import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.commons.util.UrlUtil;
import junit.framework.TestCase;
import org.easymock.EasyMock;

/**
 * UsageStatisticsGeneratorImpl Tester.
 *
 * @author wseliga
 */
public class UsageStatisticsGeneratorImplTest extends TestCase {


	public void testGetStatisticsUrlSuffix() {
		CfgManager cfgManager = EasyMock.createNiceMock(CfgManager.class);
		EasyMock.expect(cfgManager.getAllUniqueServers()).andReturn(MiscUtil.buildArrayList(
				new BambooServerCfg("bamboo1", new ServerId()),
				new CrucibleServerCfg("crucible1", new ServerId()),
				new BambooServerCfg("bamboo2", new ServerId()))).anyTimes();
		EasyMock.replay(cfgManager);

		final UsageStatisticsGeneratorImpl generator = new UsageStatisticsGeneratorImpl(true, 123, cfgManager);
		assertEquals("uid=123&version=" + UrlUtil.encodeUrl(PluginUtil.getInstance().getVersion())
				+ "&bambooServers=2&crucibleServers=1&jiraServers=0",
				generator.getStatisticsUrlSuffix());
		final UsageStatisticsGeneratorImpl generator2 = new UsageStatisticsGeneratorImpl(false, 234, cfgManager);
		assertEquals("uid=234", generator2.getStatisticsUrlSuffix());
	}


}
