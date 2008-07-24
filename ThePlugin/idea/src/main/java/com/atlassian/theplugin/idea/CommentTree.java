/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.idea.ui.tree.AtlassianTree;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeModel;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;

import javax.swing.plaf.TreeUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 16, 2008
 * Time: 10:39:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommentTree extends AtlassianTree {

	public CommentTree(AtlassianTreeModel model) {
		super(model);
//		putClientProperty("JTree.lineStyle", "None");
//		setShowsRootHandles(false);
		setRootVisible(false);
		setRowHeight(0);
	}

	public CommentTree() {
		super();
	}

	public void initializeUI() {
		registerUI();
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				if (CommentTree.this.isVisible()) {
					registerUI();
				}
			}
		});
	}

	private void registerUI() {
		CommentTree.this.setUI(new BasicWideNodeTreeUI());
	}

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
				throw new NullPointerException("null component passed to " +
						"BasicTreeUI.installUI()");
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
			return DISPATCHING_RENDERER;
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

					if (rh > 0 && rh != prefSize.height)
						prefSize.height = rh;
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

					aComponent = currentCellRenderer.getTreeCellRendererComponent
							(tree, value, tree.isRowSelected(row),
									expanded, treeModel.isLeaf(value), row,
									false);
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
}