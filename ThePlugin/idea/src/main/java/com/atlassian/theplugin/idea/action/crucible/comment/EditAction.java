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

import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.CommentEditForm;
import com.atlassian.theplugin.idea.crucible.CrucibleConstants;
import com.atlassian.theplugin.idea.crucible.CrucibleHelper;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListenerImpl;
import com.atlassian.theplugin.idea.crucible.events.GeneralCommentAboutToUpdate;
import com.atlassian.theplugin.idea.crucible.events.VersionedCommentAboutToUpdate;
import com.atlassian.theplugin.idea.crucible.tree.ReviewItemTreePanel;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.GeneralCommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.VersionedCommentTreeNode;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 23, 2008
 * Time: 3:49:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class EditAction extends AbstractCommentAction {
	private static final String EDIT_TEXT = "Edit";

	@Override
	public void update(AnActionEvent e) {
		AtlassianTreeNode node = getSelectedNode(e);
		String text = EDIT_TEXT;
		boolean enabled = node != null && checkIfAuthor(node);
		e.getPresentation().setEnabled(enabled);
		if (e.getPlace().equals(CrucibleConstants.MENU_PLACE) || (e.getPlace().equals(ReviewItemTreePanel.MENU_PLACE))) {
			e.getPresentation().setVisible(enabled);
		}
		e.getPresentation().setText(text);
	}

	public void actionPerformed(AnActionEvent e) {
		Project currentProject = e.getData(DataKeys.PROJECT);
		AtlassianTreeNode node = getSelectedNode(e);
		if (node != null && currentProject != null) {
			editComment(currentProject, node);
		}
	}


	private void editComment(Project project, AtlassianTreeNode treeNode) {
		if (treeNode instanceof GeneralCommentTreeNode) {
			GeneralCommentTreeNode node = (GeneralCommentTreeNode) treeNode;
			GeneralComment comment = node.getComment();
			editGeneralComment(project, node.getReview(), comment);
		} else if (treeNode instanceof VersionedCommentTreeNode) {
			VersionedCommentTreeNode node = (VersionedCommentTreeNode) treeNode;
			VersionedComment comment = node.getComment();
			editVersionedComment(project, node.getReview(), node.getFile(), comment);
		}
	}

	private void editGeneralComment(final Project project, final ReviewAdapter review, final GeneralComment comment) {
		CommentEditForm dialog = new CommentEditForm(project, review, (CommentBean) comment,
				CrucibleHelper.getMetricsForReview(project, review));
		dialog.pack();
		dialog.setModal(true);
		dialog.show();
		if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
			IdeaHelper.getReviewActionEventBroker(project).trigger(
					new GeneralCommentAboutToUpdate(CrucibleReviewActionListenerImpl.ANONYMOUS,
							review, comment));
		}
	}

	private void editVersionedComment(Project project, ReviewAdapter review, CrucibleFileInfo file, VersionedComment comment) {
		CommentEditForm dialog = new CommentEditForm(project, review, (CommentBean) comment,
				CrucibleHelper.getMetricsForReview(project, review));
		dialog.pack();
		dialog.setModal(true);
		dialog.show();
		if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
			IdeaHelper.getReviewActionEventBroker(project).trigger(
					new VersionedCommentAboutToUpdate(CrucibleReviewActionListenerImpl.ANONYMOUS,
							review, file, comment));
		}
	}
}