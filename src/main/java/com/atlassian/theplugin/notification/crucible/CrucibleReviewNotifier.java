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

package com.atlassian.theplugin.notification.crucible;

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModelListener;
import com.atlassian.theplugin.crucible.model.UpdateContext;
import com.atlassian.theplugin.crucible.model.UpdateReason;
import com.atlassian.theplugin.idea.crucible.CrucibleStatusListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This one is supposed to be per project.
 */
public class CrucibleReviewNotifier implements CrucibleStatusListener, CrucibleReviewListModelListener {
	private final List<CrucibleNotificationListener> listenerList = new ArrayList<CrucibleNotificationListener>();

	private Set<ReviewAdapter> reviews = new HashSet<ReviewAdapter>();
	private List<CrucibleNotification> notifications = new ArrayList<CrucibleNotification>();

	private boolean firstRun = true;
	private Project project;


	public CrucibleReviewNotifier(@NotNull final Project project) {
		this.project = project;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public void registerListener(CrucibleNotificationListener listener) {
		synchronized (listenerList) {
			if (!listenerList.contains(listener)) {
				listenerList.add(listener);
			}
		}
	}

	public void unregisterListener(CrucibleNotificationListener listener) {
		synchronized (listenerList) {
			listenerList.remove(listener);
		}
	}

	private void checkReviewersStatus(ReviewAdapter oldReview, ReviewAdapter newReview) throws ValueNotYetInitialized {
		boolean allCompleted = true;
		boolean atLeastOneChanged = false;
		for (Reviewer reviewer : newReview.getReviewers()) {
			for (Reviewer oldReviewer : oldReview.getReviewers()) {
				if (reviewer.getUserName().equals(oldReviewer.getUserName())) {
					if (reviewer.isCompleted() != oldReviewer.isCompleted()) {
						notifications.add(new ReviewerCompletedNotification(newReview, reviewer));
						atLeastOneChanged = true;
					}
				}
			}
			if (!reviewer.isCompleted()) {
				allCompleted = false;
			}
		}
		if (allCompleted && atLeastOneChanged) {
			notifications.add(new ReviewCompletedNotification(newReview));
		}
	}

	private void checkGeneralReplies(ReviewAdapter review, GeneralComment oldComment, GeneralComment newComment) {
		for (GeneralComment reply : newComment.getReplies()) {
			GeneralComment existingReply = null;
			if (oldComment != null) {
				for (GeneralComment oldReply : oldComment.getReplies()) {
					if (reply.getPermId().getId().equals(oldReply.getPermId().getId())) {
						existingReply = oldReply;
						break;
					}
				}
				if ((existingReply == null) || !existingReply.getMessage().equals(reply.getMessage())) {
					if (existingReply == null) {
						notifications.add(new NewReplyCommentNotification(review, newComment, reply));
					} else {
						notifications.add(new UpdatedReplyCommentNotification(review, newComment, reply));
					}
				}
			}
		}

		if (oldComment != null) {
			List<GeneralComment> deletedGen = getDeletedComments(
					oldComment.getReplies(), newComment.getReplies());
			for (GeneralComment gc : deletedGen) {
				notifications.add(new RemovedReplyCommentNotification(review, gc));
			}
		}
	}

	private void checkVersionedReplies(ReviewAdapter review, final PermId filePermId, VersionedComment oldComment,
									   VersionedComment newComment) {
		for (VersionedComment reply : newComment.getReplies()) {
			VersionedComment existingReply = null;
			if (oldComment != null) {
				for (VersionedComment oldReply : oldComment.getReplies()) {
					if (reply.getPermId().getId().equals(oldReply.getPermId().getId())) {
						existingReply = oldReply;
						break;
					}
				}
				if ((existingReply == null) || !existingReply.getMessage().equals(reply.getMessage())) {
					if (existingReply == null) {
						notifications.add(new NewReplyCommentNotification(review, newComment, reply));
					} else {
						notifications.add(new UpdatedReplyCommentNotification(review, newComment, reply));
					}
				}
			}
		}

		if (oldComment != null) {
			List<VersionedComment> deletedVcs = getDeletedComments(
					oldComment.getReplies(), newComment.getReplies());
			for (VersionedComment vc : deletedVcs) {
				notifications.add(new RemovedReplyCommentNotification(review, vc));
			}
		}
	}


	private void checkState(final ReviewAdapter oldReview, final ReviewAdapter newReview) {
		// check state change
		if (!oldReview.getState().equals(newReview.getState())) {
			notifications.add(new ReviewStateChangedNotification(oldReview, newReview.getState()));
		}

	}

	private void checkComments(final ReviewAdapter oldReview, final ReviewAdapter newReview, final boolean checkFiles)
			throws ValueNotYetInitialized {
		for (GeneralComment comment : newReview.getGeneralComments()) {
			GeneralComment existing = null;
			for (GeneralComment oldComment : oldReview.getGeneralComments()) {
				if (comment.getPermId().getId().equals(oldComment.getPermId().getId())) {
					existing = oldComment;
					break;
				}
			}

			if ((existing == null)
					|| !existing.getMessage().equals(comment.getMessage())
					|| existing.isDefectRaised() != comment.isDefectRaised()) {
				if (existing == null) {
					notifications.add(new NewGeneralCommentNotification(newReview, comment));
				} else {
					notifications.add(new UpdatedGeneralCommentNotification(newReview, comment));
				}
			}
			checkGeneralReplies(newReview, existing, comment);
		}

		List<GeneralComment> deletedGen = getDeletedComments(
				oldReview.getGeneralComments(), newReview.getGeneralComments());
		for (GeneralComment gc : deletedGen) {
			notifications.add(new RemovedGeneralCommentNotification(newReview, gc));
		}

		if (checkFiles) {
			for (CrucibleFileInfo fileInfo : newReview.getFiles()) {
				for (VersionedComment comment : fileInfo.getVersionedComments()) {
					VersionedComment existing = null;
					for (CrucibleFileInfo oldFile : oldReview.getFiles()) {
						for (VersionedComment oldComment : oldFile.getVersionedComments()) {
							if (comment.getPermId().getId().equals(oldComment.getPermId().getId())) {
								existing = oldComment;
								break;
							}
						}
					}
					if ((existing == null)
							|| !existing.getMessage().equals(comment.getMessage())
							|| existing.isDefectRaised() != comment.isDefectRaised()) {
						if (existing == null) {
							notifications.add(new NewVersionedCommentNotification(newReview, comment));
						} else {
							notifications.add(new UpdatedVersionedCommentNotification(newReview, comment));
						}
					}
					checkVersionedReplies(newReview, fileInfo.getPermId(), existing, comment);
				}
			}

			// todo does not check replies
			List<VersionedComment> oldVersionedComments = new ArrayList<VersionedComment>();
			List<VersionedComment> newVersionedComments = new ArrayList<VersionedComment>();
			for (CrucibleFileInfo oldFile : oldReview.getFiles()) {
				oldVersionedComments.addAll(oldFile.getVersionedComments());
			}
			for (CrucibleFileInfo newFile : newReview.getFiles()) {
				newVersionedComments.addAll(newFile.getVersionedComments());
			}
			List<VersionedComment> deletedVcs = getDeletedComments(
					oldVersionedComments, newVersionedComments);
			for (VersionedComment vc : deletedVcs) {
				notifications.add(new RemovedVersionedCommentNotification(newReview, vc));
			}
		}
	}

	private <T extends Comment> List<T> getDeletedComments(List<T> org, List<T> modified) {
		List<T> deletedList = new ArrayList<T>();

		for (T corg : org) {
			boolean found = false;
			for (T cnew : modified) {
				if (cnew.getPermId().equals(corg.getPermId())) {
					found = true;
					break;
				}
			}
			if (!found) {
				deletedList.add(corg);
			}
		}

		return deletedList;
	}

	public void showError(String errorString) {
		// ignore
	}

	public void resetState() {
		reviews.clear();
		//exceptionNotifications.clear();

		for (CrucibleNotificationListener listener : listenerList) {
			listener.resetState();
		}
	}

	public List<CrucibleNotification> getNotifications() {
		return notifications;
	}


	public void reviewAdded(UpdateContext updateContext) {
		if (updateContext.getUpdateReason() == UpdateReason.REFRESH
				|| updateContext.getUpdateReason() == UpdateReason.TIMER_FIRED) {
			notifications.add(new NewReviewItemNotification(updateContext.getReviewAdapter()));
		}
	}

	public void reviewRemoved(UpdateContext updateContext) {
	}

	public void reviewChanged(UpdateContext updateContext) {
		final ReviewAdapter oldReviewAdapter = updateContext.getOldReviewAdapter();
		final ReviewAdapter newReviewAdapter = updateContext.getReviewAdapter();

		if ((oldReviewAdapter != null && newReviewAdapter != null)
				&& (updateContext.getUpdateReason() == UpdateReason.REFRESH
				|| updateContext.getUpdateReason() == UpdateReason.TIMER_FIRED)) {
			checkState(oldReviewAdapter, newReviewAdapter);
			// check reviewers status
			try {
				checkReviewersStatus(oldReviewAdapter, newReviewAdapter);
			} catch (ValueNotYetInitialized valueNotYetInitialized) {
				//all is it correct
			}

			// check comments status
			try {
				checkComments(oldReviewAdapter, newReviewAdapter, true);
			} catch (ValueNotYetInitialized valueNotYetInitialized) {
				//all is it correct
			}
		}
	}

	public void modelChanged(UpdateContext updateContext) {
	}

	public void reviewListUpdateStarted(UpdateContext updateContext) {

		notifications.clear();
	}

	public void reviewListUpdateFinished(UpdateContext updateContext) {
		if (!firstRun) {
			if (updateContext.getUpdateReason() == UpdateReason.REFRESH
					|| updateContext.getUpdateReason() == UpdateReason.TIMER_FIRED) {
				for (CrucibleNotificationListener listener : listenerList) {
					listener.updateNotifications(notifications);
				}
			}
		}
		firstRun = false;
	}

	public void reviewChangedWithoutFiles(UpdateContext updateContext) {
		final ReviewAdapter oldReviewAdapter = updateContext.getOldReviewAdapter();
		final ReviewAdapter newReviewAdapter = updateContext.getReviewAdapter();
		if ((oldReviewAdapter != null && newReviewAdapter != null)
				&& (updateContext.getUpdateReason() == UpdateReason.REFRESH
				|| updateContext.getUpdateReason() == UpdateReason.TIMER_FIRED)) {
			checkState(oldReviewAdapter, newReviewAdapter);
			// check reviewers status
			try {
				checkReviewersStatus(oldReviewAdapter, newReviewAdapter);
			} catch (ValueNotYetInitialized valueNotYetInitialized) {
				//all is it correct
			}

			// check comments status
			try {
				checkComments(oldReviewAdapter, newReviewAdapter, false);
			} catch (ValueNotYetInitialized valueNotYetInitialized) {
				//all is it correct
			}
		}

	}

	public void reviewListUpdateError(UpdateContext updateContext, Exception exception) {
		if (updateContext.getUpdateReason() == UpdateReason.REFRESH
				|| updateContext.getUpdateReason() == UpdateReason.TIMER_FIRED) {
			notifications.add(new NewExceptionNotification(exception));
		}
	}
}


