package com.atlassian.theplugin.crucible.model;

import com.atlassian.theplugin.commons.crucible.CrucibleReviewListener;
import com.atlassian.theplugin.commons.crucible.CrucibleReviewListenerAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFilter;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.idea.crucible.ReviewNotificationBean;
import org.jetbrains.annotations.NotNull;

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
	private final ReviewListModelBuilder reviewListModelBuilder;

	public CrucibleReviewListModelImpl(@NotNull final ReviewListModelBuilder reviewListModelBuilder) {
		this.reviewListModelBuilder = reviewListModelBuilder;
	}

	public synchronized Collection<ReviewAdapter> getReviews() {
		Set<ReviewAdapter> plainReviews = new HashSet<ReviewAdapter>();
		for (Set<ReviewAdapter> reviewAdapters : reviews.values()) {
			plainReviews.addAll(reviewAdapters);
		}
		return plainReviews;
	}

	private synchronized long startNewRequest() {
		return epoch.incrementAndGet();
	}

	public boolean isRequestObsolete(long currentEpoch) {
		return epoch.get() > currentEpoch;
	}

	public void rebuildModel(UpdateReason updateReason) {
		long newRequest = startNewRequest();
		notifyReviewListUpdateStarted(new UpdateContext(updateReason, null));
		reviewListModelBuilder.getReviewsFromServer(this, updateReason, newRequest);
	}

	private synchronized void addReview(CrucibleFilter crucibleFilter,
									   ReviewAdapter review, UpdateReason updateReason) {
		ReviewAdapter a = null;

		Collection<ReviewAdapter> localReviews = getReviews();
		if (localReviews.contains(review)) {
			for (ReviewAdapter reviewAdapter : localReviews) {
				if (reviewAdapter.equals(review)) {
					a = reviewAdapter;
					break;
				}
			}
		}

		if (a != null) {
			a.fillReview(review);
			if (!this.reviews.containsKey(crucibleFilter)) {
				this.reviews.put(crucibleFilter, new HashSet<ReviewAdapter>());
			}
			this.reviews.get(crucibleFilter).add(review);
		} else {
			if (!this.reviews.containsKey(crucibleFilter)) {
				this.reviews.put(crucibleFilter, new HashSet<ReviewAdapter>());
			}
			this.reviews.get(crucibleFilter).add(review);
			review.addReviewListener(reviewListener);
			notifyReviewAdded(new UpdateContext(updateReason, review));
		}

	}

	private synchronized void removeReview(ReviewAdapter review, UpdateReason updateReason) {
		for (CrucibleFilter filter : reviews.keySet()) {

			if (reviews.get(filter).contains(review)) {
				review.removeReviewListener(reviewListener);
				reviews.get(filter).remove(review);
				UpdateContext updateContext = new UpdateContext(updateReason, review);
				notifyReviewRemoved(updateContext);
			}
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

	public synchronized void updateReviews(final long executedEpoch,
										   final Map<CrucibleFilter, ReviewNotificationBean> updatedReviews,
										   final UpdateReason updateReason) {
		if (executedEpoch != this.epoch.get()) {
			return;
		}

		for (CrucibleFilter crucibleFilter : updatedReviews.keySet()) {
			Collection<ReviewAdapter> r = updatedReviews.get(crucibleFilter).getReviews();
			if (!this.reviews.containsKey(crucibleFilter)) {
				this.reviews.put(crucibleFilter, new HashSet<ReviewAdapter>());
			}
			for (ReviewAdapter reviewAdapter : r) {
				addReview(crucibleFilter, reviewAdapter, updateReason);
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
			removeReview(r, updateReason);
		}

		// remove categories
		Collection<CrucibleFilter> filters = reviews.keySet();
		for (Iterator<CrucibleFilter> crucibleFilterIterator = filters.iterator(); crucibleFilterIterator.hasNext();) {
			CrucibleFilter crucibleFilter = crucibleFilterIterator.next();
			if (!updatedReviews.containsKey(crucibleFilter)) {
				crucibleFilterIterator.remove();
			}
		}
		notifyReviewListUpdateFinished(new UpdateContext(updateReason, null));
	}

	private void notifyReviewAdded(UpdateContext updateContext) {
		for (CrucibleReviewListModelListener listener : modelListeners) {
			listener.reviewAdded(updateContext);
		}
	}

	private void notifyReviewRemoved(UpdateContext updateContext) {
		for (CrucibleReviewListModelListener listener : modelListeners) {
			listener.reviewRemoved(updateContext);
		}
	}

	private void notifyReviewListUpdateStarted(UpdateContext updateContext) {
		for (CrucibleReviewListModelListener listener : modelListeners) {
			listener.reviewListUpdateStarted(updateContext);
		}
	}

	private void notifyReviewListUpdateFinished(UpdateContext updateContext) {
		for (CrucibleReviewListModelListener listener : modelListeners) {
			listener.reviewListUpdateFinished(updateContext);
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
				listener.reviewChangedWithoutFiles(new UpdateContext(null, newReview));
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
