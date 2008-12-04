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
package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModel;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModelListener;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;

/**
 * @author Jacek Jaroczynski
 */
public class ReviewTreeModel extends DefaultTreeModel implements CrucibleReviewListModelListener {

	private CrucibleReviewListModel reviewListModel;

	public ReviewTreeModel(CrucibleReviewListModel reviewListModel) {
		super(new DefaultMutableTreeNode());
		this.reviewListModel = reviewListModel;

		reviewListModel.addListener(this);
	}

	/*
	Override TreeModel methods
	 */

	public Object getRoot() {
		return super.getRoot();
	}

	public Object getChild(Object parent, int index) {
		if (parent != null && parent instanceof DefaultMutableTreeNode && parent == root) {
			return reviewListModel.getReviews().toArray()[index];
		}
		return null;
	}

	public int getChildCount(Object parent) {
		if (parent != null && parent instanceof DefaultMutableTreeNode && parent == root) {
			return reviewListModel.getReviews().size();
		}

		return 0;
	}

	public boolean isLeaf(Object node) {
		if (node == super.getRoot()) {
			return false;
		}

		return true;
	}

	public void valueForPathChanged(TreePath path, Object newValue) {
		System.out.println("valueForPathChanged");
	}

	public int getIndexOfChild(Object parent, Object child) {
		if (parent != null && parent instanceof DefaultMutableTreeNode && parent == root) {
			return new ArrayList<ReviewAdapter>(reviewListModel.getReviews()).indexOf((ReviewAdapter) child);
		}

		return -1;
	}

	/*
	Implement CrucibleReviewListModelListener interface
	 */

	public void reviewAdded(ReviewAdapter review) {
		System.out.println("review added");
	}

	public void reviewRemoved(ReviewAdapter review) {
		System.out.println("review removed");
	}

	public void reviewChanged(ReviewAdapter review) {
		System.out.println("review changed");
	}

	public void reviewListUpdateStarted(ServerId serverId) {
		System.out.println("reviews update started");
	}

	public void reviewListUpdateFinished(ServerId serverId) {
		System.out.println("reviews updated finished");

		this.nodeStructureChanged(root);
	}
}
