package com.atlassian.theplugin.crucible.model;

import com.atlassian.connector.intellij.crucible.CrucibleServerFacade;
import com.atlassian.connector.intellij.crucible.IntelliJCrucibleServerFacade;
import com.atlassian.connector.intellij.crucible.RecentlyOpenReviewsFilter;
import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFilter;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.configuration.CrucibleWorkspaceConfiguration;
import com.atlassian.theplugin.configuration.WorkspaceConfigurationBean;
import com.atlassian.theplugin.idea.config.MissingPasswordHandler;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;
import com.atlassian.theplugin.idea.crucible.ReviewNotificationBean;
import com.intellij.openapi.project.Project;
import java.util.Collection;
import java.util.Map;

public class ReviewListModelBuilderImpl implements ReviewListModelBuilder {
	private final CrucibleWorkspaceConfiguration crucibleProjectConfiguration;
	private final CrucibleServerFacade crucibleServerFacade;
	private final Project project;
	private final MissingPasswordHandler missingPasswordHandler;
	private final ProjectCfgManagerImpl projectCfgManager;

	public ReviewListModelBuilderImpl(final Project project,
			final ProjectCfgManagerImpl projectCfgManager,
			final WorkspaceConfigurationBean projectConfigurationBean) {
		this.project = project;
		this.projectCfgManager = projectCfgManager;
		this.crucibleProjectConfiguration = projectConfigurationBean.getCrucibleConfiguration();
		this.crucibleServerFacade = IntelliJCrucibleServerFacade.getInstance();
		this.missingPasswordHandler = new MissingPasswordHandler(crucibleServerFacade, projectCfgManager, project);
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
				new CrucibleQueryExecutor(crucibleServerFacade, projectCfgManager, project, missingPasswordHandler,
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
