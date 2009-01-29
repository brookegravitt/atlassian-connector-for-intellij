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
package com.atlassian.theplugin.idea.crucible.ui;

import com.atlassian.theplugin.idea.ui.tree.paneltree.TreeUISetup;
import com.atlassian.theplugin.idea.ui.tree.comment.CommentTreeNode;

import javax.swing.*;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.awt.*;

import org.jetbrains.annotations.NotNull;

public class ReviewDetailsTreeMouseListener extends MouseAdapter implements MouseMotionListener, ComponentListener {
	public ReviewDetailsTreeMouseListener(@NotNull JTree jtree, final ReviewCommentRenderer renderer, TreeUISetup treeUISetup) {
		this.jtree = jtree;
		this.renderer = renderer;
		this.treeUISetup = treeUISetup;
		jtree.addMouseListener(this);
		jtree.addMouseMotionListener(this);
		jtree.addComponentListener(this);
	}

	private final JTree jtree;
	private ReviewCommentRenderer renderer;
	private final TreeUISetup treeUISetup;
	private DefaultMutableTreeNode lastHoveredNode;
	private Component lastRendererComponent;

	private boolean isMoreLessLinkHit(MouseEvent mouseevent) {
		TreePath treepath = jtree.getPathForLocation(mouseevent.getX(), mouseevent.getY());
		if (treepath != null) {
			Rectangle rectangle = jtree.getPathBounds(treepath);
			int x = mouseevent.getX() - rectangle.x;
			int y = mouseevent.getY() - rectangle.y;

			final Object o = treepath.getLastPathComponent();
			if (o instanceof DefaultMutableTreeNode) {
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
				if (node != lastHoveredNode) {
					lastHoveredNode = node;
					lastRendererComponent = renderer.getTreeCellRendererComponent(
							jtree, node, false, false, node.isLeaf(), -1, false);
				}

				if (lastRendererComponent instanceof CommentPanel) {
					final CommentPanel commentPanel = (CommentPanel) lastRendererComponent;
					final Rectangle bounds = commentPanel.getMoreBounds();
					if (bounds != null) {
						return bounds.contains(x, y);
					}
				}
			}
		}
		return false;
	}


	@Override
	public void mouseClicked(final MouseEvent e) {
		if (isMoreLessLinkHit(e)) {
			final TreePath treepath = jtree.getPathForLocation(e.getX(), e.getY());
			if (treepath != null) {
				final Object o = treepath.getLastPathComponent();
				if (o instanceof CommentTreeNode) {
					CommentTreeNode commentTreeNode = (CommentTreeNode) o;
					commentTreeNode.setExpanded(!commentTreeNode.isExpanded());
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							treeUISetup.forceTreePrefSizeRecalculation(jtree);
						}
					});
					lastHoveredNode = null;
					lastRendererComponent = null;
				}
			}
		}
	}

	@SuppressWarnings({"override"})
	public void mouseMoved(final MouseEvent e) {
		jtree.setCursor(isMoreLessLinkHit(e) ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
	}

	/**
	 * Needed for compilation under JDK 1.5, which has screwed up MouseAdapter
	 * @param e event
	 */
	@SuppressWarnings({"override"})
	public void mouseDragged(final MouseEvent e) {
	}

	public void componentResized(final ComponentEvent e) {
		lastHoveredNode = null;
		lastRendererComponent = null;
	}

	public void componentMoved(final ComponentEvent e) {
	}

	public void componentShown(final ComponentEvent e) {
	}

	public void componentHidden(final ComponentEvent e) {
	}
}
