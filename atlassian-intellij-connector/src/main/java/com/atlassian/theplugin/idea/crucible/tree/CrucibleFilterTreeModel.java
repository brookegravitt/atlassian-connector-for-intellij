package com.atlassian.theplugin.idea.crucible.tree;

import com.atlassian.theplugin.commons.crucible.api.model.CustomFilterBean;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.crucible.model.CrucibleFilterListModel;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;

/**
 * User: pmaruszak
 */
public class CrucibleFilterTreeModel extends DefaultTreeModel {
	private CrucibleFilterListModel filterModel;
	private CustomFilterBean manualFilter;


	public CrucibleFilterTreeModel(CrucibleFilterListModel filterModel, CustomFilterBean manualFilter) {
		super(new DefaultMutableTreeNode(), false);
		this.filterModel = filterModel;
		this.manualFilter = manualFilter;

	}

	@Override
	public Object getChild(Object parent, int index) {
		if (parent == root) {

			DefaultMutableTreeNode p = (DefaultMutableTreeNode) root;
			PredefinedFilter predefinedFilter;

			if (index >= 0 && index < filterModel.getPredefinedFilters().size()) {
				predefinedFilter = (PredefinedFilter) (filterModel.getPredefinedFilters()).toArray()[index];

				if (predefinedFilter != null) {

					if (index < p.getChildCount()) {
						return p.getChildAt(index);
					}

					CruciblePredefinedFilterTreeNode n =
							new CruciblePredefinedFilterTreeNode(predefinedFilter);
					p.add(n);
					return n;

				}
			} else if (index >= 0 && index == filterModel.getPredefinedFilters().size()) {

					if (index < p.getChildCount()) {
						return p.getChildAt(index);
					}

					CrucibleCustomFilterTreeNode n =
							new CrucibleCustomFilterTreeNode(filterModel, manualFilter);
					p.add(n);
					return n;				
			}


		}
		return null;
	}

	@Override
	public int getChildCount(Object parent) {
		if (parent == root) {
			//one means CustomFilter
			return filterModel.getPredefinedFilters().size() + getNumberOfCustomFilters();
		}

		return 0;
	}

	@Override
	public Object getRoot() {
		return super.getRoot();
	}

		@Override
	public boolean isLeaf(Object node) {
		if (node == super.getRoot()) {
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
