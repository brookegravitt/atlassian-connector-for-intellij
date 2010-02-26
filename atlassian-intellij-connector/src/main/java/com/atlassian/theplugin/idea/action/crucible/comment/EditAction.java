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

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.idea.crucible.CommentTooltipPanel;
import com.atlassian.theplugin.idea.crucible.CommentTooltipPanelWithRunners;
import com.atlassian.theplugin.idea.crucible.CrucibleConstants;
import com.atlassian.theplugin.idea.crucible.tree.ReviewItemTreePanel;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.CommentTreeNode;
import com.intellij.openapi.actionSystem.AnActionEvent;

//PL-123
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

	public void actionPerformed(AnActionEvent event) {
		AtlassianTreeNode node = getSelectedNode(event);
		if (node != null) {
            event.getPresentation().putClientProperty(CommentTooltipPanel.JBPOPUP_PARENT_COMPONENT, getTree(event));
			editComment(event, node);
		}
	}


	private void editComment(AnActionEvent event, AtlassianTreeNode treeNode) {
		if (treeNode instanceof CommentTreeNode) {
			CommentTreeNode node = (CommentTreeNode) treeNode;
			Comment comment = node.getComment();
            Comment parent = comment.getParentComment();
			editGeneralComment(event, node.getReview(), comment, parent);
		}
	}

	private void editGeneralComment(AnActionEvent event, final ReviewAdapter review,
                                    final Comment comment, final Comment parent) {

        CommentTooltipPanel.showCommentTooltipPopup(
                event,
                new CommentTooltipPanelWithRunners(event, review, null, comment, parent, CommentTooltipPanel.Mode.EDIT),
                null);
	}
}