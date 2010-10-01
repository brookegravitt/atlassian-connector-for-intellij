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
package com.atlassian.theplugin.idea.crucible.tree.node;

import com.atlassian.theplugin.crucible.model.CrucibleReviewListModel;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Jacek Jaroczynski
 */
public abstract class NodeManipulator {
	protected final CrucibleReviewListModel reviewListModel;
	protected final DefaultMutableTreeNode rootNode;

	public NodeManipulator(final CrucibleReviewListModel reviewListModel, final DefaultMutableTreeNode rootNode) {
		this.reviewListModel = reviewListModel;
		this.rootNode = rootNode;
	}

	public abstract int getChildCount(Object parent);

	public abstract Object getChild(Object parent, int index);
}
