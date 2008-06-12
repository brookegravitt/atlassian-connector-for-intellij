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

package com.atlassian.theplugin.commons.bamboo;

import com.atlassian.theplugin.commons.Server;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.configuration.ProductServerConfiguration;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;

import java.util.*;

/**
 * Renders Bamboo build results
 */
	public class HtmlBambooStatusListener implements BambooStatusListener {

	private final BambooStatusDisplay display;

	public static final String BODY_WITH_STYLE =
			"<body style=\"font-size:12pt ; font-family: arial, helvetica, sans-serif\">";
	private PluginConfiguration configuration;

	public HtmlBambooStatusListener(BambooStatusDisplay aDisplay, PluginConfiguration configuration) {
		display = aDisplay;
		this.configuration = configuration;
	}

	public void updateBuildStatuses(Collection<BambooBuild> buildStatuses) {

		BuildStatus status = BuildStatus.UNKNOWN;

		if (buildStatuses == null || buildStatuses.size() == 0) {
			status = BuildStatus.UNKNOWN;
		} else {
			List<BambooBuild> sortedStatuses = new ArrayList<BambooBuild>(buildStatuses);
			Collections.sort(sortedStatuses, new Comparator<BambooBuild>() {
				public int compare(BambooBuild b1, BambooBuild b2) {
					return b1.getServerUrl().compareTo(b2.getServerUrl());
				}
			});

			String lastServer = null;

			for (BambooBuild buildInfo : buildStatuses) {
				if (!buildInfo.getServerUrl().equals(lastServer)) {
					Server server = getServerFromUrl(buildInfo.getServerUrl());
					if (server == null) { // PL-122 lguminski immuning to a situation when getServerFromUrl returns null 
						continue;
					}
				}
				if (buildInfo.getEnabled()) {
					switch (buildInfo.getStatus()) {
						case BUILD_FAILED:
							status = BuildStatus.BUILD_FAILED;
							break;
						case UNKNOWN:
//							if (status != BUILD_FAILED && status != BuildStatus.BUILD_SUCCEED) {
//								status = BuildStatus.UNKNOWN;
//							}
							break;
						case BUILD_SUCCEED:
							if (status != BuildStatus.BUILD_FAILED) {
								status = BuildStatus.BUILD_SUCCEED;
							}
							break;
						default:
							throw new IllegalStateException("Unexpected build status encountered");
					}
				}
				lastServer = buildInfo.getServerUrl();
			}
		}
		display.updateBambooStatus(status, new PopupInfo());
	}

	protected Server getServerFromUrl(String serverUrl) {
		ProductServerConfiguration productServers =
				configuration.getProductServers(ServerType.BAMBOO_SERVER);
		for (Iterator<Server> iterator = productServers.transientgetEnabledServers().iterator(); iterator.hasNext();) {
			Server server = iterator.next();
			if (serverUrl.equals(server.getUrlString())) {
				return server;
			}
		}

		return null;
	}

	public void resetState() {
		// set empty list of builds
		updateBuildStatuses(new ArrayList<BambooBuild>());
	}
}
