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

import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CustomField;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.PermIdBean;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewBean;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewerBean;
import com.atlassian.theplugin.commons.crucible.api.model.State;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.ReviewDataImpl;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrucibleReviewNotifierTest extends TestCase {

    private ReviewBean prepareReview() {
        return new ReviewBean();
    }

    private ReviewerBean prepareReviewer(String userName, String displayName, boolean completed) {
        ReviewerBean reviewer = new ReviewerBean();
        reviewer.setUserName(userName);
        reviewer.setDisplayName(displayName);
        reviewer.setCompleted(completed);

        return reviewer;
    }

    private GeneralComment prepareGeneralComment(final PermId permId, final GeneralComment reply) {
        return new GeneralComment() {

            public PermId getPermId() {
                return permId;
            }

            public String getMessage() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean isDraft() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean isDeleted() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean isDefectRaised() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean isDefectApproved() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean isReply() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public User getUser() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Date getCreateDate() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public List<GeneralComment> getReplies() {
                List<GeneralComment> replies = new ArrayList<GeneralComment>();
                if (reply != null) {
                    replies.add(reply);
                }
                return replies;
            }

            public Map<String, CustomField> getCustomFields() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public STATE getState() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };
    }

    private VersionedComment prepareVersionedComment(final PermId permId, final PermId itemId, final VersionedComment reply) {
        return new VersionedComment() {

            public PermId getPermId() {
                return permId;
            }

            public PermId getReviewItemId() {
                return itemId;
            }

            public boolean isToLineInfo() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public int getToStartLine() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public int getToEndLine() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean isFromLineInfo() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public int getFromStartLine() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public int getFromEndLine() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getMessage() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean isDraft() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean isDeleted() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean isDefectRaised() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean isDefectApproved() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean isReply() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public User getUser() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Date getCreateDate() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public List<VersionedComment> getReplies() {
                List<VersionedComment> replies = new ArrayList<VersionedComment>();
                if (reply != null) {
                    replies.add(reply);
                }
                return replies;
            }

            public Map<String, CustomField> getCustomFields() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public STATE getState() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };
    }

    private CrucibleFileInfo prepareReviewItem(final PermId newItem) {
        return new CrucibleFileInfo() {

            public VersionedVirtualFile getOldFileDescriptor() {
                return null;
            }

            public int getNumberOfComments() throws ValueNotYetInitialized {
                return 0;
            }

            public int getNumberOfDefects() throws ValueNotYetInitialized {
                return 0;
            }

            public PermId getPermId() {
                return newItem;
            }

            public List<VersionedComment> getVersionedComments() throws ValueNotYetInitialized {
                return null;
            }

            public String getRepositoryName() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public VersionedVirtualFile getFileDescriptor() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };
    }

    private List<ReviewData> prepareReviewData(State state) throws ValueNotYetInitialized {
        PermIdBean reviewId1 = new PermIdBean();
        reviewId1.setId("CR-1");
        PermIdBean newItem = new PermIdBean();
        newItem.setId("CRF:11");
        PermIdBean newCommentId = new PermIdBean();
        newCommentId.setId("CMT:11");
        PermIdBean newVCommentId = new PermIdBean();
        newVCommentId.setId("CMT:12");

        PermIdBean reviewId2 = new PermIdBean();
        reviewId1.setId("CR-2");
        PermIdBean newItem1 = new PermIdBean();
        newItem1.setId("CRF:21");
        PermIdBean newCommentId1 = new PermIdBean();
        newCommentId1.setId("CMT:21");
        PermIdBean newVCommentId1 = new PermIdBean();
        newVCommentId1.setId("CMT:22");


        List<ReviewData> reviews = new ArrayList<ReviewData>();

        Reviewer reviewer1 = prepareReviewer("bob", "Bob", false);
        Reviewer reviewer2 = prepareReviewer("alice", "Alice", false);
        Reviewer reviewer3 = prepareReviewer("scott", "Scott", false);
        Reviewer reviewer4 = prepareReviewer("alice", "Alice", false);

        Review review1 = prepareReview();
        ((ReviewBean) review1).setFiles(new ArrayList<CrucibleFileInfo>());
        ((ReviewBean) review1).setGeneralComments(new ArrayList<GeneralComment>());
        ((ReviewBean) review1).setVersionedComments(new ArrayList<VersionedComment>());
        ((ReviewBean) review1).setPermId(reviewId1);
        ((ReviewBean) review1).setState(state);
        ((ReviewBean) review1).setReviewers(Arrays.asList(reviewer1, reviewer2));


        review1.getGeneralComments().add(prepareGeneralComment(newCommentId, null));
        review1.getVersionedComments().add(prepareVersionedComment(newVCommentId, newItem, null));
        review1.getFiles().add(prepareReviewItem(newItem));


        Review review2 = prepareReview();
        ((ReviewBean) review2).setFiles(new ArrayList<CrucibleFileInfo>());
        ((ReviewBean) review2).setGeneralComments(new ArrayList<GeneralComment>());
        ((ReviewBean) review2).setVersionedComments(new ArrayList<VersionedComment>());
        ((ReviewBean) review2).setPermId(reviewId2);
        ((ReviewBean) review2).setState(state);
        ((ReviewBean) review2).setReviewers(Arrays.asList(reviewer3, reviewer4));

        review2.getGeneralComments().add(prepareGeneralComment(newCommentId1, null));
        review2.getVersionedComments().add(prepareVersionedComment(newVCommentId1, newItem1, null));
        review2.getFiles().add(prepareReviewItem(newItem1));

        reviews.add(new ReviewDataImpl(review1, null));
        reviews.add(new ReviewDataImpl(review2, null));

        return reviews;
    }

    public void testNewReviews() throws ValueNotYetInitialized {
        List<ReviewData> emptyReviews = new ArrayList<ReviewData>();
        List<ReviewData> reviews = prepareReviewData(State.REVIEW);

        Map<PredefinedFilter, List<ReviewData>> map = new HashMap<PredefinedFilter, List<ReviewData>>();
        map.put(PredefinedFilter.ToReview, emptyReviews);

        CrucibleReviewNotifier notifier = new CrucibleReviewNotifier();
        notifier.updateReviews(map, new HashMap<String, List<ReviewData>>());
        assertEquals(0, notifier.getNotifications().size());

        map.put(PredefinedFilter.ToReview, reviews);
        notifier.updateReviews(map, new HashMap<String, List<ReviewData>>());
        assertEquals(reviews.size(), notifier.getNotifications().size());

        notifier.updateReviews(map, new HashMap<String, List<ReviewData>>());
        assertEquals(0, notifier.getNotifications().size());
    }

    public void testResetStateReviews() throws ValueNotYetInitialized {
        List<ReviewData> emptyReviews = new ArrayList<ReviewData>();
        List<ReviewData> reviews = prepareReviewData(State.REVIEW);

        Map<PredefinedFilter, List<ReviewData>> map = new HashMap<PredefinedFilter, List<ReviewData>>();
        map.put(PredefinedFilter.ToReview, emptyReviews);

        CrucibleReviewNotifier notifier = new CrucibleReviewNotifier();
        notifier.updateReviews(map, new HashMap<String, List<ReviewData>>());
        assertEquals(0, notifier.getNotifications().size());

        map.put(PredefinedFilter.ToReview, reviews);
        notifier.updateReviews(map, new HashMap<String, List<ReviewData>>());
        assertEquals(2, notifier.getNotifications().size());
        assertTrue(notifier.getNotifications().get(0) instanceof NewReviewNotification);
        assertTrue(notifier.getNotifications().get(1) instanceof NewReviewNotification);

        notifier.updateReviews(map, new HashMap<String, List<ReviewData>>());
        assertEquals(0, notifier.getNotifications().size());

        notifier.resetState();

        notifier.updateReviews(map, new HashMap<String, List<ReviewData>>());
        assertEquals(2, notifier.getNotifications().size());
        assertTrue(notifier.getNotifications().get(0) instanceof NewReviewNotification);
        assertTrue(notifier.getNotifications().get(1) instanceof NewReviewNotification);
    }

    public void testStatusChange() throws ValueNotYetInitialized {
        List<ReviewData> reviews = prepareReviewData(State.REVIEW);

        Map<PredefinedFilter, List<ReviewData>> map = new HashMap<PredefinedFilter, List<ReviewData>>();
        CrucibleReviewNotifier notifier = new CrucibleReviewNotifier();

        map.put(PredefinedFilter.ToReview, reviews);
        notifier.updateReviews(map, new HashMap<String, List<ReviewData>>());
        assertEquals(reviews.size(), notifier.getNotifications().size());

        reviews = prepareReviewData(State.CLOSED);
        map.put(PredefinedFilter.ToReview, reviews);
        notifier.updateReviews(map, new HashMap<String, List<ReviewData>>());
        assertEquals(2, notifier.getNotifications().size());
        assertTrue(notifier.getNotifications().get(0) instanceof ReviewStateChangedNotification);
        assertTrue(notifier.getNotifications().get(1) instanceof ReviewStateChangedNotification);
    }

    public void testReviewerStatus() throws ValueNotYetInitialized {
        List<ReviewData> reviews = prepareReviewData(State.REVIEW);

        Map<PredefinedFilter, List<ReviewData>> map = new HashMap<PredefinedFilter, List<ReviewData>>();
        CrucibleReviewNotifier notifier = new CrucibleReviewNotifier();

        map.put(PredefinedFilter.ToReview, reviews);
        notifier.updateReviews(map, new HashMap<String, List<ReviewData>>());
        assertEquals(reviews.size(), notifier.getNotifications().size());

        reviews = prepareReviewData(State.REVIEW);
        ((ReviewerBean) reviews.get(0).getReviewers().get(0)).setCompleted(true);
        map.put(PredefinedFilter.ToReview, reviews);
        notifier.updateReviews(map, new HashMap<String, List<ReviewData>>());
        assertEquals(1, notifier.getNotifications().size());
        assertTrue(notifier.getNotifications().get(0) instanceof ReviewerCompletedNotification);

        reviews = prepareReviewData(State.REVIEW);
        ((ReviewerBean) reviews.get(0).getReviewers().get(0)).setCompleted(false);
        map.put(PredefinedFilter.ToReview, reviews);
        notifier.updateReviews(map, new HashMap<String, List<ReviewData>>());
        assertEquals(1, notifier.getNotifications().size());
        assertTrue(notifier.getNotifications().get(0) instanceof ReviewerCompletedNotification);

        reviews = prepareReviewData(State.REVIEW);
        ((ReviewerBean) reviews.get(0).getReviewers().get(0)).setCompleted(true);
        ((ReviewerBean) reviews.get(0).getReviewers().get(1)).setCompleted(true);
        map.put(PredefinedFilter.ToReview, reviews);
        notifier.updateReviews(map, new HashMap<String, List<ReviewData>>());
        assertEquals(3, notifier.getNotifications().size());
        assertTrue(notifier.getNotifications().get(0) instanceof ReviewerCompletedNotification);
        assertTrue(notifier.getNotifications().get(1) instanceof ReviewerCompletedNotification);
        assertTrue(notifier.getNotifications().get(2) instanceof ReviewCompletedNotification);
    }

    public void testNewItem() throws ValueNotYetInitialized {
        List<ReviewData> reviews = prepareReviewData(State.REVIEW);

        Map<PredefinedFilter, List<ReviewData>> map = new HashMap<PredefinedFilter, List<ReviewData>>();
        CrucibleReviewNotifier notifier = new CrucibleReviewNotifier();

        map.put(PredefinedFilter.ToReview, reviews);
        notifier.updateReviews(map, new HashMap<String, List<ReviewData>>());
        assertEquals(reviews.size(), notifier.getNotifications().size());

        reviews = prepareReviewData(State.REVIEW);

        final PermId newItem = new PermId() {

            public String getId() {
                return "CRF:2";
            }
        };
        reviews.get(0).getFiles().add(new CrucibleFileInfo() {

            public VersionedVirtualFile getOldFileDescriptor() {
                return null;
            }

            public int getNumberOfComments() throws ValueNotYetInitialized {
                return 0;
            }

            public int getNumberOfDefects() throws ValueNotYetInitialized {
                return 0;
            }

            public PermId getPermId() {
                return newItem;
            }

            public List<VersionedComment> getVersionedComments() throws ValueNotYetInitialized {
                return null;
            }

            public String getRepositoryName() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public VersionedVirtualFile getFileDescriptor() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        map.put(PredefinedFilter.ToReview, reviews);
        notifier.updateReviews(map, new HashMap<String, List<ReviewData>>());
        assertEquals(1, notifier.getNotifications().size());
        assertTrue(notifier.getNotifications().get(0) instanceof NewReviewItemNotification);
    }

    public void testNewGeneralComment() throws ValueNotYetInitialized {
        List<ReviewData> reviews = prepareReviewData(State.REVIEW);

        Map<PredefinedFilter, List<ReviewData>> map = new HashMap<PredefinedFilter, List<ReviewData>>();
        CrucibleReviewNotifier notifier = new CrucibleReviewNotifier();

        map.put(PredefinedFilter.ToReview, reviews);
        notifier.updateReviews(map, new HashMap<String, List<ReviewData>>());
        assertEquals(reviews.size(), notifier.getNotifications().size());

        reviews = prepareReviewData(State.REVIEW);

        final PermId newCommentId = new PermId() {

            public String getId() {
                return "CMT:2";
            }
        };
        reviews.get(0).getGeneralComments().add(new GeneralComment() {

            public PermId getPermId() {
                return newCommentId;
            }

            public String getMessage() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean isDraft() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean isDeleted() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean isDefectRaised() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean isDefectApproved() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean isReply() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public User getUser() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Date getCreateDate() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public List<GeneralComment> getReplies() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Map<String, CustomField> getCustomFields() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public STATE getState() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        map.put(PredefinedFilter.ToReview, reviews);
        notifier.updateReviews(map, new HashMap<String, List<ReviewData>>());
        assertEquals(1, notifier.getNotifications().size());
        assertTrue(notifier.getNotifications().get(0) instanceof NewGeneralCommentNotification);
    }

    public void testNewVersionedComment() throws ValueNotYetInitialized {
        List<ReviewData> reviews = prepareReviewData(State.REVIEW);

        Map<PredefinedFilter, List<ReviewData>> map = new HashMap<PredefinedFilter, List<ReviewData>>();
        CrucibleReviewNotifier notifier = new CrucibleReviewNotifier();

        map.put(PredefinedFilter.ToReview, reviews);
        notifier.updateReviews(map, new HashMap<String, List<ReviewData>>());
        assertEquals(reviews.size(), notifier.getNotifications().size());

        reviews = prepareReviewData(State.REVIEW);

        final PermId newCommentId = new PermId() {

            public String getId() {
                return "CMT:3";
            }
        };
        final PermId newId = new PermId() {

            public String getId() {
                return "CRF:2";
            }
        };
        reviews.get(0).getVersionedComments().add(new VersionedComment() {

            public PermId getPermId() {
                return newCommentId;
            }

            public PermId getReviewItemId() {
                return newId;
            }

            public boolean isToLineInfo() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public int getToStartLine() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public int getToEndLine() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean isFromLineInfo() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public int getFromStartLine() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public int getFromEndLine() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getMessage() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean isDraft() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean isDeleted() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean isDefectRaised() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean isDefectApproved() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean isReply() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public User getUser() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Date getCreateDate() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public List<VersionedComment> getReplies() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Map<String, CustomField> getCustomFields() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public STATE getState() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        map.put(PredefinedFilter.ToReview, reviews);
        notifier.updateReviews(map, new HashMap<String, List<ReviewData>>());
        assertEquals(1, notifier.getNotifications().size());
        assertTrue(notifier.getNotifications().get(0) instanceof NewVersionedCommentNotification);
    }

}
