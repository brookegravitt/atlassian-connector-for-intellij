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

import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.idea.VcsIdeaHelper;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.vfs.VcsVirtualFile;
import com.intellij.openapi.vcs.vfs.VcsFileSystem;

import java.util.Collection;
import java.util.ArrayList;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Jun 18, 2008
 * Time: 6:06:47 PM
 * To change this template use File | Settings | File Templates.
 */
public final class CrucibleHelper {
	//private static Set<OpenFileDescriptor> openDescriptors = new Set<OpenFileDescriptor>();
	
	private static final Color VERSIONED_COMMENT_BACKGROUND_COLOR = Color.LIGHT_GRAY;
	private static final Color VERSIONED_COMMENT_STRIP_MARK_COLOR = Color.BLUE;

	private CrucibleHelper() {
    }


	public static void showVirtualFileWithComments(final Project project, final CrucibleFileInfo reviewItem,
            final Collection<VersionedComment> fileComments) {

		int line  = 1;

		if (!fileComments.isEmpty()) {
			line = fileComments.iterator().next().getFromStartLine();
		}

		Editor editor = showVirtualFileInEditor(project,
                getOpenFileDescriptor(project, reviewItem.getFileDescriptor().getAbsoluteUrl(),
						reviewItem.getFileDescriptor().getRevision(), line, 1));
		TextAttributes textAttributes = new TextAttributes();
		textAttributes.setBackgroundColor(VERSIONED_COMMENT_BACKGROUND_COLOR);
		highlightCommentsInEditor(project, editor, reviewItem, fileComments, textAttributes);
	}

    /**
     *	Shows virtual file taken from repository in Idea Editor.
     * 	Higlights all versioned comments for given file
     * 	Adds StripeMark on the right side of file window with set tool tip text that corresponde
     *   to VersionedComment.getMessage content
     */
    public static void showVirtualFileWithComments(Project project, final ReviewData reviewAdapter,
            final CrucibleFileInfo reviewItem) {
		Collection<VersionedComment> fileComments = null;
		try {

			fileComments = CrucibleServerFacadeImpl.getInstance().getVersionedComments(reviewAdapter.getServer(),
                    reviewAdapter.getPermId(), reviewItem.getPermId());
			showVirtualFileWithComments(project, reviewItem, fileComments);
		} catch (RemoteApiException e) {
			PluginUtil.getLogger().error(e.getMessage());
		} catch (ServerPasswordNotProvidedException e) {
			PluginUtil.getLogger().error(e.getMessage());
		}
	}

	/*
	*	Shows taken file from VCS in editor but do not higlkights VersionedComments
	* */
	public static  Editor showVirtualFileInEditor(Project project, OpenFileDescriptor ofd) {
		Editor editor = null;

		if (ofd != null) {
			FileEditorManager fem = FileEditorManager.getInstance(project);
			editor = fem.openTextEditor(ofd, true);
		}

		return editor;
	}
	/*
	  * Shows file taken from VCS ineditor asnd sets curson in specified by "comment" line.
	  * If function showVirtualFileWithComments is not called before then no comment highlighting or
	?  * StripMarkap
	* */
	public static void selectVersionedCommentLineInEditor(
            Project project, CrucibleFileInfo reviewItem, VersionedComment comment) {
		OpenFileDescriptor ofd = getOpenFileDescriptor(project, reviewItem.getFileDescriptor().getUrl(),
				reviewItem.getFileDescriptor().getRevision(), 
                comment.getFromStartLine(), 1);

		if (ofd != null) {
			FileEditorManager fem = FileEditorManager.getInstance(project);
			fem.openTextEditor(ofd, true);
		}

	}

	private static OpenFileDescriptor getOpenFileDescriptor(Project project, String filePath, String fileRevision,
            int line, int col) {
		VirtualFile baseDir = project.getBaseDir();
		String baseUrl = VcsIdeaHelper.getRepositoryUrlForFile(baseDir);
		OpenFileDescriptor ofd = null;
		VcsVirtualFile vcvf = null;

		if (baseUrl != null && filePath.startsWith(baseUrl)) {

			String relUrl = filePath.substring(baseUrl.length());


			VirtualFile vfl = null;
//			try {
//				vfl = (VcsVirtualFile)VfsUtil.findFileByURL(new URL(filePath), VirtualFileManager.getInstance());
//			} catch (MalformedURLException e) {
//				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//			}

			vfl = VfsUtil.findRelativeFile(relUrl, baseDir);

			VirtualFile[] openFiles = FileEditorManager.getInstance(project).getOpenFiles();

			for (VirtualFile file : openFiles) {
				if (file.getPath().equals(filePath)
                        && file instanceof VcsVirtualFile && ((VcsVirtualFile) file).getRevision().equals(fileRevision)) {
					vcvf = (VcsVirtualFile) file;
				}
			}


			if (vcvf == null) {
				VcsFileRevision revision = VcsIdeaHelper.getFileRevision(vfl, fileRevision);
				if (revision != null) {
					 vcvf = new VcsVirtualFile(filePath, revision, VcsFileSystem.getInstance());
				}
			}

			ofd = new OpenFileDescriptor(project, vcvf, line, col);

		}


		return ofd;
	}

	private static void highlightCommentsInEditor(Project project, Editor editor, CrucibleFileInfo reviewItem,
            Collection<VersionedComment> fileVersionedComments, TextAttributes textAttribute) {
		Collection<RangeHighlighter> ranges = new ArrayList<RangeHighlighter>();


		if (editor != null) {
			for (VersionedComment comment : fileVersionedComments) {
					//for (int i = comment.getFromStartLine(); i <= comment.getFromEndLine(); i++){
						RangeHighlighter rh = editor.getDocument().getMarkupModel(project).addLineHighlighter(
                                comment.getFromStartLine(), HighlighterLayer.SELECTION, textAttribute);
						rh.setErrorStripeTooltip(reviewItem.getPermId().getId() + ":" + comment.getMessage());
						rh.setErrorStripeMarkColor(VERSIONED_COMMENT_STRIP_MARK_COLOR);

					//}
			}
		}
	}
}
