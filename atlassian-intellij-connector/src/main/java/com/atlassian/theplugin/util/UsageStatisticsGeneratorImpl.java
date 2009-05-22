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

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.configuration.GeneralConfigurationBean;
import com.atlassian.theplugin.commons.util.UrlUtil;

import java.util.TreeSet;

public class UsageStatisticsGeneratorImpl implements UsageStatisticsGenerator {
	private final boolean reportStatistics;
	private final long uid;
	private GeneralConfigurationBean generalConfig;
	private final CfgManager cfgManager;

	public UsageStatisticsGeneratorImpl(boolean reportStatistics, final long uid,
			GeneralConfigurationBean generalConfig, final CfgManager cfgManager) {
		this.reportStatistics = reportStatistics;
		this.uid = uid;
		this.generalConfig = generalConfig;
		this.cfgManager = cfgManager;
	}

	public String getStatisticsUrlSuffix() {
		StringBuilder sb = new StringBuilder("uid=" + uid);
		if (reportStatistics) {
			int[] counts = new int[ServerType.values().length];

			for (ServerCfg serverCfg : cfgManager.getAllUniqueServers()) {
				counts[serverCfg.getServerType().ordinal()]++;
			}

			sb.append("&version=").append(UrlUtil.encodeUrl(PluginUtil.getInstance().getVersion()));
			sb.append("&bambooServers=").append(counts[ServerType.BAMBOO_SERVER.ordinal()]);
			sb.append("&crucibleServers=").append(counts[ServerType.CRUCIBLE_SERVER.ordinal()]);
			sb.append("&jiraServers=").append(counts[ServerType.JIRA_SERVER.ordinal()]);

			if (generalConfig != null) {
				TreeSet<String> counters = new TreeSet<String>(generalConfig.getStatsCountersMap().keySet());
				for (String counter : counters) {
					sb.append("&").append(counter).append("=").append(generalConfig.getStatsCountersMap().get(counter));
				}
			}
		}
		return sb.toString();
	}
}
