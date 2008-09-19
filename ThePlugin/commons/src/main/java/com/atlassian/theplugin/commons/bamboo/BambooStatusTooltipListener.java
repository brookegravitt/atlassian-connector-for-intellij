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

import com.atlassian.theplugin.commons.configuration.BambooTooltipOption;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This listener fires crucible tooltip if bamboo build has changes status between SUCCEED and FAILED
 */
public class BambooStatusTooltipListener implements BambooStatusListener {

	private Map<String, BambooBuild> prevBuildStatuses = new HashMap<String, BambooBuild>(0);
	private final BambooStatusDisplay display;
	private final PluginConfiguration pluginConfiguration;
	private BambooPopupInfo popupInfo = new BambooPopupInfo();


	/**
	 *
	 * @param display reference to display component
	 * @param bambooTooltipOption how incoming status changes should be handled
	 */
	public BambooStatusTooltipListener(final BambooStatusDisplay display, final PluginConfiguration pluginConfiguration) {
		this.display = display;
		this.pluginConfiguration = pluginConfiguration;
	}

	public void updateBuildStatuses(Collection<BambooBuild> newBuildStatuses) {
		
		popupInfo.clear();

		final BambooTooltipOption bambooTooltipOption = pluginConfiguration.getBambooConfigurationData()
				.getBambooTooltipOption();
		if (bambooTooltipOption == BambooTooltipOption.NEVER) {
			return;
		}

		BuildStatus status = null;
		boolean fireTooltip = false;

		if (newBuildStatuses != null && newBuildStatuses.size() > 0) {

			for (BambooBuild currentBuild : newBuildStatuses) {

				// if the build was reported then check it, if not then skip it

					switch (currentBuild.getStatus()) {

						case BUILD_FAILED:

							BambooBuild prevBuild = prevBuildStatuses.get(currentBuild.getBuildKey());

							if (prevBuildStatuses.containsKey(currentBuild.getBuildKey())) {

								if (prevBuild.getStatus() == BuildStatus.BUILD_SUCCEED
										|| (prevBuild.getStatus() == BuildStatus.BUILD_FAILED
										&& !prevBuild.getBuildNumber().equals(currentBuild.getBuildNumber())
										&& bambooTooltipOption == BambooTooltipOption.ALL_FAULIRES_AND_FIRST_SUCCESS)) {

									// build has changed status from SUCCEED to FAILED
									// or this is new build and still failed
									fireTooltip = true;
									status = BuildStatus.BUILD_FAILED;

									// prepare information
									popupInfo.add(currentBuild);
								}
							}

							prevBuildStatuses.put(currentBuild.getBuildKey(), currentBuild);
							break;
						case UNKNOWN:
							// no action here
							break;
						case BUILD_SUCCEED:

							if (prevBuildStatuses.containsKey(currentBuild.getBuildKey())) {
								if (prevBuildStatuses.get(currentBuild.getBuildKey()).getStatus() == BuildStatus.BUILD_FAILED) {
									// build has changed status from FAILED to SUCCEED
									fireTooltip = true;
									if (status == null) {
										status = BuildStatus.BUILD_SUCCEED;
									}
									// prepare information
									popupInfo.add(currentBuild);
								}
							}

							prevBuildStatuses.put(currentBuild.getBuildKey(), currentBuild);
							break;
						default:
							throw new IllegalStateException("Unexpected build status encountered");
					}
				}
			}
		
		if (fireTooltip && status != null) {
			display.updateBambooStatus(status, popupInfo);
			//display.updateBambooStatus(status, popupInfo.toString());
		}
	}


	public void resetState() {
		popupInfo.clear();
	}

}
