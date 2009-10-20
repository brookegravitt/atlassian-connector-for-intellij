package com.atlassian.theplugin.idea.crucible.tree;

import com.atlassian.connector.intellij.crucible.RecentlyOpenReviewsFilter;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.configuration.CrucibleWorkspaceConfiguration;
import com.atlassian.theplugin.crucible.model.CrucibleFilterSelectionListener;
import com.atlassian.theplugin.idea.ui.tree.paneltree.TreeRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.util.*;

/**
 * User: pmaruszak
 */
public class FilterTree extends JTree {
	private CrucibleWorkspaceConfiguration crucibleConfiguration;
	private Collection<CrucibleFilterSelectionListener> listeners = new ArrayList<CrucibleFilterSelectionListener>();
	private FilterTree.LocalTreeSelectionListener localSelectionListener = new LocalTreeSelectionListener();

	public FilterTree(CrucibleFilterTreeModel filterTreeModel, CrucibleWorkspaceConfiguration crucibleConfiguration) {
		super(filterTreeModel);

		this.crucibleConfiguration = crucibleConfiguration;

		init();
	}

	public void init() {
		setShowsRootHandles(false);
		getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		setRootVisible(false);
		expandTree();

		setCellRenderer(new TreeRenderer());

		restoreSelection();

		addSelectionListener();
	}

	private void addSelectionListener() {
		getSelectionModel().addTreeSelectionListener(localSelectionListener);
	}

	private void removeSelectionListener() {
		getSelectionModel().removeTreeSelectionListener(localSelectionListener);
	}

	// lame Swing JTree API causes this silliness, don't blame me
	public void redrawNodes() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				DefaultTreeModel model = (DefaultTreeModel) getModel();
				redrawChildren(model, (TreeNode) model.getRoot());
			}
		});
	}

	private void redrawChildren(DefaultTreeModel model, TreeNode node) {
		for (int i = 0; i < model.getChildCount(node); ++i) {
			TreeNode child = (TreeNode) model.getChild(node, i);
			redrawChildren(model, child);
			model.nodeChanged(child);
		}
	}

	public void addSelectionListener(CrucibleFilterSelectionListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeSelectionListener(CrucibleFilterSelectionListener listener) {
		listeners.remove(listener);
	}

	private void fireSelectionChangedPredefinedFilter(Collection<PredefinedFilter> filters) {

		if (crucibleConfiguration.getCrucibleFilters() != null
				&& crucibleConfiguration.getCrucibleFilters().getPredefinedFilters() != null) {

			Boolean[] confFilters = crucibleConfiguration.getCrucibleFilters().getPredefinedFilters();

			// clear all predefined filters from configuration
			for (int i = 0; i < confFilters.length; ++i) {
				confFilters[i] = false;
			}

			// rember the filters selection in plugin configuration
			for (PredefinedFilter filter : filters) {
				confFilters[filter.ordinal()] = true;
			}

			for (CrucibleFilterSelectionListener listener : listeners) {
				listener.selectionChangedPredefinedFilters(filters);
			}
		}
	}


	private void fireSelectionChangedRecentlyOpenFilter(final RecentlyOpenReviewsFilter recentlyOpenFilter) {
		if (crucibleConfiguration.getCrucibleFilters() != null
				&& crucibleConfiguration.getCrucibleFilters().getRecenltyOpenFilter() != null) {

			if (recentlyOpenFilter == null) {
				crucibleConfiguration.getCrucibleFilters().getRecenltyOpenFilter().setEnabled(false);
			} else {
				crucibleConfiguration.getCrucibleFilters().getRecenltyOpenFilter().setEnabled(true);
			}

			for (CrucibleFilterSelectionListener listener : listeners) {
				listener.selectionChangedRecentlyOpenFilter();
			}
		}
	}

	private void fireSelectionChanged() {
		for (CrucibleFilterSelectionListener listener : listeners) {
			listener.filterSelectionChanged();
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

		if (crucibleConfiguration == null || crucibleConfiguration.getCrucibleFilters() == null) {
			return;
		}

		boolean customFilter = false;
		boolean recenltyOpenFilter = false;
		Collection<PredefinedFilter> selectedPredefinedFilters = new ArrayList<PredefinedFilter>();

		if (crucibleConfiguration.getCrucibleFilters().getManualFilter() != null
				&& crucibleConfiguration.getCrucibleFilters().getManualFilter().isEnabled()) {

			customFilter = true;
		}

		if (crucibleConfiguration.getCrucibleFilters().getRecenltyOpenFilter() != null
				&& crucibleConfiguration.getCrucibleFilters().getRecenltyOpenFilter().isEnabled()) {

			recenltyOpenFilter = true;
		}

		if (crucibleConfiguration.getCrucibleFilters().getPredefinedFilters() != null) {

			Boolean[] confFilters = crucibleConfiguration.getCrucibleFilters().getPredefinedFilters();

			// find stored filters
			for (int i = 0; i < confFilters.length && i < PredefinedFilter.values().length; ++i) {
				if (confFilters[i] != null && confFilters[i]) {
					// remember node
					selectedPredefinedFilters.add(PredefinedFilter.values()[i]);
				}
			}
		}

		// select nodes
		selectNodes(selectedPredefinedFilters, customFilter, recenltyOpenFilter);
	}

	private void selectNodes(Collection<PredefinedFilter> predefinedFilters,
			final boolean customFilter, final boolean recenltyOpenFilter) {

		DefaultMutableTreeNode rootNode = ((DefaultMutableTreeNode) (getModel().getRoot()));
		if (rootNode == null) {
			return;
		}

		Collection<TreePath> selectedPaths = new ArrayList<TreePath>();

		int noOfCustomFilters = ((CrucibleFilterTreeModel) getModel()).getNumberOfCustomFilters();

		if (predefinedFilters.size() == PredefinedFilter.values().length) {
			// all predefines filters selected
			// create path for CrucibleMyReviewsTreeNode
			for (int i = 0; i < rootNode.getChildCount() - noOfCustomFilters; ++i) {
				if (rootNode.getChildAt(i) instanceof CrucibleMyReviewsTreeNode) {
					CrucibleMyReviewsTreeNode myReviewsNode = (CrucibleMyReviewsTreeNode) rootNode.getChildAt(i);
					selectedPaths.add(new TreePath(myReviewsNode.getPath()));
				}
			}
		} else {

			// not all predefined filtes selected
			// create paths for predefined filters
			for (PredefinedFilter predefinedFilter : predefinedFilters) {
				for (int i = 0; i < rootNode.getChildCount() - noOfCustomFilters; ++i) {
					if (rootNode.getChildAt(i) instanceof CrucibleMyReviewsTreeNode) {
						CrucibleMyReviewsTreeNode myReviewsNode = (CrucibleMyReviewsTreeNode) rootNode.getChildAt(i);
						for (int j = 0; j < myReviewsNode.getChildCount(); ++j) {

							if (myReviewsNode.getChildAt(j) instanceof CruciblePredefinedFilterTreeNode) {
								CruciblePredefinedFilterTreeNode node = (CruciblePredefinedFilterTreeNode) myReviewsNode
										.getChildAt(j);

								if (node.getFilter().equals(predefinedFilter)) {
									selectedPaths.add(new TreePath(node.getPath()));
									break;
								}
							}
						}
						break;
					}
				}
			}
		}

		// create path for CustomFilter (single custom filter support here)
		if (customFilter) {
			for (int i = rootNode.getChildCount() - 1; i >= 0; --i) {
				if (rootNode.getChildAt(i) instanceof CrucibleCustomFilterTreeNode) {
					CrucibleCustomFilterTreeNode node = (CrucibleCustomFilterTreeNode) rootNode.getChildAt(i);
					selectedPaths.add(new TreePath(node.getPath()));
					break;
				}
			}
		}

		// create path for RecenltyOpenFilter
		if (recenltyOpenFilter) {
			for (int i = rootNode.getChildCount() - 1; i >= 0; --i) {
				if (rootNode.getChildAt(i) instanceof CrucibleRecentlyOpenFilterTreeNode) {
					CrucibleRecentlyOpenFilterTreeNode node = (CrucibleRecentlyOpenFilterTreeNode) rootNode.getChildAt(i);
					selectedPaths.add(new TreePath(node.getPath()));
					break;
				}
			}
		}

		setSelectionPaths(selectedPaths.toArray(new TreePath[selectedPaths.size()]));
	}

	/**
	 * Takes current selection and removes predefined filters nodes from that selection
	 * It doesn't notify tree about changed selection (selection listener is removed and then restored)
	 */
	private void clearPredefinedFiltersSelection() {

		removeSelectionListener();

		DefaultMutableTreeNode rootNode = ((DefaultMutableTreeNode) (getModel().getRoot()));
		if (rootNode == null) {
			return;
		}

		int noOfCustomFilters = ((CrucibleFilterTreeModel) getModel()).getNumberOfCustomFilters();

		for (int i = 0; i < rootNode.getChildCount() - noOfCustomFilters; ++i) {
			if (rootNode.getChildAt(i) instanceof CrucibleMyReviewsTreeNode) {
				CrucibleMyReviewsTreeNode node = (CrucibleMyReviewsTreeNode) rootNode.getChildAt(i);
				removeDescendantSelectedPaths(new TreePath(node.getPath()), false);
			}
		}

		addSelectionListener();
	}

	private void expandTree() {
		for (int i = 0; i < getRowCount(); i++) {
			expandRow(i);
		}
	}

	private final class LocalTreeSelectionListener implements TreeSelectionListener {

		private Set<TreePath> prevSelection = new HashSet<TreePath>();
		private Set<PredefinedFilter> prevPredefinedFilterSelection = null;
		private Boolean prevCustomFilterSelected = null;
		private Boolean prevRecentlyOpenFilterSelected = null;

		public void valueChanged(TreeSelectionEvent e) {
			Set<PredefinedFilter> predefinedFilters =
					new HashSet<PredefinedFilter>(PredefinedFilter.values().length + 1, 1);

			CustomFilter customFilter = null;
			RecentlyOpenReviewsFilter recentlyOpenFilter = null;
			boolean allMyReviews = false;

			TreePath[] selectionPaths = getSelectionPaths();

			if (selectionPaths != null) {
				for (TreePath selectionPath : selectionPaths) {
					if (selectionPath != null && selectionPath.getLastPathComponent() != null) {
						if (selectionPath.getLastPathComponent() instanceof CruciblePredefinedFilterTreeNode) {
							PredefinedFilter filter = ((CruciblePredefinedFilterTreeNode)
									selectionPath.getLastPathComponent()).getFilter();
							predefinedFilters.add(filter);
						} else if (selectionPath.getLastPathComponent() instanceof CrucibleCustomFilterTreeNode) {
							customFilter = ((CrucibleCustomFilterTreeNode)
									selectionPath.getLastPathComponent()).getFilter();
						} else if (selectionPath.getLastPathComponent() instanceof CrucibleMyReviewsTreeNode) {
							allMyReviews = true;
						} else if (selectionPath.getLastPathComponent() instanceof CrucibleRecentlyOpenFilterTreeNode) {
							recentlyOpenFilter = ((CrucibleRecentlyOpenFilterTreeNode) selectionPath.getLastPathComponent()).
									getRecentlyOpenReviewsFilter();
						}
					}
				}
			}

			if (allMyReviews) {
				clearPredefinedFiltersSelection();

				if (prevSelection.equals(new HashSet<TreePath>(Arrays.asList(getSelectionPaths())))) {
					// if current selection is the same as previous do nothing
					return;
				}

				predefinedFilters.addAll(Arrays.asList(PredefinedFilter.values()));
			}

			// remember current selection
			if (getSelectionPaths() != null) {
				prevSelection = new HashSet<TreePath>(Arrays.asList(getSelectionPaths()));
			}

			if (prevPredefinedFilterSelection == null || !prevPredefinedFilterSelection.equals(predefinedFilters)) {
				prevPredefinedFilterSelection = predefinedFilters;
				fireSelectionChangedPredefinedFilter(predefinedFilters);
			}

			// custom filter selection change notification
			if (customFilter != null && (prevCustomFilterSelected == null || !prevCustomFilterSelected)) {
				prevCustomFilterSelected = true;
				fireSelectedCustomFilter(customFilter);
			} else if (customFilter == null && (prevCustomFilterSelected == null || prevCustomFilterSelected)) {
				prevCustomFilterSelected = false;
				fireUnselectedCustomFilter();
			}

			// recenlty open filter selection change notification
			if (recentlyOpenFilter != null && (prevRecentlyOpenFilterSelected == null || !prevRecentlyOpenFilterSelected)) {
				prevRecentlyOpenFilterSelected = true;
				fireSelectionChangedRecentlyOpenFilter(recentlyOpenFilter);
			} else if (recentlyOpenFilter == null
					&& (prevRecentlyOpenFilterSelected == null || prevRecentlyOpenFilterSelected)) {
				prevRecentlyOpenFilterSelected = false;
				fireSelectionChangedRecentlyOpenFilter(recentlyOpenFilter);
			}

			// selection changed general notification
			fireSelectionChanged();

			redrawNodes();
		}

	}

    @Nullable
     public DefaultMutableTreeNode getSelectedNode() {
         TreePath selectionPath = getSelectionModel().getSelectionPath();
         if (selectionPath != null) {
             return (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
         }

         return null;
     }
        
}
