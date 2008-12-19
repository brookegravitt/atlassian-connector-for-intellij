package com.atlassian.theplugin.crucible.model;

import com.atlassian.theplugin.commons.crucible.CrucibleReviewListener;
import com.atlassian.theplugin.commons.crucible.CrucibleReviewListenerAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFilter;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.idea.crucible.ReviewNotificationBean;
import com.intellij.openapi.application.ApplicationManager;

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

	public CrucibleReviewListModelImpl(final ReviewListModelBuilder reviewListModelBuilder) {
		this.reviewListModelBuilder = reviewListModelBuilder;
	}

	public synchronized Collection<ReviewAdapter> getReviews() {
		Set<ReviewAdapter> plainReviews = new HashSet<ReviewAdapter>();
		for (Set<ReviewAdapter> reviewAdapters : reviews.values()) {
			plainReviews.addAll(reviewAdapters);
		}
		return plainReviews;
	}

	public int getReviewCount(CrucibleFilter filter) {
		if (reviews.containsKey(filter)) {
			return reviews.get(filter).size();
		}
		return -1;
	}

	public int getPredefinedFiltersReviewCount() {
		Set<ReviewAdapter> combined = new HashSet<ReviewAdapter>();
		if (reviews.keySet().size() == 0) {
			return -1;
		}
		for (CrucibleFilter crucibleFilter : reviews.keySet()) {
			if (crucibleFilter instanceof PredefinedFilter) {
				combined.addAll(reviews.get(crucibleFilter));
			}
		}
		return combined.size();
	}

	private synchronized long startNewRequest() {
		return epoch.incrementAndGet();
	}

	public boolean isRequestObsolete(long currentEpoch) {
		return epoch.get() > currentEpoch;
	}

	public void rebuildModel(final UpdateReason updateReason) {
		final long requestId = startNewRequest();
		notifyReviewListUpdateStarted(new UpdateContext(updateReason, null));
		final Map<CrucibleFilter, ReviewNotificationBean> newReviews;
		try {
			newReviews = reviewListModelBuilder.getReviewsFromServer(this, updateReason, requestId);
			ApplicationManager.getApplication().invokeLater(new Runnable() {
				public void run() {
					updateReviews(requestId, newReviews, updateReason);
				}
			});
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public synchronized void addReview(CrucibleFilter crucibleFilter,
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

	public synchronized void removeReview(ReviewAdapter review, UpdateReason updateReason) {
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

		try {
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
		} finally {
			notifyReviewListUpdateFinished(new UpdateContext(updateReason, null));
		}
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
