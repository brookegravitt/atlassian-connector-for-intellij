package com.atlassian.theplugin.idea.action.reviews;

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.idea.Constants;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: jgorycki
 * Date: Dec 3, 2008
 * Time: 12:28:04 PM
 */
public class ViewReviewAction extends AbstractCrucibleToolbarAction {
	public void actionPerformed(AnActionEvent e) {
		ReviewAdapter review = e.getData(Constants.REVIEW_KEY);
		if (review != null) {
			BrowserUtil.launchBrowser(review.getReviewUrl());
		}
	}

	@Override
	public boolean onUpdate(AnActionEvent e) {
		return e.getData(Constants.REVIEW_KEY) != null;
	}
}
