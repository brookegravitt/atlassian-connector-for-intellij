package com.atlassian.theplugin.idea.crucible.tree;

import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModel;
import com.atlassian.theplugin.idea.ui.tree.paneltree.AbstractTreeNode;
import com.atlassian.theplugin.idea.ui.tree.paneltree.SelectableLabel;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public class CruciblePredefinedFilterTreeNode extends AbstractTreeNode {
	private PredefinedFilter filter;
	private final CrucibleReviewListModel reviewListModel;
    private static final SelectableLabel SELECTABLE_LABEL = new SelectableLabel(false, true, null , "a", ICON_HEIGHT);;

    CruciblePredefinedFilterTreeNode(PredefinedFilter filter, CrucibleReviewListModel reviewListModel) {
		super(filter.getFilterName(), null, null);
		this.filter = filter;
		this.reviewListModel = reviewListModel;

	}

	public String toString() {
		int cnt = reviewListModel.getReviewCount(filter);
		String txt = filter.getFilterName();
		if (cnt > -1) {
			txt += " (" + cnt + ")";
		}
		return txt;
	}

	public JComponent getRenderer(JComponent c, boolean selected, boolean expanded, boolean hasFocus) {
        String txt = selected ? toString() : filter.getFilterName();
        SELECTABLE_LABEL.setSelected(selected);
        SELECTABLE_LABEL.setEnabled(c.isEnabled());
        SELECTABLE_LABEL.setFont(c.getFont());
        SELECTABLE_LABEL.setText(txt);

        if (UIManager.getDimension(SELECTABLE_LABEL) != null) {
            SELECTABLE_LABEL.setPreferredSize(UIManager.getDimension(SELECTABLE_LABEL));
        } else {
            SelectableLabel tmp = new SelectableLabel(selected, c.isEnabled(), c.getFont(), txt, ICON_HEIGHT);
            SELECTABLE_LABEL.setPreferredSize(tmp.getPreferredSize());
            tmp = null;
        }
		return SELECTABLE_LABEL;
//        return new SelectableLabel(selected, c.isEnabled(), c.getFont(), "<html>" + toString(), ICON_HEIGHT);
	}

	public PredefinedFilter getFilter() {
		return filter;
	}
}
