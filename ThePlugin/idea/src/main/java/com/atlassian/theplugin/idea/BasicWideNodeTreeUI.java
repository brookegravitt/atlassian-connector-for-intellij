package com.atlassian.theplugin.idea;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

/**
	 * Author: Craig Wood
 * Piece of code from http://saloon.javaranch.com/cgi-bin/ubb/ultimatebb.cgi?ubb=get_topic&f=2&t=015891
 */
class BasicWideNodeTreeUI extends BasicTreeUI {
	private int lastWidth;
	private boolean leftToRight;
	protected JTree tree;

	@Override
	public void installUI(JComponent c) {
		if (c == null) {
			throw new NullPointerException("null component passed to BasicTreeUI.installUI()");
		}
		tree = (JTree) c;
		super.installUI(c);
	}

	@Override
	protected void prepareForUIInstall() {
		super.prepareForUIInstall();
		leftToRight = tree.getComponentOrientation().isLeftToRight();
		Container parent = tree.getParent();
		if (parent != null) {
			lastWidth = parent.getWidth();
		}
	}

	@Override
	protected TreeCellRenderer createDefaultCellRenderer() {
		return new DefaultTreeCellRenderer();
	}

	@Override
	protected AbstractLayoutCache.NodeDimensions createNodeDimensions() {
		return new NodeDimensionsHandler();
	}

	public class NodeDimensionsHandler extends AbstractLayoutCache.NodeDimensions {
		public Rectangle getNodeDimensions(Object value, int row, int depth,
										   boolean expanded, Rectangle size) {

			// Return size of editing component, if editing and asking
			// for editing row.
			if (editingComponent != null && editingRow == row) {
				Dimension prefSize = editingComponent.getPreferredSize();
				int rh = getRowHeight();

				if (rh > 0 && rh != prefSize.height) {
					prefSize.height = rh;
}
if (size != null) {
					size.x = getRowX(row, depth);
					size.width = prefSize.width;
					size.height = prefSize.height;
				} else {
					size = new Rectangle(getRowX(row, depth), 0,
							prefSize.width, prefSize.height);
				}

				if (!leftToRight) {
					size.x = lastWidth - size.width - size.x - 2;
				}
				return size;
			}
			// Not editing, use renderer.
			if (currentCellRenderer != null) {
				Component aComponent;

				aComponent = currentCellRenderer.getTreeCellRendererComponent(tree, value, tree.isRowSelected(row),
						expanded, treeModel.isLeaf(value), row, false);
if (tree != null) {
					// Only ever removed when UI changes, this is OK!
					rendererPane.add(aComponent);
					aComponent.validate();
				}
				Dimension prefSize = aComponent.getPreferredSize();

				if (size != null) {
					size.x = getRowX(row, depth);
					size.width = //prefSize.width;
							lastWidth - size.x; // <*** the only change
					size.height = prefSize.height;
				} else {
					size = new Rectangle(getRowX(row, depth), 0,
							prefSize.width, prefSize.height);
				}

				if (!leftToRight) {
					size.x = lastWidth - size.width - size.x - 2;
				}
				return size;
			}
			return null;
		}
	}
}
