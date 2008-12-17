package com.atlassian.theplugin.crucible.model;

import com.atlassian.theplugin.commons.crucible.CrucibleReviewListener;
import com.atlassian.theplugin.commons.crucible.CrucibleReviewListenerAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFilter;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.idea.crucible.ReviewNotificationBean;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User: jgorycki
 * Date: Dec 2, 2008
 * Time: 10:49:25 AM
 */
public class CrucibleReviewListModelImpl implements CrucibleReviewListModel {
	private List<CrucibleReviewListModelListener> modelListeners = new ArrayList<CrucibleReviewListModelListener>();
	private Map<CrucibleFilter, Set<ReviewAdapter>> reviews = new HashMap<CrucibleFilter, Set<ReviewAdapter>>();
	private ReviewAdapter selectedReview;

	private AtomicLong epoch = new AtomicLong(0);

	private CrucibleReviewListener reviewListener = new LocalCrucibleReviewListener(modelListeners);

	public synchronized Collection<ReviewAdapter> getReviews() {
		Set<ReviewAdapter> plainReviews = new HashSet<ReviewAdapter>();
		for (Set<ReviewAdapter> reviewAdapters : reviews.values()) {
			plainReviews.addAll(reviewAdapters);
		}
		return plainReviews;
	}

	public synchronized void addReview(long epoch, CrucibleFilter crucibleFilter, ReviewAdapter review) {
		ReviewAdapter a = null;

		if (epoch != this.epoch.get()) {
			return;
		}
		Collection<ReviewAdapter> reviews = getReviews();
		if (reviews.contains(review)) {
			for (ReviewAdapter reviewAdapter : reviews) {
				if (reviewAdapter.equals(review)) {
					a = reviewAdapter;
					break;
				}
			}
		}

		if (a != null) {
			a.fillReview(review);
		} else {
			if (!this.reviews.containsKey(crucibleFilter)) {
				this.reviews.put(crucibleFilter, new HashSet<ReviewAdapter>());
			}
			this.reviews.get(crucibleFilter).add(review);
			review.addReviewListener(reviewListener);
			notifyReviewAdded(review);
		}

	}

	public long getEpoch() {
		return epoch.addAndGet(1);

	}


	public synchronized void removeReview(ReviewAdapter review) {

		for (CrucibleFilter filter : reviews.keySet()) {

			if (reviews.get(filter).contains(review)) {
				review.removeReviewListener(reviewListener);
				reviews.get(filter).remove(review);
				notifyReviewRemoved(review);
			}
		}
	}

	public synchronized void removeAll() {
		Set<ReviewAdapter> removed = new HashSet<ReviewAdapter>();
		removed.addAll(getReviews());
		reviews.clear();
		for (ReviewAdapter r : removed) {
			notifyReviewRemoved(r);
		}
	}

	public synchronized void setSelectedReview(ReviewAdapter review) {
		if (review == null || getReviews().contains(review)) {
			selectedReview = review;
		}
	}

	public synchronized ReviewAdapter getSelectedReview() {
		if (getReviews().contains(selectedReview)) {
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

	public synchronized void updateReviews(long epoch, Map<CrucibleFilter, ReviewNotificationBean> updatedReviews) {

		if (epoch != this.epoch.get()) {
			return;
		}

		notifyReviewListUpdateStarted();

		for (CrucibleFilter crucibleFilter : updatedReviews.keySet()) {
			Collection<ReviewAdapter> r = updatedReviews.get(crucibleFilter).getReviews();
			for (ReviewAdapter reviewAdapter : r) {
				addReview(epoch, crucibleFilter, reviewAdapter);
			}
		}

		///create set in order to remove duplicates
		Set<ReviewAdapter> reviewSet = new HashSet<ReviewAdapter>();
		for (CrucibleFilter crucibleFilter : updatedReviews.keySet()) {
			reviewSet.addAll(updatedReviews.get(crucibleFilter).getReviews());
		}

		List<ReviewAdapter> removed = new ArrayList<ReviewAdapter>();

		removed.addAll(getReviews());
		removed.removeAll(reviewSet);

		for (ReviewAdapter r : removed) {
			removeReview(r);
		}

		// remove categories
		for (CrucibleFilter crucibleFilter : reviews.keySet()) {
			if (!updatedReviews.containsKey(crucibleFilter)) {
				reviews.remove(crucibleFilter);
			}
		}

		notifyReviewListUpdateFinished();

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

	private void notifyReviewListUpdateStarted() {
		for (CrucibleReviewListModelListener listener : modelListeners) {
			listener.reviewListUpdateStarted();
		}
	}

	private void notifyReviewListUpdateFinished() {
		for (CrucibleReviewListModelListener listener : modelListeners) {
			listener.reviewListUpdateFinished();
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
