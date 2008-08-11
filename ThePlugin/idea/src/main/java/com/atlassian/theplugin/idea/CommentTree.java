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

import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
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
		putClientProperty("JTree.lineStyle", "None");
		setShowsRootHandles(false);
		setRootVisible(false);
		setRowHeight(0);
	}

	public CommentTree() {
		super();
	}

	@Override
	protected void setExpandedState(final TreePath path, final boolean state) {
		if (state) {
			super.setExpandedState(path, state);
		}
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
		CommentTree.this.setUI(new MyTreeUI());
	}

	private class MyTreeUI extends BasicWideNodeTreeUI {
		@Override
		protected TreeCellRenderer createDefaultCellRenderer() {
			return DISPATCHING_RENDERER;
		}
	}


}