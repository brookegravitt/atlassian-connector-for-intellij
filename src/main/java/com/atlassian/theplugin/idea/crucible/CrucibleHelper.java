package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.ReviewItem;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;?
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.idea.IdeaHelper;
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
	private static final Color VERSIONED_COMMENT_BACKGROUND_COLOR = Color.LIGHT_GRAY;
	private static final Color VERSIONED_COMMENT_STRIP_MARK_COLOR = Color.BLUE;

	private CrucibleHelper(){};

	/*
	*	Shows virtual file taken from repository in Idea Editor.
	* 	Higlights all versioned comments for given file
	* 	Adds StripeMark on the right side of file window with set tool tip text that corresponde to VersionedComment.getMessage content

	* */
	public static void showVirtualFileWithComments(final ReviewDataInfoAdapter reviewAdapter, final ReviewItem reviewItem){
		Collection<VersionedComment> fileComments = null;
		try {

			fileComments = CrucibleServerFacadeImpl.getInstance().getVersionedComments(reviewAdapter.getServer(), reviewAdapter.getPermaId(), reviewItem.getPermId());
			Editor editor = showVirtualFileInEditor(reviewItem);
			TextAttributes textAttributes = new TextAttributes();
			textAttributes.setBackgroundColor(VERSIONED_COMMENT_BACKGROUND_COLOR);

			highlightCommentsInEditor(editor, fileComments, textAttributes);
		} catch (RemoteApiException e) {
			PluginUtil.getLogger().error(e.getMessage());
		} catch (ServerPasswordNotProvidedException e) {
			PluginUtil.getLogger().error(e.getMessage());
		}
	}

	/*
	*	Shows taken file from VCS in editor but do not higlkights VersionedComments
	* */
	public static  Editor showVirtualFileInEditor(ReviewItem reviewItem){
		Project project = IdeaHelper.getCurrentProject();
		Editor editor = null;
		VirtualFile vf = getVirtualFile(reviewItem.getToPath());

		if (vf != null) {
			FileEditorManager fem = FileEditorManager.getInstance(project);
			OpenFileDescriptor ofd = new OpenFileDescriptor(project, vf);

			editor = fem.openTextEditor(ofd, true);

		}

		return editor;
	}
	/*
	  * Shows file taken from VCS ineditor asnd sets curson in specified by "comment" line.
	  * If function showVirtualFileWithComments is not called before then no comment highlighting or
	  * StripMarkap 
	* */
	public static void selectVersionedCommentLineInEditor(ReviewItem reviewItem, VersionedComment comment){
		VirtualFile vf = getVirtualFile(reviewItem.getToPath());

		if (vf != null) {
			Project project = IdeaHelper.getCurrentProject();
			FileEditorManager fem = FileEditorManager.getInstance(project);
			OpenFileDescriptor ofd = new OpenFileDescriptor(project, vf, comment.getFromStartLine(), 1);
		}

	}

	private static VirtualFile getVirtualFile(String filePath){
		VirtualFile baseDir = IdeaHelper.getCurrentProject().getBaseDir();
		String baseUrl = VcsIdeaHelper.getRepositoryUrlForFile(baseDir);
		VirtualFile vf = null;

		if (filePath.startsWith(baseUrl)) {
		   String relUrl = filePath.substring(baseUrl.length());
		   vf = VfsUtil.findRelativeFile(relUrl, baseDir);

		}

		return vf;
	}

	private static void highlightCommentsInEditor(Editor fileEditor, Collection<VersionedComment> fileVersionedComments, TextAttributes textAttribute){
		Collection<RangeHighlighter> ranges = new ArrayList<RangeHighlighter>();
		Project project = IdeaHelper.getCurrentProject();

		for (VersionedComment comment: fileVersionedComments) {
				//for (int i = comment.getFromStartLine(); i <= comment.getFromEndLine(); i++){
					RangeHighlighter rh = fileEditor.getDocument().getMarkupModel(project).addLineHighlighter( comment.getFromStartLine(), HighlighterLayer.SELECTION, textAttribute);
					rh.setErrorStripeTooltip(comment.getMessage());
					rh.setErrorStripeMarkColor(VERSIONED_COMMENT_STRIP_MARK_COLOR);

				//}
			}
	}
}
