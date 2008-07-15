package com.atlassian.theplugin.idea.ui.tree.file;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.remoteapi.rest.AbstractHttpSession;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.FileType;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 14, 2008
 * Time: 11:07:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleFileNode extends FileNode {

	private CrucibleFileInfo fileInfo;

	public CrucibleFileNode(CrucibleFileInfo file) {
		super(AbstractHttpSession.getLastComponentFromUrl(file.getFileDescriptor().getUrl()));
		this.fileInfo = file;
	}

	@Override
	public ColoredTreeCellRenderer getTreeCellRenderer() {
		return CrucibleFileNodeRenderer.getInstance();
	}

	public CrucibleFileInfo getFileInfo() {
		return fileInfo;
	}

	private static class CrucibleFileNodeRenderer extends ColoredTreeCellRenderer {
		private static CrucibleFileNodeRenderer instance;

		public static ColoredTreeCellRenderer getInstance() {
			if (instance == null) {
				instance = new CrucibleFileNodeRenderer();
			}
			return instance;
		}

		public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {
			CrucibleFileNode node = (CrucibleFileNode) value;
			append(node.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);

			StringBuilder txt = new StringBuilder();
			txt.append(" (rev: ");
			txt.append(node.getFileInfo().getOldFileDescriptor().getRevision());
			txt.append("-");
			txt.append(node.getFileInfo().getFileDescriptor().getRevision());
			txt.append(")");
			append(txt.toString(), SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES);

			try {
				String noOfDefects = Integer.valueOf(node.getFileInfo().getNumberOfDefects()).toString();
				String noOfComments = Integer.valueOf(node.getFileInfo().getNumberOfComments()).toString();
				append(" (", SimpleTextAttributes.REGULAR_ATTRIBUTES);
				append(noOfDefects,
					SimpleTextAttributes.ERROR_ATTRIBUTES);
				append("/", SimpleTextAttributes.REGULAR_ATTRIBUTES);
				append(noOfComments,
						SimpleTextAttributes.REGULAR_ATTRIBUTES);
				append(")", SimpleTextAttributes.REGULAR_ATTRIBUTES);
			} catch (ValueNotYetInitialized valueNotYetInitialized) {
				valueNotYetInitialized.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}

			FileTypeManager mgr = FileTypeManager.getInstance();
			FileType type = mgr.getFileTypeByFileName(node.getName());
			setIcon(type.getIcon());
		}
	}

}
