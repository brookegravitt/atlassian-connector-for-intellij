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

import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTree;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.CommentTreeNode;
import com.intellij.openapi.actionSystem.AnActionEvent;

import javax.swing.tree.TreePath;

public class PrevCommentAction extends AbstractCommentAction {

	public void actionPerformed(final AnActionEvent e) {
		AtlassianTree tree = (AtlassianTree) getTree(e);
		AtlassianTreeNode prevNode = getPrevCommentNode(e, getSelectedNode(e));
		if (tree != null && prevNode != null) {
			TreePath path = new TreePath(prevNode.getPath());
			tree.scrollPathToVisible(path);
			tree.setSelectionPath(path);
			AtlassianClickAction action = prevNode.getAtlassianClickAction();
			if (action != null) {
				action.execute(prevNode, 2);
			}
		}
	}

	public void update(final AnActionEvent e) {

		AtlassianTreeNode node = getSelectedNode(e);
		boolean enabled = getPrevCommentNode(e, node) != null;

		e.getPresentation().setEnabled(enabled);
	}

	private AtlassianTreeNode getPrevCommentNode(AnActionEvent event, AtlassianTreeNode node) {
		if (node == null) {
			return null;
		}
		AtlassianTreeNode n = (AtlassianTreeNode) node.getPreviousNode();
		while (n != null) {
			if (n instanceof CommentTreeNode) {
				CommentTreeNode ctn = (CommentTreeNode) n;
				if (!ctn.getComment().isReply() && !shouldSkipComment(event, ctn.getComment())) {
					return n;
				}
			}
			n = (AtlassianTreeNode) n.getPreviousNode();
		}
		return null;
	}
}