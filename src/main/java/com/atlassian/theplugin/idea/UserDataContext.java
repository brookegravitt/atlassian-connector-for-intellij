package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.bamboo.HtmlBambooStatusListener;
import com.atlassian.theplugin.crucible.CrucibleStatusListener;
import com.atlassian.theplugin.crucible.ReviewDataInfo;
import com.atlassian.theplugin.idea.crucible.CrucibleStatusIcon;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.content.ContentManager;
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


	private CrucibleStatusIcon display;

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


			final Project project = ProjectManager.getInstance().getOpenProjects()[0];

			StringBuffer sb = new StringBuffer(
					"<table width=\"100%\">"
					+ "<tr><td width=20><img src=\"/icons/crucible-blue-16.png\" height=16 width=16 border=0></td>"
					+ "<td colspan=2><b>"
					+ newReviews.size()
					+ " New Crucible Review"
					+ (newReviews.size() != 1 ? "s" : "")
					+ "</b></td></tr>");

			for (ReviewDataInfo newReview : newReviews) {
				String id = newReview.getPermaId().getId();
				sb.append(
						"<tr><td colspan=2 width=\"1%\" nowrap valign=top><a href=\""
						+ newReview.getReviewUrl() + "\">"
						+ id
						+ "</a></td><td>"
						+ newReview.getName()
						+ "</td></tr>");
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
					ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(
							ThePluginProjectComponent.TOOL_WINDOW_NAME);
					ContentManager contentManager = toolWindow.getContentManager();
					toolWindow.activate(null);
					contentManager.setSelectedContent(contentManager.findContent("Crucible"));

					display.setStandardIcon();
				}
			});
			content.setCaretPosition(0); // do thi to make sure scroll pane is always at the top / header
			WindowManager.getInstance().getStatusBar(project).fireNotificationPopup(
					new JScrollPane(content), new Color(R, G, B));
		}

		reviews = new ArrayList<ReviewDataInfo>(incomingReviews);
	}
}
