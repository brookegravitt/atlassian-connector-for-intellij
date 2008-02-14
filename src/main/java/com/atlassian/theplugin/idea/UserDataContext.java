package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.bamboo.HtmlBambooStatusListener;
import com.atlassian.theplugin.crucible.CrucibleStatusListener;
import com.atlassian.theplugin.crucible.RemoteReview;
import com.atlassian.theplugin.util.ClasspathHTMLEditorKit;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.content.ContentManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserDataContext implements CrucibleStatusListener {
	private List<RemoteReview> reviews = new ArrayList<RemoteReview>();

	// a set containing the 'last new reviews' that we saw (for painting nicely)
	private List<RemoteReview> newReviews = new ArrayList<RemoteReview>();

	private static final int R = 255;
	private static final int G = 255;
	private static final int B = 200;

	public void updateReviews(Collection<RemoteReview> incomingReviews) {
		if (reviews.size() > 0 && !reviews.containsAll(incomingReviews)) {
			newReviews = new ArrayList(incomingReviews);
			newReviews.removeAll(reviews);

			final Project project = ProjectManager.getInstance().getOpenProjects()[0];

			StringBuffer sb = new StringBuffer(
					"<table width=\"100%\">"
					+ "<tr><td width=20><img src=\"/icons/crucible-blue-16.png\" height=16 width=16 border=0></td>"
					+ "<td colspan=2><b>"
					+ newReviews.size()
					+ " New Crucible Review"
					+ (newReviews.size() != 1 ? "s" : "")
					+ "</b></td></tr>");

			for (RemoteReview newReview : newReviews) {
				String id = newReview.getReviewData().getPermaId().getId();
				sb.append(
						"<tr><td colspan=2 width=\"1%\" nowrap valign=top><a href=\""
						+ newReview.getReviewUrl() + "\">"
						+ id
						+ "</a></td><td>"
						+ newReview.getReviewData().getName()
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
				}
			});
			content.setCaretPosition(0); // do thi to make sure scroll pane is always at the top / header
			WindowManager.getInstance().getStatusBar(project).fireNotificationPopup(
					new JScrollPane(content), new Color(R, G, B));
		}

		reviews = new ArrayList<RemoteReview>(incomingReviews);
	}
}
