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

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.connector.intellij.crucible.content.ReviewFileContentException;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
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
    private final Project project;

    public CrucibleVcsContentProvider(Project project, CrucibleFileInfo fileInfo, VirtualFile virtualFile) {
        super(virtualFile, fileInfo);
        this.project = project;
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
            // PL-1678: kalamon - ugliest hack ever. On CVS, when a file is added on a branch, a revision 1.1
            // gets created on a trunk anyway, but it is a deleted revision, so IDEA (probably correctly)
            // claims it is unable to find it. The "proper" revision on the trunk has a rev. number
            // 1.1.2.1 (or somesuch, depending on the branch revision number. Additionally, CRU _does_
            // report that the file is added so in theory we don't need to retrieve rev. 1.1. Unfortunately,
            // CRU sometimes mishandled moved files, claiming they have been added in one place and
            // removed in the orther, so in practice, we need to treat this file as moved,
            // as it has a "from" revision.
            //
            // But like I said - this is CVS-specific hack that is uglier than Motorhead's Lemmy
            if ("CVS".equals(vcs.getDisplayName())
                    && "Revision 1.1 does not exist in repository".equals(e.getMessage())) {
                return null;
            }
			throw new ReviewFileContentException(e);
		}
	}
}