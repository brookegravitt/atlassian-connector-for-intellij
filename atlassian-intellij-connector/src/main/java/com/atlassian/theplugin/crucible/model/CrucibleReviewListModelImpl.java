package com.atlassian.theplugin.crucible.model;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFilter;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.notification.CrucibleNotification;
import com.atlassian.theplugin.commons.crucible.api.model.notification.NewExceptionNotification;
import com.atlassian.theplugin.commons.crucible.api.model.notification.NewReviewNotification;
import com.atlassian.theplugin.commons.crucible.api.model.notification.NotVisibleReviewNotification;
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
	private final ReviewListModelBuilder reviewListModelBuilder;

	private AtomicLong epoch = new AtomicLong(0);

	public CrucibleReviewListModelImpl(final ReviewListModelBuilder reviewListModelBuilder) {
		this.reviewListModelBuilder = reviewListModelBuilder;
		reviews.put(PredefinedFilter.OpenInIde, new HashSet<ReviewAdapter>());
	}

	public synchronized Collection<ReviewAdapter> getReviews() {
		Set<ReviewAdapter> plainReviews = new HashSet<ReviewAdapter>();

		for (CrucibleFilter crucibleFilter : reviews.keySet()) {
			if (crucibleFilter != PredefinedFilter.OpenInIde) {
				plainReviews.addAll(reviews.get(crucibleFilter));
			}
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
		final List<CrucibleNotification> notifications = Collections.emptyList();
		notifyReviewListUpdateStarted(new UpdateContext(updateReason, null, null));
		final Map<CrucibleFilter, ReviewNotificationBean> newReviews;

		try {
			newReviews = reviewListModelBuilder.getReviewsFromServer(this, updateReason, requestId);

			ApplicationManager.getApplication().invokeLater(new Runnable() {
				public void run() {
					final List<CrucibleNotification> updateNotifications = new ArrayList<CrucibleNotification>();
					try {
						updateNotifications.addAll(updateReviews(requestId, newReviews, updateReason));
					} finally {
						notifyReviewListUpdateFinished(new UpdateContext(updateReason, null, updateNotifications));
					}
				}
			});
		} catch (InterruptedException e) {
			// this exception is just to notify that query was interrupted and
			// new request is performed
		} catch (Throwable t) {
			notifyReviewListUpdateFinished(new UpdateContext(updateReason, null, notifications));
		}
	}

	public synchronized List<CrucibleNotification> addReview(CrucibleFilter crucibleFilter,
			ReviewAdapter review, UpdateReason updateReason) {
		List<CrucibleNotification> notifications = new ArrayList<CrucibleNotification>();
		ReviewAdapter existingReview = null;
		for (CrucibleFilter filter : reviews.keySet()) {
			if (reviews.get(filter).contains(review)) {
				for (ReviewAdapter reviewAdapter : reviews.get(filter)) {
					if (reviewAdapter.equals(review)) {
						existingReview = reviewAdapter;
						break;
					}
				}
			}
		}

		if (existingReview != null) {
			notifications = existingReview.fillReview(review);
			getCollectionForFilter(reviews, crucibleFilter).add(review);
			if (!notifications.isEmpty()) {
				notifyReviewChanged(new UpdateContext(updateReason, review, notifications));
			}
		} else {
			getCollectionForFilter(reviews, crucibleFilter).add(review);
			notifications.add(new NewReviewNotification(review));
			notifyReviewAdded(new UpdateContext(updateReason, review, notifications));
		}
		return notifications;
	}

	private Set<ReviewAdapter> getCollectionForFilter(final Map<CrucibleFilter, Set<ReviewAdapter>> r,
			final CrucibleFilter crucibleFilter) {
		if (!r.containsKey(crucibleFilter)) {
			r.put(crucibleFilter, new HashSet<ReviewAdapter>());
		}
		return r.get(crucibleFilter);
	}

	public synchronized void addReviewToCategory(CrucibleFilter crucibleFilter,
			ReviewAdapter review) {
		reviews.get(crucibleFilter).add(review);
	}

	public synchronized void removeReviewFromCategory(CrucibleFilter crucibleFilter,
			ReviewAdapter review) {
		reviews.get(crucibleFilter).remove(review);
	}

	public synchronized List<CrucibleNotification> removeReview(ReviewAdapter review, UpdateReason updateReason) {
		List<CrucibleNotification> notifications = new ArrayList<CrucibleNotification>();
		for (CrucibleFilter filter : reviews.keySet()) {
			if (reviews.get(filter).contains(review)) {
				reviews.get(filter).remove(review);
				List<CrucibleNotification> singleNotification = new ArrayList<CrucibleNotification>();
				CrucibleNotification event = new NotVisibleReviewNotification(review);
				singleNotification.add(event);
				notifications.add(event);
				UpdateContext updateContext = new UpdateContext(updateReason, review, singleNotification);
				notifyReviewRemoved(updateContext);
			}
		}
		return notifications;
	}

	public synchronized void setSelectedReview(ReviewAdapter review) {
		if (review == null || getReviews().contains(review)) {
			selectedReview = review;
		}
	}

	public Collection<ReviewAdapter> getOpenInIdeReviews() {
		return reviews.get(PredefinedFilter.OpenInIde);
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

	public synchronized List<CrucibleNotification> updateReviews(final long executedEpoch,
			final Map<CrucibleFilter, ReviewNotificationBean> updatedReviews,
			final UpdateReason updateReason) {
		if (executedEpoch != this.epoch.get()) {
			return Collections.emptyList();
		}

		final List<CrucibleNotification> notifications = new ArrayList<CrucibleNotification>();

		Collection<ReviewAdapter> openInIde = null;

		for (CrucibleFilter crucibleFilter : updatedReviews.keySet()) {
			if (crucibleFilter == PredefinedFilter.OpenInIde) {
				openInIde = updatedReviews.get(crucibleFilter).getReviews();
				if (openInIde != null) {
					getCollectionForFilter(reviews, PredefinedFilter.OpenInIde);
					for (ReviewAdapter reviewAdapter : openInIde) {
						notifications.addAll(addReview(PredefinedFilter.OpenInIde, reviewAdapter, updateReason));
					}
				}
			}
		}

		for (CrucibleFilter crucibleFilter : updatedReviews.keySet()) {
			if (crucibleFilter == PredefinedFilter.OpenInIde) {
				continue;
			}

			Collection<ReviewAdapter> updated = updatedReviews.get(crucibleFilter).getReviews();
			if (updated != null) {
				for (ReviewAdapter reviewAdapter : updated) {
					if (openInIde != null && openInIde.contains(reviewAdapter)) {
						addReviewToCategory(crucibleFilter, reviewAdapter);
					} else {
						notifications.addAll(addReview(crucibleFilter, reviewAdapter, updateReason));
					}
				}
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
			notifications.addAll(removeReview(r, updateReason));
		}

		// cleanup categories
		// @todo - make it more effective
		for (CrucibleFilter crucibleFilter : updatedReviews.keySet()) {
			if (crucibleFilter == PredefinedFilter.OpenInIde) {
				continue;
			}

			Collection<ReviewAdapter> updated = updatedReviews.get(crucibleFilter).getReviews();

			final Set<ReviewAdapter> filterReviews = getCollectionForFilter(reviews, crucibleFilter);
			final Set<ReviewAdapter> reviewsForDeleteFromCategory = new HashSet<ReviewAdapter>();
			for (ReviewAdapter reviewAdapter : filterReviews) {
				boolean found = false;
				if (updated != null) {
					for (ReviewAdapter adapter : updated) {
						if (adapter.getPermId().equals(reviewAdapter.getPermId())) {
							found = true;
							break;
						}
					}
				}
				if (!found) {
					reviewsForDeleteFromCategory.add(reviewAdapter);
				}
			}
			reviews.get(crucibleFilter).removeAll(reviewsForDeleteFromCategory);
		}

		// remove categories
		Collection<CrucibleFilter> filters = reviews.keySet();
		for (Iterator<CrucibleFilter> crucibleFilterIterator = filters.iterator();
			 crucibleFilterIterator.hasNext();) {
			CrucibleFilter crucibleFilter = crucibleFilterIterator.next();
			if (crucibleFilter != PredefinedFilter.OpenInIde && !updatedReviews.containsKey(crucibleFilter)) {
				crucibleFilterIterator.remove();
			}
		}

		for (ReviewNotificationBean bean : updatedReviews.values()) {
			if (bean.getException() != null) {
				notifications.add(new NewExceptionNotification(bean.getException(), bean.getServer()));
				notifyReviewListUpdateError(new UpdateContext(updateReason, null, notifications), bean.getException());
			}
		}

		return notifications;
	}

	private void notifyReviewAdded(UpdateContext updateContext) {
		for (CrucibleReviewListModelListener listener : modelListeners) {
			listener.reviewAdded(updateContext);
		}
	}

	private void notifyReviewChanged(UpdateContext updateContext) {
		for (CrucibleReviewListModelListener listener : modelListeners) {
			listener.reviewChanged(updateContext);
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

	private void notifyReviewListUpdateError(final UpdateContext updateContext, final Exception exception) {
		for (CrucibleReviewListModelListener listener : modelListeners) {
			listener.reviewListUpdateError(updateContext, exception);
		}
	}
}
