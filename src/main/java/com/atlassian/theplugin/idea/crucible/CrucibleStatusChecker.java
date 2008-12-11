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

package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.SchedulableChecker;
import com.atlassian.theplugin.commons.StatusListener;
import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.configuration.CrucibleConfigurationBean;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.CrucibleVersion;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilterBean;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginFailedException;
import com.atlassian.theplugin.commons.util.DateUtil;
import com.atlassian.theplugin.configuration.CrucibleProjectConfiguration;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModel;
import com.atlassian.theplugin.remoteapi.MissingPasswordHandler;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;


/**
 * IDEA-specific class that uses
 * {@link com.atlassian.theplugin.commons.crucible.CrucibleServerFacade} to retrieve builds info and
 * passes raw data to configured {@link CrucibleStatusListener}s.<p>
 * <p/>
 * Intended to be triggered by a {@link java.util.Timer} through the {@link #newTimerTask()}.<p>
 * <p/>
 * Thread safe.
 */
public final class CrucibleStatusChecker implements SchedulableChecker {
	private final List<CrucibleStatusListener> listenerList = new ArrayList<CrucibleStatusListener>();
	private final CrucibleServerFacade crucibleServerFacade;
	private static Date lastActionRun = new Date();
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("H:mm:ss:SSS");
	private static StringBuffer sb = new StringBuffer();

	private CrucibleVersion crucibleVersion = CrucibleVersion.UNKNOWN;
	private static final String NAME = "Atlassian Crucible checker";
	private final CfgManager cfgManager;
	private final Project project;
	private final CrucibleConfigurationBean crucibleConfigurationBean;
	private final CrucibleProjectConfiguration crucibleProjectConfiguration;
	private final MissingPasswordHandler missingPasswordHandler;
	private final CrucibleReviewListModel reviewListModel;


	public CrucibleStatusChecker(CfgManager cfgManager, Project project,
								 CrucibleConfigurationBean crucibleConfigurationBean,
								 CrucibleProjectConfiguration crucibleProjectConfiguration,
								 final MissingPasswordHandler missingPasswordHandler,
								 CrucibleReviewListModel reviewListModel) {
		this.cfgManager = cfgManager;
		this.project = project;
		this.crucibleConfigurationBean = crucibleConfigurationBean;
		this.crucibleProjectConfiguration = crucibleProjectConfiguration;
		this.missingPasswordHandler = missingPasswordHandler;
		this.reviewListModel = reviewListModel;
		this.crucibleServerFacade = CrucibleServerFacadeImpl.getInstance();

	}

	public List<CrucibleStatusListener> getListenerList() {
		return listenerList;
	}

	public void registerListener(CrucibleStatusListener listener) {
		synchronized (listenerList) {
			listenerList.add(listener);
		}
	}

	public void unregisterListener(CrucibleStatusListener listener) {
		synchronized (listenerList) {
			listenerList.remove(listener);
		}
	}

private void doRunCrucible() {
	try {
		// collect review info from each server and each required filter
		final Map<PredefinedFilter, ReviewNotificationBean> reviews
				= new HashMap<PredefinedFilter, ReviewNotificationBean>();
		final Map<String, ReviewNotificationBean> customFilterReviews
				= new HashMap<String, ReviewNotificationBean>();

		CustomFilterBean manualFilter = crucibleProjectConfiguration.getCrucibleFilters().getManualFilter();

		for (final CrucibleServerCfg server : retrieveEnabledCrucibleServers()) {

			final List<ReviewAdapter> allServerReviews = new ArrayList<ReviewAdapter>();
			boolean communicationFailed = false;

			// retrieve reviews for predefined filters
			for (int i = 0;
				 i < crucibleProjectConfiguration.getCrucibleFilters().getPredefinedFilters().length
						 &&	i < PredefinedFilter.values().length; i++) {

				// if predefined filter is enabled
				if (crucibleProjectConfiguration.getCrucibleFilters().getPredefinedFilters()[i]) {
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
				if (!customFilterReviews.containsKey(manualFilter.getTitle())) {
					List<ReviewAdapter> list = new ArrayList<ReviewAdapter>();
					ReviewNotificationBean bean = new ReviewNotificationBean();
					bean.setReviews(list);
					customFilterReviews.put(manualFilter.getTitle(), bean);
				}

				ReviewNotificationBean customFilterNotificationBean = customFilterReviews.get(manualFilter.getTitle());

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

			// update global list model for processed server (and notify model listeners)
			if (!communicationFailed) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						reviewListModel.updateReviews(server, allServerReviews);
					}
				});
			}
		}

		// dispatch to the status checker listeners
		// todo it should be removed after switching all listeners to the new review model
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				synchronized (listenerList) {
					for (CrucibleStatusListener listener : listenerList) {
						listener.updateReviews(reviews, customFilterReviews);
					}
				}
			}
		});
	} catch (Throwable t) {
		PluginUtil.getLogger().error(t);
	}
}


	/**
	 * DO NOT use that method in 'dispatching thread' of IDEA. It can block GUI for several seconds.
	 */
	private void doRun() {
		try {
			doRunCrucible();
		} catch (Throwable t) {
			PluginUtil.getLogger().error(t);
		}
	}

	private Collection<CrucibleServerCfg> retrieveEnabledCrucibleServers() {
		return cfgManager.getAllEnabledCrucibleServers(CfgUtil.getProjectId(project));
	}


	/**
	 * Create a new instance of {@link java.util.TimerTask} for {@link java.util.Timer} re-scheduling purposes.
	 *
	 * @return new instance of TimerTask
	 */
	public TimerTask newTimerTask() {
		return new TimerTask() {
			@Override
			public void run() {
				doRun();
			}
		};
	}

	public boolean canSchedule() {
		crucibleVersion = CrucibleVersion.UNKNOWN;
		return !retrieveEnabledCrucibleServers().isEmpty();
	}

	public long getInterval() {
		return (long) crucibleConfigurationBean.getPollTime() * DateUtil.SECONDS_IN_MINUTE * DateUtil.MILISECONDS_IN_SECOND;
	}

	public void resetListenersState() {
		for (StatusListener listener : listenerList) {
			listener.resetState();
		}
	}

	public String getName() {
		return NAME;
	}
}