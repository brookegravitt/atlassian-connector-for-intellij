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

package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.bamboo.HtmlBambooStatusListener;
import com.atlassian.theplugin.crucible.CrucibleStatusListener;
import com.atlassian.theplugin.crucible.ReviewDataInfo;
import com.atlassian.theplugin.idea.GenericHyperlinkListener;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.PluginToolWindow;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import thirdparty.javaworld.ClasspathHTMLEditorKit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This one is supposed to be per project.
 */
public class CrucibleNewReviewNotifier implements CrucibleStatusListener {
	private List<ReviewDataInfo> reviews = new ArrayList<ReviewDataInfo>();


	private final CrucibleStatusIcon display;
	private static final Color BACKGROUND_COLOR = new Color(255, 255, 200);

	public CrucibleNewReviewNotifier(CrucibleStatusIcon display) {
		this.display = display;
	}


	public void updateReviews(Collection<ReviewDataInfo> incomingReviews) {
		if (!reviews.containsAll(incomingReviews)) {
			// a set containing the 'last new reviews' that we saw (for painting nicely)
			List<ReviewDataInfo> newReviews = new ArrayList<ReviewDataInfo>(incomingReviews);
			newReviews.removeAll(reviews);

			// notify display
			if (newReviews.size() > 0) {
				display.triggerNewReviewAction(newReviews.size());
			}


			final Project project = IdeaHelper.getCurrentProject();

			if (project != null) {
				StringBuilder sb = new StringBuilder("<table width=\"100%\">");
				sb.append("<tr><td width=20><img src=\"/icons/crucible-blue-16.png\" height=16 width=16 border=0></td>")
					.append("<td colspan=2><b>")
					.append(newReviews.size())
					.append(" New Crucible Review")
					.append(newReviews.size() != 1 ? "s" : "")
					.append("</b></td></tr>");

				for (ReviewDataInfo newReview : newReviews) {
					String id = newReview.getPermaId().getId();
					sb.append("<tr><td colspan=2 width=\"1%\" nowrap valign=top><a href=\"")
						.append(newReview.getReviewUrl()).append("\">")
						.append(id).append("</a></td><td>").append(newReview.getName()).append("</td></tr>");
				}
				sb.append("</table>");
				JEditorPane content = new JEditorPane();
				content.setEditable(false);
				content.setContentType("text/html");
				content.setEditorKit(new ClasspathHTMLEditorKit());
				content.setText("<html>" + HtmlBambooStatusListener.BODY_WITH_STYLE + sb.toString() + "</body></html>");
				content.setBackground(BACKGROUND_COLOR);
				content.addHyperlinkListener(new GenericHyperlinkListener());

				content.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						display.resetIcon();
						PluginToolWindow.focusPanel(project, PluginToolWindow.ToolWindowPanels.CRUCIBLE);
					}
				});
				content.setCaretPosition(0); // do thi to make sure scroll pane is always at the top / header
				WindowManager.getInstance().getStatusBar(project).fireNotificationPopup(
						new JScrollPane(content), BACKGROUND_COLOR);
			}
		}

		reviews = new ArrayList<ReviewDataInfo>(incomingReviews);
	}

	public void resetState() {
		display.resetIcon();
	}
}
