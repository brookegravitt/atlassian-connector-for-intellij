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

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
* User: Jacek
* Date: 2008-06-12
* Time: 12:17:12
* To change this template use File | Settings | File Templates.
*/
public class BambooPopupInfo {

	private static final String ICON_PLAN_PASSED = "icn_plan_passed.gif";
	private static final String ICON_PLAN_FAILED = "icn_plan_failed.gif";

	private List<BambooBuild> bambooBuilds = new ArrayList<BambooBuild>();

	public void add(BambooBuild bambooBuild) {
		bambooBuilds.add(bambooBuild);

	}

	/**
	 *
	 * @return html version
	 */
	public String toHtml() {

		StringBuilder htmlContent = new StringBuilder();

		for (BambooBuild build : bambooBuilds) {
			htmlContent.append(createHtmlRow(
					build.getBuildKey(),
					build.getBuildNumber(),
					build.getBuildResultUrl(),
					build.getStatus()));
		}

		return htmlContent.toString();
	}

	/**
	 *
	 * @return calls toHtml()
	 */
	public String toString() {
		return toHtml();
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
		sb
				.append("<span style=\"font-size:12pt ; font-family: arial, helvetica, sans-serif; font-weight: bold; color: ");
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

	public List<BambooBuild> getBambooBuilds() {
		return bambooBuilds;
	}

	public void clear() {
		bambooBuilds.clear();
	}
}
