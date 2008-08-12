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

package com.atlassian.theplugin.idea.ui.tree;

import com.atlassian.theplugin.idea.ui.tree.file.FolderNode;
import com.intellij.openapi.diagnostic.Logger;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class AtlassianTree extends JTree {
	protected static final AtlassianTreeCellRenderer DISPATCHING_RENDERER = new AtlassianTreeCellRenderer();

	public AtlassianTree() {
		this(new AtlassianTreeModel(new FolderNode("/")));
	}

	public AtlassianTree(AtlassianTreeModel model) {
		super(model);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		setCellRenderer(DISPATCHING_RENDERER);
		setRootVisible(true);
		addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent event) {
				//To change body of implemented methods use File | Settings | File Templates.
			}

			public void mousePressed(MouseEvent e) {
				int selRow = getRowForLocation(e.getX(), e.getY());
				TreePath selPath = getPathForLocation(e.getX(), e.getY());
				if (selRow != -1) {
					if (selPath != null) {
						AtlassianTreeNode selectedNode = (AtlassianTreeNode) selPath.getLastPathComponent();
						AtlassianClickAction action = selectedNode.getAtlassianClickAction();
						if (action != null) {
							action.execute(selectedNode, e.getClickCount());
						}
					}
				}
			}

			public void mouseReleased(MouseEvent event) {
				//To change body of implemented methods use File | Settings | File Templates.
			}

			public void mouseEntered(MouseEvent event) {
				//To change body of implemented methods use File | Settings | File Templates.
			}

			public void mouseExited(MouseEvent event) {
				//To change body of implemented methods use File | Settings | File Templates.
			}
		});
		addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(final TreeSelectionEvent e) {
				AtlassianTreeNode node = (AtlassianTreeNode)
						getLastSelectedPathComponent();
				if (node != null) {
					AtlassianClickAction action = node.getAtlassianClickAction();
					if (action != null) {
						action.execute(node, 1);
					}
				}
			}
		});
	}

	public void expandAll() {
		for (int i = 0; i < getRowCount(); i++) {
			expandRow(i);
		}
	}

	public void expandFromNode(AtlassianTreeNode node) {
		for (int i = 0; i < node.getChildCount(); i++) {
			expandFromNode(node.getChildAt(i));
		}
		expandRow(getRowForPath(new TreePath(node.getPath())));
	}

	public void collapseAll() {
		for (int i = 0; i < getRowCount(); i++) {
			collapseRow(i);
		}
	}

	public void focusOnNode(AtlassianTreeNode node) {
		if (node == null) {
			return;
		}
		if (node.equals(getLastSelectedPathComponent())) {
			return;
		}
		for (int i = 0; i < getRowCount(); i++) {
			if (((AtlassianTreeNode) getPathForRow(i).getLastPathComponent()).equals(node)) {
				this.setSelectionRow(i);
				this.scrollRowToVisible(i);
			}
		}
	}

	@Override
	public Rectangle getPathBounds(TreePath path) {
		Rectangle rect = super.getPathBounds(path);
		Container parent = getParent();
		Rectangle newRect;
		if (parent != null && !(parent instanceof CellRendererPane)) {
			// redefined to show as many childen as possible
			rect = new Rectangle(rect.getBounds().x,
					rect.getBounds().y, rect.getBounds().width, parent.getHeight());
		}
		return rect;
	}

	protected static class AtlassianTreeCellRenderer extends DefaultTreeCellRenderer {

		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			try {
				AtlassianTreeNode node = (AtlassianTreeNode) value;
				return node.getTreeCellRenderer()
						.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
			} catch (ClassCastException e) {
				// well, wrong leaf type. I guess this is wrong - unless some genius
				// decides to mis-use my tree :)
				Logger.getInstance(getClass().getName()).error(e);
				return null;
			}
		}
	}


}
