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

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.commons.configuration.BambooTooltipOption;
import com.atlassian.theplugin.commons.configuration.BambooConfigurationBean;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This listener fires notification tooltip if bamboo build has changes status between SUCCEED and FAILED
 */
public class BambooStatusTooltipListener implements BambooStatusListener {


	private Map<String, BambooBuild> prevBuildStatuses = new HashMap<String, BambooBuild>(0);
	private final BambooStatusDisplay display;
	private final PluginConfiguration pluginConfiguration;
	private static final String ICON_PLAN_PASSED = "icn_plan_passed.gif";
	private static final String ICON_PLAN_FAILED = "icn_plan_failed.gif";

	/**
	 *
	 * @param display reference to display component
	 * @param pluginConfiguration global plugin configuration
	 */
	public BambooStatusTooltipListener(BambooStatusDisplay display, PluginConfiguration pluginConfiguration) {
		this.display = display;
		this.pluginConfiguration = pluginConfiguration;
	}

	public void updateBuildStatuses(Collection<BambooBuild> newBuildStatuses) {

		// get config option for tooltip
		BambooTooltipOption tooltipConfigOption =
				((BambooConfigurationBean) pluginConfiguration.getProductServers(ServerType.BAMBOO_SERVER)).
						getBambooTooltipOption();

		if (tooltipConfigOption == BambooTooltipOption.NEVER) {
			return;
		}

		StringBuilder tooltipContent = new StringBuilder();

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
										||
										(prevBuild.getStatus() == BuildStatus.BUILD_FAILED
											&&
										!prevBuild.getBuildNumber().equals(currentBuild.getBuildNumber())
											&&
										tooltipConfigOption == BambooTooltipOption.ALL_FAULIRES_AND_FIRST_SUCCESS)) {

									// build has changed status from SUCCEED to FAILED
									// or this is new build and still failed
									fireTooltip = true;
									status = BuildStatus.BUILD_FAILED;
									// prepare information
									tooltipContent.append(createHtmlRow(
											currentBuild.getBuildKey(),
											currentBuild.getBuildNumber(),
											currentBuild.getBuildResultUrl(),
											BuildStatus.BUILD_FAILED));
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
									tooltipContent.append(createHtmlRow(currentBuild.getBuildKey(),
											currentBuild.getBuildNumber(), currentBuild.getBuildResultUrl(),
											BuildStatus.BUILD_SUCCEED));
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
			display.updateBambooStatus(status, tooltipContent.toString());
		}
	}

	private String createHtmlRow(String buildKey, String buildNumber, String url, BuildStatus buildStatus) {
		String color = "grey";
		String status = "unknown";
		String icon = "";

		if (buildStatus == BuildStatus.BUILD_FAILED) {
			color = "red";
			status = "failed";
			icon = ICON_PLAN_FAILED;
		} else if (buildStatus == BuildStatus.BUILD_SUCCEED) {
			color = "green";
			status = "succeed";
			icon = ICON_PLAN_PASSED;
		}

		StringBuilder sb = new StringBuilder("<table width=\"100%\">");
		sb.append("<tr><td valign=bottom width=16>");
		sb.append("<img src=\"/icons/");
		sb.append(icon);
		sb.append("\" height=16 width=16 border=0 valing=bottom/>&nbsp;");
		sb.append("</td><td nowrap valign=top align=left>");
		sb.append(
			"<span style=\"font-size:12pt ; font-family: arial, helvetica, sans-serif; font-weight: bold; color: ");
		sb.append(color);
		sb.append("\">");
		sb.append("<a href=\"");
		sb.append(url);
		sb.append("\">");
		sb.append(buildKey);
		sb.append("-");
		sb.append(buildNumber);
		sb.append("</a> ");
		sb.append(status);
		sb.append("</span></td></tr></table>");

		return sb.toString();
	}

	public void resetState() {
		// do nothing
	}
}
