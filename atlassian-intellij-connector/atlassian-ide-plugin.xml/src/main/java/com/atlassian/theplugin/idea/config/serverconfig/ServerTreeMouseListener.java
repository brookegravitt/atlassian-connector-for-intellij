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
package com.atlassian.theplugin.idea.config.serverconfig;

import com.atlassian.theplugin.idea.config.serverconfig.model.ServerInfoNode;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;

/**
 * User: pmaruszak
 */
public class ServerTreeMouseListener extends MouseAdapter implements MouseMotionListener, ComponentListener {
	private final JTree jtree;
	private final String popupToolbarName;


	public ServerTreeMouseListener(@NotNull JTree jtree, final String popupToolbarName) {
		this.jtree = jtree;
		this.popupToolbarName = popupToolbarName;
		jtree.addMouseListener(this);
		jtree.addMouseMotionListener(this);
		jtree.addComponentListener(this);

	}

	public void mousePressed(final MouseEvent e) {
		if (isHLinkHit(e)) {
			TreePath treepath = jtree.getPathForLocation(e.getX(), e.getY());
			if (treepath != null) {
				final Object o = treepath.getLastPathComponent();
				if (o instanceof ServerInfoNode) {
					BrowserUtil.launchBrowser(((ServerInfoNode) o).getServerType().getInfoUrl());
				}
			}
		}
		showPopupMenu(e);
	}

	public void mouseReleased(final MouseEvent e) {
		showPopupMenu(e);
	}

	public void mouseDragged(final MouseEvent e) {

	}

	public void mouseMoved(final MouseEvent e) {
		jtree.setCursor(isHLinkHit(e) ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());

	}

	public void componentResized(final ComponentEvent e) {

	}

	public void componentMoved(final ComponentEvent e) {

	}

	public void componentShown(final ComponentEvent e) {

	}

	public void componentHidden(final ComponentEvent e) {
		
	}

	private boolean isHLinkHit(MouseEvent mouseevent) {
		TreePath treepath = jtree.getPathForLocation(mouseevent.getX(), mouseevent.getY());
		if (treepath != null) {
			final Object o = treepath.getLastPathComponent();
			if (o instanceof ServerInfoNode) {
				return true;
			}
		}
		return false;
	}

	private void showPopupMenu(MouseEvent e) {
		if (!e.isPopupTrigger()) {
			return;
		}

		final JTree theTree = (JTree) e.getComponent();

		TreePath path = theTree.getPathForLocation(e.getX(), e.getY());
		if (path != null) {
			theTree.setSelectionPath(path);
		}
		ActionGroup menu = (ActionGroup) ActionManager.getInstance().getAction(popupToolbarName);
		if (menu == null) {
			return;
		}
		ActionManager.getInstance().createActionPopupMenu(toString(), menu)
				.getComponent().show(e.getComponent(), e.getX(), e.getY());
			
	}
}
