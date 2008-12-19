package com.atlassian.theplugin.idea.crucible.tree;

import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModel;
import com.atlassian.theplugin.idea.ui.tree.paneltree.AbstractTreeNode;
import com.atlassian.theplugin.idea.ui.tree.paneltree.SelectableLabel;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public class CrucibleCustomFilterTreeNode  extends AbstractTreeNode {
	private CustomFilter filter;
	private final CrucibleReviewListModel reviewListModel;

	private static final String NAME = "Custom Filter";
	
	public CrucibleCustomFilterTreeNode(CustomFilter filter, CrucibleReviewListModel reviewListModel) {
		super(NAME, null, null);
		this.filter = filter;
		this.reviewListModel = reviewListModel;
	}
	public String toString() {
		int cnt = reviewListModel.getReviewCount(filter);
		String txt = NAME;
		if (cnt > -1) {
			txt += " (" + cnt + ")";
		}
		return txt;
	}

	public JComponent getRenderer(JComponent c, boolean selected, boolean expanded, boolean hasFocus) {
		return new SelectableLabel(selected, c.isEnabled(), "<html>" + toString(), ICON_HEIGHT);
	}

	public void onSelect() {
	}

	public CustomFilter getFilter() {
		return filter;
	}
}
