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

import com.atlassian.theplugin.idea.crucible.CrucibleConstants;
import com.atlassian.theplugin.idea.crucible.tree.ReviewItemTreePanel;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTree;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.intellij.openapi.actionSystem.AnActionEvent;

import javax.swing.tree.TreePath;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Aug 7, 2008
 * Time: 3:35:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class PrevCommentAction extends AbstractCommentAction {
	public void actionPerformed(final AnActionEvent e) {
		AtlassianTree tree = (AtlassianTree) getTree(e);
		AtlassianTreeNode prevNode = ((AtlassianTreeNode) getSelectedNode(e).getPreviousNode());
		if (prevNode != null) {
			TreePath path = new TreePath(prevNode.getPath());
			tree.scrollPathToVisible(path);
			tree.setSelectionPath(path);
			prevNode.getAtlassianClickAction().execute(prevNode, 1);
		}
	}

	public void update(final AnActionEvent e) {
		AtlassianTreeNode node = getSelectedNode(e);

		boolean enabled = node != null && node.getPreviousNode() != null && node.getPreviousNode() != CrucibleConstants.ROOT;
		e.getPresentation().setEnabled(enabled);

		if (e.getPlace().equals(CrucibleConstants.MENU_PLACE) || (e.getPlace().equals(ReviewItemTreePanel.MENU_PLACE))) {
			e.getPresentation().setVisible(false);
		}		
	}
}
