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
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldDef;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.VcsIdeaHelper;
import com.atlassian.theplugin.idea.crucible.editor.CommentHighlighter;
import com.atlassian.theplugin.idea.crucible.editor.OpenDiffToolAction;
import com.atlassian.theplugin.idea.crucible.editor.OpenEditorDiffActionImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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

		VcsIdeaHelper.openFileWithDiffs(project
				, true
				, reviewItem.getFileDescriptor().getAbsoluteUrl()
				, reviewItem.getOldFileDescriptor().getRevision()
				, reviewItem.getFileDescriptor().getRevision()
				, reviewItem.getCommitType()
				, 1
				, 1
				, new OpenEditorDiffActionImpl(project, review, reviewItem, true));
	}

	public static void showRevisionDiff(final Project project, final CrucibleFileInfo reviewItem) {

		final String filename = reviewItem.getFileDescriptor().getAbsoluteUrl();
		final String fileRevision = reviewItem.getOldFileDescriptor().getRevision();
		final String toRevision = reviewItem.getFileDescriptor().getRevision();
		VcsIdeaHelper.openFileWithDiffs(project
				, true
				, filename
				, fileRevision
				, toRevision
				, reviewItem.getCommitType()
				, 1
				, 1
				, new OpenDiffToolAction(project, filename, fileRevision, toRevision));
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

	public static void openFileOnComment(final Project project, final ReviewAdapter review, final CrucibleFileInfo file,
			final VersionedComment comment) {

		ApplicationManager.getApplication().runReadAction(new Runnable() {
			public void run() {
				VcsIdeaHelper.openFileWithDiffs(project
						, true
						, file.getFileDescriptor().getAbsoluteUrl()
						, file.getOldFileDescriptor().getRevision()
						, file.getFileDescriptor().getRevision()
						, file.getCommitType()
						, comment.getToStartLine()
						, 0
						, new OpenEditorDiffActionImpl(project, review, file, comment.getToStartLine() - 1, 0, true)
				);
			}
		});

	}
}
