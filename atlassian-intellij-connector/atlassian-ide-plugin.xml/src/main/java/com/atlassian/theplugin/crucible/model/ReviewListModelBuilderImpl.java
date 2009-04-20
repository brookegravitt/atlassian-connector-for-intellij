package com.atlassian.theplugin.crucible.model;

import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.configuration.CrucibleWorkspaceConfiguration;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.idea.crucible.ReviewNotificationBean;
import com.atlassian.theplugin.remoteapi.MissingPasswordHandler;
import com.intellij.openapi.project.Project;

import java.util.Collection;
import java.util.Map;

public class ReviewListModelBuilderImpl implements ReviewListModelBuilder {
	private final CrucibleWorkspaceConfiguration crucibleProjectConfiguration;
	private final CrucibleServerFacade crucibleServerFacade;
	private final Project project;
	private final MissingPasswordHandler missingPasswordHandler;
	private final CfgManager cfgManager;

	public ReviewListModelBuilderImpl(final Project project,
			final CfgManager cfgManager,
			final ProjectConfigurationBean projectConfigurationBean) {
		this.project = project;
		this.cfgManager = cfgManager;
		this.crucibleProjectConfiguration = projectConfigurationBean.getCrucibleConfiguration();
		this.crucibleServerFacade = CrucibleServerFacadeImpl.getInstance();
		this.missingPasswordHandler = new MissingPasswordHandler(crucibleServerFacade, cfgManager, project);
	}

	public Map<CrucibleFilter, ReviewNotificationBean> getReviewsFromServer(
			final CrucibleReviewListModel crucibleReviewListModel,
			final UpdateReason updateReason,
			final long epoch) throws InterruptedException {

		final Boolean[] predefinedFilters = crucibleProjectConfiguration.getCrucibleFilters().getPredefinedFilters();
		final CustomFilter customFilter = crucibleProjectConfiguration.getCrucibleFilters().getManualFilter();
		final RecentlyOpenReviewsFilter recentlyOpenFilter =
				crucibleProjectConfiguration.getCrucibleFilters().getRecenltyOpenFilter();


		final CrucibleQueryExecutor crucibleQueryExecutor =
				new CrucibleQueryExecutor(crucibleServerFacade, cfgManager, project, missingPasswordHandler,
						crucibleReviewListModel);
		final Map<CrucibleFilter, ReviewNotificationBean> reviews =
				crucibleQueryExecutor.runQuery(predefinedFilters, customFilter, recentlyOpenFilter, epoch);


		Collection<ReviewAdapter> active = crucibleReviewListModel.getOpenInIdeReviews();
		if (!active.isEmpty()) {
			ReviewNotificationBean bean = crucibleQueryExecutor.runDetailedReviewsQuery(active, epoch);
			reviews.put(PredefinedFilter.OpenInIde, bean);
		}

		return reviews;
	}
}
