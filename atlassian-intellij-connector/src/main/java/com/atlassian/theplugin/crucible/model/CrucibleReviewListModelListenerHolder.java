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

import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

	public void addReview(ReviewAdapter review) {
		parent.addReview(review);
	}

	public void removeReview(ReviewAdapter review) {
		parent.removeReview(review);
	}

	public void removeAll() {
		parent.removeAll();
	}

	public void updateReviews(CrucibleServerCfg serverCfg, Collection<ReviewAdapter> updatedReviews) {
		parent.updateReviews(serverCfg, updatedReviews);
	}

	public ReviewAdapter getSelectedReview() {
		return parent.getSelectedReview();
	}

	public void setSelectedReview(ReviewAdapter review) {
		parent.setSelectedReview(review);
	}
}
