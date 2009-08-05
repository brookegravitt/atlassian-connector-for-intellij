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
import com.atlassian.theplugin.commons.configuration.BambooTooltipOption;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapterIdea;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This listener fires crucible tooltip if bamboo build has changes status between SUCCEED and FAILED
 */
public class BambooStatusTooltipListener implements BambooStatusListener {

	private final Map<String, BambooBuildAdapterIdea> prevBuildStatuses = new HashMap<String, BambooBuildAdapterIdea>(0);
	private final BambooStatusDisplay display;
	private final PluginConfiguration pluginConfiguration;
	private final BambooPopupInfo popupInfo = new BambooPopupInfo();


	/**
	 * @param display			 reference to display component
	 * @param pluginConfiguration cfg how incoming status changes should be handled
	 */
	public BambooStatusTooltipListener(final BambooStatusDisplay display,
			final PluginConfiguration pluginConfiguration) {
		this.display = display;
		this.pluginConfiguration = pluginConfiguration;
	}

	public void updateBuildStatuses(Collection<BambooBuildAdapterIdea> newBuildStatuses, Collection<Exception> generalExceptions) {

		popupInfo.clear();

		final BambooTooltipOption bambooTooltipOption = pluginConfiguration != null
				? pluginConfiguration.getBambooConfigurationData().getBambooTooltipOption()
				: null;
		if (bambooTooltipOption == BambooTooltipOption.NEVER) {
			return;
		}

		BuildStatus status = null;
		boolean fireTooltip = false;

		if (newBuildStatuses != null && newBuildStatuses.size() > 0) {

			for (BambooBuildAdapterIdea currentBuild : newBuildStatuses) {

				if (pluginConfiguration == null || !pluginConfiguration.getBambooConfigurationData().isOnlyMyBuilds()
						|| (pluginConfiguration.getBambooConfigurationData().isOnlyMyBuilds() && currentBuild.isMyBuild())) {

					// if the build was reported then check it, if not then skip it

					switch (currentBuild.getStatus()) {
						case FAILURE:
						BambooBuildAdapterIdea prevBuild = prevBuildStatuses.get(getBuildMapKey(currentBuild));
							if (prevBuildStatuses.containsKey(getBuildMapKey(currentBuild))) {
								if (prevBuild.getStatus() == BuildStatus.SUCCESS
										|| (prevBuild.getStatus() == BuildStatus.FAILURE
										&& currentBuild.isValid() && prevBuild.isValid()
										&& prevBuild.getNumber() != currentBuild.getNumber()
										&& bambooTooltipOption == BambooTooltipOption.ALL_FAULIRES_AND_FIRST_SUCCESS)) {

									// build has changed status from SUCCEED to FAILED
									// or this is new build and still failed
									fireTooltip = true;
									status = BuildStatus.FAILURE;

									// prepare information
									popupInfo.add(currentBuild);
								}
							}

							prevBuildStatuses.put(getBuildMapKey(currentBuild), currentBuild);
							break;
						case UNKNOWN:
						case BUILDING:
						case IN_QUEUE:
							// no action here
							break;
						case SUCCESS:

							if (prevBuildStatuses.containsKey(getBuildMapKey(currentBuild))) {
								if (prevBuildStatuses.get(getBuildMapKey(currentBuild)).getStatus()
										== BuildStatus.FAILURE) {
									// build has changed status from FAILED to SUCCEED
									fireTooltip = true;
									if (status == null) {
										status = BuildStatus.SUCCESS;
									}
									// prepare information
									popupInfo.add(currentBuild);
								}
							}

							prevBuildStatuses.put(getBuildMapKey(currentBuild), currentBuild);
							break;
						default:
							throw new IllegalStateException("Unexpected build status encountered");
					}
				}
			}
		}

		if (fireTooltip && status != null) {
			display.updateBambooStatus(status, popupInfo);
		}
	}

	private static String getBuildMapKey(BambooBuildAdapterIdea build) {
		String serverId = "none";
		if (build.getServer() != null) {
			serverId = build.getServer().getServerId().toString();
		}
		return serverId + build.getPlanKey();
	}

	public void resetState() {
		popupInfo.clear();
	}

}
