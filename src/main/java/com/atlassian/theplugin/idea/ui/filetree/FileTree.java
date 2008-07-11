package com.atlassian.theplugin.idea.ui.filetree;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.util.Icons;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: jgorycki
 * Date: Jul 11, 2008
 * Time: 2:02:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class FileTree extends JTree {

	public FileTree(TreeModel model) {
		super(model);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		setCellRenderer(new FileTreeCellRenderer());
		setRootVisible(true);
	}

	private static class FileTreeCellRenderer extends DefaultTreeCellRenderer {
		private FileTreeCellRenderer() {
			super();
			setOpenIcon(Icons.DIRECTORY_OPEN_ICON);
			setClosedIcon(Icons.DIRECTORY_CLOSED_ICON);
		}

		public Component getTreeCellRendererComponent(JTree tree,
													  Object value,
													  boolean selected,
													  boolean expanded,
													  boolean leaf,
													  int row,
													  boolean hasFocus) {
			Component c = super.getTreeCellRendererComponent(
					tree, value, selected, expanded, leaf, row, hasFocus);

			try {
				FileNode node = (FileNode) value;
				if (node.isLeaf()) {
					FileTypeManager mgr = FileTypeManager.getInstance();
					FileType type = mgr.getFileTypeByFileName(node.getName());
					setIcon(type.getIcon());
				}
			} catch (ClassCastException e) {
				// should not happen, making compiler happy
				setIcon(null);
			}

			return c;
		}
	}


}
