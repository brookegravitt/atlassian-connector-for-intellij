package com.atlassian.theplugin.crucible.model;

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.AbstractCfgManager;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginFailedException;
import com.atlassian.theplugin.idea.crucible.ReviewNotificationBean;
import com.atlassian.theplugin.remoteapi.MissingPasswordHandler;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;

import java.util.*;

public class CrucibleQueryExecutor {
	private final CrucibleServerFacade crucibleServerFacade;
	private final CfgManager cfgManager;
	private final Project project;
	private final MissingPasswordHandler missingPasswordHandler;
	private final CrucibleReviewListModel crucibleReviewListModel;

	public CrucibleQueryExecutor(final CrucibleServerFacade crucibleServerFacade,
			final CfgManager cfgManager,
			final Project project,
			final MissingPasswordHandler missingPasswordHandler,
			final CrucibleReviewListModel crucibleReviewListModel) {
		this.crucibleServerFacade = crucibleServerFacade;
		this.cfgManager = cfgManager;
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
		final Map<CrucibleFilter, ReviewNotificationBean> reviews
				= new HashMap<CrucibleFilter, ReviewNotificationBean>();

		for (final CrucibleServerCfg server : cfgManager.getAllCrucibleServers(CfgUtil.getProjectId(project))) {
			if (server.isEnabled()) {

				// retrieve reviews for predefined filters
				for (int i = 0;
					 i < predefinedFilters.length
							 && i < PredefinedFilter.values().length; i++) {

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

								List<Review> review = crucibleServerFacade.getReviewsForFilter(cfgManager.getServerData(server), filter);
								List<ReviewAdapter> reviewData = new ArrayList<ReviewAdapter>(review.size());
								for (Review r : review) {
									final ReviewAdapter reviewAdapter = new ReviewAdapter(r, cfgManager.getServerData(server));
									reviewData.add(reviewAdapter);
								}

								predefinedFiterNotificationBean.getReviews().addAll(reviewData);

							} catch (ServerPasswordNotProvidedException exception) {
								ApplicationManager.getApplication().invokeLater(missingPasswordHandler,
										ModalityState.defaultModalityState());
								predefinedFiterNotificationBean.setException(exception);
								predefinedFiterNotificationBean.setServer(cfgManager.getServerData(server));
								break;
							} catch (RemoteApiLoginFailedException exception) {
								ApplicationManager.getApplication().invokeLater(missingPasswordHandler,
										ModalityState.defaultModalityState());
								predefinedFiterNotificationBean.setException(exception);
								predefinedFiterNotificationBean.setServer(cfgManager.getServerData(server));
								break;
							} catch (RemoteApiException e) {
								PluginUtil.getLogger().info("Error getting Crucible reviews for " + server.getName()
										+ " server", e);
								predefinedFiterNotificationBean.setException(e);
								predefinedFiterNotificationBean.setServer(cfgManager.getServerData(server));
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
		}
		return reviews;
	}

	private void retriveRecenltyOpenFilterReviews(final RecentlyOpenReviewsFilter recentlyOpenFilter,
			final Map<CrucibleFilter, ReviewNotificationBean> reviews, final CrucibleServerCfg server) {

		if (recentlyOpenFilter != null && recentlyOpenFilter.isEnabled()) {

			// create notification bean for the filter if not exist
			if (!reviews.containsKey(recentlyOpenFilter)) {
				List<ReviewAdapter> list = new ArrayList<ReviewAdapter>();
				ReviewNotificationBean bean = new ReviewNotificationBean();
				bean.setReviews(list);
				reviews.put(recentlyOpenFilter, bean);
			}

			ReviewNotificationBean recenltyOpenFilterNotificationBean = reviews.get(recentlyOpenFilter);

			for (ReviewRecentlyOpenBean recentReview : recentlyOpenFilter.getRecentlyOpenReviews()) {

				if (server.getServerId().toString().equals(recentReview.getServerId())) {

					// get review from the server
					try {
						PluginUtil.getLogger().debug(
								"Crucible: updating status for server: " + server.getUrl()
										+ ", recenlty viewed reviews filter");

						Review r = crucibleServerFacade.getReview(cfgManager.getServerData(server),
								new PermIdBean(recentReview.getReviewId()));
						recenltyOpenFilterNotificationBean.getReviews().add(new ReviewAdapter(r, cfgManager.getServerData(server)));

					} catch (ServerPasswordNotProvidedException exception) {
						ApplicationManager.getApplication().invokeLater(
								new MissingPasswordHandler(crucibleServerFacade, cfgManager, project),
								ModalityState.defaultModalityState());
						recenltyOpenFilterNotificationBean.setException(exception);
						recenltyOpenFilterNotificationBean.setServer(cfgManager.getServerData(server));
					} catch (RemoteApiException e) {
						PluginUtil.getLogger().info("Error getting Crucible review for " + server.getName()
								+ " server", e);
						recenltyOpenFilterNotificationBean.setException(e);
						recenltyOpenFilterNotificationBean.setServer(cfgManager.getServerData(server));
					}
				}
			}
		}

	}

	private void retriveManualFilterReviews(final CustomFilter manualFilter,
			final Map<CrucibleFilter, ReviewNotificationBean> reviews, final CrucibleServerCfg server) {
		if (manualFilter != null && manualFilter.isEnabled()) {

			// create notification bean for the filter if not exist
			if (!reviews.containsKey(manualFilter)) {
				List<ReviewAdapter> list = new ArrayList<ReviewAdapter>();
				ReviewNotificationBean bean = new ReviewNotificationBean();
				bean.setReviews(list);
				reviews.put(manualFilter, bean);
			}

			ReviewNotificationBean customFilterNotificationBean = reviews.get(manualFilter);

			if (server.getServerId().toString().equals(manualFilter.getServerUid())) {

				// get reviews for filter from the server
				try {
					PluginUtil.getLogger().debug("Crucible: updating status for server: "
							+ server.getUrl() + ", custom filter");
					List<Review> customFilter
							= crucibleServerFacade.getReviewsForCustomFilter(cfgManager.getServerData(server),
							manualFilter);

					List<ReviewAdapter> reviewData = new ArrayList<ReviewAdapter>(customFilter.size());
					for (Review r : customFilter) {
						final ReviewAdapter reviewAdapter = new ReviewAdapter(r, cfgManager.getServerData(server));
						reviewData.add(reviewAdapter);
					}

					customFilterNotificationBean.getReviews().addAll(reviewData);

				} catch (ServerPasswordNotProvidedException exception) {
					ApplicationManager.getApplication().invokeLater(
							new MissingPasswordHandler(crucibleServerFacade, cfgManager, project),
							ModalityState.defaultModalityState());
					customFilterNotificationBean.setException(exception);
					customFilterNotificationBean.setServer(cfgManager.getServerData(server));
				} catch (RemoteApiException e) {
					PluginUtil.getLogger().info("Error getting Crucible reviews for " + server.getName()
							+ " server", e);
					customFilterNotificationBean.setException(e);
					customFilterNotificationBean.setServer(cfgManager.getServerData(server));
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
					&& cfgManager.getServer(CfgUtil.getProjectId(project), review.getServerData()).isEnabled()) {

				try {
					PluginUtil.getLogger().debug("Crucible: updating status for server: "
							+ review.getServerData().getUrl() + ", review: " + review.getPermId().getId());

					if (crucibleReviewListModel.isRequestObsolete(epoch)) {
						throw new InterruptedException();
					}

					Review r = crucibleServerFacade.getReview(review.getServerData(),
							review.getPermId());
					reviewNotificationBean.getReviews().add(new ReviewAdapter(r, review.getServerData()));
				} catch (ServerPasswordNotProvidedException exception) {
					ApplicationManager.getApplication().invokeLater(missingPasswordHandler,
							ModalityState.defaultModalityState());
					reviewNotificationBean.setException(exception);
					reviewNotificationBean.setServer(review.getServerData());
				} catch (RemoteApiLoginFailedException exception) {
					ApplicationManager.getApplication().invokeLater(missingPasswordHandler,
							ModalityState.defaultModalityState());
					reviewNotificationBean.setException(exception);
					reviewNotificationBean.setServer(review.getServerData());
				} catch (RemoteApiException e) {
					PluginUtil.getLogger().info("Error getting Crucible reviews for " + review.getServerData().getName()
							+ " server", e);
					reviewNotificationBean.setException(e);
					reviewNotificationBean.setServer(review.getServerData());
				}
			}
		}

		return reviewNotificationBean;
	}
}
