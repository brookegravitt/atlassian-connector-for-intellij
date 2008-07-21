package com.atlassian.theplugin.idea.ui.tree.file;

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.Icons;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 15, 2008
 * Time: 6:03:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleChangeSetTitleNode extends FileNode {
	private ReviewData changeSet;
	private static final TreeCellRenderer MY_RENDERER = new CrucibleChangeSetTitleNodeRenderer();

	public CrucibleChangeSetTitleNode(ReviewData changeSet, AtlassianClickAction action) {
		super(changeSet.getName(), action);
		this.changeSet = changeSet;
	}

	@Override
	public TreeCellRenderer getTreeCellRenderer() {
		return MY_RENDERER;
	}

	public ReviewData getChangeSet() {
		return changeSet;
	}

	public void setChangeSet(ReviewData changeSet) {
		this.changeSet = changeSet;
	}

	private static class CrucibleChangeSetTitleNodeRenderer extends ColoredTreeCellRenderer {
		private static final SimpleTextAttributes TEXT_ITALIC = new SimpleTextAttributes(SimpleTextAttributes.STYLE_ITALIC, null);
		private static final SimpleTextAttributes RED_ITALIC = new SimpleTextAttributes(SimpleTextAttributes.STYLE_ITALIC, Color.red);

		public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded,
										  boolean leaf, int row, boolean hasFocus) {
			StringBuilder sb = new StringBuilder();
			CrucibleChangeSetTitleNode node = (CrucibleChangeSetTitleNode) value;
			append(node.getChangeSet().getPermId().getId(), new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD,
					Color.red));
			append(" ", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
			append(node.getChangeSet().getName(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
			try {
				List<GeneralComment> generalComments = node.getChangeSet().getGeneralComments();
				if (generalComments.size() > 0) {
					int noOfDefects = 0;
					for (GeneralComment comment : generalComments) {
						if (comment.isDefectRaised()) {
							noOfDefects++;
						}
					}
					append(" ",
							TEXT_ITALIC);
					append(String.valueOf(generalComments.size()),
							TEXT_ITALIC);
					append(" comment", TEXT_ITALIC);
					if (generalComments.size() != 1) {
						append("s", TEXT_ITALIC);
					}
					if (noOfDefects > 0) {
						append(" (", TEXT_ITALIC);
						append(String.valueOf(noOfDefects),
								RED_ITALIC);
						append(" defect", RED_ITALIC);
						if (noOfDefects != 1) {
							append("s", RED_ITALIC);
						}
						append(")", TEXT_ITALIC);
					}
				}
			} catch (ValueNotYetInitialized valueNotYetInitialized) {
				// ignore
			}
			setIcon(expanded ? Icons.DIRECTORY_OPEN_ICON : Icons.DIRECTORY_CLOSED_ICON);
		}
	}

}
