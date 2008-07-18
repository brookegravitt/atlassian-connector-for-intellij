package com.atlassian.theplugin.idea.ui.tree.comment;

import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.util.CommentPanelBuilder;

import javax.swing.tree.TreeCellRenderer;
import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 18, 2008
 * Time: 2:54:59 AM
 * To change this template use File | Settings | File Templates.
 */
public class FileNameNode extends AtlassianTreeNode {
	private CrucibleFileInfo file;
	private static final TreeCellRenderer MY_RENDERER = new MyRenderer();

	public FileNameNode(CrucibleFileInfo file) {
		super();
		this.file = file;
	}

	public CrucibleFileInfo getFile() {
		return file;
	}

	public TreeCellRenderer getTreeCellRenderer() {
		return MY_RENDERER;
	}

	private static class MyRenderer implements TreeCellRenderer {
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			FileNameNode node = (FileNameNode) value;
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(new JLabel(node.getFile().getFileDescriptor().getName()));
			panel.setBackground(Color.white);
			panel.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder")
					: BorderFactory.createEmptyBorder(1,1,1,1));
			return panel;

		}
	}
}
