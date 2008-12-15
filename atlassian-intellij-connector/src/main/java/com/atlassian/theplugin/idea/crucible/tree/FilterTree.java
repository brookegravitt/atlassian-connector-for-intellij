package com.atlassian.theplugin.idea.crucible.tree;

import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.configuration.CrucibleProjectConfiguration;
import com.atlassian.theplugin.crucible.model.CrucibleFilterSelectionListener;
import org.jetbrains.annotations.NotNull;

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
	private Collection<CrucibleFilterSelectionListener> listeners = new ArrayList<CrucibleFilterSelectionListener>();

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
				}

				fireSelectedPredefinedFilter(predefinedFilters);

				if (customFilter != null) {
	 					fireSelectedCustomFilter(customFilter);
					} else {
						fireUnselectedCustomFilter();
				}
			}
		});
	}

	public void addSelectionListener(CrucibleFilterSelectionListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeSelectionListener(CrucibleFilterSelectionListener listener) {
		listeners.remove(listener);
	}

	private void fireSelectedPredefinedFilter(Collection<PredefinedFilter> filters) {
		for (CrucibleFilterSelectionListener listener : listeners) {
			listener.selectedPredefinedFilters(filters);
		}
	}

	private void fireSelectedCustomFilter(@NotNull CustomFilter filter) {
		if (crucibleConfiguration.getCrucibleFilters() != null
				&& crucibleConfiguration.getCrucibleFilters().getManualFilter() != null) {
			crucibleConfiguration.getCrucibleFilters().getManualFilter().setEnabled(true);
			for (CrucibleFilterSelectionListener listener : listeners) {
				listener.selectedCustomFilter(filter);
			}
		}
	}

	private void fireUnselectedCustomFilter() {
		if (crucibleConfiguration.getCrucibleFilters() != null
				&& crucibleConfiguration.getCrucibleFilters().getManualFilter() != null) {
			crucibleConfiguration.getCrucibleFilters().getManualFilter().setEnabled(false);
			for (CrucibleFilterSelectionListener listener : listeners) {
				listener.unselectedCustomFilter();
			}
		}
	}

	private void restoreSelection() {
		if (crucibleConfiguration.getCrucibleFilters() != null
				&& crucibleConfiguration.getCrucibleFilters().getPredefinedFilters() != null) {

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
			selectNodes(selectedPredefinedFilters);
		}
	}

	private void selectNodes(Collection<PredefinedFilter> predefinedFilters) {
		DefaultMutableTreeNode rootNode = ((DefaultMutableTreeNode) (getModel().getRoot()));
		if (rootNode == null) {
			return;
		}

		// create selected TreePath-s for PredefinedFilters
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

		// create selected TreePath for CustomFilter (single custom filter support)
		if (crucibleConfiguration != null
				&& crucibleConfiguration.getCrucibleFilters().getManualFilter() != null
				&& crucibleConfiguration.getCrucibleFilters().getManualFilter().isEnabled()) {
			for (int i = rootNode.getChildCount() - 1; i >= 0; --i) {
				if (rootNode.getChildAt(i) instanceof CrucibleCustomFilterTreeNode) {
					CrucibleCustomFilterTreeNode node = (CrucibleCustomFilterTreeNode) rootNode.getChildAt(i);
					selectedPaths.add(new TreePath(node.getPath()));
					break;
				}
			}
		}

		setSelectionPaths(selectedPaths.toArray(new TreePath[0]));
	}

}
