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

import com.atlassian.theplugin.commons.VersionedFileInfo;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;

import java.util.Date;
import java.util.List;

public interface CrucibleFileInfo extends VersionedFileInfo {
	VersionedVirtualFile getOldFileDescriptor();

	int getNumberOfComments() throws ValueNotYetInitialized;

	int getNumberOfDefects() throws ValueNotYetInitialized;

	PermId getPermId();

	List<VersionedComment> getVersionedComments() throws ValueNotYetInitialized;

	String getRepositoryName();

	FileType getFileType();

	String getAuthorName();

	Date getCommitDate();
}
