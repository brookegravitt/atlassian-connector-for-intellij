package com.atlassian.theplugin.idea.ui.tree.file;

import com.atlassian.theplugin.commons.BambooFileInfo;
import com.atlassian.theplugin.commons.remoteapi.rest.AbstractHttpSession;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;

public class BambooFileNode extends FileNode {

	private BambooFileInfo file;
	private static final TreeCellRenderer MY_RENDERER = new BambooFileNodeRenderer();

	public BambooFileNode(BambooFileInfo file) {
		super(AbstractHttpSession.getLastComponentFromUrl(file.getFileDescriptor().getUrl()));
		this.file = file;
	}

	public String getRevision() {
		return file.getFileDescriptor().getRevision();
	}

	@Override
	public TreeCellRenderer getTreeCellRenderer() {
		return MY_RENDERER;
	}

	private static class BambooFileNodeRenderer extends ColoredTreeCellRenderer {
		public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {
			BambooFileNode node = (BambooFileNode) value;
			append(node.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);

			StringBuilder txt = new StringBuilder();
			txt.append(" (rev: ");
			txt.append(node.getRevision());
			txt.append(")");
			append(txt.toString(), SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES);

			FileTypeManager mgr = FileTypeManager.getInstance();
			FileType type = mgr.getFileTypeByFileName(node.getName());
			setIcon(type.getIcon());
		}
	}
}
