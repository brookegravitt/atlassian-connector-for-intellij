package com.atlassian.theplugin.bamboo;

import static com.atlassian.theplugin.bamboo.BuildStatus.BUILD_FAILED;

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

    private static final int TIME_OFFSET = -12;

    public static final String BODY_WITH_STYLE =
            "<body style=\"font-size:12pt ; font-family: arial, helvetica, sans-serif\">";

    public HtmlBambooStatusListener(BambooStatusDisplay aDisplay) {
		display = aDisplay;
	}

	private String formatLatestPollAndBuildTime(BambooBuild buildInfo) {
		StringBuilder sb = new StringBuilder("<td nowrap align=\"right\">");
		DateFormat pollTimeDateFormat = DateFormat.getTimeInstance();

		sb.append(pollTimeDateFormat.format(buildInfo.getPollingTime())).append("</td>");

		Date buildTime = buildInfo.getBuildTime();
		String buildTimeStr = (null == buildTime) ? "&nbsp;" : formatBuildTime(buildTime);
		sb.append("<td nowrap align=\"right\">").append(buildTimeStr).append("</td>");

		return sb.toString();
	}

	private String formatBuildTime(Date date) {
		Calendar barrier = Calendar.getInstance();
		barrier.add(Calendar.HOUR_OF_DAY, TIME_OFFSET);

		DateFormat buildDateFormat;

		if (date.before(barrier.getTime())) {
			buildDateFormat = DateFormat.getDateTimeInstance();
		} else {
			buildDateFormat = DateFormat.getTimeInstance();
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
		return drawRow(buildInfo, "ltgrey", "icn_plan_disabled.gif");
	}

	private String drawRow(BambooBuild buildInfo, String colour, String icon)
	{
		StringBuilder sb = new StringBuilder("<tr>");
		sb.append("<td><a href='" + buildInfo.getBuildUrl() + "'><img src=\"/icons/" + icon + "\" height=\"16\" width=\"16\" border=\"0\" align=\"absmiddle\"></a></td>");
		if (buildInfo.getStatus() == BuildStatus.UNKNOWN)
		{
			// TODO: jgorycki: this generates bug PL-95
			// In case of bamboo error garbage is displayed in the tooltip
			String shortMessage = buildInfo.getMessage() != null ? lineSeparator.split(buildInfo.getMessage(), 2)[0] : null;
			sb.append("<td><font color=\"" + colour + "\">").append(shortMessage).append("</font></td>");
		}
		else
		{
            String font = "<font color=\"" + colour + "\">";
            boolean bamboo2 = !buildInfo.getProjectName().equals("");
                sb.append("<td width=\"1%\" nowrap>");
                if (bamboo2)
                {
                    sb.append("<b>");
                    sb.append("<a href='" + buildInfo.getProjectUrl() + "'>" + font + buildInfo.getProjectName() + "</font></a>&nbsp;&nbsp;");
                    sb.append("<a href='" + buildInfo.getBuildUrl() + "'>" + font + buildInfo.getBuildName() + "</font></a>");
                    sb.append(font + " &gt; </font>");
                    sb.append("</b>");
                }
                sb.append("<a href='" + buildInfo.getBuildResultUrl() + "'>" + font + "<b>" + buildInfo.getBuildKey() + "-" + buildInfo.getBuildNumber() + "</b></font></a></td>");
		}
		sb.append(formatLatestPollAndBuildTime(buildInfo));
		sb.append("</tr>");

		return sb.toString();
	}

	Pattern lineSeparator = Pattern.compile("$", Pattern.MULTILINE);


	public void updateBuildStatuses(Collection<BambooBuild> buildStatuses) {

		BuildStatus status = BuildStatus.BUILD_SUCCEED;
		StringBuilder sb = new StringBuilder("<html>" + BODY_WITH_STYLE);

		if (buildStatuses == null || buildStatuses.size() == 0) {
			sb.append("No plans defined.");
			status = BuildStatus.UNKNOWN;
		} else {
			sb.append("<table width=\"100%\">");
			sb.append("<th width=\"1%\"></th><th width=\"100%\" align=\"left\">Build</th><th width=\"1%\">Last Polling</th><th width=\"1%\">Last Build</th>");
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
		display.updateBambooStatus(status, sb.toString());
	}
}
