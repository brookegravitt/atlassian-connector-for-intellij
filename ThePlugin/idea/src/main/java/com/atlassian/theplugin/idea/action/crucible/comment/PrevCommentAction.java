package com.atlassian.theplugin.idea.action.crucible.comment;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTree;
import com.atlassian.theplugin.idea.CommentTreePanel;

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
		AtlassianTreeNode prevNode = ((AtlassianTreeNode)getSelectedNode(e).getPreviousNode());
		if (prevNode != null){
			TreePath path = new TreePath(prevNode.getPath());
			tree.scrollPathToVisible(path);
			tree.setSelectionPath(path);
			prevNode.getAtlassianClickAction().execute(prevNode,1);
		}
	}

	public void update(final AnActionEvent e) {
		AtlassianTreeNode node = getSelectedNode(e);

		boolean enabled = node != null && node.getPreviousNode() != null && node.getPreviousNode() != CommentTreePanel.ROOT;
		e.getPresentation().setEnabled(enabled);

		if (e.getPlace().equals(CommentTreePanel.MENU_PLACE)) {
			e.getPresentation().setVisible(false);
		}		
	}
}
