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
    private boolean revisionOnLocalFS = false;

	public IdeaReviewFileContent(VirtualFile virtualFile, final byte[] content, final boolean revisionOnStorage) {
		super(content);
		this.virtualFile = virtualFile;
        this.revisionOnLocalFS = revisionOnStorage;
	}

	public VirtualFile getVirtualFile() {
		return virtualFile;
	}

      public boolean isRevisionOnLocalFS() {
        return revisionOnLocalFS;
    }
}
