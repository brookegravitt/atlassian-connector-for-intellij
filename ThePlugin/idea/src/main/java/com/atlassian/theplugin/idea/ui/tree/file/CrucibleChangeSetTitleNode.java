package com.atlassian.theplugin.idea.ui.tree.file;

import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.util.Icons;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 15, 2008
 * Time: 6:03:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleChangeSetTitleNode extends FileNode {
	public CrucibleChangeSetTitleNode(String fullName) {
		super(fullName);
	}

	@Override
	public ColoredTreeCellRenderer getTreeCellRenderer() {
		return CrucibleChangeSetTitleNodeRenderer.getInstance();
	}

	private static class CrucibleChangeSetTitleNodeRenderer extends ColoredTreeCellRenderer {
		private static CrucibleChangeSetTitleNodeRenderer instance;

		public static ColoredTreeCellRenderer getInstance() {
			if (instance == null) {
				instance = new CrucibleChangeSetTitleNodeRenderer();
			}
			return instance;
		}

		public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {
			CrucibleChangeSetTitleNode node = (CrucibleChangeSetTitleNode) value;
			append(node.getName(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);

			setIcon(expanded ? Icons.DIRECTORY_OPEN_ICON : Icons.DIRECTORY_CLOSED_ICON);
		}
	}

}
