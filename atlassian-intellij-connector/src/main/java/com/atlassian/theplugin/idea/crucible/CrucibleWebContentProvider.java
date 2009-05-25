/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.api.content.ReviewFileContentException;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.vfs.VcsVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;

public class CrucibleWebContentProvider extends IdeaReviewFileContentProvider {

	public CrucibleWebContentProvider(CrucibleFileInfo fileInfo, VirtualFile virtualFile, final Project project) {
        super(project, virtualFile, fileInfo);
    }

    public IdeaReviewFileContent getContent(final ReviewAdapter review,
                                            final VersionedVirtualFile versionedVirtualFile) throws ReviewFileContentException {
        try {

            // doggy workaround - PL-1287
            String serverUrl = review.getServerData().getUrl();
            String contentUrl = versionedVirtualFile.getContentUrl();

            String[] serverTokens = serverUrl.split("/");
            String[] contentTokens = contentUrl.split("/");

            if (serverTokens.length > 0 && contentTokens.length > 0) {
                if (serverTokens[serverTokens.length - 1].equals(contentTokens[0])) {
                    contentUrl = contentUrl.substring(contentTokens[0].length(), contentUrl.length());
                }
            }
            byte[] content = CrucibleServerFacadeImpl.getInstance()
                    .getFileContent(review.getServerData(), contentUrl);

            VirtualFile file = new VcsVirtualFile(versionedVirtualFile.getUrl(), content,
                    versionedVirtualFile.getRevision(),
                    virtualFile.getFileSystem());

            return new IdeaReviewFileContent(file, content);
        } catch (RemoteApiException e) {
            throw new ReviewFileContentException(e);
        } catch (ServerPasswordNotProvidedException e) {
            throw new ReviewFileContentException(e);
        }
    }
}