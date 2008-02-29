package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.bamboo.HtmlBambooStatusListener;
import com.atlassian.theplugin.crucible.CrucibleStatusListener;
import com.atlassian.theplugin.crucible.ReviewDataInfo;
import com.atlassian.theplugin.idea.crucible.CrucibleStatusIcon;
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

public class UserDataContext implements CrucibleStatusListener {
	private List<ReviewDataInfo> reviews = new ArrayList<ReviewDataInfo>();


	private CrucibleStatusIcon display = null;

	public void setDisplay(CrucibleStatusIcon display) {
		this.display = display;
	}

	private static final int R = 255;
	private static final int G = 255;
	private static final int B = 200;

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
				content.setBackground(new Color(R, G, B));
				content.addHyperlinkListener(new GenericHyperlinkListener());

				content.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						display.resetIcon();
						IdeaHelper.focusPanel(project, IdeaHelper.TOOLWINDOW_PANEL_CRUCIBLE);
					}
				});
				content.setCaretPosition(0); // do thi to make sure scroll pane is always at the top / header
				WindowManager.getInstance().getStatusBar(project).fireNotificationPopup(
						new JScrollPane(content), new Color(R, G, B));
			}
		}

		reviews = new ArrayList<ReviewDataInfo>(incomingReviews);
	}
}
