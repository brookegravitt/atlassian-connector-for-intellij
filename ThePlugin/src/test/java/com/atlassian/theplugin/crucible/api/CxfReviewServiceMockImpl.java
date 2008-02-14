package com.atlassian.theplugin.crucible.api;

import com.atlassian.theplugin.crucible.CrucibleServerFacadeTest;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.*;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-11
 * Time: 14:17:05
 * To change this template use File | Settings | File Templates.
 */
public class CxfReviewServiceMockImpl implements RpcReviewServiceName {
	public static final String VALID_URL = "http://localhost:9001";
	public static final String VALID_LOGIN = "validLogin";
	public static final String VALID_PASSWORD = "validPassword";

	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(localName = "getChildReviews", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.review.GetChildReviews")
	@ResponseWrapper(localName = "getChildReviewsResponse", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.review.GetChildReviewsResponse")
	@WebMethod
	public List<ReviewData> getChildReviews(@WebParam(name = "token", targetNamespace = "")
	String token, @WebParam(name = "arg1", targetNamespace = "")
	PermId arg1) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(localName = "getAllRevisionComments", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.review.GetAllRevisionComments")
	@ResponseWrapper(localName = "getAllRevisionCommentsResponse", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.review.GetAllRevisionCommentsResponse")
	@WebMethod
	public List<Object> getAllRevisionComments(@WebParam(name = "token", targetNamespace = "")
	String token, @WebParam(name = "arg1", targetNamespace = "")
	PermId arg1) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@RequestWrapper(localName = "removeReviewItem", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.review.RemoveReviewItem")
	@ResponseWrapper(localName = "removeReviewItemResponse", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.review.RemoveReviewItemResponse")
	@WebMethod
	public void removeReviewItem(@WebParam(name = "token", targetNamespace = "")
	String token, @WebParam(name = "arg1", targetNamespace = "")
	PermId arg1, @WebParam(name = "arg2", targetNamespace = "")
	PermId arg2) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(localName = "addFisheyeDiff", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.review.AddFisheyeDiff")
	@ResponseWrapper(localName = "addFisheyeDiffResponse", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.review.AddFisheyeDiffResponse")
	@WebMethod
	public Object addFisheyeDiff(@WebParam(name = "token", targetNamespace = "")
	String token, @WebParam(name = "arg1", targetNamespace = "")
	PermId arg1, @WebParam(name = "arg2", targetNamespace = "")
	String arg2, @WebParam(name = "arg3", targetNamespace = "")
	String arg3, @WebParam(name = "arg4", targetNamespace = "")
	String arg4, @WebParam(name = "arg5", targetNamespace = "")
	String arg5, @WebParam(name = "arg6", targetNamespace = "")
	String arg6) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(localName = "getAllReviews", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.review.GetAllReviews")
	@ResponseWrapper(localName = "getAllReviewsResponse", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.review.GetAllReviewsResponse")
	@WebMethod
	public List<ReviewData> getAllReviews(@WebParam(name = "token", targetNamespace = "")
	String token) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(localName = "getVersionedComments", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.review.GetVersionedComments")
	@ResponseWrapper(localName = "getVersionedCommentsResponse", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.review.GetVersionedCommentsResponse")
	@WebMethod
	public List<Object> getVersionedComments(@WebParam(name = "token", targetNamespace = "")
	String token, @WebParam(name = "arg1", targetNamespace = "")
	PermId arg1) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(localName = "addComment", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.review.AddComment")
	@ResponseWrapper(localName = "addCommentResponse", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.review.AddCommentResponse")
	@WebMethod
	public Object addComment(@WebParam(name = "token", targetNamespace = "")
	String token, @WebParam(name = "arg1", targetNamespace = "")
	PermId arg1, @WebParam(name = "arg2", targetNamespace = "")
	Object arg2) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(localName = "getReviewers", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.review.GetReviewers")
	@ResponseWrapper(localName = "getReviewersResponse", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.review.GetReviewersResponse")
	@WebMethod
	public List<String> getReviewers(@WebParam(name = "token", targetNamespace = "")
	String token, @WebParam(name = "arg1", targetNamespace = "")
	PermId arg1) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(localName = "addGeneralComment", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.review.AddGeneralComment")
	@ResponseWrapper(localName = "addGeneralCommentResponse", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.review.AddGeneralCommentResponse")
	@WebMethod
	public Object addGeneralComment(@WebParam(name = "token", targetNamespace = "")
	String token, @WebParam(name = "arg1", targetNamespace = "")
	PermId arg1, @WebParam(name = "arg2", targetNamespace = "")
	Object arg2) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(localName = "createReviewFromPatch", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.review.CreateReviewFromPatch")
	@ResponseWrapper(localName = "createReviewFromPatchResponse", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.review.CreateReviewFromPatchResponse")
	@WebMethod
	public ReviewData createReviewFromPatch(@WebParam(name = "token", targetNamespace = "")
	String token, @WebParam(name = "review", targetNamespace = "")
	ReviewData review, @WebParam(name = "patch", targetNamespace = "")
	String patch) {

		if (token == null || token.length() == 0) {
			throw new RuntimeException("auth token invalid");
		}

		PermId permId = new PermId();
		permId.setId("some id");
		review.setPermaId(permId);
		if (review.getProjectKey().equals(CrucibleServerFacadeTest.INVALID_PROJECT_KEY)){
					throw new RuntimeException("Invalid project key");
		}

		return review;
	}

	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(localName = "getReviewItemsForReview", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.review.GetReviewItemsForReview")
	@ResponseWrapper(localName = "getReviewItemsForReviewResponse", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.review.GetReviewItemsForReviewResponse")
	@WebMethod
	public List<Object> getReviewItemsForReview(@WebParam(name = "token", targetNamespace = "")
	String token, @WebParam(name = "arg1", targetNamespace = "")
	PermId arg1) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(localName = "changeState", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.review.ChangeState")
	@ResponseWrapper(localName = "changeStateResponse", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.review.ChangeStateResponse")
	@WebMethod
	public ReviewData changeState(@WebParam(name = "token", targetNamespace = "")
	String token, @WebParam(name = "arg1", targetNamespace = "")
	PermId arg1, @WebParam(name = "arg2", targetNamespace = "")
	Action arg2) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(localName = "getReview", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.review.GetReview")
	@ResponseWrapper(localName = "getReviewResponse", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.review.GetReviewResponse")
	@WebMethod
	public ReviewData getReview(@WebParam(name = "token", targetNamespace = "")
	String token, @WebParam(name = "arg1", targetNamespace = "")
	PermId arg1) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(localName = "getReviewsInStates", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.review.GetReviewsInStates")
	@ResponseWrapper(localName = "getReviewsInStatesResponse", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.review.GetReviewsInStatesResponse")
	@WebMethod
	public List<ReviewData> getReviewsInStates(@WebParam(name = "token", targetNamespace = "")
	String token, @WebParam(name = "arg1", targetNamespace = "")
	List<State> arg1) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(localName = "createReview", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.review.CreateReview")
	@ResponseWrapper(localName = "createReviewResponse", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.review.CreateReviewResponse")
	@WebMethod
	public ReviewData createReview(@WebParam(name = "token", targetNamespace = "")
	String token, @WebParam(name = "review", targetNamespace = "")
	ReviewData review) {

		if (token == null || token.length() == 0) {
			throw new RuntimeException("auth token invalid");
		}

		PermId permId = new PermId();
		permId.setId("some id");
		review.setPermaId(permId);
		if (review.getProjectKey().equals(CrucibleServerFacadeTest.INVALID_PROJECT_KEY)){
					throw new RuntimeException("Invalid project key");
		}

		return review;
	}

	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(localName = "getGeneralComments", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.review.GetGeneralComments")
	@ResponseWrapper(localName = "getGeneralCommentsResponse", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.review.GetGeneralCommentsResponse")
	@WebMethod
	public List<Object> getGeneralComments(@WebParam(name = "token", targetNamespace = "")
	String token, @WebParam(name = "arg1", targetNamespace = "")
	PermId arg1) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
