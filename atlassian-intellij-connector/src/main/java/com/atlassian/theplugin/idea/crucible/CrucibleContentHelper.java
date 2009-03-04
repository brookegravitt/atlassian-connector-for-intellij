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

import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewItemContentType;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.crucible.editor.CommentHighlighter;
import com.atlassian.theplugin.idea.crucible.editor.OpenDiffAction;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.util.CodeNavigationUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.vfs.VcsVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.apache.commons.collections.map.ReferenceMap;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public final class CrucibleContentHelper {

	private CrucibleContentHelper() {
	}

	// CHECKSTYLE:OFF
	public static void openFileWithDiffs(final Project project, final boolean modal,
			@NotNull final ReviewAdapter review, @NotNull final CrucibleFileInfo reviewItem,
			final int line, final int col, @Nullable final OpenDiffAction action) {

		final PsiFile psiFile = CodeNavigationUtil
				.guessCorrespondingPsiFile(project, reviewItem.getFileDescriptor().getAbsoluteUrl());
		if (psiFile != null) {

			final VirtualFile vfl = psiFile.getVirtualFile();
			if (vfl != null) {
				ApplicationManager.getApplication().invokeLater(new Runnable() {
					public void run() {
						fetchAndOpenFileWithDiffs(project, modal, review, reviewItem, vfl, line, col, action);
					}
				});
				return;
			}
		}
		ApplicationManager.getApplication().invokeLater(new Runnable() {
			public void run() {
				Messages.showErrorDialog(project,
						"Your project does not contain requested file "
								+ reviewItem.getFileDescriptor().getAbsoluteUrl() + "."
						, "Your project does not contain requested file");
			}
		});
	}

	/**
	 * Must be run from UI thread!
	 *
	 * @param project		  project
	 * @param modal			modal window or task in status bar
	 * @param review		   review
	 * @param crucibleFileInfo review file
	 * @param virtualFile	  file to fetch from VCS
	 * @param line			 line to go to
	 * @param column		   column to go to
	 * @param action		   action to execute upon sucsessful completion of the fetching
	 */
	// CHECKSTYLE:OFF
	public static void fetchAndOpenFileWithDiffs(final Project project, final boolean modal,
			@NotNull final ReviewAdapter review, @NotNull final CrucibleFileInfo crucibleFileInfo,
			@NotNull final VirtualFile virtualFile,
			final int line, final int column, @Nullable final OpenDiffAction action) {
		// CHECKSTYLE:ON

		final String fromRevision = crucibleFileInfo.getOldFileDescriptor().getRevision();
		final String toRevision = crucibleFileInfo.getFileDescriptor().getRevision();

		String niceFileMessage;
		switch (crucibleFileInfo.getCommitType()) {
			case Added:
				niceFileMessage = " " + virtualFile.getName() + " (rev: " + toRevision + ") from Crucible";
				break;
			case Deleted:
				niceFileMessage = " " + virtualFile.getName() + " (rev: " + fromRevision + ") from Crucible";
				break;
			case Modified:
			case Moved:
			case Copied:
			case Unknown:
			default:
				niceFileMessage = "s " + virtualFile.getName() + " (rev: ";
				if (!StringUtils.isEmpty(fromRevision)) {
					niceFileMessage += fromRevision;
					if (!StringUtils.isEmpty(toRevision)) {
						niceFileMessage += ", ";
					}
				}
				if (!StringUtils.isEmpty(toRevision)) {
					niceFileMessage += " " + toRevision;
				}
				niceFileMessage += ") from Crucible";
				break;
		}

		new FetchingTwoFilesTask(project, modal, niceFileMessage, review, crucibleFileInfo, virtualFile, line, column, action)
				.queue();
	}

	private static class FetchingTwoFilesTask extends Task.Backgroundable {
		private OpenFileDescriptor displayDescriptor = null;
		private VirtualFile referenceVirtualFile = null;

		private VcsException exception;
		private final Project project;
		private final String niceFileMessage;
		private final ReviewAdapter review;
		private final CrucibleFileInfo crucibleFileInfo;
		private final VirtualFile virtualFile;
		private final String fromRevision;
		private final String toRevision;
		private final int line;
		private final int column;
		private final OpenDiffAction action;
		private final boolean modal;

		public FetchingTwoFilesTask(final Project project, final boolean modal, final String niceFileMessage,
				final ReviewAdapter review,
				final CrucibleFileInfo crucibleFileInfo, final VirtualFile virtualFile, final int line, final int column,
				final OpenDiffAction action) {
			super(project, "Fetching file" + niceFileMessage, false);
			this.project = project;
			this.niceFileMessage = niceFileMessage;
			this.review = review;
			this.crucibleFileInfo = crucibleFileInfo;
			this.fromRevision = crucibleFileInfo.getOldFileDescriptor().getRevision();
			this.toRevision = crucibleFileInfo.getFileDescriptor().getRevision();
			this.virtualFile = virtualFile;
			this.line = line;
			this.column = column;
			this.action = action;
			this.modal = modal;
		}

		@Override
		public boolean shouldStartInBackground() {
			return !modal;
		}

		@Override
		public void run(@NotNull ProgressIndicator indicator) {
			indicator.setIndeterminate(false);
			VirtualFile displayVirtualFile = null;
			try {
				switch (crucibleFileInfo.getCommitType()) {
					case Modified:
					case Moved:
					case Copied:
						if (!StringUtils.isEmpty(fromRevision)) {
							referenceVirtualFile = getCrucibleVirtualFile(virtualFile, review, crucibleFileInfo,
									fromRevision, ReviewItemContentType.OldContent);
						}
						displayVirtualFile = getCrucibleVirtualFile(virtualFile, review, crucibleFileInfo, toRevision,
								ReviewItemContentType.NewContent);
						break;
					case Added:
						displayVirtualFile = getCrucibleVirtualFile(virtualFile, review, crucibleFileInfo, toRevision,
								ReviewItemContentType.NewContent);
						break;
					case Deleted:
						referenceVirtualFile = getCrucibleVirtualFile(virtualFile, review, crucibleFileInfo, fromRevision,
								ReviewItemContentType.OldContent);
						break;
					default:
						break;
				}
				if (displayVirtualFile != null) {
					displayDescriptor = new OpenFileDescriptor(project, displayVirtualFile, line - 1, column);
				}
			} catch (VcsException e) {
				exception = e;
			}
		}

		@Override
		public void onSuccess() {
			if (exception != null) {
				DialogWithDetails.showExceptionDialog(project, "The following error has occured while fetching "
						+ niceFileMessage + ":\n" + exception.getMessage(), exception, "Error fetching file");
				return;
			}
			if (action != null) {
				action.run(displayDescriptor, referenceVirtualFile, crucibleFileInfo.getCommitType());
			}
		}
	}

	private static VirtualFile getFromCacheOrFetch(@NotNull VirtualFile virtualFile,
			ReviewAdapter review, CrucibleFileInfo crucibleFileInfo,
			@NotNull String revision,
			@NotNull ReviewItemContentType reviewItemContentType) throws VcsException {
		VirtualFile vcvf = getFileFromCache(virtualFile, revision);
		if (vcvf != null) {
			return vcvf;
		}

		try {
			String content = CrucibleServerFacadeImpl.getInstance().getFileContent(review.getServer(), crucibleFileInfo,
					reviewItemContentType);
			VirtualFile file = new VcsVirtualFile(crucibleFileInfo.getFileDescriptor().getName(), content.getBytes(), revision,
					virtualFile.getFileSystem());//PlainTextMemoryVirtualFile(crucibleFileInfo.getFileDescriptor().getName(),
			//content);
			putFileInfoCache(file, virtualFile, revision);

			return file;
		} catch (RemoteApiException e) {
			throw new RuntimeException(e);
		} catch (ServerPasswordNotProvidedException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private static final Map<String, VirtualFile> FETCHED_FILES_CACHE
			= Collections.<String, VirtualFile>synchronizedMap(new ReferenceMap());


	private static String getFileCacheKey(VirtualFile file, String revision) {
		return revision + ":" + file.getPath();
	}

	private static VirtualFile getFileFromCache(VirtualFile virtualFile, String revision) {
		String key = getFileCacheKey(virtualFile, revision);
		return FETCHED_FILES_CACHE.get(key);
	}

	private static void putFileInfoCache(VirtualFile file, VirtualFile virtualFile, String revision) {
		String key = getFileCacheKey(virtualFile, revision);
		FETCHED_FILES_CACHE.put(key, file);
	}

	@Nullable
	private static VirtualFile getCrucibleVirtualFile(VirtualFile virtualFile,
			ReviewAdapter review, CrucibleFileInfo crucibleFileInfo,
			String revision, ReviewItemContentType reviewItemContentType) throws VcsException {

		VirtualFile vcvf = getFromCacheOrFetch(virtualFile, review, crucibleFileInfo, revision, reviewItemContentType);
		if (vcvf != null && !FileDocumentManager.getInstance().isFileModified(virtualFile)) {
			try {
				byte[] currentContent = virtualFile.contentsToByteArray();
				if (Arrays.equals(currentContent, vcvf.contentsToByteArray())) {
					virtualFile.putUserData(CommentHighlighter.REVIEWITEM_CURRENT_CONTENT_KEY, Boolean.TRUE);
					return virtualFile;
				}
			} catch (IOException e) {
				return null;
			}

		}
		virtualFile.putUserData(CommentHighlighter.REVIEWITEM_CURRENT_CONTENT_KEY, Boolean.FALSE);
		return vcvf;
	}
}