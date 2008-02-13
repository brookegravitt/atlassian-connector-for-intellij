package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.crucible.CrucibleStatusListener;
import com.atlassian.theplugin.crucible.RemoteReview;
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
	List<RemoteReview> reviews = new ArrayList<RemoteReview>();

	public void updateReviews(Collection<RemoteReview> incomingReviews) {
		if (!reviews.containsAll(incomingReviews))
		{
			List newReviews = new ArrayList(incomingReviews);
			newReviews.removeAll(reviews);

			final Project project = ProjectManager.getInstance().getOpenProjects()[0];
			JComponent popup = new JLabel(newReviews.size() + " new reviews!");
			popup.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					System.out.println("UserDataContext.mouseClicked");
					ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(ThePluginProjectComponent.TOOL_WINDOW_NAME);
					ContentManager contentManager = toolWindow.getContentManager();
					toolWindow.activate(null);
					contentManager.setSelectedContent(contentManager.getContent(1));
				}
			});
			WindowManager.getInstance().getStatusBar(project).fireNotificationPopup(popup, new Color(255, 255, 200));
		}

		reviews = new ArrayList<RemoteReview>(incomingReviews);
	}
}
