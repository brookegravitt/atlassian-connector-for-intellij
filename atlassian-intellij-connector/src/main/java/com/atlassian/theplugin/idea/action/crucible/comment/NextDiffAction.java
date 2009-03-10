package com.atlassian.theplugin.idea.action.crucible.comment;

import com.atlassian.theplugin.idea.ui.tree.AtlassianTree;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.file.CrucibleFileNode;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vcs.ex.Range;

import javax.swing.*;

/**
 * User: jgorycki
 * Date: Mar 3, 2009
 * Time: 3:25:21 PM
 */
public class NextDiffAction extends AbstractDiffNavigationAction {

	protected Range getSubsequentRange(Editor ed) {
		return getNextRange(ed);
	}

	protected CrucibleFileNode getSubsequentFileNode(AtlassianTree tree,
													 AtlassianTreeNode selectedNode, boolean alsoThis) {
		return getNextFileNode(tree, selectedNode, alsoThis);
	}

	protected boolean wantLastNode() {
		return false;
	}

	public void registerShortcutsInEditor(Editor editor) {
		final AnAction globalShowNextAction = ActionManager.getInstance().getAction("VcsShowNextChangeMarker");
		JComponent c = editor.getComponent();
		copyShortcutFrom(globalShowNextAction);
		registerCustomShortcutSet(getShortcutSet(), c);
	}
}
