package com.atlassian.theplugin.bamboo;

import static com.atlassian.theplugin.bamboo.BuildStatus.BUILD_FAILED;
import com.atlassian.theplugin.idea.ThePluginApplicationComponent;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Renders Bamboo build results as HTML and passes it to configured {@link BambooStatusDisplay}
 */
public class HtmlBambooStatusListener implements BambooStatusListener {

	private final BambooStatusDisplay display;
    // jgorycki: crap. IDEA HTML rendered does not really understand CSS. We have to resort to <hr> ugliness
    private static final String configurePluginLink =
            "<div style=\"text-align:center\""
            + "<hr width=90%>"
            + "<p>"
            + "<a href=\"" + ThePluginApplicationComponent.PLUGIN_CONFIG_URL + "\">Configure</a>"
            + "</p>"
            + "</div>";

	public HtmlBambooStatusListener(BambooStatusDisplay aDisplay) {
		display = aDisplay;
	}

	private String formatLatestPollAndBuildTime(BambooBuild buildInfo) {
		StringBuilder sb = new StringBuilder("<td>");
		DateFormat pollTimeDateFormat = DateFormat.getTimeInstance();

		sb.append(pollTimeDateFormat.format(buildInfo.getPollingTime())).append("</td>");

		Date buildTime = buildInfo.getBuildTime();
		String buildTimeStr = (null == buildTime) ? "---" : formatBuildTime(buildTime);
		sb.append("<td>").append(buildTimeStr).append("</td>");

		return sb.toString();
	}

	private String formatBuildTime(Date date) {
		Calendar barrier = Calendar.getInstance();
		barrier.add(Calendar.HOUR_OF_DAY, -12);

		DateFormat buildDateFormat;

		if (date.before(barrier.getTime())) {
			buildDateFormat = DateFormat.getDateTimeInstance();
		} else {
			buildDateFormat = DateFormat.getTimeInstance();
		}

		return buildDateFormat.format(date);

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
		sb.append(formatLatestPollAndBuildTime(buildInfo));
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
		sb.append(formatLatestPollAndBuildTime(buildInfo));
		sb.append("</tr>");

		return sb.toString();
	}

	Pattern lineSeparator = Pattern.compile("$", Pattern.MULTILINE);

	private String getErrorBuildRow(BambooBuild buildInfo) {
		StringBuilder sb = new StringBuilder("<tr><td><a href='");
		sb.append(buildInfo.getPlanUrl());
		sb.append("'>");
		sb.append(buildInfo.getBuildKey());
		sb.append("</a></td><td></td><td>");
        // TODO: jgorycki: this generates bug PL-95
        // In case of bamboo error garbage is displayed in the tooltip
        String shortMessage = buildInfo.getMessage() != null ?
                lineSeparator.split(buildInfo.getMessage(), 2)[0] : null;
		sb.append("<font color=\"ltgray\">").append(shortMessage).append("</font>");
		sb.append("</td>");
		sb.append(formatLatestPollAndBuildTime(buildInfo));
		sb.append("</tr>");

		return sb.toString();
	}

	public void updateBuildStatuses(Collection<BambooBuild> buildStatuses) {

		BuildStatus status = BuildStatus.BUILD_SUCCEED;
		StringBuilder sb = new StringBuilder(
                "<html>"
                + "<body style=\"font-size:12pt ; font-family: arial, helvetica, sans-serif\">");

		if (buildStatuses == null || buildStatuses.size() == 0) {
			sb.append("No plans defined. " + configurePluginLink);
			status = BuildStatus.UNKNOWN;
		} else {
			sb.append("<table>");
			sb.append("<th>Plan</th><th>Build</th><th>Status</th><th>Last Polling</th><th>Last Build</th>");
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
			sb.append("<p>" + configurePluginLink + "</p>");
		}
		sb.append("</body></html>");
		display.updateBambooStatus(status, sb.toString());
	}
}
