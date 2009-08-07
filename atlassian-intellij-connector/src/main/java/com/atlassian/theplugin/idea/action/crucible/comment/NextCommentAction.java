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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public class NextCommentAction extends AbstractCommentAction {
	
	public void actionPerformed(final AnActionEvent e) {
		AtlassianTree tree = (AtlassianTree) getTree(e);
		AtlassianTreeNode nextNode = getNextCommentNode(e, getSelectedNode(e));
		if (tree != null && nextNode != null) {
			TreePath path = new TreePath(nextNode.getPath());
			tree.scrollPathToVisible(path);
			tree.setSelectionPath(path);
			AtlassianClickAction action = nextNode.getAtlassianClickAction();
			if (action != null) {
				action.execute(nextNode, 2);
			}
		}
	}

	public void update(final AnActionEvent e) {

		AtlassianTreeNode node = getSelectedNode(e);
		boolean enabled = getNextCommentNode(e, node) != null;

		e.getPresentation().setEnabled(enabled);
	}

	private AtlassianTreeNode getNextCommentNode(AnActionEvent event, AtlassianTreeNode node) {
        AtlassianTree tree =(AtlassianTree) getTree(event);
        if (tree == null) {
            return null;
        }
		DefaultMutableTreeNode start = node;
		if (start == null) {
			start = (DefaultMutableTreeNode) tree.getModel().getRoot();
		}
		if (start != null) {
			AtlassianTreeNode n = (AtlassianTreeNode) start.getNextNode();
			while (n != null) {
				if (n instanceof CommentTreeNode) {
					CommentTreeNode ctn = (CommentTreeNode) n;
					if (!ctn.getComment().isReply() && !shouldSkipComment(event, ctn.getComment())) {
						return n;
					}
				}
				n = (AtlassianTreeNode) n.getNextNode();
			}
		}
		return null;
	}
}
