package com.atlassian.theplugin.idea.ui.tree.file;

import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.util.Icons;
import com.atlassian.theplugin.commons.crucible.CrucibleChangeSet;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 15, 2008
 * Time: 6:03:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleChangeSetTitleNode extends FileNode {
	private CrucibleChangeSet changeSet;

	public CrucibleChangeSetTitleNode(CrucibleChangeSet changeSet) {
		super(changeSet.getName());
		this.changeSet = changeSet;
	}

	@Override
	public ColoredTreeCellRenderer getTreeCellRenderer() {
		return CrucibleChangeSetTitleNodeRenderer.getInstance();
	}

	public CrucibleChangeSet getChangeSet() {
		return changeSet;
	}

	public void setChangeSet(CrucibleChangeSet changeSet) {
		this.changeSet = changeSet;
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
			append(node.getChangeSet().getPermaId().getId(), new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD,
					Color.red));
			append(" ", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
			append(node.getChangeSet().getName(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);

			setIcon(expanded ? Icons.DIRECTORY_OPEN_ICON : Icons.DIRECTORY_CLOSED_ICON);
		}
	}

}
