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

package com.atlassian.theplugin.idea.crucible.table.renderer;

import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;


public class ReviewInfoCellRenderer extends ReviewCellRenderer {
	public static final String BODY_WITH_STYLE =
			"<body style=\"font-size:12pt ; font-family: arial, helvetica, sans-serif\">";

	private String buildTolltip(ReviewData review) {
		StringBuilder sb = new StringBuilder(
                "<html>"
                + BODY_WITH_STYLE);

		sb.append("<table width=\"100%\">");
		sb.append("<tr><td colspan=5><b><font color=blue>");
        sb.append(review.getPermId().getId());
        sb.append("</font></b>");

		sb.append("<tr><td valign=\"top\"><b>Summary:</b></td><td valign=\"top\">");
		sb.append(review.getName());
		sb.append("");
		sb.append("</td></tr>");

		sb.append("<tr><td valign=\"top\"><b>Description:</b></td><td valign=\"top\">");
		sb.append(review.getDescription());
		sb.append("");
		sb.append("</td></tr>");

		sb.append("<tr><td valign=\"top\"><b>Author:</b></td><td valign=\"top\">");
		sb.append(review.getAuthor().getDisplayName());
		sb.append("");
		sb.append("</td></tr>");

        sb.append("<tr><td valign=\"top\"><b>Moderator:</b></td><td valign=\"top\">");
        sb.append(review.getModerator().getDisplayName());
        sb.append("");
        sb.append("</td></tr>");

        sb.append("<tr><td valign=\"top\"><b>Created:</b></td><td valign=\"top\">");
		sb.append(review.getCreateDate());
		sb.append("");
		sb.append("</td></tr>");

		sb.append("<tr><td valign=\"top\"><b>Number of comments:</b></td><td valign=\"top\">");
		try {
			sb.append(Integer.toString(review.getGeneralComments().size() + review.getVersionedComments().size()));
		} catch (ValueNotYetInitialized valueNotYetInitialized) {
			sb.append("N/A");
		}
		sb.append("");
		sb.append("</td></tr>");

		sb.append("<tr><td valign=\"top\"><b>Status:</b></td><td valign=\"top\">");
		sb.append(review.getState().value());
		sb.append("");
		sb.append("</td></tr>");

		sb.append("</table>");
		sb.append("</body></html>");
		return sb.toString();
	}


	protected String getCellText(ReviewData review) {
		return review.getName();
	}

	protected String getCellToolTipText(ReviewData review) {
		return buildTolltip(review);
	}
}