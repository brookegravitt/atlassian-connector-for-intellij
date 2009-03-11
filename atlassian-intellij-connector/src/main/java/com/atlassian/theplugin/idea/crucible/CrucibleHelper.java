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
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldDef;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.VcsIdeaHelper;
import com.atlassian.theplugin.idea.crucible.editor.CommentHighlighter;
import com.atlassian.theplugin.idea.crucible.editor.OpenCrucibleDiffToolAction;
import com.atlassian.theplugin.idea.crucible.editor.OpenDiffAction;
import com.atlassian.theplugin.idea.crucible.editor.OpenEditorDiffActionImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class CrucibleHelper {
	private CrucibleHelper() {
	}

	/**
	 * Shows virtual file taken from repository in Idea Editor.
	 * Higlights all versioned comments for given file
	 * Adds StripeMark on the right side of file window with set tool tip text that corresponde
	 * to VersionedComment.getMessage content
	 * Note: must be run from event dispatch thread or inside read-action only!
	 *
	 * @param project	project
	 * @param review	 review data
	 * @param reviewItem review item
	 */
	public static void showVirtualFileWithComments(final Project project,
			final ReviewAdapter review,
			final CrucibleFileInfo reviewItem) {
		openFileWithDiffs(project
				, true
				, review
				, reviewItem
				, 1
				, 1
				, new OpenEditorDiffActionImpl(project, review, reviewItem, true));
	}

	public static void showRevisionDiff(final Project project, final ReviewAdapter review, final CrucibleFileInfo reviewItem) {
		openFileWithDiffs(project
				, true
				, review
				, reviewItem
				, 1
				, 1
				, new OpenCrucibleDiffToolAction(project, reviewItem, "", ""));
	}

	public static void openFileOnComment(final Project project, final ReviewAdapter review, final CrucibleFileInfo file,
			final VersionedComment comment) {

		ApplicationManager.getApplication().runReadAction(new Runnable() {
			public void run() {
				int line;
				switch (file.getCommitType()) {
					case Deleted:
						line = comment.getFromStartLine() - 1;
						break;
					default:
						line = comment.getToStartLine() - 1;
						break;
				}

				openFileWithDiffs(project
						, true
						, review
						, file
						, comment.getToStartLine()
						, 0
						, new OpenEditorDiffActionImpl(project, review, file, line, 0, true)
				);
			}
		});
	}

	// CHECKSTYLE:OFF
	public static void openFileWithDiffs(final Project project, final boolean modal,
			@NotNull final ReviewAdapter review, @NotNull final CrucibleFileInfo reviewItem,
			final int line, final int col, @Nullable final OpenDiffAction action) {
		// CHECKSTYLE:ON

		switch (reviewItem.getRepositoryType()) {
			case SCM:
				boolean contentUrlAvailable = false;
				try {
					contentUrlAvailable = CrucibleServerFacadeImpl.getInstance().checkContentUrlAvailable(review.getServer());
				} catch (RemoteApiException e) {
					// unable to get version
				} catch (ServerPasswordNotProvidedException e) {
					// unable to get version					
				}

				if (contentUrlAvailable) {
					CrucibleContentHelper.openFileWithDiffs(project, modal, review, reviewItem, line, col, action);
				} else {
					final String filename = reviewItem.getFileDescriptor().getAbsoluteUrl();
					final String fileRevision = reviewItem.getOldFileDescriptor().getRevision();
					final String toRevision = reviewItem.getFileDescriptor().getRevision();
					VcsIdeaHelper.openFileWithDiffs(project
							, modal
							, filename
							, fileRevision
							, toRevision
							, reviewItem.getCommitType()
							, 1
							, 1
							, action);
				}
				break;
			case UPLOAD:
				CrucibleContentHelper.openFileWithDiffs(project, modal, review, reviewItem, line, col, action);
				break;
			case PATCH:
				Messages.showErrorDialog(project, "Reviews based on patch upload are not supported", "Review not supported");
				break;
			default:
				break;
		}
	}

	public static List<CustomFieldDef> getMetricsForReview(@NotNull final Project project,
			@NotNull final ReviewAdapter review) {
		java.util.List<CustomFieldDef> metrics = new ArrayList<CustomFieldDef>();
		try {
			metrics = CrucibleServerFacadeImpl.getInstance()
					.getMetrics(review.getServer(), review.getMetricsVersion());
		} catch (RemoteApiException e) {
			IdeaHelper.handleRemoteApiException(project, e);
		} catch (ServerPasswordNotProvidedException e) {
			IdeaHelper.handleMissingPassword(e);
		}
		return metrics;
	}

	public static Editor getEditorForCrucibleFile(ReviewAdapter review, CrucibleFileInfo file) {
		Editor[] editors = EditorFactory.getInstance().getAllEditors();
		for (Editor editor : editors) {
			final Document document = editor.getDocument();
			if (document != null) {
				final VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
				if (virtualFile != null) {
					final ReviewAdapter mr = virtualFile.getUserData(CommentHighlighter.REVIEW_DATA_KEY);
					final CrucibleFileInfo mf = virtualFile.getUserData(CommentHighlighter.REVIEWITEM_DATA_KEY);
					if (mr != null && mf != null) {
						if (review.getPermId().equals(mr.getPermId()) && file.getPermId().equals(mf.getPermId())) {
							return editor;
						}
					}
				}
			}
		}
		return null;
	}

	public static Collection<UploadItem> getUploadItemsFromChanges(final Project project, final Collection<Change> changes) {
		Collection<UploadItem> uploadItems = new ArrayList<UploadItem>();
		for (Change change : changes) {
			try {
				FilePath path = change.getBeforeRevision().getFile();
				String fileUrl = VcsIdeaHelper.getRepositoryUrlForFile(project, path.getVirtualFile());

				try {
					URL url = new URL(fileUrl);
					fileUrl = url.getPath();
				} catch (MalformedURLException e) {
					String rootUrl = VcsIdeaHelper.getRepositoryRootUrlForFile(project, path.getVirtualFile());
					fileUrl = StringUtils.difference(rootUrl, fileUrl);
				}

				uploadItems.add(new UploadItem(fileUrl, change.getBeforeRevision().getContent(),
						change.getAfterRevision().getContent(), change.getBeforeRevision().getRevisionNumber().asString()));
			} catch (VcsException e) {
				throw new RuntimeException(e);
			}
		}
		return uploadItems;
	}
}
