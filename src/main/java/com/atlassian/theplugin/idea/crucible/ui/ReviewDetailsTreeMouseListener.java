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

import com.atlassian.theplugin.idea.ui.tree.comment.CommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.paneltree.TreeUISetup;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;

/**
 * This component is by design thread-unsafe (due to potential performance reason as synchronization would need to occur
 * on every mouse movement).
 * It should be only used in Event Dispatch Thread.
 */
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
	private Rectangle hyperlinkBounds;


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
					final Component rendererComponent = renderer
							.getTreeCellRendererComponent(jtree, node, false, false, node.isLeaf(), -1, false);
					if (rendererComponent instanceof ReviewCommentPanel) {
						final ReviewCommentPanel reviewCommentPanel = (ReviewCommentPanel) rendererComponent;
						// it will work as long as getMoreBounds returns a new object which is not shared
						// and modified in a different place. It works as Component.getBounds() creates a copy every time.
						hyperlinkBounds = reviewCommentPanel.getMoreBounds();
					} else {
						hyperlinkBounds = null;
					}
				}
					if (hyperlinkBounds != null) {
						return hyperlinkBounds.contains(x, y);
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
					lastHoveredNode = null;
					// whole code below is evil, but it does the trick. Without it
					// (calling treeUISetup.forceTreePrefSizeRecalculation only once)
					// we may end up with empty space where vertical scrollbar
					treeUISetup.forceTreePrefSizeRecalculation(jtree);
					// this second call ensures that it will be processed after the messages triggered
					// by the first call are already processed (we put the message at the end of EDT)
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							treeUISetup.forceTreePrefSizeRecalculation(jtree);
						}
					});
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
	}

	public void componentMoved(final ComponentEvent e) {
	}

	public void componentShown(final ComponentEvent e) {
	}

	public void componentHidden(final ComponentEvent e) {
	}
}
