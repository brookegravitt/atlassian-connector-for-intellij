package com.atlassian.theplugin.idea.ui.filetree;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.Icons;
import com.intellij.util.ui.UIUtil;
import com.atlassian.theplugin.util.ColorToHtml;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

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
					LeafFileNode lfn = (LeafFileNode) node;
					Color statsColor = selected
							? UIUtil.getTreeSelectionForeground() : UIUtil.getTreeSelectionBackground();
					StringBuilder txt = new StringBuilder();
					txt.append("<html><body>");
					txt.append(getText());
					txt.append(" <font color=");
					txt.append(ColorToHtml.getHtmlFromColor(statsColor));
					txt.append("><i> (rev: ");
					txt.append(lfn.getRevision());
					txt.append(")</i></font>");
					txt.append("</body></html>");
					setText(txt.toString());

					FileTypeManager mgr = FileTypeManager.getInstance();
					FileType type = mgr.getFileTypeByFileName(node.getName());
					setIcon(type.getIcon());
				}
			} catch (ClassCastException e) {
				// well, wrong leaf type. I guess this is wrong - unless some genius
				// decides to mis-use my tree :)
				Logger.getInstance(getClass().getName()).error(e);
				setIcon(null);
			}

			return c;
		}
	}


}
