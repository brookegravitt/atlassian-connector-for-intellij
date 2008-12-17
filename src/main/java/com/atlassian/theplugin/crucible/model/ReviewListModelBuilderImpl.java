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
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;

import java.util.Map;

public class ReviewListModelBuilderImpl implements ReviewListModelBuilder {
	private final CrucibleReviewListModel crucibleReviewListModel;
	private final CrucibleProjectConfiguration crucibleProjectConfiguration;
	private final CrucibleServerFacade crucibleServerFacade;
	private final Project project;
	private final MissingPasswordHandler missingPasswordHandler;
	private final CfgManager cfgManager;

	public ReviewListModelBuilderImpl(final CrucibleReviewListModel crucibleReviewListModel,
									  final Project project,
									  final CfgManager cfgManager,
									  final ProjectConfigurationBean projectConfigurationBean) {
		this.crucibleReviewListModel = crucibleReviewListModel;
		this.project = project;
		this.cfgManager = cfgManager;
		this.crucibleProjectConfiguration = projectConfigurationBean.getCrucibleConfiguration();
		this.crucibleServerFacade = CrucibleServerFacadeImpl.getInstance();
		this.missingPasswordHandler = new MissingPasswordHandler(crucibleServerFacade, cfgManager, project);
	}

	public void getReviewsFromServer(final boolean sendNotifications) {
		final long epoch = getEpoch();
		final Boolean[] predefinedFilters = crucibleProjectConfiguration.getCrucibleFilters().getPredefinedFilters();
		final CustomFilter customFilter = crucibleProjectConfiguration.getCrucibleFilters().getManualFilter();

		try {
			final CrucibleQueryExecutor crucibleQueryExecutor =
					new CrucibleQueryExecutor(crucibleServerFacade, cfgManager, project, missingPasswordHandler,
										crucibleReviewListModel, epoch);
			final Map<CrucibleFilter, ReviewNotificationBean> reviews =
					crucibleQueryExecutor.runQuery(predefinedFilters, customFilter);
			ApplicationManager.getApplication().invokeLater(new Runnable() {
				public void run() {
					crucibleReviewListModel.updateReviews(epoch, reviews, sendNotifications);
				}
			});
		} catch (InterruptedException ex) {
			PluginUtil.getLogger().info(ex);
		}
	}

	public long getCurrentEpoch() {
		return crucibleReviewListModel.getCurrentEpoch();
	}

	private long getEpoch() {
		return crucibleReviewListModel.getEpoch();
	}

}
