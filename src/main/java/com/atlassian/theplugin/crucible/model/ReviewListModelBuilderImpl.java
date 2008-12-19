package com.atlassian.theplugin.crucible.model;

import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFilter;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.configuration.CrucibleProjectConfiguration;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.idea.crucible.ReviewNotificationBean;
import com.atlassian.theplugin.remoteapi.MissingPasswordHandler;
import com.intellij.openapi.project.Project;

import java.util.Map;

public class ReviewListModelBuilderImpl implements ReviewListModelBuilder {
	private final CrucibleProjectConfiguration crucibleProjectConfiguration;
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

		final CrucibleQueryExecutor crucibleQueryExecutor =
				new CrucibleQueryExecutor(crucibleServerFacade, cfgManager, project, missingPasswordHandler,
						crucibleReviewListModel);
		final Map<CrucibleFilter, ReviewNotificationBean> reviews =
				crucibleQueryExecutor.runQuery(predefinedFilters, customFilter, epoch);
		return reviews;

	}
}
