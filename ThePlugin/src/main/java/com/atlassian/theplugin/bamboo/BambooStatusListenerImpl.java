package com.atlassian.theplugin.bamboo;

import static com.atlassian.theplugin.bamboo.BuildStatus.BUILD_FAILED;
import com.atlassian.theplugin.idea.BambooStatusIcon;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 15, 2008
 * Time: 3:49:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class BambooStatusListenerImpl implements Runnable {
//	private int counter = 0;
	static final String DEFAULT_DATE_TIME_FROMAT = "";


	private BambooStatusIcon statusBarIcon;

	private Collection<BambooBuild> builds = new ArrayList<BambooBuild>();

	public synchronized void setBuilds(Collection<BambooBuild> builds) {
		this.builds = builds;
	}

	public BambooStatusListenerImpl(BambooStatusIcon icon) {
		statusBarIcon = icon;
	}

	private String getLatestPoolAndBuildTime(BambooBuild buildInfo) {
		StringBuilder sb = new StringBuilder("<td>");
		DateFormat df = DateFormat.getTimeInstance();

		sb.append(df.format(buildInfo.getPollingTime()) + "</td>");
		sb.append("<td>"
				+ ((buildInfo.getBuildRelativeBuildDate().length() == 0) ? "---" : buildInfo.getBuildRelativeBuildDate())
				+ "</td>");

		return sb.toString();
	}

	private String getSuccessBuildRow(BambooBuild buildInfo) {
		StringBuilder sb = new StringBuilder("<tr><td><a href='");
		sb.append(buildInfo.getPlanUrl());
		sb.append("'>");
		sb.append(buildInfo.getBuildKey());
		sb.append("</a></td><td><a href='");
		sb.append(buildInfo.getBuildUrl());
		sb.append("'>");
		sb.append("build ");
		sb.append(buildInfo.getBuildNumber());
		sb.append("</a>");
		sb.append("</td><td>");
		sb.append("<font color=\"green\">success</font>");
		sb.append("</td>");
		sb.append(getLatestPoolAndBuildTime(buildInfo));
		sb.append("</tr>");

		return sb.toString();
	}

	private String getFailedBuildRow(BambooBuild buildInfo) {
		StringBuilder sb = new StringBuilder("<tr><td><a href='");
		sb.append(buildInfo.getPlanUrl());
		sb.append("'>");
		sb.append(buildInfo.getBuildKey());
		sb.append("</a></td><td><a href='");
		sb.append(buildInfo.getBuildUrl());
		sb.append("'>");
		sb.append("build ");
		sb.append(buildInfo.getBuildNumber());
		sb.append("</a>");
		sb.append("</td><td>");
		sb.append("<font color=\"red\">failed</font>");
		sb.append("</td>");
		sb.append(getLatestPoolAndBuildTime(buildInfo));
		sb.append("</tr>");

		return sb.toString();
	}

	private String getErrorBuildRow(BambooBuild buildInfo) {
		StringBuilder sb = new StringBuilder("<tr><td><a href='");
		sb.append(buildInfo.getPlanUrl());
		sb.append("'>");
		sb.append(buildInfo.getBuildKey());
		sb.append("</a></td><td></td><td>");
		sb.append("<font color=\"ltgray\">").append(buildInfo.getMessage()).append("</font>");
		sb.append("</td><td></td><td></td></tr>");

		return sb.toString();
	}

	public void updateBuildStatuses(Collection<BambooBuild> buildStatuses) {

		BuildStatus status = BuildStatus.BUILD_SUCCEED;
		StringBuilder sb = new StringBuilder("<html><body>");

		if (buildStatuses == null || buildStatuses.size() == 0) {
			sb.append("No plans defined.");
		} else {
			sb.append("<table>");
			sb.append("<th>Plan</th><th>Build</th><th>Status</th><th>Last Pooling</th><th>Last Build</th>");
			for (BambooBuild buildInfo : buildStatuses) {
				switch (buildInfo.getStatus()) {
					case BUILD_FAILED:
						sb.append(getFailedBuildRow(buildInfo));
						status = BUILD_FAILED;
						break;
					case UNKNOWN:
						sb.append(getErrorBuildRow(buildInfo));
						if (status != BUILD_FAILED) {
							status = BuildStatus.UNKNOWN;
						}
						break;
					case BUILD_SUCCEED:
						sb.append(getSuccessBuildRow(buildInfo));
						break;
					default:
						throw new IllegalStateException("Unexpected build status encountered");
				}
			}
			sb.append("</table>");
		}
		sb.append("</body></html>");
		statusBarIcon.updateBambooStatus(status, sb.toString());
	}

	public void run() {
		updateBuildStatuses(builds);
	}
}
