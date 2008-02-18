package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.idea.ThePluginProjectComponent;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.ContentManager;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-15
 * Time: 10:57:34
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleStatusIcon extends JLabel {

	private transient ThePluginProjectComponent projectComponent;

	private static final Icon ICON_NEW = IconLoader.getIcon("/icons/crucible-blue-16.png");
	private static final Icon ICON_STANDARD = IconLoader.getIcon("/icons/crucible-grey-16.png");
	private static final String NO_NEW_REVIEWS = "No new reviews.";
	private static final String NEW_REVIEWS = "New reviews are available. Click for details.";

	public CrucibleStatusIcon(final ThePluginProjectComponent component) {
		projectComponent = component;

		setStandardIcon();

		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				ToolWindow toolWindow = projectComponent.getToolWindow();
                ContentManager contentManager = toolWindow.getContentManager();
                toolWindow.activate(null);
                contentManager.setSelectedContent(contentManager.findContent("Crucible"));

				// restore standard Icon on click
				setStandardIcon();
			}
		});
	}

	public void triggerNewReviewAction(int numOfNewReviews) {
		this.setIcon(ICON_NEW);
		this.setToolTipText(NEW_REVIEWS);
		this.setText(Integer.toString(numOfNewReviews));
	}

	public void setStandardIcon() {
		this.setIcon(ICON_STANDARD);
		this.setToolTipText(NO_NEW_REVIEWS);
		this.setText(null);
	}
}
