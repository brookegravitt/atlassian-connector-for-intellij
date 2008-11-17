package com.atlassian.theplugin.idea.ui.tree.clickaction;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.idea.crucible.CrucibleHelper;
import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.VersionedCommentTreeNode;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;

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

		if (editor != null || noOfClicks > 1) {
			CrucibleHelper.openFileOnComment(project, review, file, comment);
		}
	}

}
