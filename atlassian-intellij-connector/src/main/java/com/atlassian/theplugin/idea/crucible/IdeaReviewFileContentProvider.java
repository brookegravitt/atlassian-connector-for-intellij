package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.crucible.api.content.ReviewFileContentProvider;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * User: pmaruszak
 * Date: May 22, 2009
 */
public interface IdeaReviewFileContentProvider extends ReviewFileContentProvider {
    VirtualFile getVirtualFile();
}
