package com.atlassian.theplugin.idea.crucible.tree;

import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.idea.ui.tree.paneltree.AbstractTreeNode;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * User: pmaruszak
 */
public class FilterTree extends JTree {

	public FilterTree(CrucibleFilterTreeModel filterTreeModel) {
		super(filterTreeModel);
	}

	public void init() {
		setShowsRootHandles(true);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		setRootVisible(false);
		
		getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent e) {
					final TreePath selectionPath = getSelectionModel().getSelectionPath();
					if (selectionPath != null && selectionPath.getLastPathComponent() != null) {
						((AbstractTreeNode) selectionPath.getLastPathComponent()).onSelect();
					}
				}
			});

	}

	public void selectPredefinedFilter(PredefinedFilter predefinedFilter) {
		DefaultMutableTreeNode rootNode = ((DefaultMutableTreeNode) (getModel().getRoot()));
		if (rootNode == null) {
			return;
		}
		int noOfCustomFilters = ((CrucibleFilterTreeModel)getModel()).getNumberOfCustomFilters();
		for (int i = 0; i < rootNode.getChildCount() - noOfCustomFilters; i++) {
			if (rootNode.getChildAt(i) instanceof CruciblePredefinedFilterTreeNode) {
				CruciblePredefinedFilterTreeNode node = (CruciblePredefinedFilterTreeNode) rootNode.getChildAt(i);

				if (node.getFilter().equals(predefinedFilter)) {
					setSelectionPath(new TreePath(node.getPath()));
					break;
				}
			}
		}
	}
}
