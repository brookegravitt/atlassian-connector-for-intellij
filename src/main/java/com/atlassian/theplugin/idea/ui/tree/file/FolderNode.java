package com.atlassian.theplugin.idea.ui.tree.file;

import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.Filter;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.Icons;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Aug 7, 2008
 * Time: 11:09:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class FolderNode extends FileNode {
	private static final ColoredTreeCellRenderer MY_RENDERER = new FileNodeRenderer();
	
	public FolderNode(String fullName, AtlassianClickAction action) {
		super(fullName, action);
	}

	public FolderNode(final FileNode node) {
		super(node);
	}

	public FolderNode(final String name) {
		this(name, AtlassianClickAction.EMPTY_ACTION);
	}

	public AtlassianTreeNode getClone() {
		return new FolderNode(this);
	}

	public AtlassianTreeNode filter(final Filter filter) {
		AtlassianTreeNode result = null;
		if (filter.isValid(this)) {
			result = getClone();
			for (int i = 0; i < getChildCount(); i++) {
				AtlassianTreeNode child = getChildAt(i).filter(filter);
				if (child != null) {
					result.addNode(child);
				}
			}
			if (result.getChildCount() <= 0) {
					result = null;
			}

		}
		return result;
	}

	public TreeCellRenderer getTreeCellRenderer() {
		return MY_RENDERER;
	}

	private static class FileNodeRenderer extends ColoredTreeCellRenderer {
		public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {
			FileNode node = (FileNode) value;
			append(node.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);

			setIcon(expanded ? Icons.DIRECTORY_OPEN_ICON : Icons.DIRECTORY_CLOSED_ICON);
		}
	}
}
