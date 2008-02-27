package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.idea.StatusBarPluginIcon;
import com.atlassian.theplugin.ServerType;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Defines crucible icon behaviour.
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-15
 * Time: 10:57:34
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleStatusIcon extends StatusBarPluginIcon {

	private static final Icon ICON_NEW = IconLoader.getIcon("/icons/crucible-blue-16.png");
	private static final Icon ICON_STANDARD = IconLoader.getIcon("/icons/crucible-grey-16.png");
	private static final String NO_NEW_REVIEWS = "No new reviews.";
	private static final String NEW_REVIEWS = "New reviews are available. Click for details.";

	public CrucibleStatusIcon(final Project project) {
		super(project);
		resetIcon();

		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
                IdeaHelper.focusPanel(IdeaHelper.TOOLWINDOW_PANEL_CRUCIBLE);
				resetIcon();
			}
		});
	}

	/**
	 * Sets the icon to NEW REVEWS state (blue icon, number of revious in text label, tooltip text)
	 * @param numOfNewReviews number of new reviews
	 */
	public void triggerNewReviewAction(int numOfNewReviews) {
		this.setIcon(ICON_NEW);
		this.setToolTipText(NEW_REVIEWS);
		this.setText(Integer.toString(numOfNewReviews));
	}

	/**
	 * Sets the icon to standard state (sets grey icon, removes text label, change tooltip)
	 */
	public void resetIcon() {
		this.setIcon(ICON_STANDARD);
		this.setToolTipText(NO_NEW_REVIEWS);
		this.setText(null);
	}

	public void showOrHideIcon() {
		super.showOrHideIcon(ServerType.CRUCIBLE_SERVER);
	}
}
