package com.atlassian.theplugin.idea.crucible.tree;

import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.crucible.model.CrucibleFilterListModel;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModel;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.project.Project;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;

/**
 * User: pmaruszak
 */
public class CrucibleFilterTreeModel extends DefaultTreeModel {
    private Project project;
    private CrucibleFilterListModel filterModel;
	private final CrucibleReviewListModel reviewListModel;

    
	public CrucibleFilterTreeModel(Project project, CrucibleFilterListModel filterModel,
                                   CrucibleReviewListModel reviewListModel) {
		super(new DefaultMutableTreeNode(), false);
        this.project = project;
        this.filterModel = filterModel;
		this.reviewListModel = reviewListModel;
	}

	@Override
	public Object getChild(Object parent, int index) {
		if (parent == root) {
			DefaultMutableTreeNode p = (DefaultMutableTreeNode) parent;

			if (index == 0) {
				// My Reviews node
				if (index < p.getChildCount()) {
					return p.getChildAt(index);
				}

				CrucibleMyReviewsTreeNode node = new CrucibleMyReviewsTreeNode(reviewListModel);
				p.add(node);
				return node;

			} else if (index <= getNumberOfCustomFilters()) {
				// Custom Filter node
				if (index < p.getChildCount()) {
					return p.getChildAt(index);
				}

				CrucibleCustomFilterTreeNode node =
						new CrucibleCustomFilterTreeNode(IdeaHelper.getProjectCfgManager(project),
                                filterModel.getCustomFilter(), reviewListModel);
				p.add(node);
				return node;
			} else if (index == getNumberOfCustomFilters() + 1) {
				// Recently Open Reviews node
				if (index < p.getChildCount()) {
					return p.getChildAt(index);
				}

				CrucibleRecentlyOpenFilterTreeNode node = new CrucibleRecentlyOpenFilterTreeNode(
						filterModel.getRecentlyOpenReviewsFilter(), reviewListModel);
				p.add(node);
				return node;
			}
		} else if (parent instanceof CrucibleMyReviewsTreeNode && index < filterModel.getPredefinedFilters().size()) {
			// Predefined Filter node
			CrucibleMyReviewsTreeNode p = (CrucibleMyReviewsTreeNode) parent;

			PredefinedFilter predefinedFilter = (PredefinedFilter) (filterModel.getPredefinedFilters()).toArray()[index];

			if (predefinedFilter != null) {

				if (index < p.getChildCount()) {
					return p.getChildAt(index);
				}

				CruciblePredefinedFilterTreeNode n =
						new CruciblePredefinedFilterTreeNode(predefinedFilter, reviewListModel);
				p.add(n);
				return n;
			}
		}
		return null;
	}

	@Override
	public int getChildCount(Object parent) {
		if (parent == root) {
			// (My Filters node) +  (Custom Filters) + (Recently Open)
			return 1 + getNumberOfCustomFilters() + 1;
		} else if (parent instanceof CrucibleMyReviewsTreeNode) {
			return filterModel.getPredefinedFilters().size();
		}

		return 0;
	}

	@Override
	public Object getRoot() {
		return super.getRoot();
	}

	@Override
	public boolean isLeaf(Object node) {
		if (node == super.getRoot()
				|| node instanceof CrucibleMyReviewsTreeNode) {
			return false;
		}

		return true;
	}

	public int getNumberOfCustomFilters() {
		return 1;
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		System.out.println("valueForPathChanged");
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		if (parent == root) {
			if (child instanceof CruciblePredefinedFilterTreeNode) {
				PredefinedFilter predefinedFilter = ((CruciblePredefinedFilterTreeNode) child).getFilter();
				return new ArrayList<PredefinedFilter>(filterModel.getPredefinedFilters()).indexOf(predefinedFilter);

			} else if (child instanceof CrucibleCustomFilterTreeNode) {

				return filterModel.getPredefinedFilters().size(); //last index + 1
			}
		}

		return -1;
	}
}
