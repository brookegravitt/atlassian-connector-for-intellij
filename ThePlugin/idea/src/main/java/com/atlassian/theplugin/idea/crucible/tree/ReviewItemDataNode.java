package com.atlassian.theplugin.idea.crucible.tree;

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

	@Override
	public String toString() {
		return this.reviewItem.toString();
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

