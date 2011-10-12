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

import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.exception.PatchCreateErrorException;
import com.atlassian.theplugin.idea.IdeaVersionFacade;
import com.atlassian.theplugin.idea.VcsIdeaHelper;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class CrucibleHelper {
	private CrucibleHelper() {
	}


	public static Collection<UploadItem> getUploadItemsFromChanges(final Project project, final Collection<Change> changes) {
		Collection<UploadItem> uploadItems = new ArrayList<UploadItem>();
		for (Change change : changes) {
			try {
				ContentRevision contentRevision;
				if (change.getBeforeRevision() != null) {
					contentRevision = change.getBeforeRevision();
				} else {
					//for added but not committed files after revision is available only
					contentRevision = change.getAfterRevision();
				}
				// PL-1619
				if (contentRevision == null) {
					continue;
				}

				String fileUrl = null;
				VirtualFile file = contentRevision.getFile().getVirtualFile();
				if (file == null) {
					file = contentRevision.getFile().getVirtualFileParent();
					if (file == null) {
						continue;
					}
					fileUrl = VcsIdeaHelper.getRepositoryUrlForFile(project, file);
					if (fileUrl != null) {
						fileUrl = fileUrl + "/" + contentRevision.getFile().getName();
					}
				} else {
					fileUrl = VcsIdeaHelper.getRepositoryUrlForFile(project, file);
				}

				try {
					URL url = new URL(fileUrl);
					fileUrl = url.getPath();
				} catch (MalformedURLException e) {
					String rootUrl = VcsIdeaHelper
							.getRepositoryRootUrlForFile(project, contentRevision.getFile().getVirtualFile());
					fileUrl = StringUtils.difference(rootUrl, fileUrl);
				}

				ContentRevision revOld = change.getBeforeRevision();
				ContentRevision revNew = change.getAfterRevision();

				byte[] byteOld = revOld != null && revOld.getContent() != null
						? revOld.getContent().getBytes() : new byte[0];
				byte[] byteNew = revNew != null && revNew.getContent() != null
						? revNew.getContent().getBytes() : new byte[0];

				// @todo implement it handling of binary files
				uploadItems.add(new UploadItem(fileUrl, byteOld, byteNew));

			} catch (VcsException e) {
				throw new RuntimeException(e);
			}
		}
		return uploadItems;
	}

	@SuppressWarnings("unchecked")
	public static String getPatchFromChanges(final Project project, final Collection<Change> changes)
			throws VcsException, IOException, PatchCreateErrorException {
		final StringWriter sw = new StringWriter();

		List list = IdeaVersionFacade.getInstance().buildPatch(project,
				changes, project != null
						&& project.getBaseDir() != null ? project.getBaseDir().getPresentableUrl() : "");
		if (list != null) {
			String s1 = CodeStyleSettingsManager.getInstance(project).getCurrentSettings().getLineSeparator();
			IdeaVersionFacade.getInstance().UnifiedDiffWriterWrite(project, list, sw, s1);
		}
		return sw.toString();
	}




}
