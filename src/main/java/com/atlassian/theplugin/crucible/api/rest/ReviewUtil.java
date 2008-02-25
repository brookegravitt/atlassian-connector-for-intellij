package com.atlassian.theplugin.crucible.api.rest;

import com.atlassian.theplugin.crucible.api.PermIdBean;
import com.atlassian.theplugin.crucible.api.ReviewData;
import com.atlassian.theplugin.crucible.api.ReviewDataBean;
import com.atlassian.theplugin.crucible.api.State;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.CDATA;


public final class ReviewUtil {

	///CLOVER:OFF
	private ReviewUtil() {
	}
	///CLOVER:ON

	public static String getChildText(Element node, String childName) {
		try {
			return node.getChild(childName).getText();
		} catch (Exception e) {
			return "";
		}
	}

	public static ReviewData parseReviewNode(Element reviewNode) {
		ReviewDataBean review = new ReviewDataBean();

		review.setAuthor(getChildText(reviewNode, "author"));
		review.setCreator(getChildText(reviewNode, "creator"));
		review.setModerator(getChildText(reviewNode, "moderator"));
		review.setDescription(getChildText(reviewNode, "description"));
		review.setName(getChildText(reviewNode, "name"));
		review.setProjectKey(getChildText(reviewNode, "projectKey"));
		review.setRepoName(getChildText(reviewNode, "repoName"));

		String stateString = getChildText(reviewNode, "state");
		if (!"".equals(stateString)) {
			review.setState(State.fromValue(stateString));
		}

		if (reviewNode.getChild("permaId") != null) {
			PermIdBean permId = new PermIdBean();
			permId.setId(reviewNode.getChild("permaId").getChild("id").getText());
			review.setPermaId(permId);
		}

		return review;
	}

	public static void addTag(Element root, String tagName, String tagValue) {
		Element newElement = new Element(tagName);
		newElement.addContent(tagValue);
		root.addContent(newElement);
	}

	public static Document prepareCreateReviewNode(ReviewData review, String patch) {
		Element root = new Element("createReview");
		Document doc = new Document(root);
		root.addContent(prepareReviewNodeElement(review));

		if (patch != null) {
			Element patchData = new Element("patch");
			root.addContent(patchData);

			CDATA patchT = new CDATA(patch);
			patchData.setContent(patchT);
		}

		return doc;
	}

	public static Document prepareReviewNode(ReviewData review) {
		Element reviewData = prepareReviewNodeElement(review);
		return new Document(reviewData);
	}

	private static Element prepareReviewNodeElement(ReviewData review) {
		Element reviewData = new Element("reviewData");

		addTag(reviewData, "author", review.getAuthor());
		addTag(reviewData, "creator", review.getCreator());
		addTag(reviewData, "description", review.getDescription());
		addTag(reviewData, "moderator", review.getModerator());
		addTag(reviewData, "name", review.getName());
		addTag(reviewData, "projectKey", review.getProjectKey());
		addTag(reviewData, "repoName", review.getRepoName());
		if (review.getState() != null) {
			addTag(reviewData, "state", review.getState().value());
		}
		if (review.getPermaId() != null) {
			Element permIdElement = new Element("permaId");
			reviewData.addContent(permIdElement);
			addTag(permIdElement, "id", review.getPermaId().getId());
		}

		return reviewData;
	}
}
