package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.crucible.api.content.ReviewFileContent;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * User: mwent
 * Date: Mar 13, 2009
 * Time: 12:20:40 PM
 */
public class IdeaReviewFileContent extends ReviewFileContent {
	private final VirtualFile virtualFile;

	public IdeaReviewFileContent(VirtualFile virtualFile, final byte[] content, final boolean revisionOnStorage) {
		super(content, revisionOnStorage);
		this.virtualFile = virtualFile;
	}

	public VirtualFile getVirtualFile() {
		return virtualFile;
	}
}
