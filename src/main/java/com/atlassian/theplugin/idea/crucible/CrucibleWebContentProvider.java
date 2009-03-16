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
import com.atlassian.theplugin.commons.crucible.api.content.ReviewFileContentProvider;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.intellij.openapi.vcs.vfs.VcsVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;

public class CrucibleWebContentProvider implements ReviewFileContentProvider {
	private final CrucibleFileInfo fileInfo;
	private final VirtualFile virtualFile;

	public CrucibleWebContentProvider(CrucibleFileInfo fileInfo, VirtualFile virtualFile) {
		this.fileInfo = fileInfo;
		this.virtualFile = virtualFile;
	}

	public CrucibleFileInfo getFileInfo() {
		return fileInfo;
	}

	public IdeaReviewFileContent getContent(final ReviewAdapter review,
			final VersionedVirtualFile versionedVirtualFile) throws ReviewFileContentException {
		try {
			byte[] content = CrucibleServerFacadeImpl.getInstance()
					.getFileContent(review.getServer(), versionedVirtualFile.getContentUrl());
			VirtualFile file = new VcsVirtualFile(versionedVirtualFile.getContentUrl(), content,
					versionedVirtualFile.getRevision(),
					virtualFile.getFileSystem());

			return new IdeaReviewFileContent(file, null);
		} catch (RemoteApiException e) {
			throw new ReviewFileContentException(e);
		} catch (ServerPasswordNotProvidedException e) {
			throw new ReviewFileContentException(e);
		}
	}
}