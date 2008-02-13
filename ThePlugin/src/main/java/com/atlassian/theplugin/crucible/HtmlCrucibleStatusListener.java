package com.atlassian.theplugin.crucible;

import static com.atlassian.theplugin.bamboo.BuildStatus.BUILD_FAILED;
import com.atlassian.theplugin.bamboo.BambooStatusListener;
import com.atlassian.theplugin.bamboo.BambooStatusDisplay;
import com.atlassian.theplugin.bamboo.BambooBuild;
import com.atlassian.theplugin.bamboo.BuildStatus;
import com.atlassian.theplugin.idea.ThePluginApplicationComponent;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * Renders Crucible reviews as HTML and passes it to configured {@link CrucibleStatusDisplay}
 */
public class HtmlCrucibleStatusListener implements CrucibleStatusListener {

	private final CrucibleStatusDisplay display;
	public HtmlCrucibleStatusListener(CrucibleStatusDisplay aDisplay) {
		display = aDisplay;
	}

    public void updateReviews(Collection<RemoteReview> reviews) {
		StringBuilder sb = new StringBuilder(
                "<html>"
                + "<body style=\"font-size:12pt ; font-family: arial, helvetica, sans-serif\">");

		if (reviews == null || reviews.size() == 0) {
			sb.append("No reviews at this time.");
		} else {
			sb.append("<table>");
			sb.append("<th>Key</th><th>Summary</th><th>Author</th><th>State</th><th>Reviewers</th>");
			for (RemoteReview review : reviews) {
                sb.append("<tr><td><a href='");
                sb.append(review.getReviewUrl());
                sb.append("'>");
                sb.append(review.getReviewData().getPermaId());
                sb.append("</a></td>");
                sb.append("<td>" + review.getReviewData().getDescription() + "</td>");
                sb.append("<td>" + review.getReviewData().getAuthor() + "</td>");
                sb.append("<td>" + review.getReviewData().getState() + "</td>");
                sb.append("<td>");
                for (Iterator<String> iterator = review.getReviewers().iterator(); iterator.hasNext();) {
                    String reviewer = iterator.next();
                    sb.append(reviewer + "<br>");
                }
                sb.append("</td></tr>");
			}
			sb.append("</table>");
		}
		sb.append("</body></html>");
		display.updateCrucibleStatus(sb.toString());
	}
}