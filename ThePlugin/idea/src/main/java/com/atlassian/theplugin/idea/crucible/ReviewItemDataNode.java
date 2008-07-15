package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;

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

	private CrucibleFileInfo file;

	public ReviewItemDataNode(CrucibleFileInfo aReviewItemData) {
		this.file = aReviewItemData;
	}

	public CrucibleFileInfo getFile() {
		return file;
	}

	public void setFile(CrucibleFileInfo file) {
		this.file = file;
	}

	public String toString() {
		return file.toString();
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ReviewItemDataNode that = (ReviewItemDataNode) o;

		if (file.equals(that.file)) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		return file.hashCode();
	}
}

