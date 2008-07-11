package com.atlassian.theplugin.idea.ui.tree.file;

import com.atlassian.theplugin.commons.VersionedFileDescriptor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;

import javax.swing.*;

public class LeafFileNode extends FileNode {

	private VersionedFileDescriptor file;

	public LeafFileNode(VersionedFileDescriptor file) {
		super(file.getFileName().substring(file.getFileName().lastIndexOf('/') + 1));
		this.file = file;
	}

	public String getRevision() {
		return file.getRevision();
	}

	@Override
	public ColoredTreeCellRenderer getTreeCellRenderer() {
		return LeafFileNodeRenderer.getInstance();
	}

	private static class LeafFileNodeRenderer extends ColoredTreeCellRenderer {
		private static LeafFileNodeRenderer instance;

		public static ColoredTreeCellRenderer getInstance() {
			if (instance == null) {
				instance = new LeafFileNodeRenderer();
			}
			return instance;
		}

		public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			LeafFileNode node = (LeafFileNode) value;
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
