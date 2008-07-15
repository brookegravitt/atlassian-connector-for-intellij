package com.atlassian.theplugin.idea.ui.tree.file;

import com.atlassian.theplugin.commons.BambooFileInfo;
import com.atlassian.theplugin.commons.remoteapi.rest.AbstractHttpSession;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;

import javax.swing.*;

public class BambooFileNode extends FileNode {

	private BambooFileInfo file;

	public BambooFileNode(BambooFileInfo file) {
		super(AbstractHttpSession.getLastComponentFromUrl(file.getFileDescriptor().getUrl()));
		this.file = file;
	}

	public String getRevision() {
		return file.getFileDescriptor().getRevision();
	}

	@Override
	public ColoredTreeCellRenderer getTreeCellRenderer() {
		return BambooFileNodeRenderer.getInstance();
	}

	private static class BambooFileNodeRenderer extends ColoredTreeCellRenderer {
		private static BambooFileNodeRenderer instance;

		public static ColoredTreeCellRenderer getInstance() {
			if (instance == null) {
				instance = new BambooFileNodeRenderer();
			}
			return instance;
		}

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
