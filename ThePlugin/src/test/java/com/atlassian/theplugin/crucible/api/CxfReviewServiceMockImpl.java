package com.atlassian.theplugin.crucible.api;

import com.atlassian.theplugin.crucible.CrucibleServerFacadeTest;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class CxfReviewServiceMockImpl implements RpcReviewServiceName {
	public static final String VALID_URL = "http://localhost:9001";
	public static final String VALID_LOGIN = "validLogin";
	public static final String VALID_PASSWORD = "validPassword";

	public List<ReviewData> getChildReviews(String token, PermId arg1) {
		return null;
	}

	public List<Object> getAllRevisionComments(String token, PermId arg1) {
		return null;
	}

	public void removeReviewItem(String token, PermId arg1,	PermId arg2) {
	}

	public Object addFisheyeDiff(String token, PermId arg1, String arg2, String arg3, String arg4, String arg5, String arg6) {
		return null;
	}

	public List<ReviewData> getAllReviews(String token) {
		return null;
	}

	public List<Object> getVersionedComments(String token, PermId arg1) {
		return null;
	}

	public Object addComment(String token, PermId arg1, Object arg2) {
		return null;
	}

	public List<String> getReviewers(String token, PermId arg1) {
		return Arrays.asList("Alice", "Bob", "Charlie");
	}

	public Object addGeneralComment(String token, PermId arg1, Object arg2) {
		return null;
	}

	public ReviewData createReviewFromPatch(String token, ReviewData review, String patch) {

		if (token == null || token.length() == 0) {
			throw new RuntimeException("auth token invalid");
		}

		PermId permId = new PermId();
		permId.setId("some id");
		review.setPermaId(permId);
		if (review.getProjectKey().equals(CrucibleServerFacadeTest.INVALID_PROJECT_KEY)) {
			throw new RuntimeException("Invalid project key");
		}

		return review;
	}

	public List<Object> getReviewItemsForReview(String token, PermId arg1) {
		return null;
	}

	public ReviewData changeState(String token, PermId arg1, Action arg2) {
		return null;
	}

	public ReviewData getReview(String token, PermId arg1) {
		return null;
	}

	public java.util.List<com.atlassian.theplugin.crucible.api.soap.xfire.review.ReviewData> getReviewsInStates(
			java.lang.String token,
			java.util.List<com.atlassian.theplugin.crucible.api.soap.xfire.review.State> arg1) {
		List<ReviewData> reviews = new ArrayList<ReviewData>();
		int i = 1;
		for (State state : arg1) {

//			reviews.add(prepareReviewData("test" + i++, state));
//			reviews.add(prepareReviewData("test" + i++, state));
		}
		return reviews;
	}

	public ReviewData createReview(String token, ReviewData review) {

		if (token == null || token.length() == 0) {
			throw new RuntimeException("auth token invalid");
		}

		PermId permId = new PermId();
		permId.setId("some id");
		review.setPermaId(permId);
		if (review.getProjectKey().equals(CrucibleServerFacadeTest.INVALID_PROJECT_KEY)) {
			throw new RuntimeException("Invalid project key");
		}

		return review;
	}

	public List<Object> getGeneralComments(String token, PermId arg1) {
		return null; 
	}


}
