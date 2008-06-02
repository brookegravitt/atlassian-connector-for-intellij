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

package com.atlassian.theplugin.commons.crucible;

import com.atlassian.theplugin.commons.StatusListener;
import com.atlassian.theplugin.commons.crucible.api.UserData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Renders Crucible reviews as HTML and passes it to configured {@link CrucibleStatusDisplay}
 */
public class HtmlCrucibleStatusListener implements StatusListener {

	private final CrucibleStatusDisplay display;
	public static final String BODY_WITH_STYLE =
			"<body style=\"font-size:12pt ; font-family: arial, helvetica, sans-serif\">";


	public HtmlCrucibleStatusListener(CrucibleStatusDisplay aDisplay) {
		display = aDisplay;
	}

    public void updateReviews(Collection<ReviewDataInfo> reviews) {
		StringBuilder sb = new StringBuilder(
                "<html>"
                + BODY_WITH_STYLE);

		if (reviews == null || reviews.size() == 0) {
			sb.append("No reviews at this time.");
		} else {
			sb.append("<table width=\"100%\">");
			sb.append(
					"<tr><td colspan=5>Currently <b>").append(
						reviews.size()).append(
							" open code reviews</b> for you.<br>&nbsp;</td></tr>");
			sb.append("<tr><th>Key</th><th>Summary</th><th>Author</th><th>State</th><th>Reviewers</th></tr>");
			for (ReviewDataInfo review : reviews) {
                sb.append("<tr><td valign=\"top\"><b><font color=blue><a href='");
                sb.append(review.getReviewUrl());
                sb.append("'>");
                sb.append(review.getPermaId().getId());
                sb.append("</a></font></b></td>");
                sb.append("<td valign=\"top\">" + review.getName() + "</td>");
                sb.append("<td valign=\"top\">" + review.getAuthor() + "</td>");
                sb.append("<td valign=\"top\">" + review.getState().value() + "</td>");
                sb.append("<td valign=\"top\">");
                for (Iterator<UserData> iterator = review.getReviewers().iterator(); iterator.hasNext();) {
                    UserData reviewer = iterator.next();
                    sb.append(reviewer.getUserName());
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

	public void resetState() {
		updateReviews(new ArrayList<ReviewDataInfo>());
	}
}