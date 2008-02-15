package com.atlassian.theplugin.bamboo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This listener fires notification tooltip if bamboo build has changes status form SUCCEED to FAILED
 */
public class BambooStatusListenerImpl implements BambooStatusListener {


	private Map<String, BambooBuild> prevBuildStatuses = new HashMap<String, BambooBuild>(0);
	private BambooStatusDisplay display;
	private static final String ICON_PLAN_PASSED = "icn_plan_passed.gif";
	private static final String ICON_PLAN_FAILED = "icn_plan_failed.gif";

	/**
	 *
	 * @param display reference to display component
	 */
	public BambooStatusListenerImpl(BambooStatusDisplay display) {
		this.display = display;
	}

	public void updateBuildStatuses(Collection<BambooBuild> newBuildStatuses) {
		StringBuilder tooltipContent = new StringBuilder();

		BuildStatus status = null;
		boolean fireTooltip = false;

		if (newBuildStatuses != null && newBuildStatuses.size() > 0) {

			for (BambooBuild currentBuild : newBuildStatuses) {

				switch (currentBuild.getStatus()) {

					case BUILD_FAILED:

						if (prevBuildStatuses.containsKey(currentBuild.getBuildKey())) {

							BambooBuild prevBuild = prevBuildStatuses.get(currentBuild.getBuildKey());

							if (prevBuild.getStatus() == BuildStatus.BUILD_SUCCEED ||
									(prevBuild.getStatus() == BuildStatus.BUILD_FAILED &&
									!prevBuild.getBuildNumber().equals(currentBuild.getBuildNumber()))) {
								
								// build has changes status from SUCCEED to FAILED
								// or this is new build and still failed 
								fireTooltip = true;
								status = BuildStatus.BUILD_FAILED;
								// prepare information
								tooltipContent.append(createHtmlRow(currentBuild.getBuildKey(), currentBuild.getBuildNumber(),
										currentBuild.getBuildResultUrl(), BuildStatus.BUILD_FAILED));
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
								// build has changes status from FAILED to SUCCEED
								fireTooltip = true;
								if (status == null) {
									status = BuildStatus.BUILD_SUCCEED;
								}
								// prepare information
								tooltipContent.append(createHtmlRow(currentBuild.getBuildKey(),
										currentBuild.getBuildNumber(), currentBuild.getBuildResultUrl(), BuildStatus.BUILD_SUCCEED));
							}
						}
						prevBuildStatuses.put(currentBuild.getBuildKey(), currentBuild);

						break;
					default:
						throw new IllegalStateException("Unexpected build status encountered");
				}
			}
		}

		if (fireTooltip == true && status != null) {
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

		StringBuffer sb = new StringBuffer("<table width=\"100%\">");
		sb.append("<tr><td valign=bottom width=16>");
		sb.append("<img src=\"/icons/" + icon + "\" height=16 width=16 border=0 valing=bottom/>&nbsp;");
		sb.append("</td><td ñowrap valign=top align=left>");
		sb.append(
				"<span style=\"font-size:12pt ; font-family: arial, helvetica, sans-serif; font-weight: bold; color: "
				+ color
				+ "\">");
		sb.append("<a href=\"" + url + "\">" + buildKey + "-" + buildNumber + "</a> " + status + "</span>");
		sb.append("</td></tr></table>");

		return sb.toString();
	}

}
