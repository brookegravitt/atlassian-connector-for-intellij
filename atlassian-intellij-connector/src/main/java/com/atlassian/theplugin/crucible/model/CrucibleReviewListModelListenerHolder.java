/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atlassian.theplugin.crucible.model;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFilter;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.idea.crucible.ReviewNotificationBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: jgorycki
 * Date: Dec 15, 2008
 * Time: 4:07:35 PM
 */
public abstract class CrucibleReviewListModelListenerHolder
		implements CrucibleReviewListModelListener, CrucibleReviewListModel {

	private List<CrucibleReviewListModelListener> listeners = new ArrayList<CrucibleReviewListModelListener>();
	protected final CrucibleReviewListModel parent;

	protected CrucibleReviewListModelListenerHolder(CrucibleReviewListModel parent) {
		this.parent = parent;
		parent.addListener(this);
	}

	public void addListener(CrucibleReviewListModelListener l) {
		listeners.add(l);
	}

	public void removeListener(CrucibleReviewListModelListener l) {
		listeners.remove(l);
	}

	public void reviewAdded(UpdateContext updateContext) {
		for (CrucibleReviewListModelListener l : listeners) {
			l.reviewAdded(updateContext);
		}
	}

	public void reviewRemoved(UpdateContext updateContext) {
		for (CrucibleReviewListModelListener l : listeners) {
			l.reviewRemoved(updateContext);
		}
	}

	public void reviewChanged(UpdateContext updateContext) {
		for (CrucibleReviewListModelListener l : listeners) {
			l.reviewChanged(updateContext);
		}
	}

	public void reviewListUpdateStarted(UpdateContext updateContext) {
		for (CrucibleReviewListModelListener l : listeners) {
			l.reviewListUpdateStarted(updateContext);
		}
	}

	public void reviewListUpdateFinished(UpdateContext updateContext) {
		for (CrucibleReviewListModelListener l : listeners) {
			l.reviewListUpdateFinished(updateContext);
		}
	}

	public void reviewChangedWithoutFiles(UpdateContext updateContext) {
		for (CrucibleReviewListModelListener l : listeners) {
			l.reviewChangedWithoutFiles(updateContext);
		}
	}

	public void modelChanged(UpdateContext updateContext) {
		for (CrucibleReviewListModelListener l : listeners) {
			l.modelChanged(updateContext);
		}
	}

	public void reviewListUpdateError(final UpdateContext updateContext, final Exception exception) {
		for (CrucibleReviewListModelListener l : listeners) {
			l.reviewListUpdateError(updateContext, exception);
		}
	}

	/*
	 public void removeAll() {
		 parent.removeAll();
	 }
 */
	public void updateReviews(final long epoch,
							  final Map<CrucibleFilter, ReviewNotificationBean> updatedReviews,
							  final UpdateReason updateReason) {
		parent.updateReviews(epoch, updatedReviews, updateReason);
	}

	public ReviewAdapter getSelectedReview() {
		return parent.getSelectedReview();
	}

	public void setSelectedReview(ReviewAdapter review) {
		parent.setSelectedReview(review);
	}

	public ReviewAdapter getActiveReview() {
		return parent.getActiveReview();
	}

	public void setActiveReview(ReviewAdapter review) {
		parent.setActiveReview(review);
	}
}
