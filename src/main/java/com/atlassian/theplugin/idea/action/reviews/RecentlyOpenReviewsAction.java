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
package com.atlassian.theplugin.idea.action.reviews;

import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.configuration.CrucibleProjectConfiguration;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.configuration.ReviewRecentlyOpenBean;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.ReviewsToolWindowPanel;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;

import java.util.List;

/**
 * @author Jacek Jaroczynski
 */
public class RecentlyOpenReviewsAction extends AnAction {
	@Override
	public void actionPerformed(final AnActionEvent e) {

		final Project project = IdeaHelper.getCurrentProject(e);
		if (project == null) {
			return;
		}

		final ReviewsToolWindowPanel reviewsWindow = IdeaHelper.getReviewsToolWindowPanel(e);
		if (reviewsWindow == null) {
			return;
		}

		final ProjectConfigurationBean projectConf = IdeaHelper.getProjectComponent(e, ProjectConfigurationBean.class);
		if (projectConf == null) {
			return;
		}

		final CrucibleProjectConfiguration crucibleConf = projectConf.getCrucibleConfiguration();
		if (crucibleConf == null) {
			return;
		}

		final List<ReviewRecentlyOpenBean> recentlyOpenReviews = crucibleConf.getRecentlyOpenReviews();
		if (recentlyOpenReviews.size() > 0) {
			// prepare list of recentlyOpenReviews from the config list


			ProgressManager.getInstance().run(new Task.Modal(project, "Retrieving recently open reviews", true) {
				private List<ReviewAdapter> reviews;

				public void run(final ProgressIndicator indicator) {
					reviews = reviewsWindow.getReviewAdapters(recentlyOpenReviews);
				}

				public void onSuccess() {
					ListPopup popup =
							JBPopupFactory.getInstance()
									.createListPopup(new QuickSearchReviewAction.ReviewListPopupStep(reviews, reviewsWindow));
					popup.show(e.getInputEvent().getComponent());
				}
			});


		} else {
			Messages.showInfoMessage(project, "No recently open reviews found.", PluginUtil.PRODUCT_NAME);
		}
	}
}
