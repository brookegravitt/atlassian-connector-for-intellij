package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.crucible.api.ReviewItemData;
import com.intellij.openapi.vfs.VirtualFile;

public class ReviewItemVirtualFile {
	private VirtualFile virtualFile;
	private ReviewItemData reviewItem;

	public ReviewItemVirtualFile() {
	}

	public ReviewItemVirtualFile(VirtualFile virtualFile, ReviewItemData reviewItem) {
		this.virtualFile = virtualFile;
		this.reviewItem = reviewItem;
	}

	public VirtualFile getVirtualFile() {
		return virtualFile;
	}

	public void setVirtualFile(VirtualFile virtualFile) {
		this.virtualFile = virtualFile;
	}

	public String getFromRevision() {
		return reviewItem.getFromRevision();
	}

	public String getToRevision() {
		return reviewItem.getToRevision();
	}

	public ReviewItemData getReviewItem() {
		return reviewItem;
	}
}
