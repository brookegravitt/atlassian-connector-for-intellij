package com.atlassian.theplugin.idea.ui.tree.clickaction;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.idea.crucible.CrucibleHelper;
import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.VersionedCommentTreeNode;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class CrucibleVersionedCommentClickAction implements AtlassianClickAction {
	private Project project;

	public CrucibleVersionedCommentClickAction(Project project) {
		this.project = project;
	}

	public void execute(final AtlassianTreeNode node, final int noOfClicks) {
		VersionedCommentTreeNode anode = (VersionedCommentTreeNode) node;

		ReviewAdapter review = anode.getReview();
		CrucibleFileInfo file = anode.getFile();
		Editor editor = CrucibleHelper.getEditorForCrucibleFile(review, file);
		VersionedComment comment = anode.getComment();

		switch (noOfClicks) {
			case 1:
				if (editor != null) {
					Document document = editor.getDocument();
					VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
					if (virtualFile != null) {
						OpenFileDescriptor display = new OpenFileDescriptor(project, virtualFile, comment.getToStartLine() - 1,
								0);
						if (display.canNavigateToSource()) {
							display.navigate(false);
						}
					}
				}
				break;
			case 2:
				CrucibleHelper.openFileOnComment(project, review, file, comment);
				break;
			default:
				break;
		}
	}
}
