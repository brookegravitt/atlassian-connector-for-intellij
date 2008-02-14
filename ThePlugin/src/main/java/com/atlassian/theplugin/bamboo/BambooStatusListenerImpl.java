package com.atlassian.theplugin.bamboo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This listener fires notification tooltip if bamboo build has changes status form SUCCEED to FAILED
 */
public class BambooStatusListenerImpl implements BambooStatusListener {


	private Map buildPrevStatus = new HashMap<String, BuildStatus>(0);
	private BambooStatusDisplay display;

	/**
	 *
	 * @param display reference to display component
	 */
	public BambooStatusListenerImpl(BambooStatusDisplay display) {
		this.display = display;
	}

	public void updateBuildStatuses(Collection<BambooBuild> buildStatuses) {
		StringBuilder tooltipContent = new StringBuilder();

		BuildStatus status = null;
		boolean fireTooltip = false;


		if (buildStatuses != null && buildStatuses.size() > 0) {
			for (BambooBuild buildInfo : buildStatuses) {
				switch (buildInfo.getStatus()) {
					case BUILD_FAILED:
						if (buildPrevStatus.containsKey(buildInfo.getBuildKey())) {
							if (buildPrevStatus.get(buildInfo.getBuildKey()) == BuildStatus.BUILD_SUCCEED) {
								// build has changes status from SUCCEED to FAILED
								fireTooltip = true;
								status = BuildStatus.BUILD_FAILED;
								// prepare information
								tooltipContent.append(createHtmlRow(buildInfo.getBuildKey(), buildInfo.getBuildNumber(),
										buildInfo.getBuildResultUrl(), BuildStatus.BUILD_FAILED));
							}
						}

						buildPrevStatus.put(buildInfo.getBuildKey(), buildInfo.getStatus());
					
						break;
					case UNKNOWN:
						// no action here
						break;
					case BUILD_SUCCEED:

						if (buildPrevStatus.containsKey(buildInfo.getBuildKey())) {
							if (buildPrevStatus.get(buildInfo.getBuildKey()) == BuildStatus.BUILD_FAILED) {
								// build has changes status from FAILED to SUCCEED
								fireTooltip = true;
								if (status == null) {
									status = BuildStatus.BUILD_SUCCEED;
								}
								// prepare information
								tooltipContent.append(createHtmlRow(buildInfo.getBuildKey(),
										buildInfo.getBuildNumber(), buildInfo.getBuildResultUrl(), BuildStatus.BUILD_SUCCEED));
							}
						}
						buildPrevStatus.put(buildInfo.getBuildKey(), buildInfo.getStatus());

						break;
					default:
						throw new IllegalStateException("Unexpected build status encountered");
				}
			}
		}

		if (fireTooltip == true) {
			display.updateBambooStatus(status, tooltipContent.toString());
		}
	}

	private String createHtmlRow(String buildKey, String buildNumber, String url, BuildStatus buildStatus) {
		String color = "grey";
		String status = "unknown";

		if (buildStatus == BuildStatus.BUILD_FAILED) {
			color = "red";
			status = "failed";
		} else if (buildStatus == BuildStatus.BUILD_SUCCEED) {
			color = "green";
			status = "succeed";
		}

		return "<div style=\"font-size:12pt ; font-family: arial, helvetica, sans-serif; font-weight: bold; color: " + color + "\">" + "<a href=\"" + url + "\">" + buildKey + "-" + buildNumber + "</a> " + status + "</div><br />";
	}

}
