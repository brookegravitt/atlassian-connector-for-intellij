package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CrucibleStatusIcon extends JLabel {

	private static final Icon ICON_NEW = IconLoader.getIcon("/icons/crucible-blue-16.png");
	private static final Icon ICON_STANDARD = IconLoader.getIcon("/icons/crucible-grey-16.png");
	private static final String NO_NEW_REVIEWS = "No new reviews.";
	private static final String NEW_REVIEWS = "New reviews are available. Click for details.";

	public CrucibleStatusIcon() {
		setStandardIcon();

		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
                IdeaHelper.focusPanel(IdeaHelper.TOOLWINDOW_PANEL_CRUCIBLE);
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
