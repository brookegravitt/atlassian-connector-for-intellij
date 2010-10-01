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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

public abstract class AtlassianTreeNode extends DefaultMutableTreeNode implements Comparable {
	private AtlassianClickAction action;

	protected AtlassianTreeNode(AtlassianClickAction action) {
		this.action = action;
	}

	public void addNode(AtlassianTreeNode newChild) {
		super.add(newChild);
	}

	@Override
	public AtlassianTreeNode getChildAt(final int i) {
		return (AtlassianTreeNode) super.getChildAt(i);
	}

	public abstract TreeCellRenderer getTreeCellRenderer();

	public AtlassianClickAction getAtlassianClickAction() {
		return action;
	}

	public abstract AtlassianTreeNode getClone();

	public AtlassianTreeNode filter(final Filter filter) {
		AtlassianTreeNode result = null;
		if (filter.isValid(this)) {
			AtlassianTreeNode tempResult = getClone();
			boolean foundValidChild = false;
			for (int i = 0; i < getChildCount(); i++) {
				AtlassianTreeNode child = getChildAt(i).filter(filter);
				if (child != null) {
					foundValidChild = true;
					tempResult.addNode(child);
				}
			}
			if (foundValidChild || getChildCount() == 0) {
				result = tempResult;
			}
		}
		return result;
	}

	public static final AtlassianTreeNode EMPTY_NODE = new AtlassianTreeNode(AtlassianClickAction.EMPTY_ACTION) {
		@Override
		public TreeCellRenderer getTreeCellRenderer() {
			return new TreeCellRenderer() {
				public Component getTreeCellRendererComponent(final JTree jTree, final Object o, final boolean b,
						final boolean b1, final boolean b2, final int i, final boolean b3) {
					return new JLabel("<Empty>");
				}
			};
		}

		@Override
		public AtlassianTreeNode getClone() {
			return AtlassianTreeNode.EMPTY_NODE;
		}

		public int compareTo(Object o) {
			// hmm, is it ok?
			return -1;
		}
	};

}
