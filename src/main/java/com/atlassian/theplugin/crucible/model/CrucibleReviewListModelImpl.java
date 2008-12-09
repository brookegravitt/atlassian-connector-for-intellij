package com.atlassian.theplugin.crucible.model;

import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.crucible.CrucibleReviewListener;
import com.atlassian.theplugin.commons.crucible.CrucibleReviewListenerAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;

import java.util.*;

/**
 * User: jgorycki
 * Date: Dec 2, 2008
 * Time: 10:49:25 AM
 */
public class CrucibleReviewListModelImpl implements CrucibleReviewListModel {
	private List<CrucibleReviewListModelListener> modelListeners = new ArrayList<CrucibleReviewListModelListener>();
	private List<ReviewAdapter> reviews = new ArrayList<ReviewAdapter>();
	private ReviewAdapter selectedReview;

	private CrucibleReviewListener reviewListener = new LocalCrucibleReviewListener(modelListeners);

	public synchronized Collection<ReviewAdapter> getReviews() {
		return reviews;
	}

	public synchronized void addReview(ReviewAdapter review) {

//		review.getActions().

		int idx = reviews.indexOf(review);
		if (idx != -1) {
			ReviewAdapter a = reviews.get(idx);
			a.fillReview(review);
		} else {
			reviews.add(review);
			review.addReviewListener(reviewListener);
			notifyReviewAdded(review);
		}
	}

	public synchronized void removeReview(ReviewAdapter review) {
		if (reviews.contains(review)) {
			review.removeReviewListener(reviewListener);
			reviews.remove(review);
			notifyReviewRemoved(review);
		}
	}

	public synchronized void removeAll() {
		List<ReviewAdapter> removed = new ArrayList<ReviewAdapter>();
		removed.addAll(reviews);
		reviews.clear();
		for (ReviewAdapter r : removed) {
			notifyReviewRemoved(r);
		}
	}

	public synchronized void setSelectedReview(ReviewAdapter review) {
		if (review == null || reviews.contains(review)) {
			selectedReview = review;
		}
	}

	public synchronized ReviewAdapter getSelectedReview() {
		if (reviews.contains(selectedReview)) {
			return selectedReview;
		}
		return null;
	}

	public void addListener(CrucibleReviewListModelListener listener) {
		if (!modelListeners.contains(listener)) {
			modelListeners.add(listener);
		}
	}

	public void removeListener(CrucibleReviewListModelListener listener) {
		modelListeners.remove(listener);
	}

	public synchronized void updateReviews(CrucibleServerCfg serverCfg, Collection<ReviewAdapter> updatedReviews) {

		notifyReviewListUpdateStarted(serverCfg.getServerId());

		///create set in order to remove duplicates
		Set<ReviewAdapter> reviewSet = new HashSet<ReviewAdapter>();
		reviewSet.addAll(updatedReviews);

		List<ReviewAdapter> removed = new ArrayList<ReviewAdapter>();

		for (ReviewAdapter adapter : reviewSet) {
			if (adapter.getServer().equals(serverCfg)) {
				addReview(adapter);
			}
		}

		removed.addAll(reviews);
		removed.removeAll(reviewSet);

		for (ReviewAdapter r : removed) {
			if (r.getServer().equals(serverCfg)) {
				removeReview(r);
			}
		}

		notifyReviewListUpdateFinished(serverCfg.getServerId());
	}

	private void notifyReviewChanged(ReviewAdapter review) {
		for (CrucibleReviewListModelListener listener : modelListeners) {
			listener.reviewChanged(review);
		}
	}

	private void notifyReviewAdded(ReviewAdapter review) {
		for (CrucibleReviewListModelListener listener : modelListeners) {
			listener.reviewAdded(review);
		}
	}

	private void notifyReviewRemoved(ReviewAdapter review) {
		for (CrucibleReviewListModelListener listener : modelListeners) {
			listener.reviewRemoved(review);
		}
	}

	private void notifyReviewListUpdateStarted(ServerId serverId) {
		for (CrucibleReviewListModelListener listener : modelListeners) {
			listener.reviewListUpdateStarted(serverId);
		}
	}

	private void notifyReviewListUpdateFinished(ServerId serverId) {
		for (CrucibleReviewListModelListener listener : modelListeners) {
			listener.reviewListUpdateFinished(serverId);
		}
	}

	private class LocalCrucibleReviewListener extends CrucibleReviewListenerAdapter {
		private List<CrucibleReviewListModelListener> modelListeners;

		public LocalCrucibleReviewListener(List<CrucibleReviewListModelListener> modelListeners) {
			this.modelListeners = modelListeners;
		}

//		public void createdOrEditedVersionedCommentReply(ReviewAdapter review, PermId file,
//														 VersionedComment parentComment, VersionedComment comment) {
//			notifyReviewChanged(review);
//		}
//
//		public void createdOrEditedGeneralCommentReply(ReviewAdapter review, GeneralComment parentComment,
//													   GeneralComment comment) {
//			notifyReviewChanged(review);
//		}
//
//		public void createdOrEditedGeneralComment(ReviewAdapter review, GeneralComment comment) {
//			notifyReviewChanged(review);
//		}
//
//		public void createdOrEditedVersionedComment(ReviewAdapter review, PermId file, VersionedComment comment) {
//			notifyReviewChanged(review);
//		}
//
//		public void removedComment(ReviewAdapter review, Comment comment) {
//			notifyReviewChanged(review);
//		}
//
//		public void publishedGeneralComment(ReviewAdapter review, GeneralComment comment) {
//			notifyReviewChanged(review);
//		}
//
//		public void publishedVersionedComment(ReviewAdapter review, PermId filePermId, VersionedComment comment) {
//			notifyReviewChanged(review);
//		}

		@Override
		public void reviewChangedWithoutFiles(ReviewAdapter newReview) {
			for (CrucibleReviewListModelListener listener : modelListeners) {
				listener.reviewChangedWithoutFiles(newReview);
			}
		}

//		@Override
//		public void reviewFilesChanged(ReviewAdapter reviewAdapter) {
//			notifyReviewChanged(reviewAdapter);
//		}

//		@Override
//		public void reviewChanged(ReviewAdapter reviewAdapter) {
//			notifyReviewChanged(reviewAdapter);
//		}
	}

}
