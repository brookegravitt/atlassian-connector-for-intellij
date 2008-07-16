package com.atlassian.theplugin.notification.crucible;

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewBean;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewerBean;
import com.atlassian.theplugin.commons.crucible.api.model.State;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.ReviewDataImpl;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
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

    private List<ReviewData> prepareReviewData() {
        List<ReviewData> reviews = new ArrayList<ReviewData>();

        Reviewer reviewer1 = prepareReviewer("bob", "Bob", false);
        Reviewer reviewer2 = prepareReviewer("alice", "Alice", false);
        Reviewer reviewer3 = prepareReviewer("scott", "Scott", true);

        Review review1 = prepareReview();
        ((ReviewBean)review1).setPermId(new PermId(){
            public String getId() {
                return "CR-1";
            }
        });
        ((ReviewBean)review1).setState(State.REVIEW);
        ((ReviewBean)review1).setReviewers(Arrays.asList(reviewer1, reviewer2));

        Review review2 = prepareReview();
        ((ReviewBean)review2).setPermId(new PermId(){
            public String getId() {
                return "CR-2";
            }
        });
        ((ReviewBean)review2).setState(State.REVIEW);
        ((ReviewBean)review2).setReviewers(Arrays.asList(reviewer2, reviewer3));

        reviews.add(new ReviewDataImpl(review1, null));
        reviews.add(new ReviewDataImpl(review2, null));

        return reviews;
    }

    public void testNewReviews() {
        List<ReviewData> emptyReviews = new ArrayList<ReviewData>();
        List<ReviewData> reviews = prepareReviewData();

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

    public void testReviewerStatus() throws ValueNotYetInitialized {
        List<ReviewData> reviews = prepareReviewData();

        Map<PredefinedFilter, List<ReviewData>> map = new HashMap<PredefinedFilter, List<ReviewData>>();
        CrucibleReviewNotifier notifier = new CrucibleReviewNotifier();

        map.put(PredefinedFilter.ToReview, reviews);
        notifier.updateReviews(map, new HashMap<String, List<ReviewData>>());
        assertEquals(reviews.size(), notifier.getNotifications().size());

        reviews = prepareReviewData();
        ((ReviewerBean)reviews.get(0).getReviewers().get(0)).setCompleted(true);
        map.put(PredefinedFilter.ToReview, reviews);
        notifier.updateReviews(map, new HashMap<String, List<ReviewData>>());
        assertEquals(1, notifier.getNotifications().size());
        
    }
}
