package com.atlassian.theplugin.crucible.model;

import com.atlassian.connector.intellij.crucible.CrucibleServerFacade;
import com.atlassian.connector.intellij.crucible.RecentlyOpenReviewsFilter;
import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginFailedException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.config.MissingPasswordHandler;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;
import com.atlassian.theplugin.idea.crucible.ReviewNotificationBean;
import com.atlassian.theplugin.remoteapi.MissingPasswordHandlerQueue;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.project.Project;

import java.util.*;

public class CrucibleQueryExecutor {
	private final CrucibleServerFacade crucibleServerFacade;
	private final ProjectCfgManagerImpl projectCfgManager;
	private final Project project;
	private final MissingPasswordHandler missingPasswordHandler;
	private final CrucibleReviewListModel crucibleReviewListModel;

	public CrucibleQueryExecutor(final CrucibleServerFacade crucibleServerFacade,
			final ProjectCfgManagerImpl projectCfgManager,
			final Project project,
			final MissingPasswordHandler missingPasswordHandler,
			final CrucibleReviewListModel crucibleReviewListModel) {
		this.crucibleServerFacade = crucibleServerFacade;
		this.projectCfgManager = projectCfgManager;
		this.project = project;
		this.missingPasswordHandler = missingPasswordHandler;
		this.crucibleReviewListModel = crucibleReviewListModel;
	}

	public Map<CrucibleFilter, ReviewNotificationBean> runQuery(
			final Boolean[] predefinedFilters,
			final CustomFilter manualFilter,
			final RecentlyOpenReviewsFilter recentlyOpenFilter,
			final long epoch) throws InterruptedException {

		// collect review info from each server and each required filter
		final Map<CrucibleFilter, ReviewNotificationBean> reviews = new HashMap<CrucibleFilter, ReviewNotificationBean>();

		for (final ServerData server : projectCfgManager.getAllEnabledCrucibleServerss()) {

			// retrieve reviews for predefined filters
			for (int i = 0;
				 i < predefinedFilters.length && i < PredefinedFilter.values().length; i++) {

				// if predefined filter is enabled
				if (predefinedFilters[i]) {
					PredefinedFilter filter = PredefinedFilter.values()[i];
					if (filter.isRemote()) {

						// create notification bean for the filter if not exist
						if (!reviews.containsKey(filter)) {
							ReviewNotificationBean predefinedFiterNofificationbean = new ReviewNotificationBean();
							List<ReviewAdapter> list = new ArrayList<ReviewAdapter>();
							predefinedFiterNofificationbean.setReviews(list);
							reviews.put(filter, predefinedFiterNofificationbean);
						}

						ReviewNotificationBean predefinedFiterNotificationBean = reviews.get(filter);

						// get reviews for filter from the server
						try {
							PluginUtil.getLogger().debug("Crucible: updating status for server: "
									+ server.getUrl() + ", filter type: " + filter);

							if (crucibleReviewListModel.isRequestObsolete(epoch)) {
								throw new InterruptedException();
							}

							final List<ReviewAdapter> reviewData = crucibleServerFacade.getReviewsForFilter(server, filter);
							predefinedFiterNotificationBean.getReviews().addAll(reviewData);

						} catch (ServerPasswordNotProvidedException exception) {
							MissingPasswordHandlerQueue.addHandler(missingPasswordHandler);
							predefinedFiterNotificationBean.addException(server, exception);
							break;
						} catch (RemoteApiLoginFailedException exception) {
							MissingPasswordHandlerQueue.addHandler(missingPasswordHandler);
							predefinedFiterNotificationBean.addException(server, exception);
							break;
						} catch (RemoteApiException e) {
							PluginUtil.getLogger().info("Error getting Crucible reviews for " + server.getName()
									+ " server", e);
							predefinedFiterNotificationBean.addException(server, e);
							break;
						}
					}
				}
			}

			// retrieve reviews for custom filter
			retriveManualFilterReviews(manualFilter, reviews, server);

			// retrieve reviews for recently open filter
			retriveRecenltyOpenFilterReviews(recentlyOpenFilter, reviews, server);

		}
		return reviews;
	}

	private void retriveRecenltyOpenFilterReviews(final RecentlyOpenReviewsFilter recentlyOpenFilter,
			final Map<CrucibleFilter, ReviewNotificationBean> reviews, final ServerData server) {

		if (recentlyOpenFilter != null && recentlyOpenFilter.isEnabled()) {

			// create notification bean for the filter if not exist
			if (!reviews.containsKey(recentlyOpenFilter)) {
				List<ReviewAdapter> list = new ArrayList<ReviewAdapter>();
				ReviewNotificationBean bean = new ReviewNotificationBean();
				bean.setReviews(list);
				reviews.put(recentlyOpenFilter, bean);
			}

			ReviewNotificationBean recenltyOpenFilterNotificationBean = reviews.get(recentlyOpenFilter);

			for (ReviewRecentlyOpenBean recentReview : recentlyOpenFilter.getRecentlyOpenReviewss()) {

				if (server.getServerId().equals(recentReview.getServerId())) {

					// get review from the server
					try {
						PluginUtil.getLogger().debug(
								"Crucible: updating status for server: " + server.getUrl()
										+ ", recenlty viewed reviews filter");

						ReviewAdapter r = crucibleServerFacade.getReview(server, new PermId(recentReview.getReviewId()));
						recenltyOpenFilterNotificationBean.getReviews().add(r);

					} catch (ServerPasswordNotProvidedException exception) {
						MissingPasswordHandlerQueue.addHandler(
								new MissingPasswordHandler(crucibleServerFacade, projectCfgManager, project));
						recenltyOpenFilterNotificationBean.addException(server, exception);
					} catch (RemoteApiException e) {
						PluginUtil.getLogger().info("Error getting Crucible review for " + server.getName()
								+ " server", e);
						recenltyOpenFilterNotificationBean.addException(server, e);
					}
				}
			}
		}

	}

	private void retriveManualFilterReviews(final CustomFilter manualFilter,
			final Map<CrucibleFilter, ReviewNotificationBean> reviews, final ServerData server) {
		if (manualFilter != null && manualFilter.isEnabled()) {

			// create notification bean for the filter if not exist
			if (!reviews.containsKey(manualFilter)) {
				List<ReviewAdapter> list = new ArrayList<ReviewAdapter>();
				ReviewNotificationBean bean = new ReviewNotificationBean();
				bean.setReviews(list);
				reviews.put(manualFilter, bean);
			}

			ReviewNotificationBean customFilterNotificationBean = reviews.get(manualFilter);

			if (server.getServerId().equals(manualFilter.getServerId())) {

				// get reviews for filter from the server
				try {
					PluginUtil.getLogger().debug("Crucible: updating status for server: "
							+ server.getUrl() + ", custom filter");
					final List<ReviewAdapter> reviewData = crucibleServerFacade.getReviewsForCustomFilter(server, manualFilter);

					customFilterNotificationBean.getReviews().addAll(reviewData);

				} catch (ServerPasswordNotProvidedException exception) {
					MissingPasswordHandlerQueue.addHandler(
							new MissingPasswordHandler(crucibleServerFacade, projectCfgManager, project));
					customFilterNotificationBean.addException(server, exception);
				} catch (RemoteApiException e) {
					PluginUtil.getLogger().info("Error getting Crucible reviews for " + server.getName()
							+ " server", e);
					customFilterNotificationBean.addException(server, e);
				}
			}
		}
	}

	public ReviewNotificationBean runDetailedReviewsQuery(
			final Collection<ReviewAdapter> reviews, final long epoch) throws InterruptedException {
		ReviewNotificationBean reviewNotificationBean = new ReviewNotificationBean();
		List<ReviewAdapter> outReviews = new ArrayList<ReviewAdapter>();
		reviewNotificationBean.setReviews(outReviews);

		for (ReviewAdapter review : reviews) {
			if (review != null && review.getServerData() != null
					&& review.getServerData().isEnabled()) {

				try {
					PluginUtil.getLogger().debug("Crucible: updating status for server: "
							+ review.getServerData().getUrl() + ", review: " + review.getPermId().getId());

					if (crucibleReviewListModel.isRequestObsolete(epoch)) {
						throw new InterruptedException();
					}

					ReviewAdapter r = crucibleServerFacade.getReview(review.getServerData(), review.getPermId());
					reviewNotificationBean.getReviews().add(r);
				} catch (ServerPasswordNotProvidedException exception) {
					MissingPasswordHandlerQueue.addHandler(missingPasswordHandler);
					reviewNotificationBean.addException(review.getServerData(), exception);
				} catch (RemoteApiLoginFailedException exception) {
					MissingPasswordHandlerQueue.addHandler(missingPasswordHandler);
					reviewNotificationBean.addException(review.getServerData(), exception);
				} catch (RemoteApiException e) {
					PluginUtil.getLogger().info("Error getting Crucible reviews for " + review.getServerData().getName()
							+ " server", e);
					reviewNotificationBean.addException(review.getServerData(), e);
				}
			}
		}

		return reviewNotificationBean;
	}
}
