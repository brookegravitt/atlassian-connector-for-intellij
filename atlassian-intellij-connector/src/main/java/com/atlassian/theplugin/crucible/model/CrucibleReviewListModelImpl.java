package com.atlassian.theplugin.crucible.model;

import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFilter;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.crucible.api.model.RecentlyOpenReviewsFilter;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.notification.CrucibleNotification;
import com.atlassian.theplugin.commons.crucible.api.model.notification.NewExceptionNotification;
import com.atlassian.theplugin.commons.crucible.api.model.notification.NewReviewNotification;
import com.atlassian.theplugin.commons.crucible.api.model.notification.NotVisibleReviewNotification;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.configuration.CrucibleWorkspaceConfiguration;
import com.atlassian.theplugin.configuration.WorkspaceConfigurationBean;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;
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
    //	private ReviewAdapter selectedReview;
    private final ReviewListModelBuilder reviewListModelBuilder;
    private ProjectConfiguration projectConfiguration;
    private CrucibleWorkspaceConfiguration crucibleProjectConfiguration;
    //private LocalConfigurationListenerAdapater configurationListenerAdapater = new LocalConfigurationListenerAdapater();

    private AtomicLong epoch = new AtomicLong(0);

    public CrucibleReviewListModelImpl(final ReviewListModelBuilder reviewListModelBuilder,
                                       final WorkspaceConfigurationBean projectConfigurationBean, final ProjectCfgManagerImpl projectCfgManager) {

        this.reviewListModelBuilder = reviewListModelBuilder;
        this.projectConfiguration = projectCfgManager != null ? projectCfgManager.getProjectConfiguration() : null;
        this.crucibleProjectConfiguration = projectConfigurationBean.getCrucibleConfiguration();
        reviews.put(PredefinedFilter.OpenInIde, new HashSet<ReviewAdapter>());


    }

    public Collection<ReviewAdapter> getReviews() {
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
            if (crucibleFilter instanceof PredefinedFilter && crucibleFilter != PredefinedFilter.OpenInIde) {
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
        } catch (Exception e) {
            NewExceptionNotification nen = new NewExceptionNotification(e, null);
            notifications.add(nen);
            notifyReviewListUpdateFinished(new UpdateContext(updateReason, null, notifications));
            // comment from wseliga:
            // todo this is somewhat crazy. We swallow here all the problems and stop building the model, which SUCKS!!!
        }
    }

    public List<CrucibleNotification> addReview(CrucibleFilter crucibleFilter,
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


    /**
     * Add review to the model and fires start/finish update model notifications.
     * For params description see {@link #addReview(com.atlassian.theplugin.commons.crucible.api.model.CrucibleFilter,
     * com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter, UpdateReason)}
     *
     * @param reviewAdapter
     * @param updateReason
     */
    public void openReview(final ReviewAdapter reviewAdapter, final UpdateReason updateReason) {

        // start notifiaction;
        notifyReviewListUpdateStarted(new UpdateContext(updateReason, null, null));

        // todo remove that limitation when we handle multiple open reviews in IDE
        // clear OpenInIde filter and notify if review removed
        clearOpenInIde();

        List<CrucibleNotification> notifications = addReview(PredefinedFilter.OpenInIde, reviewAdapter, updateReason);

        final RecentlyOpenReviewsFilter recentlyOpenFilter =
                crucibleProjectConfiguration.getCrucibleFilters().getRecenltyOpenFilter();

        if (recentlyOpenFilter != null && recentlyOpenFilter.isEnabled()) {
            notifications.addAll(addReview(recentlyOpenFilter, reviewAdapter, updateReason));
        }

        // finish notification;
        notifyReviewListUpdateFinished(new UpdateContext(updateReason, null, notifications));

    }

    public void clearOpenInIde(UpdateReason updateReason) {
        // start notifiaction
        notifyReviewListUpdateStarted(new UpdateContext(updateReason, null, null));

        clearOpenInIde();

        // finish notification
        notifyReviewListUpdateFinished(new UpdateContext(updateReason, null, null));
    }

    private void clearOpenInIde() {
        HashSet<ReviewAdapter> openInIde = new HashSet<ReviewAdapter>(getOpenInIdeReviews());

        // check if review from OpenInIde filter exists in any other filter
        for (ReviewAdapter r : openInIde) {
            boolean found = false;
            for (CrucibleFilter filter : reviews.keySet()) {
                if (filter != PredefinedFilter.OpenInIde) {
                    if (reviews.get(filter).contains(r)) {
                        found = true;
                        break;
                    }
                }
            }

            r.clearContentCache();
            getOpenInIdeReviews().remove(r);

            if (!found) {
                notifyReviewRemoved(new UpdateContext(UpdateReason.OPEN_IN_IDE, r, null));
            }
        }
    }

    private Set<ReviewAdapter> getCollectionForFilter(final Map<CrucibleFilter, Set<ReviewAdapter>> r,
                                                      final CrucibleFilter crucibleFilter) {
        if (!r.containsKey(crucibleFilter)) {
            r.put(crucibleFilter, new HashSet<ReviewAdapter>());
        }
        return r.get(crucibleFilter);
    }

    private void addReviewToCategory(CrucibleFilter crucibleFilter, ReviewAdapter review) {
        if (!reviews.containsKey(crucibleFilter)) {
            reviews.put(crucibleFilter, new HashSet<ReviewAdapter>());
        }
        reviews.get(crucibleFilter).add(review);
    }

    private void removeReviewFromCategory(CrucibleFilter crucibleFilter,
                                          ReviewAdapter review) {
        reviews.get(crucibleFilter).remove(review);
    }

    public List<CrucibleNotification> removeReview(ReviewAdapter review, UpdateReason updateReason) {
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

    public Collection<ReviewAdapter> getOpenInIdeReviews() {
        return reviews.get(PredefinedFilter.OpenInIde);
    }

    public void addListener(CrucibleReviewListModelListener listener) {
        if (!modelListeners.contains(listener)) {
            modelListeners.add(listener);
        }
    }

    public void removeListener(CrucibleReviewListModelListener listener) {
        modelListeners.remove(listener);
    }

    public List<CrucibleNotification> updateReviews(final long executedEpoch,
                                                    final Map<CrucibleFilter, ReviewNotificationBean> updatedReviews,
                                                    final UpdateReason updateReason) {
        if (executedEpoch != this.epoch.get()) {
            return Collections.emptyList();
        }

        final List<CrucibleNotification> notifications = new ArrayList<CrucibleNotification>();

        Collection<ReviewAdapter> openInIde = new ArrayList<ReviewAdapter>();

        for (CrucibleFilter crucibleFilter : updatedReviews.keySet()) {
            if (crucibleFilter == PredefinedFilter.OpenInIde) {

                openInIde = updatedReviews.get(crucibleFilter).getReviews();

                if (updatedReviews.get(crucibleFilter).getExceptionServers().size() > 0) {
                    openInIde.addAll(
                            getExistingReviewsFromServers(
                                    crucibleFilter, updatedReviews.get(crucibleFilter).getExceptionServers()));
                }

                if (openInIde != null && openInIde.size() > 0) {
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
            if (updatedReviews.get(crucibleFilter).getExceptionServers().size() > 0) {
                updated.addAll(
                        getExistingReviewsFromServers(
                                crucibleFilter, updatedReviews.get(crucibleFilter).getExceptionServers()));
            }


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
            reviewSet.addAll(
                    getExistingReviewsFromServers(
                            crucibleFilter, updatedReviews.get(crucibleFilter).getExceptionServers()));
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
            if (updatedReviews.get(crucibleFilter).getExceptionServers().size() > 0) {
                updated.addAll(
                        getExistingReviewsFromServers(
                                crucibleFilter, updatedReviews.get(crucibleFilter).getExceptionServers()));
            }


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
            for (ServerData server : bean.getExceptionServers()) {
                notifications.add(new NewExceptionNotification(bean.getException(server), server));
                notifyReviewListUpdateError(
                        new UpdateContext(updateReason, null, notifications), bean.getException(server));
            }
        }

        return notifications;
    }

    private Collection<ReviewAdapter> getExistingReviewsFromServers(CrucibleFilter crucibleFilter, Collection<ServerData> servers) {
        Collection<ReviewAdapter> reviewList = new ArrayList<ReviewAdapter>();

        if (reviews != null && reviews.get(crucibleFilter) != null) {
            for (ReviewAdapter reviewAdapter : reviews.get(crucibleFilter)) {
                for (ServerData s : servers) {
                    ServerCfg serverCfg =
                            projectConfiguration != null
                                    ? projectConfiguration.getServerCfg(new ServerId(s.getServerId())) : null;
                    //server disabled or communication problem
                    if ((serverCfg != null && !serverCfg.isEnabled())
                            || s.getServerId().equals(reviewAdapter.getServerData().getServerId())) {
                        reviewList.add(reviewAdapter);
                    }
                }
            }
        }

        return reviewList;
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

//    private class LocalConfigurationListenerAdapater extends ConfigurationListenerAdapter {
//        @Override
//        public void serverConnectionDataChanged(ServerId serverId) {
//            conditionalModelRefresh(serverId);
//        }
//
//        @Override
//        public void serverDataChanged(ServerId serverId) {
//           conditionalModelRefresh(serverId);
//        }
//
//        private void conditionalModelRefresh(ServerId serverId) {
//            if (reviews != null) {
//                for (ReviewAdapter reviewAdapter : getReviews()) {
//                    if (reviewAdapter.getServerData().getServerId().toString().equals(serverId)) {
//                        CrucibleReviewListModelImpl.this.rebuildModel(UpdateReason.REFRESH);
//                        break;
//                    }
//                }
//            }
//        }
//
//
//    }
}


