package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.ReviewItem;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Jun 10, 2008
 * Time: 3:08:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReviewItemDataNode extends DefaultMutableTreeNode {
	static final long serialVersionUID = -1192703287399203290L;

	private ReviewItem reviewItem;

	public ReviewItemDataNode(ReviewItem aReviewItemData) {
		this.reviewItem = aReviewItemData;
	}

	public ReviewItem getReviewItem() {
		return reviewItem;
	}

	public void setReviewItem(ReviewItem reviewItem) {
		this.reviewItem = reviewItem;
	}

	public String toString() {
		if (reviewItem.getFromPath().length() > 0 && reviewItem.getToPath().length() > 0) {
			return reviewItem.getFromPath() + " (mod)";

		} else if (reviewItem.getFromPath().length() > 0) {
			return reviewItem.getFromPath() + " (new)";
		} else if (reviewItem.getToPath().length() > 0) {
			return reviewItem.getToPath() + " (del)";
		} else {
			return "unknown";
		}		
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ReviewItemDataNode that = (ReviewItemDataNode) o;

		if (reviewItem.equals(that.reviewItem)) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		return reviewItem.hashCode();
	}
}

