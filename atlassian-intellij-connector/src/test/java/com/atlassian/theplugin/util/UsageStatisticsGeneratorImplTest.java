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
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.configuration.GeneralConfigurationBean;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.commons.util.UrlUtil;
import junit.framework.TestCase;

import java.util.ArrayList;

/**
 * UsageStatisticsGeneratorImpl Tester.
 *
 * @author wseliga
 */
public class UsageStatisticsGeneratorImplTest extends TestCase {
	private ArrayList<ServerData> servers;

	protected void setUp() throws Exception {
		super.setUp();

		final UserCfg credentials = new UserCfg("", "");

		servers = MiscUtil.buildArrayList(
				new ServerData(new BambooServerCfg("bamboo1", new ServerIdImpl()), credentials),
				new ServerData(new CrucibleServerCfg("crucible1", new ServerIdImpl()), credentials),
				new ServerData(new BambooServerCfg("bamboo2", new ServerIdImpl()), credentials));
	}

	public void testGetStatisticsUrlSuffix() {


		final UsageStatisticsGeneratorImpl generator = new UsageStatisticsGeneratorImpl(true, 123, null, servers);
		assertEquals("uid=123&version=" + UrlUtil.encodeUrl(PluginUtil.getInstance().getVersion())
				+ "&bambooServers=2&crucibleServers=1&jiraServers=0",
				generator.getStatisticsUrlSuffix());
		final UsageStatisticsGeneratorImpl generator2 = new UsageStatisticsGeneratorImpl(false, 234, null, servers);
		assertEquals("uid=0", generator2.getStatisticsUrlSuffix());
	}

	public void testGetStatisticsUrlSuffixExtended() {

		GeneralConfigurationBean gcb = new GeneralConfigurationBean();
		gcb.setAnonymousEnhancedFeedbackEnabled(false);
		gcb.bumpCounter("i");
		gcb.bumpCounter("i");
		gcb.bumpCounter("b");
		gcb.bumpCounter("r");
		gcb.bumpCounter("r");
		gcb.bumpCounter("r");
		gcb.bumpCounter("a");

		final UsageStatisticsGeneratorImpl generator = new UsageStatisticsGeneratorImpl(true, 123, gcb, servers);
		assertEquals("uid=123&version=" + UrlUtil.encodeUrl(PluginUtil.getInstance().getVersion())
				+ "&bambooServers=2&crucibleServers=1&jiraServers=0",
				generator.getStatisticsUrlSuffix());

		gcb.setAnonymousEnhancedFeedbackEnabled(true);
		gcb.bumpCounter("i");
		gcb.bumpCounter("i");
		gcb.bumpCounter("b");
		gcb.bumpCounter("r");
		gcb.bumpCounter("r");
		gcb.bumpCounter("r");
		gcb.bumpCounter("a");

		final UsageStatisticsGeneratorImpl generator2 = new UsageStatisticsGeneratorImpl(true, 123, gcb, servers);
		assertEquals("uid=123&version=" + UrlUtil.encodeUrl(PluginUtil.getInstance().getVersion())
				+ "&bambooServers=2&crucibleServers=1&jiraServers=0&a=1&b=1&i=2&r=3",
				generator2.getStatisticsUrlSuffix());

	}

	public void testGetStatisticsUrlSuffixExtendedWithZeroCounters() {

		GeneralConfigurationBean gcb = new GeneralConfigurationBean();

		gcb.setAnonymousEnhancedFeedbackEnabled(true);
		gcb.bumpCounter("i");
		gcb.bumpCounter("i");

		gcb.addCounterIfNotPresent("a");
		gcb.addCounterIfNotPresent("i");
		gcb.addCounterIfNotPresent("r");
		gcb.addCounterIfNotPresent("b");

		final UsageStatisticsGeneratorImpl generator2 = new UsageStatisticsGeneratorImpl(true, 123, gcb, servers);
		assertEquals("uid=123&version=" + UrlUtil.encodeUrl(PluginUtil.getInstance().getVersion())
				+ "&bambooServers=2&crucibleServers=1&jiraServers=0&a=0&b=0&i=2&r=0",
				generator2.getStatisticsUrlSuffix());

	}

}
