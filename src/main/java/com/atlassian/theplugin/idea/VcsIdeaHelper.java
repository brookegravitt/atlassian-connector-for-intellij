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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diff.DiffContent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.diff.DiffProvider;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.vfs.AbstractVcsVirtualFile;
import com.intellij.openapi.vcs.vfs.ContentRevisionVirtualFile;
import com.intellij.openapi.vcs.vfs.VcsVirtualFile;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.UnsupportedEncodingException;
import java.util.*;

public final class VcsIdeaHelper {

	private static Map<String, AbstractVcsVirtualFile> fetchedFiles = new HashMap<String, AbstractVcsVirtualFile>();

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


	@Nullable
	public static String getRepositoryUrlForFile(VirtualFile vFile) {
		ProjectLevelVcsManager plm = ProjectLevelVcsManager.getInstance(IdeaHelper.getCurrentProject());
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

	public static List<VcsFileRevision> getFileRevisions(VirtualFile vFile, List<String> revisions) {
		List<VcsFileRevision> allRevisions;
		try {
			allRevisions = getFileHistory(vFile);
		} catch (VcsException e) {
			return Collections.emptyList();
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

	@Nullable
	public static AbstractVcsVirtualFile getVcsVirtualFile(Project project, VirtualFile virtualFile,
			String revision, boolean loadLazily) throws VcsException {
		findOpenFile(project, virtualFile, revision);
		String key = getFileCacheKey(virtualFile, revision);
		if (fetchedFiles.containsKey(key)) {
			return fetchedFiles.get(key);
		} else {
			AbstractVcs vcs = VcsUtil.getVcsFor(project, virtualFile);
			if (vcs == null) {
				return null;
			}
			VcsRevisionNumber vcsRevisionNumber = vcs.parseRevisionNumber(revision);
			AbstractVcsVirtualFile vcvf = getVcsVirtualFileImpl2(virtualFile, vcs, vcsRevisionNumber, loadLazily);
			fetchedFiles.put(key, vcvf);
			return vcvf;
		}
	}

	@Nullable
	private static AbstractVcsVirtualFile getVcsVirtualFileImpl(VirtualFile virtualFile, AbstractVcs vcs,
			VcsRevisionNumber vcsRevisionNumber, boolean loadLazily) throws VcsException {

		ContentRevision contentRevision = vcs.getDiffProvider().createFileContent(vcsRevisionNumber, virtualFile);
		if (contentRevision == null) {
			return null;
		}
		if (loadLazily == false) {
			// this operation is typically quite costly
			contentRevision.getContent();
		}
		return ContentRevisionVirtualFile.create(contentRevision);
	}

	@Nullable
	private static VcsVirtualFile getVcsVirtualFileImpl2(VirtualFile virtualFile, AbstractVcs vcs,
			VcsRevisionNumber vcsRevisionNumber, boolean loadLazily) throws VcsException {

		DiffProvider diffProvider = vcs.getDiffProvider();
		if (diffProvider == null) {
			return null;
		}
		ContentRevision contentRevision = diffProvider.createFileContent(vcsRevisionNumber, virtualFile);
		if (contentRevision == null) {
			return null;
		}
		if (loadLazily == false) {
			// this operation is typically quite costly
			contentRevision.getContent();
		}
		//ContentRevisionVirtualFile vcvf = ContentRevisionVirtualFile.create(contentRevision);
		return new VcsVirtualFile(contentRevision.getFile().getPath(), contentRevision.getContent().getBytes(),
				vcsRevisionNumber.asString(), virtualFile.getFileSystem());
	}


	public static AbstractVcsVirtualFile getVcsVirtualFile(Project project, VirtualFile virtualFile,
			VcsRevisionNumber vcsRevisionNumber, boolean loadLazily) throws VcsException {

		AbstractVcs vcs = VcsUtil.getVcsFor(project, virtualFile);
		return getVcsVirtualFileImpl(virtualFile, vcs, vcsRevisionNumber, loadLazily);
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
	public static void fetchAndOpenFile(final Project project, final String revision, @NotNull final VirtualFile virtualFile,
			final int line, final int column, @Nullable final OpenFileDescriptorAction action) {

		final String niceFileMessage = virtualFile.getName() + " (rev: " + revision + ") from VCS";
		new Task.Backgroundable(project, "Fetching file " + niceFileMessage, false) {

			private OpenFileDescriptor ofd;

			private VcsException exception;

			@Override
			public void run(ProgressIndicator indicator) {
				final AbstractVcsVirtualFile vcvf;
				try {
					vcvf = getVcsVirtualFile(project, virtualFile, revision, false);
				} catch (VcsException e) {
					exception = e;
					return;
				}
				ofd = new OpenFileDescriptor(project, vcvf, line, column);
			}

			@Override
			public void onSuccess() {
				if (exception != null) {
					Messages.showErrorDialog(project, "The following error has occured while fetching "
							+ niceFileMessage + ":\n" + exception.getMessage(), "Error fetching file");
					return;
				}
				if (ofd != null) {
					if (action != null) {
						action.run(ofd);
					}
					ofd.navigate(true);
				}

			}
		}.queue();
	}

	/**
	 * Must be run from UI thread!
	 * {
	 *
	 * @param project	  project
	 * @param fromRevision start VCS revision of the file
	 * @param toRevision   to VCS revision of the file
	 * @param virtualFile  file to fetch from VCS
	 * @param line		 line to go to
	 * @param column	   column to go to
	 * @param action
	 */
	public static void fetchAndOpenFileWithDiffs(final Project project, final String fromRevision, final String toRevision,
			@NotNull final VirtualFile virtualFile,
			final int line, final int column, @Nullable final OpenDiffAction action) {

		final String niceFileMessage = virtualFile.getName() + " (rev: " + fromRevision + ", " + toRevision + ") from VCS";
		new Task.Backgroundable(project, "Fetching files " + niceFileMessage, false) {
			private OpenFileDescriptor displayDescriptor;
			private AbstractVcsVirtualFile referenceVirtualFile;

			private VcsException exception;

			@Override
			public void run(ProgressIndicator indicator) {
				final AbstractVcsVirtualFile displayVirtualFile;
				try {
					referenceVirtualFile = getVcsVirtualFile(project, virtualFile, fromRevision, false);
					displayVirtualFile = getVcsVirtualFile(project, virtualFile, toRevision, false);
				} catch (VcsException e) {
					exception = e;
					return;
				}
				displayDescriptor = new OpenFileDescriptor(project, displayVirtualFile, line, column);
			}

			@Override
			public void onSuccess() {
				if (exception != null) {
					Messages.showErrorDialog(project, "The following error has occured while fetching "
							+ niceFileMessage + ":\n" + exception.getMessage(), "Error fetching file");
					return;
				}
				if (displayDescriptor != null) {
					if (action != null) {
						action.run(displayDescriptor, referenceVirtualFile);
					}
					displayDescriptor.navigate(true);
				}

			}
		}.queue();
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
		String baseUrl = getRepositoryUrlForFile(baseDir);

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
	public static void openFileWithDiffs(final Project project, String filePath, @NotNull final String fileRevision,
			final String toRevision,
			final int line, final int col, @Nullable final OpenDiffAction action) {
		                             ?
		VirtualFile baseDir = project.getBaseDir();
		String baseUrl = getRepositoryUrlForFile(baseDir);

		if (baseUrl != null && filePath.startsWith(baseUrl)) {
			String relUrl = filePath.substring(baseUrl.length());
			final VirtualFile vfl = VfsUtil.findRelativeFile(relUrl, baseDir);
			if (vfl != null) {
				ApplicationManager.getApplication().invokeLater(new Runnable() {
					public void run() {
						fetchAndOpenFileWithDiffs(project, fileRevision, toRevision, vfl, line, col, action);
					}
				});
			} else {
				ApplicationManager.getApplication().invokeLater(new Runnable() {
					public void run() {
						Messages.showErrorDialog(project,
								"Your project does not contain requested file. Please update before review",
								"Project out of date");
					}
				});

			}
		}

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

	private static String getFileCacheKey(VirtualFile file, String revision) {
		return revision + ":" + file.getPath();
	}

	@Nullable
	private static VirtualFile findOpenFile(Project project, VirtualFile virtualFile, String fileRevision) {
		final VirtualFile[] openFiles = FileEditorManager.getInstance(project).getOpenFiles();
		if (virtualFile != null) {
			for (VirtualFile file : openFiles) {
				if (virtualFile.getPath().equals(file.getPath())) {
					if (file instanceof VcsVirtualFile) {
						VcsVirtualFile vcsVirtualFile = (VcsVirtualFile) file;
						if (fileRevision.equals(vcsVirtualFile.getRevision())) {
							fetchedFiles.put(getFileCacheKey(vcsVirtualFile, fileRevision), vcsVirtualFile);
							return file;
						}
					}
				}
			}
		}
		return null;
	}

	public interface OpenFileDescriptorAction {
		/**
		 * Will be always invoked in UI thread
		 *
		 * @param ofd description which will be passed to this action
		 */
		void run(OpenFileDescriptor ofd);
	}

	public interface OpenDiffAction {
		/**
		 * Open file view based on two file revisions
		 * Will be always invoked in UI thread
		 *
		 * @param displayFile
		 * @param referenceDocument
		 */
		void run(OpenFileDescriptor displayFile, VirtualFile referenceDocument);
	}

}
