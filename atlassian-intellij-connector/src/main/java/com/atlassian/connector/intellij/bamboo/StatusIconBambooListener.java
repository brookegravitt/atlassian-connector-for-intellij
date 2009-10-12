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

package com.atlassian.connector.intellij.bamboo;

import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Renders Bamboo build results
 */
public class StatusIconBambooListener implements BambooStatusListener {

	private final BambooStatusDisplay display;

	public StatusIconBambooListener(BambooStatusDisplay aDisplay) {
		this.display = aDisplay;
	}

	public void updateBuildStatuses(Collection<BambooBuildAdapter> buildStatuses, Collection<Exception> generalExceptions) {

		BuildStatus status = BuildStatus.UNKNOWN;

		if (buildStatuses == null || buildStatuses.size() == 0) {
			status = BuildStatus.UNKNOWN;
		} else {
			List<BambooBuildAdapter> sortedStatuses = new ArrayList<BambooBuildAdapter>(buildStatuses);
			Collections.sort(sortedStatuses, new Comparator<BambooBuildAdapter>() {
				public int compare(BambooBuildAdapter b1, BambooBuildAdapter b2) {
					return b1.getServer().getUrl().compareTo(b2.getServer().getUrl());
				}
			});

			for (BambooBuildAdapter buildInfo : buildStatuses) {
				if (buildInfo.isEnabled()) {
					switch (buildInfo.getStatus()) {
						case FAILURE:
							status = BuildStatus.FAILURE;
							break;
						case UNKNOWN:
							break;
						case SUCCESS:
							if (status != BuildStatus.FAILURE) {
								status = BuildStatus.SUCCESS;
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
		updateBuildStatuses(new ArrayList<BambooBuildAdapter>(), null);
	}
}
