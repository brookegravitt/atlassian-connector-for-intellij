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

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModel;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Jacek Jaroczynski
 */
public class GeneralNodeManipulator extends NodeManipulator {
	public GeneralNodeManipulator(CrucibleReviewListModel reviewListModel, DefaultMutableTreeNode root) {
		super(reviewListModel, root);
	}

	@Override
	public int getChildCount(Object parent) {
		if (parent == rootNode) {
			return reviewListModel.getReviews().size();
		}
		return 0;
	}

	@Override
	public Object getChild(Object parent, int index) {
		if (parent == rootNode) {
			ReviewAdapter r = (ReviewAdapter) reviewListModel.getReviews().toArray()[index];
			if (r != null) {
				DefaultMutableTreeNode p = (DefaultMutableTreeNode) parent;
				if (index < p.getChildCount()) {
					return p.getChildAt(index);
				}

				CrucibleReviewTreeNode n = new CrucibleReviewTreeNode(reviewListModel, r);
				p.add(n);
				return n;
			}
		}

		return null;
	}
}
