package com.atlassian.theplugin.crucible.model;

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
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
	private final long epoch;

	public CrucibleQueryExecutor(final CrucibleServerFacade crucibleServerFacade,
								 final CfgManager cfgManager,
								 final Project project,
								 final MissingPasswordHandler missingPasswordHandler,
								 final CrucibleReviewListModel crucibleReviewListModel,
								 final long epoch) {
		this.crucibleServerFacade = crucibleServerFacade;
		this.cfgManager = cfgManager;
		this.project = project;
		this.missingPasswordHandler = missingPasswordHandler;
		this.crucibleReviewListModel = crucibleReviewListModel;
		this.epoch = epoch;
	}

	public Map<CrucibleFilter, ReviewNotificationBean> runQuery(
			final Boolean[] predefinedFilters, final CustomFilter manualFilter) throws InterruptedException {
		// collect review info from each server and each required filter
		final Map<CrucibleFilter, ReviewNotificationBean> reviews
				= new HashMap<CrucibleFilter, ReviewNotificationBean>();

		for (final CrucibleServerCfg server : retrieveAllCrucibleServers()) {

			final List<ReviewAdapter> allServerReviews = new ArrayList<ReviewAdapter>();
			boolean communicationFailed = false;

			if (server.isEnabled()) {

				// retrieve reviews for predefined filters
				for (int i = 0;
					 i < predefinedFilters.length
							 && i < PredefinedFilter.values().length; i++) {

					// if predefined filter is enabled
					if (predefinedFilters[i]) {
						PredefinedFilter filter = PredefinedFilter.values()[i];

						// create notification bean for the filter if not exist
						if (!reviews.containsKey(filter)) {
							ReviewNotificationBean predefinedFiterNofificationbean = new ReviewNotificationBean();
							List<ReviewAdapter> list = new ArrayList<ReviewAdapter>();
							predefinedFiterNofificationbean.setReviews(list);
							reviews.put(filter, predefinedFiterNofificationbean);
						}

						ReviewNotificationBean predefinedFiterNofificationbean = reviews.get(filter);

						// get reviews for filter from the server
						try {
							PluginUtil.getLogger().debug("Crucible: updating status for server: "
									+ server.getUrl() + ", filter type: " + filter);

							if (crucibleReviewListModel.getCurrentEpoch() != epoch) {
								throw new InterruptedException();
							}

							List<Review> review = crucibleServerFacade.getReviewsForFilter(server, filter);
							List<ReviewAdapter> reviewData = new ArrayList<ReviewAdapter>(review.size());
							for (Review r : review) {
								final ReviewAdapter reviewAdapter = new ReviewAdapter(r, server);
								reviewData.add(reviewAdapter);
								allServerReviews.add(reviewAdapter);
							}

							predefinedFiterNofificationbean.getReviews().addAll(reviewData);

						} catch (ServerPasswordNotProvidedException exception) {
							ApplicationManager.getApplication().invokeLater(missingPasswordHandler,
									ModalityState.defaultModalityState());
							predefinedFiterNofificationbean.setException(exception);
							communicationFailed = true;
							break;
						} catch (RemoteApiLoginFailedException exception) {
							ApplicationManager.getApplication().invokeLater(missingPasswordHandler,
									ModalityState.defaultModalityState());
							predefinedFiterNofificationbean.setException(exception);
							communicationFailed = true;
							break;
						} catch (RemoteApiException e) {
							PluginUtil.getLogger().info("Error getting Crucible reviews for " + server.getName()
									+ " server", e);
							predefinedFiterNofificationbean.setException(e);
							communicationFailed = true;
							break;
						}
					}
				}

				// retrieve reviews for custom filter
				if (manualFilter != null && manualFilter.isEnabled()) {

					// create notification bean for the filter if not exist
					if (!reviews.containsKey(manualFilter.getFilterName())) {
						List<ReviewAdapter> list = new ArrayList<ReviewAdapter>();
						ReviewNotificationBean bean = new ReviewNotificationBean();
						bean.setReviews(list);
						reviews.put(manualFilter, bean);
					}

					ReviewNotificationBean customFilterNotificationBean = reviews.get(manualFilter.getFilterName());

					if (server.getServerId().toString().equals(manualFilter.getServerUid())) {

						// get reviews for filter from the server
						try {
							PluginUtil.getLogger().debug("Crucible: updating status for server: "
									+ server.getUrl() + ", custom filter");
							List<Review> customFilter
									= crucibleServerFacade.getReviewsForCustomFilter(server, manualFilter);


							List<ReviewAdapter> reviewData = new ArrayList<ReviewAdapter>(customFilter.size());
							for (Review r : customFilter) {
								final ReviewAdapter reviewAdapter = new ReviewAdapter(r, server);
								reviewData.add(reviewAdapter);
								allServerReviews.add(reviewAdapter);
							}

							customFilterNotificationBean.getReviews().addAll(reviewData);

						} catch (ServerPasswordNotProvidedException exception) {
							ApplicationManager.getApplication().invokeLater(
									new MissingPasswordHandler(crucibleServerFacade, cfgManager, project),
									ModalityState.defaultModalityState());
							customFilterNotificationBean.setException(exception);
							communicationFailed = true;
						} catch (RemoteApiException e) {
							PluginUtil.getLogger().info("Error getting Crucible reviews for " + server.getName()
									+ " server", e);
							customFilterNotificationBean.setException(e);
							communicationFailed = true;
						}
					}
				}
			}
		}
		return reviews;
	}

	private Collection<CrucibleServerCfg> retrieveAllCrucibleServers() {
		return cfgManager.getAllCrucibleServers(CfgUtil.getProjectId(project));
	}

}
