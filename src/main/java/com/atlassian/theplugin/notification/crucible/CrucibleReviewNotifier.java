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
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.CrucibleStatusListener;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.ReviewNotificationBean;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.events.GeneralCommentAdded;
import com.atlassian.theplugin.idea.crucible.events.GeneralCommentReplyAdded;
import com.atlassian.theplugin.idea.crucible.events.VersionedCommentAdded;
import com.atlassian.theplugin.idea.crucible.events.VersionedCommentReplyAdded;
import com.intellij.openapi.project.Project;

import java.util.*;

/**
 * This one is supposed to be per project.
 */
public class CrucibleReviewNotifier implements CrucibleStatusListener {
	private final List<CrucibleNotificationListener> listenerList = new ArrayList<CrucibleNotificationListener>();

	private Set<ReviewData> reviews = new HashSet<ReviewData>();
	private List<CrucibleNotification> notifications = new ArrayList<CrucibleNotification>();
	private HashMap<PredefinedFilter, NewExceptionNotification> exceptionNotifications =
			new HashMap<PredefinedFilter, NewExceptionNotification>();

	private boolean firstRun = true;
	private Project project;

	public CrucibleReviewNotifier(final Project project) {
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
			listenerList.add(listener);
		}
	}

	public void unregisterListener(CrucibleNotificationListener listener) {
		synchronized (listenerList) {
			listenerList.remove(listener);
		}
	}

	private void checkNewReviewItems(ReviewData oldReview, ReviewData newReview) throws ValueNotYetInitialized {
		for (CrucibleReviewItemInfo item : newReview.getReviewItems()) {
			boolean found = false;
			for (CrucibleReviewItemInfo oldItem : oldReview.getReviewItems()) {
				if (item.getId().equals(oldItem.getId())) {
					found = true;
					break;
				}
			}
			if (!found) {
				notifications.add(new NewReviewItemNotification(newReview, item));
			}
		}
	}

	private void checkReviewersStatus(ReviewData oldReview, ReviewData newReview) throws ValueNotYetInitialized {
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

	private void checkGeneralReplies(ReviewData review, GeneralComment oldComment, GeneralComment newComment) {
		for (GeneralComment reply : newComment.getReplies()) {
			GeneralComment existingReply = null;
			for (GeneralComment oldReply : oldComment.getReplies()) {
				if (reply.getPermId().getId().equals(oldReply.getPermId().getId())) {
					existingReply = oldReply;
					break;
				}
			}
			if (existingReply == null) {
				notifications.add(new NewReplyCommentNotification(review, newComment, reply));
				GeneralCommentReplyAdded event = new GeneralCommentReplyAdded(CrucibleReviewActionListener.ANONYMOUS,
						review, newComment, reply);
				IdeaHelper.getReviewActionEventBroker(project).trigger(event);
			}
		}
	}

	private void checkVersionedReplies(ReviewData review, final CrucibleReviewItemInfo info, VersionedComment oldComment,
			VersionedComment newComment) {
		for (VersionedComment reply : newComment.getReplies()) {
			VersionedComment existingReply = null;
			for (VersionedComment oldReply : oldComment.getReplies()) {
				if (reply.getPermId().getId().equals(oldReply.getPermId().getId())) {
					existingReply = oldReply;
					break;
				}
			}
			if (existingReply == null) {
				notifications.add(new NewReplyCommentNotification(review, newComment, reply));
				VersionedCommentReplyAdded event = new VersionedCommentReplyAdded(CrucibleReviewActionListener.ANONYMOUS,
						review, info, newComment, reply);
				IdeaHelper.getReviewActionEventBroker(project).trigger(event);
			}
		}
	}


	private void checkComments(ReviewData oldReview, ReviewData newReview) throws ValueNotYetInitialized {
		for (GeneralComment comment : newReview.getGeneralComments()) {
			GeneralComment existing = null;
			for (GeneralComment oldComment : oldReview.getGeneralComments()) {
				if (comment.getPermId().getId().equals(oldComment.getPermId().getId())) {
					existing = oldComment;
					break;
				}
			}
			if (existing == null) {
				notifications.add(new NewGeneralCommentNotification(newReview, comment));
				GeneralCommentAdded event = new GeneralCommentAdded(CrucibleReviewActionListener.ANONYMOUS, newReview, comment);
				IdeaHelper.getReviewActionEventBroker(project).trigger(event);
			} else {
				checkGeneralReplies(newReview, existing, comment);
			}
		}

		for (CrucibleReviewItemInfo info : newReview.getReviewItems()) {
			for (VersionedComment comment : info.getComments()) {
				VersionedComment existing = null;
				for (VersionedComment oldComment : oldReview.getVersionedComments()) {
					if (comment.getPermId().getId().equals(oldComment.getPermId().getId())) {
						existing = oldComment;
						break;
					}
				}
				if (existing == null) {
					notifications.add(new NewVersionedCommentNotification(newReview, comment));
					if (project != null) {
						VersionedCommentAdded event = new VersionedCommentAdded(CrucibleReviewActionListener.ANONYMOUS,
								newReview, info, comment);
						IdeaHelper.getReviewActionEventBroker(project).trigger(event);
					}
				} else {
					checkVersionedReplies(newReview, info, existing, comment);
				}
			}
		}
	}

	public void showError(String errorString) {
		// ignore
	}

	public void updateReviews(Map<PredefinedFilter, ReviewNotificationBean> incomingReviews,
			Map<String, ReviewNotificationBean> customIncomingReviews) {

		notifications.clear();
		boolean exceptionFound = false;

		Set<ReviewData> processedReviews = new HashSet<ReviewData>();
		if (!incomingReviews.isEmpty()) {
			for (PredefinedFilter predefinedFilter : incomingReviews.keySet()) {
				if (incomingReviews.get(predefinedFilter).getException() == null) {
					List<ReviewData> incomingCategory = incomingReviews.get(predefinedFilter).getReviews();

					for (ReviewData reviewDataInfo : incomingCategory) {
						if (processedReviews.contains(reviewDataInfo)) {
							continue;
						}
						if (reviews.contains(reviewDataInfo)) {
							ReviewData existing = null;
							for (ReviewData review : reviews) {
								if (review.equals(reviewDataInfo)) {
									existing = review;
								}
							}

							// check state change
							if (!reviewDataInfo.getState().equals(existing.getState())) {
								notifications.add(new ReviewStateChangedNotification(reviewDataInfo, existing.getState()));
							}

							// check review items
							try {
								checkNewReviewItems(existing, reviewDataInfo);
							} catch (ValueNotYetInitialized valueNotYetInitialized) {
								// TODO all is it correct
							}

							// check reviewers status
							try {
								checkReviewersStatus(existing, reviewDataInfo);
							} catch (ValueNotYetInitialized valueNotYetInitialized) {
								// TODO all is it correct
							}

							// check comments status
							try {
								checkComments(existing, reviewDataInfo);
							} catch (ValueNotYetInitialized valueNotYetInitialized) {
								// TODO all is it correct
							}

							processedReviews.add(reviewDataInfo);
						} else {
							notifications.add(new NewReviewNotification(reviewDataInfo));
							processedReviews.add(reviewDataInfo);
						}
					}
					
					exceptionNotifications.remove(predefinedFilter);
				} else {
					// do not analyze events when exception was raised.
					// maybe next time wil be better
					NewExceptionNotification prevNotificationException = exceptionNotifications.get(predefinedFilter);
					NewExceptionNotification newException =
							new NewExceptionNotification(incomingReviews.get(predefinedFilter).getException());

					if ((prevNotificationException != null && !prevNotificationException.equals(newException))
							|| exceptionNotifications.size() <= 0 || prevNotificationException == null) {

						exceptionNotifications.put(predefinedFilter, newException);
						//add exception only if differs in text
						// exceptions texts are defined as string server_url + exception message
						//so no excepion will be missed
						if (!notifications.contains(newException)) {
							notifications.add(newException);
						}
					
						exceptionFound = true;
					}
				}

			}
			if (!exceptionFound) {
				reviews.clear();
				reviews.addAll(processedReviews);
			}
		}

		if (!firstRun) {
			for (CrucibleNotificationListener listener : listenerList) {
				listener.updateNotifications(notifications);
			}
		}
		firstRun = false;
	}

	public void resetState() {
		reviews.clear();
		exceptionNotifications.clear();
		
		for (CrucibleNotificationListener listener : listenerList) {
			listener.resetState();
		}
	}

	public List<CrucibleNotification> getNotifications() {
		return notifications;
	}
}
