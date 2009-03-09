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

import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.api.model.PermIdBean;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.VcsIdeaHelper;
import com.atlassian.theplugin.idea.crucible.CrucibleConstants;
import com.atlassian.theplugin.idea.crucible.ReviewsToolWindowPanel;
import com.atlassian.theplugin.idea.crucible.SearchReviewDialog;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.containers.HashSet;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Jacek Jaroczynski
 */
public class QuickSearchReviewAction extends AnAction {

	public void actionPerformed(final AnActionEvent e) {
		final Project project = IdeaHelper.getCurrentProject(e.getDataContext());
		if (project == null) {
			return;
		}

		if (!VcsIdeaHelper.isUnderVcsControl(e)) {
			Messages.showInfoMessage(project, CrucibleConstants.CRUCIBLE_MESSAGE_NOT_UNDER_VCS,
					CrucibleConstants.CRUCIBLE_TITLE_NOT_UNDER_VCS);
			return;
		}

		final ReviewsToolWindowPanel reviewsWindow = IdeaHelper.getReviewsToolWindowPanel(e);

		if (reviewsWindow == null) {
			return;
		}

		final SearchReviewDialog dialog = new SearchReviewDialog(project, reviewsWindow.getServers(),
				reviewsWindow.getCrucibleConfiguration().getView());
		dialog.show();

		if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {

			// find reviews in the local list
			final List<ReviewAdapter> localReviews = reviewsWindow.getReviews(dialog.getSearchKey());

			final Collection<CrucibleServerCfg> servers = dialog.getSelectedServers();

			if (servers.size() > 0) {

				ProgressManager.getInstance().run(new Task.Modal(project, "Searching review", true) {
					private List<ReviewAdapter> serverReviews = new ArrayList<ReviewAdapter>();

					public void run(final ProgressIndicator indicator) {
						// find serverReviews on all selected servers
						for (CrucibleServerCfg server : servers) {
							try {
								Review review = CrucibleServerFacadeImpl.getInstance().getReview(
										server, new PermIdBean(dialog.getSearchKey()));
								if (review != null) {
									serverReviews.add(new ReviewAdapter(review, server));
								}
							} catch (RemoteApiException e) {
								PluginUtil.getLogger().warn("Error getting review", e);
								// todo notify user somehow
							} catch (ServerPasswordNotProvidedException e) {
								PluginUtil.getLogger().warn("Error getting review", e);
								// todo notify user somehow
							}
						}
					}

					public void onSuccess() {
						List<ReviewAdapter> reviews = mergeReviewList(localReviews, serverReviews);
						showPopup(reviews, project, e.getInputEvent().getComponent(), dialog.getSearchKey(), reviewsWindow);
					}
				});
			} else {
				showPopup(localReviews, project, e.getInputEvent().getComponent(), dialog.getSearchKey(), reviewsWindow);
			}
		}
	}

	private void showPopup(final List<ReviewAdapter> reviews, final Project project, final Component component,
			final String searchKey, final ReviewsToolWindowPanel reviewsWindow) {
		if (reviews.size() == 0) {
			Messages.showInfoMessage(project, "Review " + searchKey + " not found.", "Atlassian IntelliJ Connector");
//			reviewsWindow.setStatusMessage("Review " + searchKey + " not found.");
		} else if (reviews.size() == 1) {
			reviewsWindow.openReview(reviews.iterator().next());
		} else if (reviews.size() > 1) {
			ListPopup popup =
					JBPopupFactory.getInstance().createListPopup(new ReviewListPopupStep(reviews, project, reviewsWindow));
			popup.show(component);
		}
	}

	private List<ReviewAdapter> mergeReviewList(final List<ReviewAdapter> localReviews,
			final List<ReviewAdapter> serverReviews) {

		Set<ReviewAdapter> reviews = new HashSet<ReviewAdapter>();

		reviews.addAll(localReviews);
		reviews.addAll(serverReviews);

		return new ArrayList<ReviewAdapter>(reviews);
	}

	private final class ReviewListPopupStep extends BaseListPopupStep<ReviewAdapter> {
		private Project project;
		private ReviewsToolWindowPanel reviewsWindow;
		private static final int LENGHT = 40;

		private ReviewListPopupStep(final List<ReviewAdapter> reviews, final Project project,
				final ReviewsToolWindowPanel reviewsWindow) {
			super("Select Review To Open", reviews, IconLoader.getIcon("/icons/crucible-16.png"));
			this.project = project;
			this.reviewsWindow = reviewsWindow;
		}

		@NotNull
		@Override
		public String getTextFor(final ReviewAdapter value) {
			StringBuilder text = new StringBuilder();

			text.append(value.getPermId().getId()).append(": ");

			if (value.getName().length() > LENGHT) {
				text.append(value.getName().substring(0, LENGHT - (2 + 1))).append("...");
			} else {
				text.append(value.getName());
			}

			text.append(" (");

			if (value.getServer().getName().length() > LENGHT) {
				text.append(value.getServer().getName().substring(0, LENGHT - (2 + 1)));
			} else {
				text.append(value.getServer().getName());
			}

			text.append(')');

			return text.toString();
		}

		@Override
		public PopupStep onChosen(final ReviewAdapter selectedValue, final boolean finalChoice) {
			// add review to the model (to show it in the main list)
			reviewsWindow.openReview(selectedValue);

			// open review
//			IdeaHelper.getCrucibleToolWindow(project).showReview(selectedValue);
			return null;
		}
	}

}
