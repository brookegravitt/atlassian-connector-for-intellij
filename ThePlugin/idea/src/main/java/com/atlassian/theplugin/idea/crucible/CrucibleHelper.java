package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.ReviewItem;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.VcsIdeaHelper;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.vfs.VcsVirtualFile;
import com.intellij.openapi.vcs.vfs.VcsFileSystem;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.awt.*;
import java.net.URL;
import java.net.MalformedURLException;

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

	private CrucibleHelper(){};


	public static void showVirtualFileWithComments(final Project project, final ReviewItem reviewItem, final Collection<VersionedComment> fileComments){

		int line  = 1;

		if (!fileComments.isEmpty()){
			line = fileComments.iterator().next().getFromStartLine();
		}

		Editor editor = showVirtualFileInEditor(project, getOpenFileDescriptor(project, reviewItem.getToPath(), reviewItem.getToRevision(), line, 1));
		TextAttributes textAttributes = new TextAttributes();
		textAttributes.setBackgroundColor(VERSIONED_COMMENT_BACKGROUND_COLOR);
		highlightCommentsInEditor(project, editor, reviewItem,fileComments, textAttributes);
	}

	/*
	*	Shows virtual file taken from repository in Idea Editor.
	* 	Higlights all versioned comments for given file
	* 	Adds StripeMark on the right side of file window with set tool tip text that corresponde to VersionedComment.getMessage content

	* */
	public static void showVirtualFileWithComments(Project project, final ReviewDataInfoAdapter reviewAdapter, final ReviewItem reviewItem){
		Collection<VersionedComment> fileComments = null;
		try {

			fileComments = CrucibleServerFacadeImpl.getInstance().getVersionedComments(reviewAdapter.getServer(), reviewAdapter.getPermaId(), reviewItem.getPermId());
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
	public static  Editor showVirtualFileInEditor(Project project, OpenFileDescriptor ofd){
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
	public static void selectVersionedCommentLineInEditor(Project project, ReviewItem reviewItem, VersionedComment comment){
		OpenFileDescriptor ofd = getOpenFileDescriptor(project, reviewItem.getToPath(), reviewItem.getToRevision(), comment.getFromStartLine(), 1);

		if (ofd != null) {
			FileEditorManager fem = FileEditorManager.getInstance(project);
			fem.openTextEditor(ofd, true);
		}

	}

	private static OpenFileDescriptor getOpenFileDescriptor(Project project, String filePath, String fileRevision, int line, int col){
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

			for(VirtualFile file: openFiles) {
				if (file.getPath().equals(filePath) && file instanceof VcsVirtualFile && ((VcsVirtualFile)file).getRevision().equals(fileRevision)) {
					vcvf = (VcsVirtualFile)file;
				}
			}


			if (vcvf == null) {
				VcsFileRevision revision = VcsIdeaHelper.getFileRevision(vfl, fileRevision);
				if (revision != null) {
					 vcvf = new VcsVirtualFile(filePath,revision, VcsFileSystem.getInstance());
				}
			}

			ofd = new OpenFileDescriptor(project, vcvf, line, col);

		}


		return ofd;
	}

	private static void highlightCommentsInEditor(Project project, Editor editor, ReviewItem reviewItem, Collection<VersionedComment> fileVersionedComments, TextAttributes textAttribute){
		Collection<RangeHighlighter> ranges = new ArrayList<RangeHighlighter>();


		if (editor != null) {
			for (VersionedComment comment: fileVersionedComments) {
					//for (int i = comment.getFromStartLine(); i <= comment.getFromEndLine(); i++){
						RangeHighlighter rh = editor.getDocument().getMarkupModel(project).addLineHighlighter( comment.getFromStartLine(), HighlighterLayer.SELECTION, textAttribute);
						rh.setErrorStripeTooltip(reviewItem.getPermId().getId() +":" + comment.getMessage());
						rh.setErrorStripeMarkColor(VERSIONED_COMMENT_STRIP_MARK_COLOR);

					//}
			}
		}
	}
}
