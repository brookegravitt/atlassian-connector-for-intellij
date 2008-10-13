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

package com.atlassian.theplugin.idea.action.crucible.comment;

import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.idea.CommentTreePanel;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.ReviewDataImpl;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.events.CommentAboutToRemove;
import com.atlassian.theplugin.idea.crucible.tree.ReviewItemTreePanel;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.GeneralCommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.VersionedCommentTreeNode;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.Icons;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 23, 2008
 * Time: 3:50:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class RemoveAction extends AbstractCommentAction {
	private static final String REMOVE_TEXT = "Remove";

	@Override
	public void update(AnActionEvent e) {
		AtlassianTreeNode node = getSelectedNode(e);
		String text = REMOVE_TEXT;
		boolean enabled = node != null && checkIfAuthor(node);
		e.getPresentation().setEnabled(enabled);
		if (e.getPlace().equals(CommentTreePanel.MENU_PLACE) || (e.getPlace().equals(ReviewItemTreePanel.MENU_PLACE))) {
			e.getPresentation().setVisible(enabled);
		}
		e.getPresentation().setText(text);
	}

	public void actionPerformed(AnActionEvent e) {
		Project currentProject = e.getData(DataKeys.PROJECT);
		AtlassianTreeNode node = getSelectedNode(e);
		if (node != null && currentProject != null) {
			removeComment(currentProject, node);
		}
	}


	private void removeComment(Project project, AtlassianTreeNode treeNode) {
		Comment comment = null;
		ReviewDataImpl review = null;

		if (treeNode instanceof GeneralCommentTreeNode) {
			GeneralCommentTreeNode node = (GeneralCommentTreeNode) treeNode;
			comment = node.getComment();
			review = node.getReview();
		} else if (treeNode instanceof VersionedCommentTreeNode) {
			VersionedCommentTreeNode node = (VersionedCommentTreeNode) treeNode;
			comment = node.getComment();
			review = node.getReview();
		}
		if (comment == null || review == null) {
			return;
		}
		removeComment(project, review, comment);
	}

	private void removeComment(final Project project, final ReviewDataImpl review, final Comment comment) {
		int result = Messages.showYesNoDialog(project, "Are you sure you want remove your comment?", "Confirmation required",
				Icons.TASK_ICON);
		if (result == DialogWrapper.OK_EXIT_CODE) {
			IdeaHelper.getReviewActionEventBroker(project).trigger(
					new CommentAboutToRemove(CrucibleReviewActionListener.ANONYMOUS,
							review, comment));
		}
	}
}
