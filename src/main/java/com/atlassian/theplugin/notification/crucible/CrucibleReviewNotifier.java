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

import com.atlassian.theplugin.commons.crucible.CrucibleChangeSet;
import com.atlassian.theplugin.commons.crucible.CrucibleStatusListener;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.notification.crucible.CrucibleNotification;
import com.atlassian.theplugin.notification.crucible.NewGeneralCommentNotification;
import com.atlassian.theplugin.notification.crucible.NewReviewItemNotification;
import com.atlassian.theplugin.notification.crucible.NewReviewNotification;
import com.atlassian.theplugin.notification.crucible.ReviewStateChangedNotification;
import com.atlassian.theplugin.notification.crucible.ReviewerCompletedNotification;
import com.atlassian.theplugin.notification.crucible.NewReplyCommentNotification;
import com.atlassian.theplugin.notification.crucible.NewVersionedCommentNotification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This one is supposed to be per project.
 */
public class CrucibleReviewNotifier implements CrucibleStatusListener {
    private Map<PredefinedFilter, List<CrucibleChangeSet>> reviews = new HashMap<PredefinedFilter, List<CrucibleChangeSet>>();
    private List<CrucibleNotification> notifications = new ArrayList<CrucibleNotification>();

    public CrucibleReviewNotifier() {
    }

    private void checkNewReviewItems(CrucibleChangeSet oldReview, CrucibleChangeSet newReview) throws ValueNotYetInitialized {
        for (CrucibleFileInfo item : newReview.getFiles()) {
            boolean found = false;
            for (CrucibleFileInfo oldItem : oldReview.getFiles()) {
                if (item.getPermId().getId().equals(oldItem.getPermId().getId())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                notifications.add(new NewReviewItemNotification(newReview, item));
            }
        }
    }

    private void checkReviewersStatus(CrucibleChangeSet oldReview, CrucibleChangeSet newReview) throws ValueNotYetInitialized {
        for (Reviewer reviewer : newReview.getReviewers()) {
            for (Reviewer oldReviewer : oldReview.getReviewers()) {
                if (reviewer.getUserName().equals(oldReviewer.getUserName())) {
                    if (reviewer.isCompleted() != oldReviewer.isCompleted()) {
                        notifications.add(new ReviewerCompletedNotification(newReview, reviewer));
                    }
                }
            }
        }
    }

    private void checkReplies(CrucibleChangeSet review, GeneralComment oldComment, GeneralComment newComment) {
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
            }
        }
    }

    private void checkComments(CrucibleChangeSet oldReview, CrucibleChangeSet newReview) throws ValueNotYetInitialized {
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
            } else {
                checkReplies(newReview, existing, comment);
            }
        }

        for (VersionedComment comment : newReview.getVersionedComments()) {
            VersionedComment existing = null;
            for (VersionedComment oldComment : oldReview.getVersionedComments()) {
                if (comment.getPermId().getId().equals(oldComment.getPermId().getId())) {
                    existing = oldComment;
                    break;
                }
            }
            if (existing == null) {
                notifications.add(new NewVersionedCommentNotification(newReview, comment));
            } else {
                checkReplies(newReview, existing, comment);
            }
        }
    }

    public void updateReviews(Map<PredefinedFilter, List<CrucibleChangeSet>> incomingReviews,
                              Map<String, List<CrucibleChangeSet>> customIncomingReviews) {

        notifications.clear();

        if (!incomingReviews.isEmpty()) {
            int newCounter = 0;
            for (PredefinedFilter predefinedFilter : incomingReviews.keySet()) {
                List<CrucibleChangeSet> incomingCategory = incomingReviews.get(predefinedFilter);
                List<CrucibleChangeSet> existingCategory = reviews.get(predefinedFilter);
                List<CrucibleChangeSet> newForCategory = new ArrayList<CrucibleChangeSet>();

                for (CrucibleChangeSet reviewDataInfo : incomingCategory) {
                    if (existingCategory != null) {
                        CrucibleChangeSet existing = null;
                        for (CrucibleChangeSet oldReviewDataInfo : existingCategory) {
                            if (reviewDataInfo.getPermaId().getId().equals(oldReviewDataInfo.getPermaId().getId())) {
                                existing = oldReviewDataInfo;
                                break;
                            }
                        }

                        if (existing == null) {
                            newForCategory.add(reviewDataInfo);
                            notifications.add(new NewReviewNotification(reviewDataInfo));
                        } else {
                            // check state change
                            if (!reviewDataInfo.getState().equals(existing.getState())) {
                                notifications.add(new ReviewStateChangedNotification(reviewDataInfo, existing.getState()));
                            }

                            // check review items
                            try {
                                checkNewReviewItems(existing, reviewDataInfo);
                            } catch (ValueNotYetInitialized valueNotYetInitialized) {
                            }

                            // check reviewers status
                            try {
                                checkReviewersStatus(existing, reviewDataInfo);
                            } catch (ValueNotYetInitialized valueNotYetInitialized) {
                            }

                            // check comments status
                            try {
                                checkComments(existing, reviewDataInfo);
                            } catch (ValueNotYetInitialized valueNotYetInitialized) {
                            }


                        }
                    } else {
                        notifications.add(new NewReviewNotification(reviewDataInfo));
                        newForCategory.add(reviewDataInfo);
                    }
                }
            }

            reviews.clear();
            for (PredefinedFilter predefinedFilter : incomingReviews.keySet()) {
                reviews.put(predefinedFilter, new ArrayList<CrucibleChangeSet>(incomingReviews.get(predefinedFilter)));
            }
        }

        for (CrucibleNotification notification : notifications) {
            System.out.println("crucible.getPresentationMessage() = " + notification.getPresentationMessage());
        }
    }

    public void resetState() {
//To change body of implemented methods use File | Settings | File Templates.
    }
}
