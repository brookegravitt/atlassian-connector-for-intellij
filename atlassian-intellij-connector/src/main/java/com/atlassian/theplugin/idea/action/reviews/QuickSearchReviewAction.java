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

import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.ReviewListToolWindowPanel;
import com.atlassian.theplugin.idea.crucible.SearchReviewDialog;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.idea.util.IdeaUiMultiTaskExecutor;
import com.atlassian.theplugin.util.PluginUtil;
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
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.containers.HashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Jacek Jaroczynski
 */
public class QuickSearchReviewAction extends AbstractCrucibleToolbarAction {
	private static final String NOT_FOUND_TEXT =
			"Unable to find review <b>%1$2s</b> on server <b>%2$2s</b>.<br>"
					+ "This most likely means that the review does not exist on this server,<br>"
					+ "but it can also be cause by server misconfiguration.<br>"
					+ "See the stack trace for detailed information.";

	public void actionPerformed(final AnActionEvent e) {
		final Project project = IdeaHelper.getCurrentProject(e.getDataContext());
		if (project == null) {
			return;
		}

		final ReviewListToolWindowPanel reviewsWindow = IdeaHelper.getReviewListToolWindowPanel(e);

		if (reviewsWindow == null) {
			return;
		}

		final SearchReviewDialog dialog = new SearchReviewDialog(project, reviewsWindow.getServers(),
				reviewsWindow.getCrucibleConfiguration().getView());
		dialog.show();

		if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {

			// find reviews in the local list
			final List<ReviewAdapter> localReviews = reviewsWindow.getLocalReviews(dialog.getSearchKey());

			final Collection<ServerData> servers = dialog.getSelectedServers();

			if (servers.size() > 0) {
				ProgressManager.getInstance().run(
						new QuickSearchTask(e, project, dialog, localReviews, servers, reviewsWindow));
			} else {
				showPopup(localReviews, project, e.getInputEvent().getComponent(), dialog.getSearchKey(), reviewsWindow);
			}
		}
	}

	private final class QuickSearchTask extends Task.Modal {
		private List<ReviewAdapter> serverReviews = new ArrayList<ReviewAdapter>();
		private boolean failed = false;
		private AnActionEvent event;
		@Nullable
		private Project project;
		private SearchReviewDialog dialog;
		private List<ReviewAdapter> localReviews;
		private Collection<ServerData> servers;
		private ReviewListToolWindowPanel reviewsWindow;

		private QuickSearchTask(AnActionEvent event, @Nullable Project project, SearchReviewDialog dialog,
				List<ReviewAdapter> localReviews, Collection<ServerData> servers,
				ReviewListToolWindowPanel reviewsWindow) {
			super(project, "Searching review", true);
			this.event = event;
			this.project = project;
			this.dialog = dialog;
			this.localReviews = localReviews;
			this.servers = servers;
			this.reviewsWindow = reviewsWindow;
		}

		public void run(@NotNull final ProgressIndicator indicator) {

			indicator.setFraction(0);

			List<IdeaUiMultiTaskExecutor.ErrorObject> problems =
					new ArrayList<IdeaUiMultiTaskExecutor.ErrorObject>();

			// find serverReviews on all selected servers
			for (ServerData server : servers) {
				try {
					Review review = CrucibleServerFacadeImpl.getInstance().getReview(server,
							new PermId(dialog.getSearchKey()));
					if (review != null) {
						serverReviews.add(new ReviewAdapter(review, server));
					}
				} catch (final RemoteApiException e) {
					Throwable cause = e.getCause();
					if (cause != null
							&& (cause.getMessage().equals("HTTP 400 (Bad Request)")
							|| cause.getMessage().equals("HTTP 404 (Not Found)"))) {
						String msg = String.format(NOT_FOUND_TEXT, dialog.getSearchKey(), server.getName());
						problems.add(new IdeaUiMultiTaskExecutor.ErrorObject(msg, e));
					} else {
						problems.add(new IdeaUiMultiTaskExecutor.ErrorObject("Error getting review", e));
					}
				} catch (final ServerPasswordNotProvidedException e) {
					problems.add(new IdeaUiMultiTaskExecutor.ErrorObject("Error getting review", e));
				}
			}
			failed = problems.size() == servers.size();

			if (failed) {
				reportProblem(problems, project);
			}
		}

		public void onSuccess() {
			if (!failed) {
				List<ReviewAdapter> reviews = mergeReviewList(localReviews, serverReviews);
				showPopup(reviews, project, event.getInputEvent().getComponent(), dialog.getSearchKey(), reviewsWindow);
			}
		}
	}

	private void reportProblem(final List<IdeaUiMultiTaskExecutor.ErrorObject> problems,
			final Project project) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				List<IdeaUiMultiTaskExecutor.ErrorObject> errorObjects = new ArrayList<IdeaUiMultiTaskExecutor.ErrorObject>();

				for (IdeaUiMultiTaskExecutor.ErrorObject problem : problems) {
					PluginUtil.getLogger().warn(problem.getMessage(), problem.getException());
					errorObjects.add(problem);
				}

				DialogWithDetails.showExceptionDialog(WindowManager.getInstance().getFrame(project), errorObjects);
			}
		});
	}

	private void showPopup(final List<ReviewAdapter> reviews, final Project project, final Component component,
			final String searchKey, final ReviewListToolWindowPanel reviewsWindow) {
		if (reviews.size() == 0) {
			Messages.showInfoMessage(project, "Review " + searchKey + " not found.", "Atlassian Connector for IntelliJ IDEA");
//			reviewsWindow.setStatusInfoMessage("Review " + searchKey + " not found.");
		} else if (reviews.size() == 1) {
			reviewsWindow.openReview(reviews.iterator().next(), true);
		} else if (reviews.size() > 1) {
			ListPopup popup = JBPopupFactory.getInstance().createListPopup(
					new ReviewListPopupStep("Found " + reviews.size() + " reviews", reviews, reviewsWindow));
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

	public static final class ReviewListPopupStep extends BaseListPopupStep<ReviewAdapter> {
		private ReviewListToolWindowPanel reviewsWindow;
		private static final int LENGHT = 40;

		public ReviewListPopupStep(final String title, final List<ReviewAdapter> reviews,
				final ReviewListToolWindowPanel reviewsWindow) {
			super(title, reviews, IconLoader.getIcon("/icons/crucible-16.png"));
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

			if (value.getServerData().getName().length() > LENGHT) {
				text.append(value.getServerData().getName().substring(0, LENGHT - (2 + 1)));
			} else {
				text.append(value.getServerData().getName());
			}

			text.append(')');

			return text.toString();
		}

		@Override
		public PopupStep onChosen(final ReviewAdapter selectedValue, final boolean finalChoice) {
			// add review to the model (to show it in the main list) and open the review
			reviewsWindow.openReview(selectedValue, true);

			return null;
		}
	}

}
