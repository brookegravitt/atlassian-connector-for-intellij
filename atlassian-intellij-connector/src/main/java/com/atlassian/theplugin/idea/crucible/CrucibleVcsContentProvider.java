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
import com.atlassian.theplugin.commons.crucible.api.content.ReviewFileContentException;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.BinaryContentRevision;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.diff.DiffProvider;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.vfs.VcsVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;

public class CrucibleVcsContentProvider extends IdeaReviewFileContentProvider {

    public CrucibleVcsContentProvider(Project project, CrucibleFileInfo fileInfo, VirtualFile virtualFile) {
        super(project, virtualFile, fileInfo);
	}

    public IdeaReviewFileContent getContent(final ReviewAdapter review,
			final VersionedVirtualFile versionedVirtualFile) throws ReviewFileContentException {
		AbstractVcs vcs = VcsUtil.getVcsFor(project, virtualFile);
		if (vcs == null) {
			return null;
		}
		VcsRevisionNumber vcsRevisionNumber = vcs.parseRevisionNumber(versionedVirtualFile.getRevision());
		if (vcsRevisionNumber == null) {
			throw new ReviewFileContentException(
					"Cannot parse revision number [" + versionedVirtualFile.getRevision() + "] for file ["
							+ virtualFile.getPath() + "]");
		}

		DiffProvider diffProvider = vcs.getDiffProvider();
		if (diffProvider == null) {
			return null;
		}
		ContentRevision contentRevision = diffProvider.createFileContent(vcsRevisionNumber, virtualFile);
		if (contentRevision == null) {
			return null;
		}
		final byte[] content;
		try {
			if (contentRevision instanceof BinaryContentRevision) {
				content = ((BinaryContentRevision) contentRevision).getBinaryContent();
			} else {
				// this operation is typically quite costly
				final String strContent = contentRevision.getContent();
				content = (strContent != null) ? strContent.getBytes() : null;
			}

			VirtualFile file = new VcsVirtualFile(contentRevision.getFile().getPath(), content,
					contentRevision.getRevisionNumber().asString(),
					virtualFile.getFileSystem());

            return new IdeaReviewFileContent(file, content);

		} catch (VcsException e) {
			throw new ReviewFileContentException(e);
		}
	}
}