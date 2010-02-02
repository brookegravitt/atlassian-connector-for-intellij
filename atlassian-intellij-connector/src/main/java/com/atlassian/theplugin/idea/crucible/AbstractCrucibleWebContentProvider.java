package com.atlassian.theplugin.idea.crucible;

import com.atlassian.connector.intellij.crucible.IntelliJCrucibleServerFacade;
import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.connector.intellij.crucible.content.ReviewFileContentException;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.util.UrlUtil;
import com.intellij.openapi.vcs.vfs.VcsVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * User: kalamon
 * Date: Oct 16, 2009
 * Time: 3:53:03 PM
 */
public abstract class AbstractCrucibleWebContentProvider extends IdeaReviewFileContentProvider {

    protected AbstractCrucibleWebContentProvider(VirtualFile virtualFile,
                                                 CrucibleFileInfo fileInfo) {
        super(virtualFile, fileInfo);
    }

    protected abstract void setContent(byte[] content);

    public IdeaReviewFileContent getContent(final ReviewAdapter review,
                                            final VersionedVirtualFile versionedVirtualFile)
            throws ReviewFileContentException {

        try {

            // doggy workaround - PL-1287
            String serverUrl = review.getServerData().getUrl();
            String contentUrl = versionedVirtualFile.getContentUrl();

            // PL-1776
            contentUrl = URLDecoder.decode(contentUrl, "UTF-8");

            if (contentUrl == null) {
                return null;
            }

            contentUrl = UrlUtil.adjustUrlPath(contentUrl, serverUrl);

            byte[] content = IntelliJCrucibleServerFacade.getInstance().getFileContent(review.getServerData(), contentUrl);

            setContent(content);

            VirtualFile file = new VcsVirtualFile(versionedVirtualFile.getUrl(), content,
                    versionedVirtualFile.getRevision(), virtualFile.getFileSystem());

            return new IdeaReviewFileContent(file, content);
        } catch (RemoteApiException e) {
            throw new ReviewFileContentException(e);
        } catch (ServerPasswordNotProvidedException e) {
            throw new ReviewFileContentException(e);
        } catch (UnsupportedEncodingException e) {
            throw new ReviewFileContentException(e);
        }
    }

}
