package com.atlassian.theplugin.idea.crucible;

import com.atlassian.connector.intellij.crucible.content.ReviewFileContentProvider;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.util.Arrays;

/**
 * User: pmaruszak
 * Date: May 22, 2009
 */
public abstract class IdeaReviewFileContentProvider implements ReviewFileContentProvider {
    protected final VirtualFile virtualFile;
    private CrucibleFileInfo fileInfo;

    protected IdeaReviewFileContentProvider(VirtualFile virtualFile, CrucibleFileInfo fileInfo) {
        this.virtualFile = virtualFile;
        this.fileInfo = fileInfo;
    }

    VirtualFile getVirtualFile() {
        return virtualFile;
    }
    

    public final boolean isContentIdentical(byte[] content) {
        try {
            return Arrays.equals(virtualFile.contentsToByteArray(), content);
        } catch (IOException e) {
            return false;
        }
    }

    public CrucibleFileInfo getFileInfo() {
        return fileInfo;
    }
}
