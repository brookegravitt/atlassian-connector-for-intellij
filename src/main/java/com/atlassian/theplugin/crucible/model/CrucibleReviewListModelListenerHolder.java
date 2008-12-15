package com.atlassian.theplugin.crucible.model;

import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.cfg.ServerId;

import java.util.List;
import java.util.ArrayList;

/**
 * User: jgorycki
 * Date: Dec 15, 2008
 * Time: 4:07:35 PM
 */
public class CrucibleReviewListModelListenerHolder implements CrucibleReviewListModelListener {
	private List<CrucibleReviewListModelListener> listeners = new ArrayList<CrucibleReviewListModelListener>();

	public void addListener(CrucibleReviewListModelListener l) {
		listeners.add(l);
	}

	public void removeListener(CrucibleReviewListModelListener l) {
		listeners.remove(l);
	}

	public void reviewAdded(ReviewAdapter review) {
		for (CrucibleReviewListModelListener l : listeners) {
			l.reviewAdded(review);
		}
	}

	public void reviewRemoved(ReviewAdapter review) {
		for (CrucibleReviewListModelListener l : listeners) {
			l.reviewRemoved(review);
		}
	}

	public void reviewChanged(ReviewAdapter review) {
		for (CrucibleReviewListModelListener l : listeners) {
			l.reviewChanged(review);
		}
	}

	public void reviewListUpdateStarted(ServerId serverId) {
		for (CrucibleReviewListModelListener l : listeners) {
			l.reviewListUpdateStarted(serverId);
		}
	}

	public void reviewListUpdateFinished(ServerId serverId) {
		for (CrucibleReviewListModelListener l : listeners) {
			l.reviewListUpdateFinished(serverId);
		}
	}

	public void reviewChangedWithoutFiles(ReviewAdapter review) {
		for (CrucibleReviewListModelListener l : listeners) {
			l.reviewChangedWithoutFiles(review);
		}
	}

	public void modelChanged() {
		for (CrucibleReviewListModelListener l : listeners) {
			l.modelChanged();
		}
	}
}
