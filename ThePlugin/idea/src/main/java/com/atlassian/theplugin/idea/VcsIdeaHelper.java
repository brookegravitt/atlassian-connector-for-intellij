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

package com.atlassian.theplugin.idea;

import com.intellij.openapi.diff.DiffContent;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class VcsIdeaHelper {

	private VcsIdeaHelper() {
	}

	public static String getRepositoryUrlForFile(VirtualFile vFile) {
		ProjectLevelVcsManager plm = ProjectLevelVcsManager.getInstance(IdeaHelper.getCurrentProject());
		if (plm == null) {
			return null;
		}
		AbstractVcs vcs = plm.getVcsFor(vFile);
		if (vcs == null) {
			return null;
		}
		CommittedChangesProvider provider = vcs.getCommittedChangesProvider();
		if (provider == null) {
			return null;
		}
		RepositoryLocation repositoryLocation
				= provider.getLocationFor(VcsUtil.getFilePath(vFile.getPath()));
		if (repositoryLocation == null) {
			return null;
		}
		return repositoryLocation.toPresentableString();
	}

	public static List<VcsFileRevision> getFileHistory(VirtualFile vFile) throws VcsException {
		ProjectLevelVcsManager vcsPLM = ProjectLevelVcsManager.getInstance(IdeaHelper.getCurrentProject());

		if (vcsPLM != null) {


			return vcsPLM.getVcsFor(vFile).getVcsHistoryProvider().createSessionFor(
                    VcsUtil.getFilePath(vFile.getPath())).getRevisionList();
		} else {
			throw new VcsException("File: " + vFile.getPath() + " is not under VCS.");
		}
	}

	public static VcsFileRevision getFileRevision(VirtualFile vFile, String revision) {
		try {
			List<VcsFileRevision> revisions = getFileHistory(vFile);
			for (VcsFileRevision vcsFileRevision : revisions) {
				if (vcsFileRevision.getRevisionNumber().asString().equals((revision))) {
					return vcsFileRevision;
				}
			}
		} catch (VcsException e) {
			// nothing to do
		}
		return null;
	}


	public static List<VcsFileRevision> getFileRevisions(VirtualFile vFile, List<String> revisions) {
		List<VcsFileRevision> allRevisions;
		try {
			allRevisions = getFileHistory(vFile);
		} catch (VcsException e) {
			return Collections.EMPTY_LIST;
		}
		List<VcsFileRevision> returnRevision = new ArrayList<VcsFileRevision>(revisions.size());
		for (VcsFileRevision allRevision : allRevisions) {
			String rev = allRevision.getRevisionNumber().asString();
			for (String revision : revisions) {
				if (revision.equals(rev)) {
					returnRevision.add(allRevision);
				}
			}
		}
		return returnRevision;
	}

	public static DiffContent getFileRevisionContent(VirtualFile vFile, VcsFileRevision revNumber) {
		AbstractVcs vcs = ProjectLevelVcsManager.getInstance(IdeaHelper.getCurrentProject()).getVcsFor(vFile);
		VcsRevisionNumber rev = revNumber.getRevisionNumber();
		try {
			return com.intellij.openapi.diff.SimpleContent.fromBytes(vcs.getDiffProvider()
					.createFileContent(rev, vFile).getContent().getBytes(), vFile.getCharset().name(), vFile.getFileType());
		} catch (UnsupportedEncodingException e) {
			// nothing to do
		} catch (VcsException e) {
			// nothing to do
		}
		return null;
	}
}
