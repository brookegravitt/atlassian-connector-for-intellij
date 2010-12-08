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

import javax.swing.*;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Jacek Jaroczynski
 */
public abstract class AbstractTree extends JTree {

	public AbstractTree() {
	}

	protected AbstractTree(final TreeModel model) {
		super(model);
	}

	public void expandTree() {
		for (int i = 0; i < getRowCount(); i++) {
			expandRow(i);
		}
	}

	public void collapseTree() {
		for (int i = 0; i < this.getRowCount(); i++) {
			this.collapseRow(i);
		}
	}

	protected Set<TreePath> getCollapsedPaths() {
		Set<TreePath> collapsedNodes = new HashSet<TreePath>();

		for (int i = 0; i < getRowCount(); ++i) {
			TreePath path = getPathForRow(i);

			if (isCollapsed(path)) {
				collapsedNodes.add(path);
			}
		}
		return collapsedNodes;
	}

    protected Set<TreePath> getExpandedPaths() {
		Set<TreePath> expandedNodes = new HashSet<TreePath>();

		for (int i = 0; i < getRowCount(); ++i) {
			TreePath path = getPathForRow(i);

			if (isExpanded(path)) {
				expandedNodes.add(path);
			}
		}
		return expandedNodes;
	}

    protected void expandPaths(Set<TreePath> expandPaths) {
		for (TreePath path : expandPaths) {
			for (int i = 0; i < getRowCount(); ++i) {
				TreePath treePath = getPathForRow(i);

				if (treePath.toString().equals(path.toString())) {
					expandPath(treePath);
					break;
				}
			}
		}
	}

	protected void collapsePaths(Set<TreePath> collapsedPaths) {
		for (TreePath path : collapsedPaths) {
			for (int i = 0; i < getRowCount(); ++i) {
				TreePath treePath = getPathForRow(i);

				if (treePath.toString().equals(path.toString())) {
					collapsePath(treePath);
					break;
				}
			}
		}
	}
}
