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

import java.util.*;

/**
 * Renders Bamboo build results
 */
public class StausIconBambooListener implements BambooStatusListener {

	private final BambooStatusDisplay display;

	public static final String BODY_WITH_STYLE =
			"<body style=\"font-size:12pt ; font-family: arial, helvetica, sans-serif\">";

	public StausIconBambooListener(BambooStatusDisplay aDisplay) {
		this.display = aDisplay;
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

			for (BambooBuild buildInfo : buildStatuses) {
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
			}
		}
		display.updateBambooStatus(status, new BambooPopupInfo());
	}

	public void resetState() {
		// set empty list of builds
		updateBuildStatuses(new ArrayList<BambooBuild>());
	}
}
