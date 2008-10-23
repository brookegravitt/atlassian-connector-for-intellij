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

package com.atlassian.theplugin.commons.crucible.api.model;

import com.atlassian.theplugin.commons.VersionedVirtualFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CrucibleFileInfoImpl implements CrucibleFileInfo {
	private VersionedVirtualFile fileDescriptor;
	private VersionedVirtualFile oldFileDescriptor;
	private String repositoryName;
	private FileType fileType;
	private String authorName;
	private Date commitDate;
	private CommitType commitType;

	private PermId permId;

	private List<VersionedComment> versionedComments;

	public CrucibleFileInfoImpl(VersionedVirtualFile fileDescriptor, VersionedVirtualFile oldFileDescriptor,
								PermId permId) {
		this.fileDescriptor = fileDescriptor;
		this.oldFileDescriptor = oldFileDescriptor;
		this.permId = permId;
		versionedComments = new ArrayList<VersionedComment>();
	}

	public List<VersionedComment> getVersionedComments() {
		return versionedComments;
	}

	public void setVersionedComments(final List<VersionedComment> versionedComments) {
		this.versionedComments = versionedComments;
	}

	public int getNumberOfDefects() {
		if (versionedComments == null) {
			return 0;
		}
		int counter = 0;
		for (VersionedComment comment : versionedComments) {
			if (comment.isDefectApproved()) {
				++counter;
			}
		}
		return counter;
	}

	public int getNumberOfComments() {
		if (versionedComments == null) {
			return 0;
		}
		int n = versionedComments.size();
		for (VersionedComment c : versionedComments) {
			n += c.getReplies().size();
		}
		return n;
	}

//	public CrucibleFileInfoImpl(PermId permId) {
//		this(null, null);
//		this.permId = permId;
//	}

	public VersionedVirtualFile getOldFileDescriptor() {
		return oldFileDescriptor;
	}

	public PermId getPermId() {
		return permId;
	}

	public void setOldFileDescriptor(VersionedVirtualFile oldFileDescriptor) {
		this.oldFileDescriptor = oldFileDescriptor;
	}

	public void setFileDescriptor(VersionedVirtualFile fileDescriptor) {
		this.fileDescriptor = fileDescriptor;
	}

	public VersionedVirtualFile getFileDescriptor() {
		return fileDescriptor;
	}

	@Override
	public String toString() {
		VersionedVirtualFile oldFile = getOldFileDescriptor();
		VersionedVirtualFile newFile = getFileDescriptor();
		if (oldFile != null && oldFile.getUrl().length() > 0
				&& newFile != null && newFile.getUrl().length() > 0) {
			return oldFile.getUrl() + " (mod)";
		} else if (oldFile != null && oldFile.getUrl().length() > 0) {
			return newFile.getUrl() + " (del)";
		} else if (newFile != null && newFile.getUrl().length() > 0) {
			return oldFile.getUrl() + " (new)";
		} else {
			return "(unknown state)";
		}

	}

//	public void setVersionedComments(List<VersionedComment> commentList) {
//		versionedComments = commentList;
//	}
//
//	public void addVersionedComment(VersionedComment comment) {
//		versionedComments.add(comment);
//	}
//
//
//	public List<VersionedComment> getVersionedComments() throws ValueNotYetInitialized {
//		if (versionedComments == null) {
//			throw new ValueNotYetInitialized("Object trasferred only partially");
//		}
//		return versionedComments;
//	}

	public String getRepositoryName() {
		return repositoryName;
	}

	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	public FileType getFileType() {
		return fileType;
	}

	public void setFileType(final FileType fileType) {
		this.fileType = fileType;
	}

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(final String authorName) {
		this.authorName = authorName;
	}

	public Date getCommitDate() {
		return commitDate;
	}

	public void setCommitDate(final Date commitDate) {
		this.commitDate = commitDate;
	}

	public CommitType getCommitType() {
		return commitType;
	}

	public void addComment(final VersionedComment comment) {
		this.versionedComments.add(comment);
	}

	public void setCommitType(final CommitType commitType) {
		this.commitType = commitType;
	}

	public void setFilePermId(final PermIdBean permId) {
		this.permId = permId;
	}
}