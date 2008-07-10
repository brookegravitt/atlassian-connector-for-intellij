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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Renders Bamboo build results as HTML and passes it to configured
 * {@link com.atlassian.theplugin.commons.bamboo.BambooStatusDisplay}
 */
	public class HtmlBambooStatusListenerNotUsed implements BambooStatusListener {

	private final BambooStatusDisplay display;

	private static final int TIME_OFFSET = -12;

	public static final String BODY_WITH_STYLE =
			"<body style=\"font-size:12pt ; font-family: arial, helvetica, sans-serif\">";
	private static final DateFormat TIME_DF = new SimpleDateFormat("hh:mm a");
	private static final DateFormat DATE_DF = new SimpleDateFormat("MMM d");
	private PluginConfiguration configuration;

	public HtmlBambooStatusListenerNotUsed(BambooStatusDisplay aDisplay, PluginConfiguration configuration) {
		display = aDisplay;
		this.configuration = configuration;
	}

	private String formatLatestPollAndBuildTime(BambooBuild buildInfo) {
		StringBuilder sb = new StringBuilder("<td nowrap align=\"right\">");

		Date buildTime = buildInfo.getBuildTime();
		String relativeBuildDate = buildInfo.getBuildRelativeBuildDate();
		String buildTimeStr;
		if (buildInfo.getEnabled()) {
			if (relativeBuildDate != null && !relativeBuildDate.equals("")) {
				buildTimeStr = buildInfo.getBuildRelativeBuildDate();
			} else {
				buildTimeStr = (null == buildTime) ? "&nbsp;" : formatBuildTime(buildTime);
			}
		} else {
			buildTimeStr = "&nbsp;";
		}
		sb.append(buildTimeStr).append("</td>");

		return sb.toString();
	}

	private String formatBuildTime(Date date) {
		Calendar barrier = Calendar.getInstance();
		barrier.add(Calendar.HOUR_OF_DAY, TIME_OFFSET);

		DateFormat buildDateFormat;

		if (date.before(barrier.getTime())) {
			buildDateFormat = DATE_DF;
		} else {
			buildDateFormat = TIME_DF;
		}

		return buildDateFormat.format(date);

	}

	private String getSuccessBuildRow(BambooBuild buildInfo) {
		return drawRow(buildInfo, "green", "icn_plan_passed.gif");
	}

	private String getFailedBuildRow(BambooBuild buildInfo) {
		return drawRow(buildInfo, "red", "icn_plan_failed.gif");
	}

	private String getErrorBuildRow(BambooBuild buildInfo) {
		return drawRow(buildInfo, "#999999", "icn_plan_disabled.gif");
	}

	private String getDisabledBuildRow(BambooBuild buildInfo) {
		return drawRow(buildInfo, "#999999", "icn_plan_disabled.gif");
	}

	private String drawRow(BambooBuild buildInfo, String colour, String icon) {
		StringBuilder sb = new StringBuilder("<tr>");
		sb.append(
				"<td width=1%><a href='"
						+ buildInfo.getBuildUrl()
						+ "'>"
						//+ "<img src=\"/icons/" + icon + "\" height=\"16\" width=\"16\" border=\"0\" align=\"absmiddle\">"
						+ "</a></td>");
		if (buildInfo.getStatus() == BuildStatus.UNKNOWN) {
			// TODO: jgorycki: this generates bug PL-95
			// In case of bamboo error garbage is displayed in the tooltip
			String shortMessage = buildInfo.getMessage() != null
					? lineSeparator.split(buildInfo.getMessage(), 2)[0] : null;
			sb.append("<td width=100%><font color=\"" + colour + "\">").append(shortMessage).append("</font></td>");
		} else {
			String font = "<font color=\"" + colour + "\">";
			boolean bamboo2 = !buildInfo.getProjectName().equals("");
			sb.append("<td width=\"100%\" nowrap>");
			if (bamboo2) {
				sb.append("<b>");
				sb.append(
						"<a href='"
								+ buildInfo.getProjectUrl()
								+ "'>"
								+ font
								+ buildInfo.getProjectName()
								+ "</font></a>&nbsp;&nbsp;");
				sb.append(
						"<a href='"
								+ buildInfo.getBuildUrl()
								+ "'>"
								+ font
								+ buildInfo.getBuildName()
								+ "</font></a>");
				sb.append(font + " &gt; </font>");
				sb.append("</b>");
			}
			if (buildInfo.getEnabled()) {
			sb.append(
					"<a href='"
							+ buildInfo.getBuildResultUrl()
							+ "'>"
							+ font
							+ "<b>"
							+ buildInfo.getBuildKey()
							+
							"-"
							+ buildInfo.getBuildNumber()
							+ "</b></font></a></td>");
			} else {
				sb.append(font + "<b>Disabled</b></font></td>");
			}
		}
		sb.append(formatLatestPollAndBuildTime(buildInfo));
		sb.append("</tr>");

		return sb.toString();
	}

	private Pattern lineSeparator = Pattern.compile("$", Pattern.MULTILINE);


	public void updateBuildStatuses(Collection<BambooBuild> buildStatuses) {

		BuildStatus status = BuildStatus.UNKNOWN;
		StringBuilder sb = new StringBuilder("<html>" + BODY_WITH_STYLE);

		if (buildStatuses == null || buildStatuses.size() == 0) {
			sb.append("No plans defined.");
			status = BuildStatus.UNKNOWN;
		} else {
			List<BambooBuild> sortedStatuses = new ArrayList<BambooBuild>(buildStatuses);
			Collections.sort(sortedStatuses, new Comparator<BambooBuild>() {
				public int compare(BambooBuild b1, BambooBuild b2) {
					return b1.getServerUrl().compareTo(b2.getServerUrl());
				}
			});

			sb.append("<table width=\"100%\">");
			String lastServer = null;

			for (BambooBuild buildInfo : buildStatuses) {
				if (!buildInfo.getServerUrl().equals(lastServer)) {
					Server server = getServerFromUrl(buildInfo.getServerUrl());
					if (server == null) { // PL-122 lguminski immuning to a situation when getServerFromUrl returns null
						continue;
					}
					if (lastServer != null) {
						sb.append("<tr><td colspan=3>&nbsp;</td></tr>");
					}
					sb.append("<tr><td colspan=3>");
					sb.append(
							"<table width=100% cellpadding=0 cellspacing=0>"
									+ "<tr>"
//                          + "<td width=1%><a href='"
// 							+ server.getUrlString()
// 							+ "'><img src=/icons/bamboo-blue-32.png height=32 width=32 border=0></a></td>"
									+ "<td width=100%><b><a href='"
									+ server.getUrlString()
									+ "'>"
									+ server.getName()
									+ "</a></b><br>"
									+ "<font style=\"font-size: 10pt;\" color=#999999>LAST UPDATE: "
									+ TIME_DF.format(buildInfo.getPollingTime()) + "</font></td>"
									+ "<td width=1% nowrap align=right valign=bottom style=\"font-size:10pt ;\">"
									+ "</td></tr></table>");
					sb.append("</td></tr>");
				}
				if (buildInfo.getEnabled()) {
					switch (buildInfo.getStatus()) {
						case BUILD_FAILED:
							sb.append(getFailedBuildRow(buildInfo));
							status = BuildStatus.BUILD_FAILED;
							break;
						case UNKNOWN:
							sb.append(getErrorBuildRow(buildInfo));
//							if (status != BUILD_FAILED && status != BuildStatus.BUILD_SUCCEED) {
//								status = BuildStatus.UNKNOWN;
//							}
							break;
						case BUILD_SUCCEED:
							sb.append(getSuccessBuildRow(buildInfo));
							if (status != BuildStatus.BUILD_FAILED) {
								status = BuildStatus.BUILD_SUCCEED;
							}
							break;
						default:
							throw new IllegalStateException("Unexpected build status encountered");
					}
				} else {
					sb.append(getDisabledBuildRow(buildInfo));
				}
				lastServer = buildInfo.getServerUrl();
			}
			sb.append("</table>");
		}
		sb.append("</body></html>");

		// todo uncomment that line to take effect
		// todo uncomment that line to take effect
//		display.updateBambooStatus(status, sb.toString());
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