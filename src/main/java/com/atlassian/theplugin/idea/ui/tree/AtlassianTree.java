package com.atlassian.theplugin.idea.ui.tree;

import com.intellij.openapi.diagnostic.Logger;
import com.atlassian.theplugin.idea.ui.tree.file.FileNode;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

public class AtlassianTree extends JTree {
	protected static final AtlassianTreeCellRenderer DISPATCHING_RENDERER = new AtlassianTreeCellRenderer();

	public AtlassianTree() {
		this(new AtlassianTreeModel(new FileNode("/")));
	}

	public AtlassianTree(AtlassianTreeModel model) {
		super(model);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		setCellRenderer(DISPATCHING_RENDERER);
		setRootVisible(true);
	}

	public void expandAll() {
		for (int i=0; i<getRowCount(); i++) {
			expandRow(i);
		}
	}

	public void collapseAll() {
		for (int i=0; i<getRowCount(); i++) {
			collapseRow(i);
		}
	}

	protected static class AtlassianTreeCellRenderer extends DefaultTreeCellRenderer {

		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {
			try {
				AtlassianTreeNode node = (AtlassianTreeNode) value;
				return node.getTreeCellRenderer()
                        .getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
			} catch (ClassCastException e) {
				// well, wrong leaf type. I guess this is wrong - unless some genius
				// decides to mis-use my tree :)
				Logger.getInstance(getClass().getName()).error(e);
				return null;
			}
		}
	}


}
