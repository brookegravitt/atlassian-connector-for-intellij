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
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginFailedException;
import com.atlassian.theplugin.commons.util.DateUtil;
import com.atlassian.theplugin.configuration.CrucibleProjectConfiguration;
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


	public CrucibleStatusChecker(CfgManager cfgManager, Project project, CrucibleConfigurationBean crucibleConfigurationBean,
			CrucibleProjectConfiguration crucibleProjectConfiguration, final MissingPasswordHandler missingPasswordHandler) {
		this.cfgManager = cfgManager;
		this.project = project;
		this.crucibleConfigurationBean = crucibleConfigurationBean;
		this.crucibleProjectConfiguration = crucibleProjectConfiguration;
		this.missingPasswordHandler = missingPasswordHandler;
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

		for (CrucibleServerCfg server : retrieveEnabledCrucibleServers()) {

			for (int i = 0;
				 i < crucibleProjectConfiguration.getCrucibleFilters().getPredefinedFilters().length &&
					i < PredefinedFilter.values().length; i++) {
				if (crucibleProjectConfiguration.getCrucibleFilters().getPredefinedFilters()[i]) {
					PredefinedFilter filter = PredefinedFilter.values()[i];
					if (!reviews.containsKey(filter)) {
						ReviewNotificationBean bean = new ReviewNotificationBean();
						List<ReviewData> list = new ArrayList<ReviewData>();
						bean.setReviews(list);
						reviews.put(filter, bean);
					}
					ReviewNotificationBean bean = reviews.get(filter);
					try {
						PluginUtil.getLogger().debug("Crucible: updating status for server: "
								+ server.getUrl() + ", filter type: " + filter);

						List<Review> review = crucibleServerFacade.getReviewsForFilter(server, filter);
						List<ReviewData> reviewData = new ArrayList<ReviewData>(review.size());
						for (Review r : review) {
							reviewData.add(new ReviewDataImpl(r, server));
						}

						bean.getReviews().addAll(reviewData);
					} catch (ServerPasswordNotProvidedException exception) {
						ApplicationManager.getApplication().invokeLater(missingPasswordHandler,
								ModalityState.defaultModalityState());
						bean.setException(exception);
					} catch (RemoteApiLoginFailedException exception) {
						ApplicationManager.getApplication().invokeLater(missingPasswordHandler,
								ModalityState.defaultModalityState());
						bean.setException(exception);
					} catch (RemoteApiException e) {
						PluginUtil.getLogger().info("Error getting Crucible reviews for " + server.getName()
								+ " server", e);
						bean.setException(e);
					}
				}
			}
		}


		CustomFilterBean filter = crucibleProjectConfiguration.getCrucibleFilters().getManualFilter();

		if (filter != null) {

			if (!customFilterReviews.containsKey(filter.getTitle())) {
				List<ReviewData> list = new ArrayList<ReviewData>();
				ReviewNotificationBean bean = new ReviewNotificationBean();
				bean.setReviews(list);
				customFilterReviews.put(filter.getTitle(), bean);
			}
			ReviewNotificationBean bean = customFilterReviews.get(filter.getTitle());

			if (filter.isEnabled()) {
				for (CrucibleServerCfg server : retrieveEnabledCrucibleServers()) {
					if (server.getServerId().toString().equals(filter.getServerUid())) {
						try {
							PluginUtil.getLogger().debug("Crucible: updating status for server: "
									+ server.getUrl() + ", custom filter");
							List<Review> customFilter
									= crucibleServerFacade.getReviewsForCustomFilter(server, filter);


							List<ReviewData> reviewData = new ArrayList<ReviewData>(customFilter.size());
							for (Review r : customFilter) {
								reviewData.add(new ReviewDataImpl(r, server));
							}

							bean.getReviews().addAll(reviewData);
						} catch (ServerPasswordNotProvidedException exception) {
							ApplicationManager.getApplication().invokeLater(
									new MissingPasswordHandler(crucibleServerFacade, cfgManager, project),
									ModalityState.defaultModalityState());
							bean.setException(exception);
						} catch (RemoteApiException e) {
							PluginUtil.getLogger().info("Error getting Crucible reviews for " + server.getName()
									+ " server", e);
							bean.setException(e);
						}
					}
				}
			}

		}

		// dispatch to the listeners
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