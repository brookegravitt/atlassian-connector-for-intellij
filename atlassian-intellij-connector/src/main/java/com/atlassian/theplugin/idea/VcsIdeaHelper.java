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

import com.atlassian.theplugin.commons.crucible.api.model.CommitType;
import com.atlassian.theplugin.idea.crucible.editor.CommentHighlighter;
import com.atlassian.theplugin.idea.crucible.editor.OpenDiffAction;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.util.CodeNavigationUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.changes.BinaryContentRevision;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.diff.DiffProvider;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.vfs.VcsVirtualFile;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.peer.PeerFactory;
import com.intellij.psi.PsiFile;
import com.intellij.vcsUtil.VcsUtil;
import org.apache.commons.collections.map.ReferenceMap;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class VcsIdeaHelper {

	@SuppressWarnings("unchecked")
	private static final Map<String, VirtualFile> FETCHED_FILES_CACHE
			= Collections.<String, VirtualFile>synchronizedMap(new ReferenceMap());

	private VcsIdeaHelper() {
	}

	public static boolean isUnderVcsControl(DataContext context) {
		return isUnderVcsControl(IdeaHelper.getCurrentProject(context));
	}

	public static boolean isUnderVcsControl(AnActionEvent action) {
		return isUnderVcsControl(action.getDataContext());
	}

	public static boolean isUnderVcsControl(Project project) {
		ProjectLevelVcsManager plm = ProjectLevelVcsManager.getInstance(project);

		return (plm != null && plm.getAllActiveVcss().length > 0);
	}

	public static String getRepositoryRootUrlForFile(Project project, VirtualFile vFile) {
		ProjectLevelVcsManager plm = ProjectLevelVcsManager.getInstance(project);
		if (plm == null) {
			return null;
		}
		VirtualFile vcsRoot = plm.getVcsRootFor(vFile);
		return getRepositoryUrlForFile(project, vcsRoot);
	}

	@Nullable
	public static String getRepositoryUrlForFile(Project project, VirtualFile vFile) {
		ProjectLevelVcsManager plm = ProjectLevelVcsManager.getInstance(project);
		if (plm == null) {
			return null;
		}

		AbstractVcs vcs = plm.getVcsFor(vFile);
		if (vcs == null) {
			return null;
		}
		CommittedChangesProvider<?, ?> provider = vcs.getCommittedChangesProvider();
		if (provider == null) {
			return null;
		}
		RepositoryLocation repositoryLocation = provider.getLocationFor(VcsUtil.getFilePath(vFile.getPath()));
		if (repositoryLocation == null) {
			return null;
		}
		return repositoryLocation.toPresentableString();
	}

	public static List<VcsFileRevision> getFileHistory(Project project, VirtualFile vFile) throws VcsException {
		ProjectLevelVcsManager vcsPLM = ProjectLevelVcsManager.getInstance(project);

		if (vcsPLM != null) {
			return vcsPLM.getVcsFor(vFile).getVcsHistoryProvider().createSessionFor(
					VcsUtil.getFilePath(vFile.getPath())).getRevisionList();
		} else {
			throw new VcsException("File: " + vFile.getPath() + " is not under VCS.");
		}
	}

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


	private static VirtualFile getFromCacheOrFetch(@NotNull Project project, @NotNull VirtualFile virtualFile,
			@NotNull String revision) throws VcsException {
		VirtualFile vcvf = getFileFromCache(virtualFile, revision);
		if (vcvf != null) {
			return vcvf;
		}
//		System.out.println("NOT FOUND IN CACHE");
		AbstractVcs vcs = VcsUtil.getVcsFor(project, virtualFile);
		if (vcs == null) {
			return null;
		}
		VcsRevisionNumber vcsRevisionNumber = vcs.parseRevisionNumber(revision);
		if (vcsRevisionNumber == null) {
			throw new VcsException("Cannot parse revision number [" + revision + "] for file ["
					+ virtualFile.getPath() + "]");
		}
		final VirtualFile remoteFile = getVcsVirtualFileImpl2(virtualFile, vcs, vcsRevisionNumber);
		if (remoteFile != null) {
			putFileInfoCache(remoteFile, virtualFile, revision);
		}
		return remoteFile;
	}

	@Nullable
	private static VirtualFile getVcsVirtualFile(Project project, VirtualFile virtualFile,
			String revision) throws VcsException {

		VirtualFile vcvf = getFromCacheOrFetch(project, virtualFile, revision);
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

	public static boolean isFileDirty(final Project project, final VirtualFile virtualFile) {
		return ChangeListManager.getInstance(project).getAffectedFiles().contains(virtualFile);
	}


	@Nullable
	private static VirtualFile getVcsVirtualFileImpl2(@NotNull VirtualFile virtualFile, @NotNull AbstractVcs vcs,
			@NotNull VcsRevisionNumber vcsRevisionNumber) throws VcsException {

		DiffProvider diffProvider = vcs.getDiffProvider();
		if (diffProvider == null) {
			return null;
		}
		ContentRevision contentRevision = diffProvider.createFileContent(vcsRevisionNumber, virtualFile);
		if (contentRevision == null) {
			return null;
		}
		final byte[] content;
		if (contentRevision instanceof BinaryContentRevision) {
			content = ((BinaryContentRevision) contentRevision).getBinaryContent();
		} else {
			// this operation is typically quite costly
			final String strContent = contentRevision.getContent();
			content = (strContent != null) ? strContent.getBytes() : null;
		}
		if (content == null) {
			return null;
		}
		return new VcsVirtualFile(contentRevision.getFile().getPath(), content,
				vcsRevisionNumber.asString(), virtualFile.getFileSystem());
	}

	/**
	 * Must be run from UI thread!
	 *
	 * @param project	 project
	 * @param revision	VCS revision of the file
	 * @param virtualFile file to fetch from VCS
	 * @param line		line to go to
	 * @param column	  column to go to
	 * @param action
	 */
	private static void fetchAndOpenFile(final Project project, final String revision, @NotNull final VirtualFile virtualFile,
			final int line, final int column, @Nullable final OpenFileDescriptorAction action) {
		final String niceFileMessage = virtualFile.getName() + " (rev: " + revision + ") from VCS";
		new FetchingFileTask(project, niceFileMessage, virtualFile, revision, line, column, action).queue();
	}

	/**
	 * Must be run from UI thread!
	 *
	 * @param project	  project
	 * @param fromRevision start VCS revision of the file
	 * @param toRevision   to VCS revision of the file
	 * @param commitType
	 * @param virtualFile  file to fetch from VCS
	 * @param line		 line to go to
	 * @param column	   column to go to
	 * @param action	   action to execute upon sucsessful completion of the fetching
	 */
	// CHECKSTYLE:OFF
	public static void fetchAndOpenFileWithDiffs(final Project project, final boolean modal, final String fromRevision,
			final String toRevision,
			@NotNull final CommitType commitType, @NotNull final VirtualFile virtualFile,
			final int line, final int column, @Nullable final OpenDiffAction action) {
		// CHECKSTYLE:ON

		String niceFileMessage;
		switch (commitType) {
			case Added:
				niceFileMessage = " " + virtualFile.getName() + " (rev: " + toRevision + ") from VCS";
				break;
			case Deleted:
				niceFileMessage = " " + virtualFile.getName() + " (rev: " + fromRevision + ") from VCS";
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
				niceFileMessage += ") from VCS";
				break;
		}

		new FetchingTwoFilesTask(project, modal, niceFileMessage, commitType, virtualFile, fromRevision,
				toRevision, line, column, action).queue();
	}

	/**
	 * Is file is currently open it does not try to refetch it
	 *
	 * @param project
	 * @param filePath
	 * @param fileRevision
	 * @param line
	 * @param col
	 * @param action
	 */
	public static void openFile(final Project project, String filePath, @NotNull final String fileRevision,
			final int line, final int col, @Nullable final OpenFileDescriptorAction action) {

		VirtualFile baseDir = project.getBaseDir();
		String baseUrl = getRepositoryUrlForFile(project, baseDir);

		if (baseUrl != null && filePath.startsWith(baseUrl)) {
			String relUrl = filePath.substring(baseUrl.length());
			final VirtualFile vfl = VfsUtil.findRelativeFile(relUrl, baseDir);
			ApplicationManager.getApplication().invokeLater(new Runnable() {
				public void run() {
					fetchAndOpenFile(project, fileRevision, vfl, line, col, action);
				}
			});
		}

	}

	// CHECKSTYLE:OFF
	public static void openFileWithDiffs(final Project project, final boolean modal, final String filePath,
			@NotNull final String fileRevision, final String toRevision,
			@NotNull final CommitType commitType,
			final int line, final int col, @Nullable final OpenDiffAction action) {
		// CHECKSTYLE:ON

		final PsiFile psiFile = CodeNavigationUtil.guessCorrespondingPsiFile(project, filePath);
		if (psiFile != null) {

			final VirtualFile vfl = psiFile.getVirtualFile();
			if (vfl != null) {
				ApplicationManager.getApplication().invokeLater(new Runnable() {
					public void run() {
						fetchAndOpenFileWithDiffs(project, modal, fileRevision, toRevision, commitType, vfl, line, col, action);
					}
				});
				return;
			}
		}

		ApplicationManager.getApplication().invokeLater(new Runnable() {
			public void run() {
				switch (commitType) {
					case Deleted:
						Messages.showErrorDialog(project,
								"Your project does not contain requested file " + filePath + ". Please update to revision "
										+ fileRevision + " before review", "File removed from repository");

						break;
					case Added:
						Messages.showErrorDialog(project,
								"Your project does not contain requested file " + filePath + ". Please update to revision "
										+ toRevision + " before review", "Project out of date");
						break;
					default:
						Messages.showErrorDialog(project, "Please update your project to revision "
								+ toRevision + " before opening review as the file " + filePath
								+ " could have been moved or deleted meanwhile",
								"Your project does not contain requested file");
						break;
				}
			}
		});
	}

	public static void openFile(final Project project, @NotNull final VirtualFile virtualFile,
			@NotNull final String fileRevision, final int line, final int col,
			@Nullable final OpenFileDescriptorAction action) {

		// do we have the same file revision opened in our project?
		ApplicationManager.getApplication().invokeLater(new Runnable() {
			public void run() {
				fetchAndOpenFile(project, fileRevision, virtualFile, line, col, action);
			}
		});
	}

	public interface OpenFileDescriptorAction {
		/**
		 * Will be always invoked in UI thread
		 *
		 * @param ofd description which will be passed to this action
		 */
		void run(OpenFileDescriptor ofd);

		boolean shouldNavigate();
	}

	private static class FetchingTwoFilesTask extends Task.Backgroundable {
		private OpenFileDescriptor displayDescriptor;
		private VirtualFile referenceVirtualFile;

		private VcsException exception;
		private final Project project;
		private final String niceFileMessage;
		private final CommitType commitType;
		private final VirtualFile virtualFile;
		private final String fromRevision;
		private final String toRevision;
		private final int line;
		private final int column;
		private final OpenDiffAction action;
		private final boolean modal;

		public FetchingTwoFilesTask(final Project project, final boolean modal, final String niceFileMessage,
				final CommitType commitType, final VirtualFile virtualFile, final String fromRevision,
				final String toRevision, final int line, final int column,
				final OpenDiffAction action) {
			super(project, "Fetching file" + niceFileMessage, false);
			this.project = project;
			this.niceFileMessage = niceFileMessage;
			this.commitType = commitType;
			this.virtualFile = virtualFile;
			this.fromRevision = fromRevision;
			this.toRevision = toRevision;
			this.line = line;
			this.column = column;
			this.action = action;
			this.modal = modal;
			displayDescriptor = null;
			referenceVirtualFile = null;
		}

		@Override
		public boolean shouldStartInBackground() {
			return !modal;
		}

		@Override
		public void run(ProgressIndicator indicator) {
			indicator.setIndeterminate(false);
			VirtualFile displayVirtualFile = null;
			try {
				switch (commitType) {
					case Modified:
					case Moved:
					case Copied:
						if (!StringUtils.isEmpty(fromRevision)) {
							referenceVirtualFile = getVcsVirtualFile(project, virtualFile, fromRevision);
						}
						displayVirtualFile = getVcsVirtualFile(project, virtualFile, toRevision);
						break;
					case Added:
						displayVirtualFile = getVcsVirtualFile(project, virtualFile, toRevision);
						break;
					case Deleted:
						referenceVirtualFile = getVcsVirtualFile(project, virtualFile, fromRevision);
						break;
					default:
						break;
				}
				if (displayVirtualFile != null) {
					displayDescriptor = new OpenFileDescriptor(project, displayVirtualFile, line - 1, column);
				} else {
					// we cannot open such file (just for clarity as by default displayDescriptor = null)
					displayDescriptor = null;
				}
			} catch (VcsException e) {
				exception = e;
			}
		}

		@Override
		public void onSuccess() {
			if (exception != null) {
				DialogWithDetails.showExceptionDialog(project, "The following error has occured while fetching "
						+ niceFileMessage + ":\n" + exception.getMessage(), exception);
				return;
			}
			if (action != null) {
				action.run(displayDescriptor, referenceVirtualFile, commitType);
			}
		}
	}

	private static class FetchingFileTask extends Task.Backgroundable {

		private OpenFileDescriptor ofd;

		private VcsException exception;
		private final Project project;
		private final String niceFileMessage;
		private final VirtualFile virtualFile;
		private final String revision;
		private final int line;
		private final int column;
		private final OpenFileDescriptorAction action;

		public FetchingFileTask(final Project project, final String niceFileMessage, final VirtualFile virtualFile,
				final String revision,
				final int line, final int column, final OpenFileDescriptorAction action) {
			super(project, "Fetching file " + niceFileMessage, false);
			this.project = project;
			this.niceFileMessage = niceFileMessage;
			this.virtualFile = virtualFile;
			this.revision = revision;
			this.line = line;
			this.column = column;
			this.action = action;
		}

		@Override
		public boolean shouldStartInBackground() {
			return false;
		}

		@Override
		public void run(ProgressIndicator indicator) {

			indicator.setIndeterminate(true);
			final VirtualFile myVirtualFile;
			try {
				myVirtualFile = getVcsVirtualFile(project, virtualFile, revision);
			} catch (VcsException e) {
				exception = e;
				return;
			}
			if (myVirtualFile != null) {
				ofd = new OpenFileDescriptor(project, myVirtualFile, line, column);
			}
		}

		@Override
		public void onSuccess() {
			if (exception != null) {
				DialogWithDetails.showExceptionDialog(project, "The following error has occured while fetching "
						+ niceFileMessage + ":\n" + exception.getMessage(), exception);
				return;
			}
			if (ofd != null) {
				if (action != null) {
					action.run(ofd);
					if (action.shouldNavigate()) {
						ofd.navigate(true);
					}
				} else {
					ofd.navigate(true);
				}
			}

		}
	}


	@Nullable
	public static VcsRevisionNumber getVcsRevisionNumber(@NotNull final Project project,
			@NotNull final VirtualFile virtualFile) {
		AbstractVcs vcs = VcsUtil.getVcsFor(project, virtualFile);
		if (vcs == null) {
			return null;
		}
		VcsContextFactory vcsContextFactory = PeerFactory.getInstance().getVcsContextFactory();
		final FilePath filePath = vcsContextFactory.createFilePathOn(virtualFile);
		if (filePath == null || !vcs.fileIsUnderVcs(filePath)) {
			return null;
		}

		DiffProvider diffProvider = vcs.getDiffProvider();
		if (diffProvider == null) {
			return null;
		}
		return diffProvider.getCurrentRevision(virtualFile);
	}
}
