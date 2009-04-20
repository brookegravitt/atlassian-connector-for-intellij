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

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AtlassianTreeModel extends DefaultTreeModel {

	public AtlassianTreeModel(AtlassianTreeNode root) {
        super(root);
	}

//	public AtlassianTreeNode locateNode(NodeSearchAlgorithm alg) {
//		return AtlassianTreeModel.locateNode(getRoot(), alg);
//	}

	public AtlassianTreeModel getFilteredModel(Filter filter) {
		AtlassianTreeNode root = getRoot();
		AtlassianTreeNode newRoot = root.filter(filter);
		if (newRoot == null) {
			newRoot = AtlassianTreeNode.EMPTY_NODE;
		}
		return new AtlassianTreeModel(newRoot);
	}

	@Override
	public AtlassianTreeNode getRoot() {
		return (AtlassianTreeNode) super.getRoot();
	}

	public void insertNode(AtlassianTreeNode node, AtlassianTreeNode parent) {
		List<AtlassianTreeNode> children = createChildrenList(parent);
		removeAndSignal(parent, children);
//		parent.removeAllChildren();
		children.add(node);
		fillAndSignal(parent, children);

		int[] idx = {parent.getIndex(node)};
//		nodesWereInserted(parent, idx);

	}

//	public void removeNode(AtlassianTreeNode node, AtlassianTreeNode parent) {
////		int[] idx = {parent.getIndex(node)};
//		List<AtlassianTreeNode> children = createChildrenList(parent);
//		removeAndSignal(parent, children);
////		parent.removeAllChildren();
//		children.remove(node);
//		fillAndSignal(parent, children);
//
//
//
////		nodesWereRemoved(parent, idx, new Object[] { node });
//	}

//	public void replaceNode(AtlassianTreeNode node, AtlassianTreeNode replaceWith) {
//
//		AtlassianTreeNode parent = (AtlassianTreeNode) node.getParent();
//
//		int[] idxOld = {parent.getIndex(node)};
//
//		List<AtlassianTreeNode> children = createChildrenList(parent);
//		removeAndSignal(parent, children);
////		parent.removeAllChildren();
//		children.remove(node);
//		children.add(replaceWith);
//		fillAndSignal(parent, children);
//
////		int[] idxNew = {parent.getIndex(replaceWith)};
////
////		nodesWereRemoved(parent, idxOld, new Object[] { node });
////		nodesWereInserted(parent, idxNew);
//	}

	private List<AtlassianTreeNode> createChildrenList(AtlassianTreeNode parent) {
		List<AtlassianTreeNode> children = new ArrayList<AtlassianTreeNode>();
		for (int i = 0; i < parent.getChildCount(); ++i) {
			children.add(parent.getChildAt(i));
		}
		return children;
	}

	private void removeAndSignal(AtlassianTreeNode parent, List<AtlassianTreeNode> children) {
		TreeNode[] nodes = new TreeNode[children.size()];
		int[] indexes = new int[children.size()];
		for (int i = 0; i < children.size(); ++i) {
			indexes[i] = i;
		}
		children.toArray(nodes);
		parent.removeAllChildren();
		nodesWereRemoved(parent, indexes, nodes);
	}

	private void fillAndSignal(AtlassianTreeNode parent, List<AtlassianTreeNode> children) {
		Collections.sort(children);
		for (AtlassianTreeNode ch : children) {
			parent.addNode(ch);
		}

		int[] indexes = new int[children.size()];
		for (int i = 0; i < children.size(); ++i) {
			indexes[i] = i;
		}

		nodesWereInserted(parent, indexes);
	}
//
//	private static AtlassianTreeNode locateNode(AtlassianTreeNode startingNode, NodeSearchAlgorithm alg) {
//		if (alg.check(startingNode)) {
//			return startingNode;
//		}
//		for (int i = 0; i < startingNode.getChildCount(); i++) {
//			AtlassianTreeNode result = locateNode(startingNode.getChildAt(i), alg);
//			if (result != null) {
//				return result;
//			}
//		}
//		return null;
//	}


}
