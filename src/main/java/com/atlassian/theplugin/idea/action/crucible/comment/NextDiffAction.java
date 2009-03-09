package com.atlassian.theplugin.idea.action.crucible.comment;

import com.atlassian.theplugin.idea.ui.tree.AtlassianTree;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.file.CrucibleFileNode;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vcs.ex.Range;

import javax.swing.*;

/**
 * User: jgorycki
 * Date: Mar 3, 2009
 * Time: 3:25:21 PM
 */
public class NextDiffAction extends AbstractDiffNavigationAction {

	public void actionPerformed(final AnActionEvent e) {
		AtlassianTree tree = (AtlassianTree) getTree(e);
		if (tree != null) {
			CrucibleFileNode node = getNextFileNode(tree, getSelectedNode(e), true);
			if (node != null) {
				selectNode(tree, node);
				Editor ed = getEditorForNode(node);
				if (ed == null) {
					openFileNode(e, node, false);
				} else {
					Range r = getNextRange(ed);
					if (r != null) {
						openFileAndSelectRange(e, node, r);
					} else {
						node = getNextFileNode(tree, getSelectedNode(e), false);
						if (node != null) {
							selectNode(tree, node);
							openFileNode(e, node, false);
						}
					}
				}
			}
		}
	}

	protected void updateForTree(final AnActionEvent e) {

		boolean enabled = false;

		AtlassianTreeNode node = getSelectedNode(e);
		CrucibleFileNode fileNode = getNextFileNode((AtlassianTree) getTree(e), node, true);
		if (fileNode != null) {
			Editor ed = getEditorForNode(fileNode);
			if (ed != null) {
				Range r = getNextRange(ed);
				enabled = r != null;
			}
			if (!enabled) {
				fileNode = getNextFileNode((AtlassianTree) getTree(e), node, false);
				enabled = fileNode != null;
			}
		}
		e.getPresentation().setEnabled(enabled);
	}

	public void registerShortcutsInEditor(Editor editor) {
		final AnAction globalShowNextAction = ActionManager.getInstance().getAction("VcsShowNextChangeMarker");
		JComponent c = editor.getComponent();
		copyShortcutFrom(globalShowNextAction);
		registerCustomShortcutSet(getShortcutSet(), c);
	}
}
