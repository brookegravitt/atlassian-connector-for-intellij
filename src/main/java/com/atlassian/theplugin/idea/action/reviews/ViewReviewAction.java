package com.atlassian.theplugin.idea.action.reviews;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.idea.Constants;

/**
 * User: jgorycki
 * Date: Dec 3, 2008
 * Time: 12:28:04 PM
 */
public class ViewReviewAction extends AnAction {
	public void actionPerformed(AnActionEvent e) {
		ReviewAdapter review = e.getData(Constants.REVIEW_KEY);
		if (review != null) {
			BrowserUtil.launchBrowser(review.getReviewUrl());
		}
	}

	public void update(AnActionEvent e) {
		e.getPresentation().setEnabled(e.getData(Constants.REVIEW_KEY) != null);
	}
}
