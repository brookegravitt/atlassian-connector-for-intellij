package com.atlassian.theplugin.crucible;

import java.util.Collection;
import java.util.Iterator;

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
			sb.append("<table width=\"100%\">");
			sb.append("<tr><td colspan=5>Currently <b>" + reviews.size() + " open code reviews</b> for you.<br>&nbsp;</td></tr>");
			sb.append("<tr><th>Key</th><th>Summary</th><th>Author</th><th>State</th><th>Reviewers</th></tr>");
			for (RemoteReview review : reviews) {
                sb.append("<tr><td valign=\"top\"><a href='");
                sb.append(review.getReviewUrl());
                sb.append("'>");
                sb.append(review.getReviewData().getPermaId().getId());
                sb.append("</a></td>");
                sb.append("<td valign=\"top\">" + review.getReviewData().getName() + "</td>");
                sb.append("<td valign=\"top\">" + review.getReviewData().getAuthor() + "</td>");
                sb.append("<td valign=\"top\">" + review.getReviewData().getState() + "</td>");
                sb.append("<td valign=\"top\">");
                for (Iterator<String> iterator = review.getReviewers().iterator(); iterator.hasNext();) {
                    String reviewer = iterator.next();
                    sb.append(reviewer);
                    if (iterator.hasNext()) {
                        sb.append("<br>");
                    }
                }
                sb.append("</td></tr>");
			}
			sb.append("</table>");
		}
		sb.append("</body></html>");
		display.updateCrucibleStatus(sb.toString());
	}
}