package com.atlassian.theplugin.idea.crucible.tree;

import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.configuration.CrucibleProjectConfiguration;
import com.atlassian.theplugin.crucible.model.CrucibleFilterListModelListener;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * User: pmaruszak
 */
public class FilterTree extends JTree {
	private CrucibleProjectConfiguration crucibleConfiguration;
	private Collection<CrucibleFilterListModelListener> listeners = new ArrayList<CrucibleFilterListModelListener>();

	public FilterTree(CrucibleFilterTreeModel filterTreeModel, CrucibleProjectConfiguration crucibleConfiguration) {
		super(filterTreeModel);

		this.crucibleConfiguration = crucibleConfiguration;

		init();
	}

	public void init() {
		setShowsRootHandles(true);
		getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		setRootVisible(false);

		restoreSelection();

		getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				Collection<PredefinedFilter> predefinedFilters =
						new HashSet<PredefinedFilter>(PredefinedFilter.values().length + 1);

				CustomFilter customFilter = null;

				TreePath[] selectionPaths = getSelectionPaths();

				if (selectionPaths != null) {
					for (TreePath selectionPath : selectionPaths) {
						if (selectionPath != null && selectionPath.getLastPathComponent() != null) {
							if (selectionPath.getLastPathComponent() instanceof CruciblePredefinedFilterTreeNode) {
								PredefinedFilter filter = ((CruciblePredefinedFilterTreeNode)
										selectionPath.getLastPathComponent()).getFilter();
//								System.out.println(filter);
								predefinedFilters.add(filter);
							} else if (selectionPath.getLastPathComponent() instanceof CrucibleCustomFilterTreeNode) {
								customFilter = ((CrucibleCustomFilterTreeNode)
										selectionPath.getLastPathComponent()).getFilter();
							}
						}
					}

					fireSelectedPredefinedFilter(predefinedFilters);
					fireSelectedCustomFilter(customFilter);

					//					final TreePath selectionPath = getSelectionModel().getSelectionPath();
					//
					//					if (selectionPath != null && selectionPath.getLastPathComponent() != null) {
					//						((AbstractTreeNode) selectionPath.getLastPathComponent()).onSelect();
					//					}
				}
			}


		});
	}

	public void addListener(CrucibleFilterListModelListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeListener(CrucibleFilterListModelListener listener) {
		listeners.remove(listener);
	}

	private void fireSelectedPredefinedFilter(Collection<PredefinedFilter> filters) {
		for (CrucibleFilterListModelListener listener : listeners) {
			listener.selectedPredefinedFilters(filters);
		}
	}

	private void fireSelectedCustomFilter(CustomFilter filter) {
		for (CrucibleFilterListModelListener listener : listeners) {
			if (filter != null) {
				listener.selectedCustomFilter(filter);
			} else {
				listener.unselectedCustomFilter();
			}
		}
	}

	private void restoreSelection() {
		Boolean[] confFilters = crucibleConfiguration.getCrucibleFilters().getPredefinedFilters();
		Collection<PredefinedFilter> selectedPredefinedFilters = new ArrayList<PredefinedFilter>();

		// find selection
		for (int i = 0; i < confFilters.length; ++i) {
			if (confFilters[i]) {
				// remember node
				selectedPredefinedFilters.add(PredefinedFilter.values()[i]);
			}
		}

		// select nodes
		selectPredefinedFilter(selectedPredefinedFilters);
	}

	private void selectPredefinedFilter(Collection<PredefinedFilter> predefinedFilters) {
		DefaultMutableTreeNode rootNode = ((DefaultMutableTreeNode) (getModel().getRoot()));
		if (rootNode == null) {
			return;
		}

		Collection<TreePath> selectedPaths = new ArrayList<TreePath>();

		int noOfCustomFilters = ((CrucibleFilterTreeModel) getModel()).getNumberOfCustomFilters();

		for (PredefinedFilter predefinedFilter : predefinedFilters) {


			for (int i = 0; i < rootNode.getChildCount() - noOfCustomFilters; i++) {
				if (rootNode.getChildAt(i) instanceof CruciblePredefinedFilterTreeNode) {
					CruciblePredefinedFilterTreeNode node = (CruciblePredefinedFilterTreeNode) rootNode.getChildAt(i);

					if (node.getFilter().equals(predefinedFilter)) {
						selectedPaths.add(new TreePath(node.getPath()));
						break;
					}
				}
			}
		}

		setSelectionPaths(selectedPaths.toArray(new TreePath[0]));
	}
}
